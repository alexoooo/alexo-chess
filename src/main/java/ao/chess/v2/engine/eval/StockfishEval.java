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
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;

import static com.google.common.base.Preconditions.checkArgument;


public class StockfishEval
        implements PositionEvaluator, MoveAndOutcomeModel, AutoCloseable
{
    //-----------------------------------------------------------------------------------------------------------------
    public static StockfishEval create(
            StockfishController controller,
            int processes,
            int hashMbPerThread,
            int nodesPerEval,
            boolean evalRollout,
            int nodesPerEstimate
    ) {
        BlockingQueue<StockfishInstance> available = new ArrayBlockingQueue<>(processes);

        for (int i = 0; i < processes; i++) {
            StockfishInstance instance = controller.start(
                    1, hashMbPerThread);
            available.add(instance);
        }

        return new StockfishEval(available, nodesPerEval, evalRollout, nodesPerEstimate);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final int nodesPerEval;
    private final boolean evalRollout;
    private final int nodesPerEstimate;

    private final List<StockfishInstance> all;
    private final BlockingQueue<StockfishInstance> available;


    //-----------------------------------------------------------------------------------------------------------------
    private StockfishEval(
            BlockingQueue<StockfishInstance> available,
            int nodesPerEval,
            boolean evalRollout,
            int nodesPerEstimate
    ) {
        all = new ArrayList<>(available);
        this.available = available;
        this.nodesPerEval = nodesPerEval;
        this.evalRollout = evalRollout;
        this.nodesPerEstimate = nodesPerEstimate;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double evaluate(int topLevelMoveCount, State state, RolloutContext context) {
        return evaluate(state);
    }


    public double evaluate(State state) {
        try {
            StockfishInstance instance = available.take();

            double value =
                    evalRollout
                    ? evaluateRollout(state, instance)
                    : instance.evaluate(state, nodesPerEval).value();

            available.add(instance);
            return value;
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }

    private double evaluateRollout(State state, StockfishInstance instance) {
        int[] moves = new int[Move.MAX_PER_PLY];
        int[] nextMoves = new int[Move.MAX_PER_PLY];
        int[] movesBuffer = new int[Move.MAX_PER_PLY];

        Colour fromPov = state.nextToAct();

        int nMoves = state.legalMoves(moves, movesBuffer);
        if (nMoves < 1) {
            Outcome outcome = state.knownOutcomeOrNull(nMoves);
            return outcome.valueFor(fromPov);
        }

        int nextCount;
        Outcome outcome;
        Long2ObjectMap<StockfishInstance.OutcomeAndMove> localCache = new Long2ObjectOpenHashMap<>();

        while (true) {
            long key = state.longHashCode();
            StockfishInstance.OutcomeAndMove estimate;
            if (localCache.containsKey(key)) {
                estimate = localCache.get(key);
            }
            else {
                estimate = instance.bestMove(state, moves, nMoves, nodesPerEval);
                localCache.put(key, estimate);
            }

            int bestMoveIndex = estimate.move();

            Move.apply(moves[bestMoveIndex], state);

            // generate opponent moves
            nextCount = state.legalMoves(nextMoves, movesBuffer);
            Outcome moveOutcome = state.knownOutcomeOrNull(nextCount);
            if (moveOutcome != null) {
                outcome = moveOutcome;
                break;
            }

            if (state.pieceCount() <= EfficientDeepOracle.pieceCount) {
                DeepOutcome deepOutcome = EfficientDeepOracle.getOrNull(state);
                outcome = deepOutcome.outcome();
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves = moves;
                moves = tempMoves;
                nMoves = nextCount;
            }
        }

        return outcome.valueFor(fromPov);
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
        return estimateAll(List.of(
                new MoveAndOutcomeQuery(state, legalMoves, moveCount)
        )).get(0);
    }

    @Override
    public ImmutableList<MoveAndOutcomeProbability> estimateAll(List<MoveAndOutcomeQuery> queries) {
        int[] moveBufferA = new int[Move.MAX_PER_PLY];
        int[] moveBufferB = new int[Move.MAX_PER_PLY];

        ImmutableList.Builder<MoveAndOutcomeProbability> builder = ImmutableList.builder();
        try {
            StockfishInstance instance = available.take();

            for (MoveAndOutcomeQuery query : queries) {
                State state = query.state;
                Colour nextToAct = state.nextToAct();

                int[] legalMoves = query.legalMoves;
                int moveCount = query.moveCount;
                checkArgument(moveCount > 0);

                double[] moveValues = new double[moveCount];
                double bestMoveValue = Double.NEGATIVE_INFINITY;
                double bestMoveWinProbability = Double.NaN;
                double bestMoveDrawProbability = Double.NaN;

                for (int i = 0; i < moveCount; i++) {
                    byte reversibleMoves = state.reversibleMoves();
                    byte castles = state.castles();
                    long castlePath = state.castlePath();
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
                        winDrawLoss = instance.evaluate(state, nodesPerEstimate).flip();
                    }

                    double moveValue = winDrawLoss.value();
                    moveValues[i] = moveValue;

                    Move.unApply(unMove, state);
                    state.restore(reversibleMoves, castles, castlePath);

                    if (bestMoveValue < moveValue) {
                        bestMoveWinProbability = winDrawLoss.winProbability();
                        bestMoveDrawProbability = winDrawLoss.drawProbability();
                        bestMoveValue = moveValue;
                    }
                }

                valuesToProbabilities(moveValues);

                MoveAndOutcomeProbability estimate = new MoveAndOutcomeProbability(
                        moveValues, bestMoveWinProbability, bestMoveDrawProbability);
                builder.add(estimate);
            }

            available.add(instance);
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(e);
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
        return Math.exp(3 * moveValue);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        for (StockfishInstance instance : all) {
            instance.close();
        }
    }
}
