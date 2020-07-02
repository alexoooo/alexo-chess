package ao.chess.v2.engine.neuro.rollout;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import ao.chess.v2.engine.neuro.puct.PuctUtils;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.util.ChildList;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;


class RolloutNode {
    //-----------------------------------------------------------------------------------------------------------------
    private static final double underpromotionEstimate = 0;
    private static final double underpromotionPrediction = 0.001;
    private static final double initialPlayEstimate = 1.0;
    private static final double estimateUncertainty = 0.01;


    //-----------------------------------------------------------------------------------------------------------------
//    private final int[] moves;
//    private final double[] predictions;

    private final LongAdder visitCount;
    private final DoubleAdder valueSum;

    private final ChildList<RolloutNode> childNodes;
//    private final DeepOutcome deepOutcomeOrNull;
//    private final long staticHashCode;

    private volatile double knownValue;



    //-----------------------------------------------------------------------------------------------------------------
    public RolloutNode(DeepOutcome deepOutcome, State state)
    {
        this(-1,
                PuctUtils.deepOutcomeValue(state, deepOutcome));
    }


    public RolloutNode(Outcome outcome, State state)
    {
        this(-1,
                outcome.valueFor(state.nextToAct()));
    }


    public RolloutNode(int moveCount)
    {
        this(moveCount, Double.NaN);
    }


    private RolloutNode(
//            int[] moves,
//            double[] predictions,
            int moveCount,
//            DeepOutcome deepOutcomeOrNull,
            double knownValue)
    {
//        this.moves = moves;
//        this.predictions = predictions;
//        this.deepOutcomeOrNull = deepOutcomeOrNull;

        visitCount = new LongAdder();
        valueSum = new DoubleAdder();

        if (moveCount == -1) {
            childNodes = null;
        }
        else {
            childNodes = new ChildList<>(moveCount);
        }

        this.knownValue = knownValue;
    }



    //-----------------------------------------------------------------------------------------------------------------
    public long visitCount() {
        return visitCount.sum();
    }


    public boolean isValueKnown() {
        return ! Double.isNaN(knownValue);
    }


    public double moveValue(int move, int[] legalMoves) {
        int moveIndex = legalMoves[Ints.indexOf(legalMoves, move)];
        return visitCount(moveIndex);
    }


