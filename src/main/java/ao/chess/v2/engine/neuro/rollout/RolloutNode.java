package ao.chess.v2.engine.neuro.rollout;


import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.endgame.v2.EfficientDeepOracle;
import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import ao.chess.v2.engine.neuro.rollout.store.KnownOutcome;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import com.google.common.base.Joiner;
import com.google.common.primitives.Ints;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.google.common.base.Preconditions.checkState;


/**
 * TODO: add transposition support, can add just nodes with # visits > threshold (e.g. 128k or 1M) to control size,
 *   used as part of evaluation function to estimate action value.
 *
 *   can be MVMap from H2, 128 bit hash key (two different hashings of the state) to long array of node offsets
 *   term = constant * score of most visited action
 *
 *  there's a stackoverflow article
 */
public class RolloutNode {
    //-----------------------------------------------------------------------------------------------------------------
    private static final double unknownMoveEstimate = 0.0;
    //    private static final double initialPlayEstimate = 1.0;
//    private static final double initialPlayFactor = 0.0;
//    private static final double initialPlayFactor = 0.1;
//    private static final double initialPlayFactor = 0.5;

//    private static final double explorationBase = Math.sqrt(2);
//    private static final double explorationBase = 0.3;

        private static final double estimateUncertainty = 0.0;
    //    private static final double estimateUncertainty = 0.01;
//    private static final double estimateUncertainty = 0.025;
    private static final double estimateUncertaintyDenominator = 1.0 + estimateUncertainty;

//    private static final double rolloutProbabilityPower = 2.0;
    //    private static final double rolloutProbabilityPower = 3.0;

//    private static final double rolloutValueWeight = 0.0;
    //    private static final double rolloutValueWeight = 0.01;
//    private static final double rolloutValueWeight = 0.05;
//    private static final double rolloutValueWeight = 0.25;
//    private static final double rolloutValueWeight = 0.5;
//    private static final double rolloutValueDenominator = 1.0 + rolloutValueWeight;

//    private static final int rolloutLengthLimit = 1;
//    private static final int rolloutLengthLimit = 3;
//    private static final int rolloutLengthLimit = 5;
//    private static final int rolloutLengthLimit = 10;

    private static final double rolloutValueDiscount = 0.9;
//    private static final double rolloutValueDiscount = 0.95;
//    private static final double rolloutValueDiscount = 0.99;

    private static final double fpuDiscount = 0.25;
//    private static final int puctThreshold = 4 * 1024;
//    private static final int puctThreshold = 16 * 1024;
    private static final int puctThreshold = 128 * 1024;
    private static final double puctExplorationLog = 18432;

    private final static double explorationMin = 1.0;
//    private final static double explorationMin = 1.25;
//    private final static double explorationVariance = 0.0;
    private final static double explorationVariance = 1.75;

//    private final static int transpositionMinimum = 1;
    private final static int transpositionMinimum = 16;
//    private final static int transpositionMinimum = 32;
//    private final static int transpositionMinimum = 128;
//    private final static int transpositionMinimum = 100_000_000;
//    private final static double transpositionPower = 0.0;
    private final static double transpositionPower = 1.0 / 4;
//    private final static double transpositionPower = 1.0 / 3;
    private final static double transpositionUseOver = 0;
//    private final static double transpositionUseOver = 1;
//    private final static double transpositionUseOver = 8;
//    private final static double transpositionUseOver = 64;


