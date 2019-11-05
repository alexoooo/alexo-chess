package ao.chess.v2.engine.mcts.player.neuro;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


class PuctNode {
    //-----------------------------------------------------------------------------------------------------------------
    private static final double minimumGuess = 0.1;
    private static final double maximumGuess = 0.9;
    private static final double firstPlayEstimate = 0.4;
    public static final double uncertainty = 0.2;


    //-----------------------------------------------------------------------------------------------------------------
    private final int[] moves;
    private final double[] predictions;

    private final LongAdder visitCount;
    private final DoubleAdder valueSum;

    private final CopyOnWriteArrayList<PuctNode> childNodes;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctNode(int[] moves, double[] predictions)
    {
        this.moves = moves;
        this.predictions = predictions;

        visitCount = new LongAdder();
        valueSum = new DoubleAdder();

        childNodes = new CopyOnWriteArrayList<>(
                Collections.nCopies(moves.length, null));
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void initRoot() {
        visitCount.increment();
    }


    public int[] legalMoves() {
        return moves;
    }


    private boolean isUnvisitedVirtual() {
        return visitCount.longValue() < 2;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void runTrajectory(
            State state,
            PuctContext context)
    {
        List<PuctNode> path = context.path;
        path.clear();

        path.add(this);
        visitCount.increment();

        boolean reachedTerminal = false;
        double estimatedValue = Double.NaN;

        while (! path.get( path.size() - 1 ).isUnvisitedVirtual())
        {
            PuctNode node = path.get( path.size() - 1 );

            int childIndex = node.puctChild(context);

            if (childIndex == -1) {
                reachedTerminal = true;
                break;
            }

            Move.apply(node.moves[childIndex], state);

            PuctNode existingChild = node.childNodes.get(childIndex);

            PuctNode child;
            boolean expanded;
            if (existingChild == null) {
                expanded = expandChild(state, node, childIndex, context);
                child = node.childNodes.get(childIndex);
            }
            else {
                expanded = false;
                child = existingChild;
            }

            path.add( child );
            child.visitCount.increment();

            if (expanded) {
                estimatedValue = context.estimatedValue;
                break;
            }
        }

        double leafValue = reachedTerminal
                ? (state.isInCheck(state.nextToAct()) ? 0 : 0.5)
                : estimatedValue;

        backupValue(path, leafValue);
    }


    private boolean expandChild(
            State state, PuctNode parent, int childIndex, PuctContext context
    ) {
//        if (state.pieceCount() <= context.oraclePieces) {
//            // TODO
//            return true;
//        }

        int moveCount = state.legalMoves(context.movesA);
        if (moveCount == 0 || moveCount == -1) {
            PuctNode newChild = new PuctNode(new int[0], new double[0]);

            PuctNode existing = parent.childNodes.set(childIndex, newChild);
            if (existing != null) {
                parent.childNodes.set(childIndex, existing);
                return false;
            }
            context.estimatedValue = state.knownOutcome().valueFor(state.nextToAct());
            return true;
        }

        int[] legalMoves = Arrays.copyOf(context.movesA, moveCount);

        INDArray input = NeuralCodec.INSTANCE.encodeState(state);
        INDArray output = context.nn.output(input);

        double[] predictions = NeuralCodec.INSTANCE
                .decodeMoveProbabilities(output, state, legalMoves);

        PuctUtils.smearProbabilities(
                predictions, uncertainty);

        PuctNode newChild = new PuctNode(legalMoves, predictions);

        PuctNode existing = parent.childNodes.set(childIndex, newChild);
        if (existing != null) {
            parent.childNodes.set(childIndex, existing);
            return false;
        }

        double outcome = NeuralCodec.INSTANCE.decodeOutcome(output);
        double adjusted = Math.max(minimumGuess, Math.min(maximumGuess, outcome));

        if (context.rollouts == 0) {
            context.estimatedValue = adjusted;
        }
        else {
            double rolloutValue = randomRollouts(state, context);
            context.estimatedValue = (rolloutValue + adjusted) / 2;
        }

        return true;
    }


    private double randomRollouts(
            State state,
            PuctContext context)
    {
        Colour pov = state.nextToAct();

        double sum = 0;
        int count = 0;
        for (int i = context.rollouts - 1; i >= 0; i--) {
            State freshState =
                    (i == 0)
                    ? state
                    : state.prototype();

            double value = computeMonteCarloPlayout(
                    freshState, pov, context);

            sum += value;
            count++;
        }

        return sum / count;
    }


    private double computeMonteCarloPlayout(
            State state,
            Colour pov,
            PuctContext context
    ) {
        int[] moves = context.movesA;
        int[] nextMoves = context.movesB;

        int nextCount = 0;
        int nMoves = state.moves(moves);
        Outcome outcome = null;

        boolean wasDrawnBy50MovesRule = false;
        do
        {
            boolean madeMove = false;

            int[] randomMoveOrder = MovePicker.pickRandom(nMoves);
            for (int moveIndex : randomMoveOrder)
            {
                int undoable = Move.apply(moves[ moveIndex ], state);

                // opponent moves
                nextCount = state.moves(nextMoves);

                if (nextCount < 0) {
                    Move.unApply(undoable, state);
                }
                else {
                    madeMove = true;
                    break;
                }
            }

            if (! madeMove) {
                outcome = state.isInCheck(state.nextToAct())
                        ? Outcome.loses(state.nextToAct())
                        : Outcome.DRAW;
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        while (! (wasDrawnBy50MovesRule =
                state.isDrawnBy50MovesRule()));

        if (wasDrawnBy50MovesRule) {
//            return 0.5;
            return MaterialEvaluation.evaluate(state, pov);
        }

        return outcome.valueFor( pov );
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int puctChild(
            PuctContext context)
    {
        int moveCount = childNodes.size();
        if (moveCount == 0) {
            return -1;
        }

        double[] moveValueSums = context.valueSums;
        long[] moveVisitCounts = context.visitCounts;
        long parentVisitCount = 1;
        for (int i = 0; i < moveCount; i++) {
            PuctNode child = childNodes.get(i);

            long childVisitCount;
            double childValueSum;

            if (child == null) {
                childVisitCount = 0;
                childValueSum = 0;
            }
            else {
                childVisitCount = child.visitCount.sum();
                childValueSum = child.valueSum.sum();
            }

            moveValueSums[i] = childValueSum;
            moveVisitCounts[i] = childVisitCount;

            parentVisitCount += childVisitCount;
        }

        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;

        for (int i = 0; i < moveCount; i++) {
            long moveVisits = moveVisitCounts[i];

            double averageOutcome =
                    moveVisits == 0
                    ? firstPlayEstimate
                    : moveValueSums[i] / moveVisits;

            double prediction = predictions[i];

            double unvisitedBonus =
                    prediction * Math.sqrt(parentVisitCount) / (moveVisits + 1);

            double score = averageOutcome + context.exploration * unvisitedBonus;

            if (score > maxScore) {
                maxScore = score;
                maxScoreIndex = i;
            }
        }

        return maxScoreIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void backupValue(
            List<PuctNode> path,
            double leafPlayout)
    {
        double reward = leafPlayout;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            // NB: negating all rewards so that we're always maximizing
            reward = 1.0 - reward;

            PuctNode node = path.get(i);
            node.valueSum.add(reward);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public int bestMove(boolean visitMax) {
        if (moves.length == 0) {
            return -1;
        }

        int bestMoveIndex = 0;
        double bestMoveScore = 0;

        for (int i = 0; i < moves.length; i++)
        {
            double moveScore = moveValueInternal(i, visitMax);

            if (bestMoveScore < moveScore) {
                bestMoveScore = moveScore;
                bestMoveIndex = i;
            }
        }

        return moves[bestMoveIndex];
    }


    public int moveIndex(int move) {
        for (int i = 0; i < moves.length; i++) {
            if (moves[i] == move) {
                return i;
            }
        }
        return -1;
    }


    public long moveValue(int move, boolean visitMax) {
        int moveIndex = moveIndex(move);
        return (long) moveValueInternal(moveIndex, visitMax);
    }


    private double moveValueInternal(int moveIndex, boolean visitMax) {
        PuctNode child = childNodes.get(moveIndex);

        if (child == null) {
            return 0;
        }

        long visits = child.visitCount.longValue();
        if (visitMax || visits == 0) {
            return visits;
        }

        double certainty = 1.0 - (1 / Math.sqrt(visits + 1));

        return certainty * child.valueSum.doubleValue() / visits * 10_000;
    }


    public double inverseValue()
    {
        long count = visitCount.longValue();
        double sum = valueSum.doubleValue();
        return 1.0 - sum / count;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        List<Integer> indexes = IntStream.range(0, moves.length).boxed().collect(Collectors.toList());

        long parentCount = 0;
        long[] counts = new long[moves.length];
        double[] values = new double[moves.length];
        for (int i = 0; i < counts.length; i++) {
            PuctNode node = childNodes.get(i);
            if (node == null) {
                continue;
            }

            counts[i] = node.visitCount.longValue();
            values[i] = node.valueSum.doubleValue();

            parentCount += counts[i];
        }

        indexes.sort((a, b) ->
                counts[a] != counts[b]
                ? -Long.compare(counts[a], counts[b])
                : -Double.compare(values[a], values[b]));

        double inverse = inverseValue();
        long parentVisitCount = parentCount;

        String childSummary = indexes
                .stream()
                .map(i -> String.format("%s %d %.4f %.4f %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
                        counts[i] == 0 ? firstPlayEstimate : values[i] / counts[i],
                        predictions[i],
                        predictions[i] * Math.sqrt(parentVisitCount) / (counts[i] + 1)))
                .collect(Collectors.joining(" | "));

        return String.format("%d - %.4f - %s",
                visitCount.longValue(),
                inverse,
                childSummary);
    }
}
