package ao.chess.v2.engine.mcts.player.neuro;


import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.state.Move;
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

        double denominator = 1 + context.alpha * moveCount;
        for (int i = 0; i < predictions.length; i++) {
            predictions[i] = (predictions[i] + context.alpha) / denominator;
        }

        PuctNode newChild = new PuctNode(legalMoves, predictions);

        PuctNode existing = parent.childNodes.set(childIndex, newChild);
        if (existing != null) {
            parent.childNodes.set(childIndex, existing);
            return false;
        }

        double outcome = NeuralCodec.INSTANCE.decodeOutcome(output);
        context.estimatedValue = Math.max(0.1, Math.min(0.9, outcome));
        return true;
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
            if (child != null) {
                moveValueSums[i] = child.valueSum.sum();

                long childVisitCount = child.visitCount.sum();
                moveVisitCounts[i] = childVisitCount;
                parentVisitCount += childVisitCount;
            }
        }

        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;

        for (int i = 0; i < moveCount; i++) {
            long moveVisits = moveVisitCounts[i];

            // NB: default to zero if unvisited as in Alpha Zero
            double averageOutcome =
                    moveVisits == 0
                    ? 0
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
    public int bestMove() {
        if (moves.length == 0) {
            return -1;
        }

        int bestMoveIndex = 0;
        long bestMoveVisits = 0;
        for (int i = 0; i < moves.length; i++) {
            PuctNode child = childNodes.get(i);
            if (child == null) {
                continue;
            }

            long moveVisits = child.visitCount.longValue();
            if (bestMoveVisits < moveVisits) {
                bestMoveVisits = moveVisits;
                bestMoveIndex = i;
            }
        }
        return moves[bestMoveIndex];
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public String toString() {
        List<Integer> indexes = IntStream.range(0, moves.length).boxed().collect(Collectors.toList());

        long[] counts = new long[moves.length];
        double[] values = new double[moves.length];
        for (int i = 0; i < counts.length; i++) {
            PuctNode node = childNodes.get(i);
            if (node == null) {
                continue;
            }

            counts[i] = node.visitCount.longValue();
            values[i] = node.valueSum.doubleValue();
        }

        indexes.sort((a, b) ->
            -Long.compare(counts[a], counts[b]));

        long count = visitCount.longValue();
        double sum = valueSum.doubleValue();
        double inverse = 1.0 - sum / count;

        String childSummary = indexes
                .stream()
                .map(i -> String.format("%s %d %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
                        counts[i] == 0 ? 0 : values[i] / counts[i]))
                .collect(Collectors.joining(" | "));

        return String.format("%d - %.4f - %s",
                visitCount.longValue(),
                inverse,
                childSummary);
    }
}