    public double inverseValue() {
        if (! Double.isNaN(knownValue)) {
            return knownValue;
        }

        long count = visitCount.longValue();
        double sum = valueSum.doubleValue();
        return count == 0
                ? initialPlayEstimate
                : 1.0 - sum / count;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutNode childOrNull(int moveIndex) {
        return childNodes.get(moveIndex);
    }


    private long visitCount(int moveIndex) {
        RolloutNode child = childNodes.get(moveIndex);
        return child == null
                ? 0
                : child.visitCount();
    }


    private double expectedValue(int moveIndex) {
        long count = visitCount(moveIndex);
        if (count == 0) {
            return initialPlayEstimate;
        }

        RolloutNode child = childNodes.get(moveIndex);
        double childValueSum = child.valueSum.doubleValue();
        return childValueSum / count;
    }



    //-----------------------------------------------------------------------------------------------------------------
    public void initRoot() {
        visitCount.increment();
    }


    private boolean isUnvisitedVirtual() {
        return visitCount.longValue() < 2;
    }


    public void runTrajectory(State state, RolloutContext context) {
        State original = state.prototype();
        List<RolloutNode> path = context.path;
        path.clear();

        path.add(this);
        visitCount.increment();

        double estimatedValue = Double.NaN;

        while (! path.get( path.size() - 1 ).isUnvisitedVirtual())
        {
            int pathLength = path.size();
            RolloutNode node = path.get( pathLength - 1 );

            int moveCount = state.legalMoves(context.movesA);
            if (moveCount == -1) {
                original.equals(state);
                System.out.println("foo");
            }
            PuctEstimate estimate = context.pool.estimateBlocking(state, context.movesA, moveCount);
            if (estimate.moveProbabilities.length != moveCount) {
                System.out.println("bar");
            }

            int childIndex = node.puctChild(
                    context.movesA, moveCount, estimate.moveProbabilities, pathLength == 1, context);

            if (node.isValueKnown()) {
                estimatedValue = node.knownValue;
                context.solutionHits.increment();
                break;
            }

            Move.apply(context.movesA[childIndex], state);

            RolloutNode existingChild = node.childNodes.get(childIndex);

            RolloutNode child;
            boolean expanded;
            if (existingChild == null) {
                expanded = expandChildAndSetEstimatedValue(
                        state, node, childIndex, context);
                child = node.childNodes.get(childIndex);

                if (child == null) {
                    // NB: cleared child of known value (race condition)
                    checkState(node.isValueKnown());
                    estimatedValue = node.knownValue;
                    break;
                }
            }
            else {
                expanded = false;
                child = existingChild;
            }

            path.add( child );
            child.visitCount.increment();

            if (child.isValueKnown()) {
                estimatedValue = child.knownValue;

                int childMoveCount = state.legalMoves(context.movesA);
                if (childMoveCount == -1) {
                    context.tablebaseHits.increment();
                }
                else if (childMoveCount == 0) {
                    context.terminalHits.increment();
                }
                else {
                    context.solutionHits.increment();
                }

                break;
            }
            else if (expanded) {
                estimatedValue = context.estimatedValue;
                break;
            }
        }

        if (Double.isNaN(estimatedValue)) {
            // NB: race condition with new node creation
            return;
        }

        backupValue(path, estimatedValue, context);
    }


    private boolean expandChildAndSetEstimatedValue(
            State state, RolloutNode parent, int childIndex, RolloutContext context
    ) {
        int moveCount = state.legalMoves(context.movesA);
        if (moveCount == 0 || moveCount == -1) {
            Outcome knownOutcome = state.knownOutcomeOrNull();
            RolloutNode newChild = new RolloutNode(knownOutcome, state);
            context.estimatedValue = newChild.knownValue;
            context.terminalHits.increment();
            return addChildIfRequired(newChild, parent, childIndex);
        }
        else if (state.isDrawnBy50MovesRule()) {
            RolloutNode newChild = new RolloutNode(DeepOutcome.DRAW, state);
            context.estimatedValue = 0.5;
            context.terminalHits.increment();
            return addChildIfRequired(newChild, parent, childIndex);
        }

        boolean tablebase =
                state.pieceCount() <= DeepOracle.instancePieceCount;

        if (tablebase) {
            DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(state);
            if (deepOutcome == null) {
                throw new IllegalStateException("Missing tablebase: " + state);
            }

            RolloutNode newChild = new RolloutNode(deepOutcome, state);
            context.estimatedValue = newChild.knownValue;
            return addChildIfRequired(newChild, parent, childIndex);
        }

        RolloutNode newChild = new RolloutNode(moveCount);

        boolean wasSet = parent.childNodes.setIfAbsent(childIndex, newChild);
        if (! wasSet) {
            context.collisions.increment();
//            return false;
        }

        context.estimatedValue = rolloutValue(
                moveCount, state, context);

        return true;
    }


    private double rolloutValue(int topLevelMoveCount, State state, RolloutContext context) {
        Colour fromPov = state.nextToAct();

        int[] moves = context.movesA;
        int nMoves = topLevelMoveCount;
        int nextCount;
        int[] nextMoves = context.movesB;
        Outcome outcome = null;

        if (nMoves < 1) {
            return Double.NaN;
        }

        int rolloutLength = 0;
        double rolloutEstimateSum = 0;

        boolean wasDrawnBy50MovesRule = false;
        do {
            PuctEstimate estimate = context.pool.estimateBlocking(state, moves, nMoves);

            rolloutLength++;
            rolloutEstimateSum +=
                    state.nextToAct() == fromPov
                    ? estimate.winProbability
                    : 1.0 - estimate.winProbability;

            int bestMoveIndex = -1;
            double bestMoveScore = Double.NEGATIVE_INFINITY;

            double moveUncertainty = estimateUncertainty / nMoves;
            double denominator = 1.0 + estimateUncertainty;

            for (int i = 0; i < nMoves; i++) {
                double probability = (estimate.moveProbabilities[i] + moveUncertainty) / denominator;
                double score = probability * probability * context.random.nextDouble();
                if (score > bestMoveScore) {
                    bestMoveScore = score;
                    bestMoveIndex = i;
                }
            }

            Move.apply(moves[bestMoveIndex], state);

            // generate opponent moves
            nextCount = state.moves(nextMoves);

            if (nextCount <= 0) {
                outcome = state.isInCheck(state.nextToAct())
                        ? Outcome.loses(state.nextToAct())
                        : Outcome.DRAW;
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves = moves;
                moves = tempMoves;
                nMoves = nextCount;
            }
        }
        while (! (wasDrawnBy50MovesRule =
                state.isDrawnBy50MovesRule()));

        if (wasDrawnBy50MovesRule) {
            outcome = Outcome.DRAW;
        }

        if (outcome == null) {
            return Double.NaN;
        }

        double outcomeValue = outcome.valueFor(fromPov);
        double averageEstimate = rolloutEstimateSum / rolloutLength;

        return (outcomeValue + averageEstimate) / 2;
    }


    private boolean addChildIfRequired(
            RolloutNode newChild,
            RolloutNode parent,
            int childIndex
    ) {
        return parent.childNodes.setIfAbsent(childIndex, newChild);
    }


    private void backupValue(
            List<RolloutNode> path,
            double leafValue,
            RolloutContext context)
    {
        double inverseValue = 1.0 - leafValue;
        // NB: negating all rewards so that we're always maximizing
        boolean reverse = true;
//        boolean reverse = false;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            double negaMaxValue = reverse ? inverseValue : leafValue;

            RolloutNode node = path.get(i);

//            if (i == 1) {
//                System.out.println(" ## " + negaMaxValue + " - " + node.toString(context));
//            }

            node.valueSum.add(negaMaxValue);

            reverse = ! reverse;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int puctChild(
            int[] moves,
            int moveCount,
            double[] movePredictions,
            boolean root,
            RolloutContext context)
    {
        if (moveCount == 0) {
            return -1;
        }

        int nonWinKnownCount = 0;

        double[] moveValueSums = context.valueSums;
        long[] moveVisitCounts = context.visitCounts;
        long parentVisitCount = 1;
        for (int i = 0; i < moveCount; i++) {
            RolloutNode child = childNodes.get(i);

            long childVisitCount;
            double childValueSum;

            if (child == null) {
                childValueSum = 0;
                childVisitCount = 0;
            }
            else {
                childValueSum = child.valueSum.sum();
                childVisitCount = child.visitCount.sum();

                double known = child.knownValue;
                if (! Double.isNaN(known)) {
                    if (known == 0.0) {
                        knownValue = 1.0;

                        if (! root) {
                            close(context);
                        }
                        return i;
                    }

                    nonWinKnownCount++;
                    childValueSum = (1.0 - known) * childVisitCount;
                }
            }

            moveValueSums[i] = childValueSum;
            moveVisitCounts[i] = childVisitCount;

            parentVisitCount += childVisitCount;
        }

        if (nonWinKnownCount == moveCount) {
            double childValue = 1.0;
            int childIndex = 0;
            for (int i = 0; i < moveCount; i++) {
                RolloutNode child = childNodes.get(i);
                if (child == null) {
                    return -1;
                }

                double value = child.knownValue;
                if (value < childValue) {
                    childValue = value;
                    childIndex = i;
                }
            }
            knownValue = 1.0 - childValue;

            if (! root) {
                close(context);
            }
            return childIndex;
        }

        return puctChildSmear(
                moveCount, moves, movePredictions, moveValueSums, moveVisitCounts, parentVisitCount, context);
    }


    private void close(RolloutContext context) {
        childNodes.close();
//        Object[] previous = childNodes.close();
//        removeFromCache(previous, context);
    }


    private int puctChildSmear(
            int moveCount,
            int[] moves,
            double[] movePredictions,
            double[] moveValueSums,
            long[] moveVisitCounts,
            long parentVisitCount,
            RolloutContext context)
    {
        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;

        double moveUncertainty = estimateUncertainty / moveCount;
        double predictionDenominator = 1.0 + estimateUncertainty;

        for (int i = 0; i < moveCount; i++) {
            long moveVisits = moveVisitCounts[i];

            boolean isUnderpromotion = Move.isPromotion(moves[i]) &&
                    Figure.VALUES[Move.promotion(moves[i])] != Figure.QUEEN;

            double averageOutcome;
            double prediction;

            if (isUnderpromotion) {
                averageOutcome = underpromotionEstimate;
                prediction = underpromotionPrediction;
            }
            else {
                if (moveVisits == 0) {
                    averageOutcome = initialPlayEstimate;
                }
                else {
                    averageOutcome = moveValueSums[i] / moveVisits;
                }

                prediction = movePredictions[i];
            }

            double smearedPrediction = (prediction + moveUncertainty) / predictionDenominator;

            double unvisitedBonus =
                    smearedPrediction *
                    (Math.sqrt(parentVisitCount) / (moveVisits + 1));

            double score = averageOutcome + unvisitedBonus;

            double adjustedScore = score;

            if (adjustedScore > maxScore ||
                    adjustedScore == maxScore && context.random.nextBoolean()) {
                maxScore = adjustedScore;
                maxScoreIndex = i;
            }
        }

        return maxScoreIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public int bestMove(
            State state,
            boolean isRepeat,
            long[] history,
            int historyCount
    ) {
        int[] moves = state.legalMoves();
        if (moves.length == 0) {
            return -1;
        }
        if (! Double.isNaN(knownValue)) {
            double minChildValue = 1.0;
            int minChildIndex = 0;
            for (int i = 0; i < moves.length; i++) {
                RolloutNode child = childOrNull(i);
                if (child == null) {
                    continue;
                }
                if (minChildValue > child.knownValue) {
                    minChildValue = child.knownValue;
                    minChildIndex = i;
                }
            }
            return moves[minChildIndex];
        }

        int contenderCount = 0;
        int[] moveContendersIndexes = new int[moves.length];
        int[] moveContendersCounts = new int[moves.length];
//        int expectedCount = (int) Math.round((double) visitCount() / moves.length + 0.5);
//        int contenderTotal = 0;

        State repeatCursorOrNull =
                isRepeat
                ? state.prototype()
                : null;

        for (int i = 0; i < moves.length; i++)
        {
            boolean moveRepeat = false;
            int move;
            if (repeatCursorOrNull != null) {
                move = Move.apply(moves[i], repeatCursorOrNull);
                long hash = repeatCursorOrNull.staticHashCode();
                for (int h = 0; h < historyCount; h++) {
                    if (history[h] == hash) {
                        moveRepeat = true;
                        break;
                    }
                }
            }
            else {
                move = -1;
            }

            long moveVisits = visitCount(i);

            long moveScore;
            if (moveRepeat) {
                double moveValue = expectedValue(i);
                if (moveValue > 0.5) {
                    moveScore = 0;
                }
                else {
                    moveScore = moveVisits;
                }
            }
            else {
                moveScore = moveVisits;
            }

            if (repeatCursorOrNull != null) {
                Move.unApply(move, repeatCursorOrNull);
            }

//            contenderTotal += (int) moveScore;
            moveContendersIndexes[contenderCount] = i;
            moveContendersCounts[contenderCount] = (int) moveScore;
            contenderCount++;
        }

        int bestMoveIndex = 0;
        double bestMoveScore = 0;

        for (int i = 0; i < contenderCount; i++) {
            double moveScore = moveContendersCounts[i];

            if (bestMoveScore < moveScore) {
                bestMoveScore = moveScore;
                bestMoveIndex = moveContendersIndexes[i];
            }
        }

        return moves[bestMoveIndex];
    }



    //-----------------------------------------------------------------------------------------------------------------
    public String principalVariation(int move, State state) {
        StringBuilder builder = new StringBuilder();

        builder.append("root ").append(visitCount()).append(" - ");

        int moveIndex = Ints.indexOf(state.legalMoves(), move);
        RolloutNode child = childNodes.get(moveIndex);

        State proto = state.prototype();
        Move.apply(move, proto);

        List<String> path = new ArrayList<>();
        path.add(Move.toInputNotation(move) + " " + child.visitCount());
        child.principalVariation(path, proto);

        builder.append("depth ").append(path.size()).append(": ");
        builder.append(Joiner.on(" / ").join(path));

        return builder.toString();
    }


    private void principalVariation(List<String> builder, State state) {
        int[] moves = state.legalMoves();
        int bestMove = bestMove(state, false, new long[0], 0);
        int maxChildIndex = Ints.indexOf(moves, bestMove);

        if (maxChildIndex == -1) {
            return;
        }
        RolloutNode maxChild = childNodes.get(maxChildIndex);
        if (maxChild == null) {
            return;
        }

        builder.add(Move.toInputNotation(moves[maxChildIndex]) + " " + maxChild.visitCount());

        Move.apply(bestMove, state);
        maxChild.principalVariation(builder, state);
    }



    public String toString(State state, RolloutContext context) {
        int[] moves = state.legalMoves();
        List<Integer> indexes = IntStream.range(0, moves.length).boxed().collect(Collectors.toList());

        long parentCount = 0;
        long[] counts = new long[moves.length];
        double[] values = new double[moves.length];
        for (int i = 0; i < counts.length; i++) {
            RolloutNode node = childNodes.get(i);
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

//        double moveUncertainty = 0;
//        double predictionDenominator = 1.0 + moveUncertainty * moves.length;

        String childSummary = indexes
                .stream()
//                .map(i -> String.format("%s %d %d %.4f %.4f %.4f",
                .map(i -> String.format("%s %d %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
//                        counts[i] == 0 ? 0 : childNodes.get(i).maxDepth(),
                        counts[i] == 0 ? initialPlayEstimate : values[i] / counts[i]
                ))
                .collect(Collectors.joining(" | "));

        return String.format("%d - %.4f - %d - %s",
                visitCount.longValue(),
                inverse,
                parentVisitCount,
                childSummary);
    }
}
