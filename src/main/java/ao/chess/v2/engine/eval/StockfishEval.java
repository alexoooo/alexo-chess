package ao.chess.v2.engine.eval;


import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.endgame.v2.EfficientDeepOracle;
import ao.chess.v2.engine.neuro.puct.MoveAndOutcomeModel;
import ao.chess.v2.engine.neuro.puct.MoveAndOutcomeProbability;
import ao.chess.v2.engine.neuro.puct.MoveAndOutcomeQuery;
import ao.chess.v2.engine.neuro.rollout.RolloutContext;
import ao.chess.v2.engine.stockfish.StockfishController;
import ao.chess.v2.engine.stockfish.StockfishInstance;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.*;

import static com.google.common.base.Preconditions.checkArgument;
import static com.google.common.base.Preconditions.checkState;


@SuppressWarnings("UnstableApiUsage")
public class StockfishEval
        implements PositionEvaluator, MoveAndOutcomeModel, AutoCloseable
{
    //-----------------------------------------------------------------------------------------------------------------
//    private static final double moveProbabilityFromValueSoftmaxFactor = 15.0;
//    private static final double moveProbabilityFromValueSoftmaxFactor = 20.0;
//    private static final double moveProbabilityFromValueSoftmaxFactor = 25.0;
    private static final double moveProbabilityFromValueSoftmaxFactor = 27.5;


    public static StockfishEval create(
            StockfishController controller,
            int processes,
            int hashMbPerThread,
            int nodesPerEval,
            int randomNodesPerEval,
            boolean evalRollout,
            int nodesPerEstimate
    ) {
        BlockingQueue<StockfishContext> available = new ArrayBlockingQueue<>(processes);

        for (int i = 0; i < processes; i++) {
            StockfishInstance instance = controller.start(
                    1, hashMbPerThread);
            available.add(new StockfishContext(instance));
        }

        return new StockfishEval(
                available, nodesPerEval, randomNodesPerEval, evalRollout, nodesPerEstimate);
    }


    private static class StockfishContext {
        final StockfishInstance instance;

        final int[] moves = new int[Move.MAX_PER_PLY];
        final int[] nextMoves = new int[Move.MAX_PER_PLY];
        final int[] movesBuffer = new int[Move.MAX_PER_PLY];

        private StockfishContext(StockfishInstance instance) {
            this.instance = instance;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final int nodesPerEval;
    private final int randomNodesPerEval;
    private final boolean evalRollout;
    private final int nodesPerEstimate;

    private final List<StockfishContext> all;
    private final BlockingQueue<StockfishContext> available;
    private final ExecutorService executor;


    //-----------------------------------------------------------------------------------------------------------------
    private StockfishEval(
            BlockingQueue<StockfishContext> available,
            int nodesPerEval,
            int randomNodesPerEval,
            boolean evalRollout,
            int nodesPerEstimate
    ) {
        all = new ArrayList<>(available);
        this.available = available;
        this.nodesPerEval = nodesPerEval;
        this.randomNodesPerEval = randomNodesPerEval;
        this.evalRollout = evalRollout;
        this.nodesPerEstimate = nodesPerEstimate;
        executor = Executors.newCachedThreadPool();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double evaluate(int topLevelMoveCount, State state, RolloutContext context) {
        return evaluate(state);
    }


    public double evaluate(State state) {
        try {
            StockfishContext context = available.take();

            double value =
                    evalRollout
                    ? evaluateRollout(state.prototype(), context)
                    : context.instance.evaluate(state, nodesPerEval).value();

            available.add(context);
            return value;
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }


    private double evaluateRollout(State state, StockfishContext context) {
//        State initialState = state.prototype();
//        State previousState = state.prototype();
//        List<String> pgn = new ArrayList<>();

        int[] moves = context.moves;
        int[] nextMoves = context.nextMoves;
        int[] movesBuffer = context.movesBuffer;

        Colour fromPov = state.nextToAct();

        int nMoves = state.legalMoves(moves, movesBuffer);
        if (nMoves < 1) {
            Outcome outcome = state.knownOutcomeOrNull(nMoves);
            return outcome.valueFor(fromPov);
        }

        int nextCount;
        Outcome outcome;

        while (true) {
            StockfishInstance.OutcomeAndMove estimate;
            int mateInOneIndex = findMateInOne(state, moves, nMoves, nextMoves, movesBuffer);
            if (mateInOneIndex != -1) {
                estimate = new StockfishInstance.OutcomeAndMove(
                        StockfishInstance.WinDrawLoss.pureWin, moves[mateInOneIndex], mateInOneIndex);

//                StockfishInstance.OutcomeAndMove stockfishEstimate = instance.bestMove(state, moves, nMoves, nodesPerEval);
//                byte reversibleMoves = state.reversibleMoves();
//                byte castles = state.castles();
//                long castlePath = state.castlePath();
//                byte enPassant = state.enPassant();
//                int undo = Move.apply(moves[stockfishEstimate.moveIndex()], state);
//                Outcome stockfishOutcome = state.knownOutcomeOrNull();
//                Move.unApply(undo, state);
//                state.restore(reversibleMoves, castles, castlePath, enPassant);
//                if (stockfishOutcome == null || stockfishOutcome.winner() != state.nextToAct()) {
//                    System.out.println("missed mate-in-one: " + Move.toInputNotation(estimate.move()) + " vs " + Move.toInputNotation(stockfishEstimate.move()));
//                }
            }
            else {
                int nextNodesPerEval = (int) Math.round(nodesPerEval + Math.random() * randomNodesPerEval);
//                int nextNodesPerEval = nodesPerEval;
                estimate = context.instance.bestMove(state, moves, nMoves, nextNodesPerEval);
            }

            int bestMoveIndex = estimate.moveIndex();

//            previousState = state.prototype();
            Move.apply(moves[bestMoveIndex], state);
//            pgn.add(PgnGenerator.toPgn(moves[bestMoveIndex], state));

            // generate opponent moves
            nextCount = state.legalMoves(nextMoves, movesBuffer);

            Outcome moveOutcome = state.knownOutcomeOrNull(nextCount);
            if (moveOutcome != null) {
                outcome = moveOutcome;
                break;
            }

            if (state.pieceCount() <= EfficientDeepOracle.pieceCount) {
                DeepOutcome deepOutcome = EfficientDeepOracle.getOrNull(state);
                outcome = Objects.requireNonNull(deepOutcome).outcome();
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves = moves;
                moves = tempMoves;
                nMoves = nextCount;
            }
        }

//        StringBuilder str = new StringBuilder();
//        for (int i = 0; i < pgn.size(); i++) {
//            if (i != 0) {
//                str.append(" ");
//            }
//            if (i % 2 == 0) {
//                str.append(((i / 2) + 1)).append(". ");
//            }
//            str.append(pgn.get(i));
//        }
//        System.out.println(str);

        return outcome.valueFor(fromPov);
    }


    private int findMateInOne(State state, int[] moves, int moveCount, int[] nextMoves, int[] movesBuffer) {
        byte reversibleMoves = state.reversibleMoves();
        byte castles = state.castles();
        long castlePath = state.castlePath();
        byte enPassant = state.enPassant();

        Colour nextToAct = state.nextToAct();
        for (int i = 0; i < moveCount; i++) {
            int move = moves[i];
            int undo = Move.apply(move, state);

            int legalMoveCount = state.legalMoves(nextMoves, movesBuffer);
            Outcome outcome = state.knownOutcomeOrNull(legalMoveCount);

            Move.unApply(undo, state);
            state.restore(reversibleMoves, castles, castlePath, enPassant);

            if (outcome != null && outcome.winner() == nextToAct) {
                return i;
            }
        }

        return -1;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public MoveAndOutcomeModel prototype() {
        return this;
    }

    @Override
    public void load() {}

    @Override
    public void prepare(int pieceCount) {}


    @Override
    public MoveAndOutcomeProbability estimate(State state, int[] legalMoves, int moveCount) {
        checkArgument(moveCount > 0);

        try {
            StockfishContext context = available.take();
            int[] moveBufferA = context.nextMoves;
            int[] moveBufferB = context.movesBuffer;

            Colour nextToAct = state.nextToAct();

            double[] moveValues = new double[moveCount];
            double bestMoveValue = Double.NEGATIVE_INFINITY;
            double bestMoveWinProbability = Double.NaN;
            double bestMoveDrawProbability = Double.NaN;

            for (int i = 0; i < moveCount; i++) {
                byte reversibleMoves = state.reversibleMoves();
                byte castles = state.castles();
                long castlePath = state.castlePath();
                byte enPassant = state.enPassant();
                int unMove = Move.apply(legalMoves[i], state);

                int opponentMoveCount = state.legalMoves(moveBufferA, moveBufferB);
                Outcome knownOutcome = state.knownOutcomeOrNull(opponentMoveCount);

                StockfishInstance.WinDrawLoss winDrawLoss;
                if (knownOutcome != null) {
                    if (knownOutcome == Outcome.DRAW) {
                        winDrawLoss = StockfishInstance.WinDrawLoss.pureDraw;
                    }
                    else if (knownOutcome.winner() == nextToAct) {
                        winDrawLoss = StockfishInstance.WinDrawLoss.pureWin;
                    }
                    else {
                        winDrawLoss = StockfishInstance.WinDrawLoss.pureLoss;
                    }
                }
                else {
                    winDrawLoss = context.instance.evaluate(state, nodesPerEstimate).flip();
                }

                double moveValue = winDrawLoss.value();
                moveValues[i] = moveValue;

                Move.unApply(unMove, state);
                state.restore(reversibleMoves, castles, castlePath, enPassant);

                if (bestMoveValue < moveValue) {
                    bestMoveWinProbability = winDrawLoss.winProbability();
                    bestMoveDrawProbability = winDrawLoss.drawProbability();
                    bestMoveValue = moveValue;
                }
            }

            valuesToProbabilities(moveValues);

            MoveAndOutcomeProbability estimate = new MoveAndOutcomeProbability(
                    moveValues, bestMoveWinProbability, bestMoveDrawProbability);

            available.add(context);
            return estimate;
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }


    @Override
    public ImmutableList<MoveAndOutcomeProbability> estimateAll(List<MoveAndOutcomeQuery> queries) {
        ImmutableList.Builder<Callable<MoveAndOutcomeProbability>> tasksBuilder =
                ImmutableList.builderWithExpectedSize(queries.size());
        for (MoveAndOutcomeQuery query : queries) {
            tasksBuilder.add(() -> estimate(
                    query.state, query.legalMoves, query.moveCount));
        }
        ImmutableList<Callable<MoveAndOutcomeProbability>> tasks = tasksBuilder.build();

        ImmutableList.Builder<MoveAndOutcomeProbability> builder = ImmutableList.builder();
        try {
            List<Future<MoveAndOutcomeProbability>> futureResults = executor.invokeAll(tasks);
            for (Future<MoveAndOutcomeProbability> futureResult : futureResults) {
                MoveAndOutcomeProbability result = futureResult.get();
                builder.add(result);
            }
        }
        catch (InterruptedException | ExecutionException e) {
            throw new RuntimeException(e);
        }
        return builder.build();
    }


    private void valuesToProbabilities(double[] moveValues) {
        double totalScore = 0;
        for (double moveValue : moveValues) {
            totalScore += valueScore(moveValue);
        }
        for (int i = 0; i < moveValues.length; i++) {
            double moveValue = moveValues[i];
            double moveScore = valueScore(moveValue);
            moveValues[i] = moveScore / totalScore;
        }
    }

    private double valueScore(double moveValue) {
        return Math.exp(moveProbabilityFromValueSoftmaxFactor * (moveValue - 0.5));
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        executor.shutdown();
        try {
            boolean terminated = executor.awaitTermination(5, TimeUnit.MINUTES);
            checkState(terminated);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        for (StockfishContext context : all) {
            context.instance.close();
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        return "[SF " + nodesPerEval + " " + (evalRollout ? "t" : "f") + " " + nodesPerEstimate + "]";
    }
}
