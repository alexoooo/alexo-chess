package ao.chess.v2.engine.neuro.rollout;


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
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;


class RolloutNode {
    //-----------------------------------------------------------------------------------------------------------------
    private static final double initialPlayEstimate = 1.0;
    private static final double explorationBase = 0.25;
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


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutNode childOrNull(int moveIndex) {
        return childNodes == null
                ? null
                : childNodes.get(moveIndex);
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
        List<RolloutNode> path = context.path;
        path.clear();

        path.add(this);
        visitCount.increment();

        double estimatedValue = Double.NaN;

        while (! path.get( path.size() - 1 ).isUnvisitedVirtual())
        {
            int pathLength = path.size();
            RolloutNode node = path.get( pathLength - 1 );

            int moveCount = state.legalMoves(context.movesA, context.movesC);
            PuctEstimate estimate = context.pool.estimateBlocking(state, context.movesA, moveCount);

            int childIndex = node.ucbChild(
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

                int childMoveCount = state.legalMoves(context.movesA, context.movesC);
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
        int moveCount = state.legalMoves(context.movesA, context.movesC);
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
        Outcome outcome;

        if (nMoves < 1) {
            return Double.NaN;
        }

        rollout:
        while (true) {
            PuctEstimate estimate = context.pool.estimateBlocking(state, moves, nMoves);

            int bestMoveIndex = 0;
            double bestMoveScore = Double.NEGATIVE_INFINITY;

            double moveUncertainty = estimateUncertainty / nMoves;
            double denominator = 1.0 + estimateUncertainty;

            for (int i = 0; i < nMoves; i++) {
                byte reversibleMoves = state.reversibleMoves();
                byte castles = state.castles();
                long castlePath = state.castlePath();

                int move = Move.apply(moves[ i ], state);
                int opponentMoveCount = state.legalMoves(nextMoves, context.movesC);
                Outcome moveOutcome = state.knownOutcomeOrNull(opponentMoveCount);
                Move.unApply(move, state);

                state.restore(reversibleMoves, castles, castlePath);

                if (moveOutcome != null) {
                    if (moveOutcome.loser() == state.nextToAct()) {
                        // non-viable move, leads to self-mate
                        continue;
                    }

                    if (moveOutcome.winner() == state.nextToAct()) {
                        outcome = moveOutcome;
                        break rollout;
                    }
                }

                double probability = (estimate.moveProbabilities[i] + moveUncertainty) / denominator;
                double score = probability * probability * context.random.nextDouble();
                if (score > bestMoveScore) {
                    bestMoveScore = score;
                    bestMoveIndex = i;
                }
            }

            Move.apply(moves[bestMoveIndex], state);

            // generate opponent moves
            nextCount = state.legalMoves(nextMoves, context.movesC);
            Outcome moveOutcome = state.knownOutcomeOrNull();
            if (moveOutcome != null) {
                if (state.isDrawnBy50MovesRule()) {
                    double povEstimate =
                            state.nextToAct().invert() == fromPov
                            ? estimate.winProbability
                            : 1.0 - estimate.winProbability;
                    return (0.5 + povEstimate) / 2;
                }
                else {
                    outcome = moveOutcome;
                    break;
                }
            }

            if (state.pieceCount() <= DeepOracle.instancePieceCount) {
                DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(state);
                outcome = deepOutcome.outcome();
                context.tablebaseRolloutHits.increment();
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
            node.valueSum.add(negaMaxValue);

            reverse = ! reverse;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int ucbChild(
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

        return ucbChild(
                moveCount, /*moves,*/ movePredictions, moveValueSums, moveVisitCounts, parentVisitCount, context);
    }


    private void close(RolloutContext context) {
        childNodes.close();
//        Object[] previous = childNodes.close();
//        removeFromCache(previous, context);
    }


    private int ucbChild(
            int moveCount,
//            int[] moves,
            double[] movePredictions,
            double[] moveValueSums,
            long[] moveVisitCounts,
            long parentVisitCount,
            RolloutContext context)
    {
        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;
        double moveUncertainty = estimateUncertainty / moveCount;

        for (int i = 0; i < moveCount; i++) {
            long moveVisits = moveVisitCounts[i];
            double prior = (movePredictions[i] + moveUncertainty) / (1 + estimateUncertainty);

            double moveScore;
            if (moveVisits == 0) {
                double unvisitedBonus = Math.sqrt(Math.log(parentVisitCount + 1));
                moveScore = (explorationBase + prior) * unvisitedBonus;
            }
            else {
                double averageOutcome = moveValueSums[i] / moveVisits;
                double unvisitedBonus = Math.sqrt(Math.log(parentVisitCount) / moveVisits);
                double explorationBonus = (explorationBase + prior) * unvisitedBonus;
                moveScore = averageOutcome + explorationBonus;
            }

            if (moveScore > maxScore ||
                    moveScore == maxScore && context.random.nextBoolean()) {
                maxScore = moveScore;
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
        if (moves == null || moves.length == 0) {
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

        State repeatCursorOrNull =
                isRepeat
                ? state.prototype()
                : null;

        for (int i = 0; i < moves.length; i++) {
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
        if (moves == null || moves.length == 0) {
            return;
        }

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

        double parentSum = 0;
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
            parentSum += values[i];
        }

        indexes.sort((a, b) ->
                counts[a] != counts[b]
                ? -Long.compare(counts[a], counts[b])
                : -Double.compare(values[a], values[b]));

        double parentValue = parentSum / parentCount;
        long parentVisitCount = parentCount;

        String childSummary = indexes
                .stream()
                .map(i -> String.format("%s %d %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
//                        counts[i] == 0 ? 0 : childNodes.get(i).maxDepth(),
                        counts[i] == 0 ? initialPlayEstimate : values[i] / counts[i]
                ))
                .collect(Collectors.joining(" | "));

        return String.format("%d %.4f - %s",
                parentVisitCount,
                parentValue,
                childSummary);
    }
}