    //-----------------------------------------------------------------------------------------------------------------
    public final long index;


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutNode(long index) {
        this.index = index;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public long visitCount(RolloutStore store) {
//        return visitCount.sum();
        return store.getVisitCount(index);
    }


    public boolean isValueKnown(RolloutStore store) {
//        return ! Double.isNaN(knownValue);
        return store.getKnownOutcome(index) != KnownOutcome.Unknown;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutNode childOrNull(int moveIndex, RolloutStore store) {
        long childIndex = store.getChildIndex(index, moveIndex);
        return childIndex == -1
                ? null
                : new RolloutNode(childIndex);
//        return childNodes == null
//                ? null
//                : childNodes.get(moveIndex);
    }


    private long visitCount(int moveIndex, RolloutStore store) {
        long childIndex = store.getChildIndex(index, moveIndex);
        return childIndex == -1
                ? 0
                : store.getVisitCount(childIndex);

//        RolloutNode child = childNodes.get(moveIndex);
//        return child == null
//                ? 0
//                : child.visitCount();
    }


    private void incrementVisitCount(RolloutStore store) {
        store.incrementVisitCount(index);
    }


    private void setKnownOutcome(KnownOutcome knownOutcome, RolloutStore store) {
        store.setKnownOutcome(index, knownOutcome);
    }


    private void addValue(double value, RolloutStore store) {
        store.addValue(index, value);
    }


    private double valueSum(RolloutStore store) {
        return store.getValueSum(index);
    }


    private double valueSquareSum(RolloutStore store) {
        return store.getValueSquareSum(index);
    }


    private double expectedValue(int moveIndex, RolloutStore store) {
        long childIndex = store.getChildIndex(index, moveIndex);
        if (childIndex == -1) {
            return unknownMoveEstimate;
        }

        return store.getAverageValue(childIndex, unknownMoveEstimate);

//        long count = visitCount(moveIndex, store);
//        if (count == 0) {
//            return initialPlayEstimate;
//        }
//
//        RolloutNode child = childNodes.get(moveIndex);
//        double childValueSum = child.valueSum.doubleValue();
//        return childValueSum / count;
    }



    //-----------------------------------------------------------------------------------------------------------------
    public void initRoot(RolloutStore store) {
//        if (store.getKnownOutcome(RolloutStore.rootIndex) != KnownOutcome.Unknown) {
//            return;
//        }

//        incrementVisitCount(store);
    }


//    private boolean isUnvisitedVirtual(RolloutStore store) {
//        return store.getVisitCount(index) < 2;
//    }


    public int runTrajectory(State state, RolloutContext context) {
        RolloutStore store = context.store;
        if (store.getKnownOutcome(RolloutStore.rootIndex) != KnownOutcome.Unknown) {
            return 0;
        }

        long rootCount = store.getVisitCount(RolloutStore.rootIndex);
        long transpositionThreshold = Math.max(transpositionMinimum, (long) Math.pow(rootCount, transpositionPower));

        List<RolloutNode> path = context.path;
        path.clear();

        path.add(this);

        double estimatedValue = Double.NaN;

        while (true)
        {
            int pathLength = path.size();
            RolloutNode node = path.get( pathLength - 1 );
            node.incrementVisitCount(store);

            int moveCount = state.legalMoves(context.movesA, context.movesC);
            if (moveCount <= 0) {
                Outcome outcome = state.knownOutcomeOrNull(moveCount);
                estimatedValue = outcome.valueFor(state.nextToAct());
                context.terminalHits.increment();
                break;
            }
            PuctEstimate estimate = context.pool.estimateBlockingCached(state, context.movesA, moveCount);

            int moveIndex = node.selectChild(
                    moveCount, estimate.winProbability, estimate.moveProbabilities, transpositionThreshold, state, context);

            if (node.isValueKnown(store)) {
                estimatedValue = node.knownValue(store);
                context.solutionHits.increment();
                break;
            }

            Move.apply(context.movesA[moveIndex], state);

            RolloutNode existingChild = node.childOrNull(moveIndex, store);

            RolloutNode child;
            boolean selectionEnded;
            if (existingChild == null) {
                int childMoveCount = state.legalMoves(context.movesA, context.movesC);
                double knownValue = node.expandChildAndGetKnownValue(
                        state, childMoveCount, moveIndex, context);
                selectionEnded = true;

                child = node.childOrNull(moveIndex, store);
                checkState(child != null);

                child.incrementVisitCount(store);
                if (Double.isNaN(knownValue)) {
                    estimatedValue = rolloutValue(childMoveCount, state, context);
                }
                else {
                    estimatedValue = knownValue;
                }
            }
            else {
                selectionEnded = false;
                child = existingChild;
            }

            path.add( child );

            if (child.isValueKnown(store)) {
                estimatedValue = child.knownValue(store);

                int childMoveCount = state.legalMoves(context.movesA, context.movesC);
                if (childMoveCount > 0) {
                    context.solutionHits.increment();
                }

                child.incrementVisitCount(store);

                break;
            }
            else if (selectionEnded) {
                break;
            }
        }

        checkState(! Double.isNaN(estimatedValue));

        context.store.backupValue(path, estimatedValue);
//        backupValue(path, estimatedValue, context);

        return path.size();
    }


    private double knownValue(RolloutStore store) {
        return store.getKnownOutcome(index).toValue();
    }


    private KnownOutcome knownOutcome(RolloutStore store) {
        return store.getKnownOutcome(index);
    }


    private double expandChildAndGetKnownValue(
            State state, int moveCount, int moveIndex, RolloutContext context
    ) {
        if (moveCount == 0 || moveCount == -1 || state.isDrawnBy50MovesRule()) {
            Outcome outcome = state.knownOutcomeOrNull();
            KnownOutcome knownOutcome = KnownOutcome.ofOutcome(outcome, state.nextToAct());

            long childIndex = context.store.expandChildIfMissing(index, moveIndex, 0);
            if (childIndex < 0) {
                context.collisions.increment();
            }
            else {
                context.store.setKnownOutcome(childIndex, knownOutcome);
                context.terminalHits.increment();
            }

            return knownOutcome.toValue();
        }

        boolean tablebase =
//                state.pieceCount() <= DeepOracle.instancePieceCount;
                state.pieceCount() <= EfficientDeepOracle.pieceCount;

        if (tablebase) {
//            DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(state);
            DeepOutcome deepOutcome = EfficientDeepOracle.getOrNull(state);
            if (deepOutcome == null) {
                throw new IllegalStateException("Missing tablebase: " + state);
            }

            KnownOutcome knownOutcome = KnownOutcome.ofOutcome(deepOutcome.outcome(), state.nextToAct());
//            context.estimatedValue = knownOutcome.toValue();

            long childIndex = context.store.expandChildIfMissing(index, moveIndex, 0);
            if (childIndex < 0) {
                context.collisions.increment();
            }
            else {
                context.store.setKnownOutcome(childIndex, knownOutcome);
                context.tablebaseHits.increment();
            }
            return knownOutcome.toValue();
        }

        long childIndex = context.store.expandChildIfMissing(index, moveIndex, moveCount);
        if (childIndex < 0) {
            context.collisions.increment();
        }

//        context.estimatedValue = rolloutValue(
//                moveCount, state, context);
//        return rolloutValue(
//                moveCount, state, context);
        return Double.NaN;
    }


    private double  rolloutValue(int topLevelMoveCount, State state, RolloutContext context) {
        Colour fromPov = state.nextToAct();

        int[] moves = context.movesA;
        int nMoves = topLevelMoveCount;
        int nextCount;
        int[] nextMoves = context.movesB;
        Outcome outcome = null;

        if (nMoves < 1) {
            return Double.NaN;
        }

        double discountedValueSum = 0;
        double nextDiscount = 1.0;
        double discountSum = 0;

        rollout:
        for (int rolloutLength = 0; rolloutLength <= context.rolloutLength; rolloutLength++) {
            PuctEstimate estimate = context.pool.estimateBlocking(state, moves, nMoves);

            double stateValue =
                    state.nextToAct() == fromPov
                    ? estimate.winProbability
                    : 1.0 - estimate.winProbability;

            discountedValueSum += stateValue * nextDiscount;
            discountSum += nextDiscount;
            nextDiscount *= rolloutValueDiscount;

            int bestMoveIndex = 0;
            double bestMoveScore = Double.NEGATIVE_INFINITY;

            double moveUncertainty = estimateUncertainty / nMoves;

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
                        // non-viable move, in check and doesn't prevent checkmate
                        continue;
                    }

                    if (moveOutcome.winner() == state.nextToAct()) {
                        // instant win
                        outcome = moveOutcome;
                        break rollout;
                    }
                }

                double probability =
                        (estimate.moveProbabilities[i] + moveUncertainty) /
                                estimateUncertaintyDenominator;

                double probabilityScore = Math.pow(probability, context.probabilityPower);
                double score = probabilityScore * context.random.nextDouble();
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
                outcome = moveOutcome;
                break;
            }

//            if (state.pieceCount() <= DeepOracle.instancePieceCount) {
            if (state.pieceCount() <= EfficientDeepOracle.pieceCount) {
//                DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(state);
                DeepOutcome deepOutcome = EfficientDeepOracle.getOrNull(state);
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

        if (outcome != null) {
            return outcome.valueFor(fromPov);
        }

        double expectedValue = discountedValueSum / discountSum;

        return context.binerize
                ? (expectedValue > context.random.nextDouble() ? 1.0 : 0.0)
                : expectedValue;
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

//            context.store.addValue(node.index, negaMaxValue);
            node.addValue(negaMaxValue, context.store);

            reverse = ! reverse;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int selectChild(
            int moveCount,
            double valuePrediction,
            double[] movePredictions,
            long transpositionThreshold,
            State state,
            RolloutContext context)
    {
        if (moveCount == 0) {
            return -1;
        }

        double[] moveValueSums = context.valueSums;
        double[] moveValueSquareSums = context.valueSquareSums;
        long[] moveVisitCounts = context.visitCounts;
        long[] moveChildIndexes = context.childIndexes;

        int solutionMoveIndex = context.store.populateChildInfoAndSelectSolution(
                index, moveCount, moveValueSums, moveValueSquareSums, moveVisitCounts, moveChildIndexes);
//        int solutionMoveIndex = populateChildInfoAndSelectSolution(
//                moveCount, moveValueSums, moveValueSquareSums, moveVisitCounts, moveChildIndexes, context);
        if (solutionMoveIndex != -1) {
            return solutionMoveIndex;
        }
        else if (moveCount == 1) {
            return 0;
        }

        long parentVisitCount = 1;
        for (int i = 0; i < moveCount; i++) {
            parentVisitCount += moveVisitCounts[i];
        }

        return banditChild(
                moveCount,
                valuePrediction,
                movePredictions,
                moveValueSums,
                moveValueSquareSums,
                moveVisitCounts,
                moveChildIndexes,
                parentVisitCount,
                transpositionThreshold,
                state,
                context);
    }


    private int populateChildInfoAndSelectSolution(
            int moveCount,
            double[] moveValueSums,
            double[] moveValueSquareSums,
            long[] moveVisitCounts,
            long[] moveChildIndexes,
            RolloutContext context)
    {
        RolloutStore store = context.store;

        int nonWinKnownCount = 0;
        for (int i = 0; i < moveCount; i++) {
            RolloutNode child = childOrNull(i, store);

            long childVisitCount;
            double childValueSum;
            double childValueSquareSum;
            long childMoveIndex;

            if (child == null) {
                childValueSum = 0;
                childValueSquareSum = 0;
                childVisitCount = 0;
                childMoveIndex = -1;
            }
            else {
                childValueSum = child.valueSum(store);
                childValueSquareSum = child.valueSquareSum(store);
                childVisitCount = child.visitCount(store);
                childMoveIndex = child.index;

                double known = child.knownValue(store);
                if (! Double.isNaN(known)) {
                    if (known == 0.0) {
                        setKnownOutcome(KnownOutcome.Win, store);
                        return i;
                    }

                    nonWinKnownCount++;
                    childValueSum = (1.0 - known) * childVisitCount;
                }
            }

            moveValueSums[i] = childValueSum;
            moveValueSquareSums[i] = childValueSquareSum;
            moveVisitCounts[i] = childVisitCount;
            moveChildIndexes[i] = childMoveIndex;
        }

        if (nonWinKnownCount == moveCount) {
            KnownOutcome bestChildOutcome = KnownOutcome.Win;
            int childIndex = 0;
            for (int i = 0; i < moveCount; i++) {
                RolloutNode child = childOrNull(i, store);
                KnownOutcome value = child.knownOutcome(store);
                if (value == KnownOutcome.Draw) {
                    bestChildOutcome = value;
                    childIndex = i;
                }
            }
            setKnownOutcome(bestChildOutcome.reverse(), store);
            return childIndex;
        }

        return -1;
    }
    

    private int banditChild(
            int moveCount,
            double valuePrediction,
            double[] movePredictions,
            double[] moveValueSums,
            double[] moveValueSquareSums,
            long[] moveVisitCounts,
            long[] moveChildIndexes,
            long parentVisitCount,
            long transpositionThreshold,
            State state,
            RolloutContext context)
    {
        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;
        double moveUncertainty = estimateUncertainty / moveCount;

        double exploration = explorationMin + Math.abs(context.random.nextGaussian() * explorationVariance);

        for (int i = 0; i < moveCount; i++) {
            long moveVisits = moveVisitCounts[i];
            double prior = (movePredictions[i] + moveUncertainty) / estimateUncertaintyDenominator;

            double averageOutcome = averageOutcome(
                    moveVisits,
                    moveValueSums[i],
                    parentVisitCount,
                    valuePrediction,
                    i,
                    moveChildIndexes[i],
                    transpositionThreshold,
                    state,
                    context);

            double ucbExploration;
            if (moveVisits == 0) {
                ucbExploration =
                        exploration *
                        (Math.sqrt(Math.log(parentVisitCount)) +
                                prior);
            }
            else {
                double averageSquareOutcome = moveValueSquareSums[i] / moveVisits;
                double variance = Math.max(0.01, Math.min(0.25,
                        averageSquareOutcome
                        - averageOutcome * averageOutcome
                        + Math.sqrt(2 * Math.log(parentVisitCount) / moveVisits)));
                ucbExploration =
                        exploration *
                        (Math.sqrt(Math.log(parentVisitCount) / moveVisits * variance) +
                                prior / Math.sqrt(moveVisits));
            }

            double unvisitedBonus;
            if (parentVisitCount >= puctThreshold) {
                unvisitedBonus = ucbExploration;
            }
            else {
                double puctExploration =
                        prior *
                        (Math.sqrt(parentVisitCount) / (moveVisits + 1)) *
                        (exploration + Math.log((parentVisitCount / puctExplorationLog) + 1));

                double ucbWeight = (double) parentVisitCount / puctThreshold;
                unvisitedBonus = ucbExploration * ucbWeight + puctExploration * (1.0 - ucbWeight);
            }

            double moveScore = averageOutcome + unvisitedBonus;

            if (moveScore > maxScore) {
                maxScore = moveScore;
                maxScoreIndex = i;
            }
        }

        return maxScoreIndex;
    }


    private double averageOutcome(
            long moveVisits,
            double moveValueSum,
            long parentVisitCount,
            double valuePrediction,
            int moveIndex,
            long moveNodeIndex,
            long transpositionThreshold,
            State state,
            RolloutContext context)
    {
        if (moveVisits == 0) {
            return Math.max(0, valuePrediction - fpuDiscount * Math.sqrt(parentVisitCount));
        }

        double transpositionValueSum = 0;
        long transpositionVisitCont = 0;
        if (moveVisits >= transpositionThreshold) {
            int move = Move.apply(context.movesA[moveIndex], state);
            long hashHigh = state.longHashCode();
            long hashLow = state.longHashCodeAlt();
            Move.unApply(move, state);

//            long moveNodeIndex = context.store.getChildIndex(index, moveIndex);

            TranspositionInfo moveTransposition = context.store.getTranspositionOrNull(hashHigh, hashLow);
            if (moveTransposition == null || moveVisits > moveTransposition.visitCount()) {
                context.store.setTransposition(hashHigh, hashLow, moveNodeIndex, moveValueSum, moveVisits);
            }
            else if (moveNodeIndex != moveTransposition.nodeIndex() &&
                    moveVisits + transpositionUseOver < moveTransposition.visitCount())
            {
                transpositionValueSum = moveTransposition.valueSum();
                transpositionVisitCont = moveTransposition.visitCount();
                context.transpositionHits.increment();
            }
        }

        return (moveValueSum + transpositionValueSum) / (moveVisits + transpositionVisitCont);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public int bestMove(
            State state,
            boolean isRepeat,
            long[] history,
            int historyCount,
            RolloutStore store
    ) {
        int[] moves = state.legalMoves();
        if (moves == null || moves.length == 0) {
            return -1;
        }
        if (! Double.isNaN(knownValue(store))) {
//            if (state.pieceCount() <= DeepOracle.instancePieceCount) {
            if (state.pieceCount() <= EfficientDeepOracle.pieceCount) {
                // TODO principal variation tablebase support
                return -1;
            }

            double minChildValue = 1.0;
            int minChildIndex = 0;
            for (int i = 0; i < moves.length; i++) {
                RolloutNode child = childOrNull(i, store);
                if (child == null) {
                    continue;
                }
                if (minChildValue > child.knownValue(store)) {
                    minChildValue = child.knownValue(store);
                    minChildIndex = i;
                }
            }
            return moves[minChildIndex];
        }

        int contenderCount = 0;
        int[] moveContendersIndexes = new int[moves.length];
        long[] moveContendersCounts = new long[moves.length];

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
                for (int h = 0; h < historyCount - 1; h++) {
                    if (history[h] == hash) {
                        moveRepeat = true;
                        break;
                    }
                }
            }
            else {
                move = -1;
            }

            long moveVisits = visitCount(i, store);

            long moveScore;
            if (moveRepeat) {
                double moveValue = expectedValue(i, store);
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

            moveContendersIndexes[contenderCount] = i;
            moveContendersCounts[contenderCount] = moveScore;
            contenderCount++;
        }

        int bestMoveIndex = 0;
        long bestMoveScore = -1;

        for (int i = 0; i < contenderCount; i++) {
            long moveScore = moveContendersCounts[i];

            if (bestMoveScore < moveScore) {
                bestMoveScore = moveScore;
                bestMoveIndex = moveContendersIndexes[i];
            }
        }

        return moves[bestMoveIndex];
    }


    //-----------------------------------------------------------------------------------------------------------------
    public String principalVariation(int move, State state, RolloutStore store) {
        StringBuilder builder = new StringBuilder();

        int moveIndex = Ints.indexOf(state.legalMoves(), move);
        RolloutNode child = childOrNull(moveIndex, store);
        if (child == null) {
            return "<empty node>";
        }

        State proto = state.prototype();
        Move.apply(move, proto);

        long childVisitCount = child.visitCount(store);
        long rootVisitCount = visitCount(store);
        builder.append("root ").append(rootVisitCount).append(" - ");

        List<String> path = new ArrayList<>();
        path.add(Move.toInputNotation(move) + " " + childVisitCount);
        child.principalVariation(path, proto, store);

        if (isValueKnown(store)) {
            builder.append("100% ");
        }
        else {
            long confidenceBasis = 100_000 * childVisitCount / rootVisitCount;
            builder.append(confidenceBasis / 1000)
                    .append('.')
                    .append(String.format("%03d", confidenceBasis % 1000))
                    .append("% ");
        }

        builder.append("depth ").append(path.size()).append(": ");
        builder.append(Joiner.on(" / ").join(path));

        return builder.toString();
    }


    private void principalVariation(List<String> builder, State state, RolloutStore store) {
        int[] moves = state.legalMoves();
//        if (moves == null || moves.length == 0 || childNodes == null) {
        if (moves == null || moves.length == 0) {
            return;
        }

        int bestMove = bestMove(state, false, new long[0], 0, store);
        int maxChildIndex = Ints.indexOf(moves, bestMove);

        if (maxChildIndex == -1) {
            return;
        }
        RolloutNode maxChild = childOrNull(maxChildIndex, store);
        if (maxChild == null) {
            return;
        }

        builder.add(Move.toInputNotation(moves[maxChildIndex]) + " " + maxChild.visitCount(store));

        Move.apply(bestMove, state);
        maxChild.principalVariation(builder, state, store);
    }


    public String ucbVariation(State state, RolloutStore store, RolloutContext context) {
        StringBuilder builder = new StringBuilder();

        long rootCount = store.getVisitCount(RolloutStore.rootIndex);
        long transpositionThreshold = Math.max(transpositionMinimum, (long) Math.pow(rootCount, transpositionPower));

        List<String> path = new ArrayList<>();
        ucbVariation(path, state.prototype(), transpositionThreshold, store, context);

        builder.append("depth ").append(path.size()).append(": ");
        builder.append(Joiner.on(" / ").join(path));

        return builder.toString();
    }


    private void ucbVariation(
            List<String> builder,
            State state,
            long transpositionThreshold,
            RolloutStore store,
            RolloutContext context
    ) {
        int moveCount = state.legalMoves(context.movesA, context.movesC);
        if (moveCount <= 0) {
            return;
        }

        PuctEstimate estimate = context.pool.estimateBlockingCached(state, context.movesA, moveCount);

        int moveIndex = selectChild(
                moveCount,
                estimate.winProbability,
                estimate.moveProbabilities,
                transpositionThreshold,
                state,
                context);
        if (moveIndex == -1) {
            return;
        }

        int move = context.movesA[moveIndex];
        Move.apply(move, state);

        RolloutNode existingChild = childOrNull(moveIndex, context.store);
        if (existingChild == null) {
            return;
        }

        long childVisits = existingChild.visitCount(context.store);
        if (childVisits == 0) {
            return;
        }

        builder.add(String.format("%s %d %.4f %.3f",
                Move.toInputNotation(move),
                childVisits,
                expectedValue(moveIndex, context.store),
                estimate.moveProbabilities[moveIndex]));

        existingChild.ucbVariation(builder, state, transpositionThreshold, store, context);
    }


    public String toString(State state, RolloutStore store) {
        int[] moves = state.legalMoves();
        List<Integer> indexes = IntStream.range(0, moves.length).boxed().collect(Collectors.toList());

        double parentSum = 0;
        long parentCount = 0;
        long[] counts = new long[moves.length];
        double[] values = new double[moves.length];
        for (int i = 0; i < counts.length; i++) {
            RolloutNode node = childOrNull(i, store);
            if (node == null) {
                continue;
            }

            counts[i] = node.visitCount(store);
            values[i] = node.valueSum(store);

            parentCount += counts[i];
            parentSum += values[i];
        }

        indexes.sort((a, b) ->
                counts[a] != counts[b]
                ? -Long.compare(counts[a], counts[b])
                : -Double.compare(values[a], values[b]));

        double parentValue =
                isValueKnown(store)
                ? knownValue(store)
                : parentSum / parentCount;

        long parentVisitCount = parentCount;

        String childSummary = indexes
                .stream()
                .map(i -> String.format("%s %d %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
                        counts[i] == 0 ? unknownMoveEstimate : values[i] / counts[i]
                ))
                .collect(Collectors.joining(" | "));

        return String.format("%,d %.4f - %s",
                parentVisitCount,
                parentValue,
                childSummary);
    }
}
