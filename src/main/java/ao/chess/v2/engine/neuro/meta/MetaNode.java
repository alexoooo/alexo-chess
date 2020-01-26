package ao.chess.v2.engine.neuro.meta;


import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.neuro.puct.PuctUtils;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.util.ChildList;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Collectors;
import java.util.stream.IntStream;


// https://arxiv.org/pdf/1911.08265.pdf
class MetaNode {
    //-----------------------------------------------------------------------------------------------------------------
    public static final double minimumGuess = 0.1;
    private static final double maximumGuess = 0.9;
    public static final double guessRange = maximumGuess - minimumGuess;

    private static final double initialPlayEstimate = minimumGuess;

    private static final double underpromotionEstimate = 0;
    private static final double underpromotionPrediction = 0.001;
//    private static final double randomizationWeight = 1 / 150.0;
    private static final double randomizationWeight = 1 / 175.0;

//    private static final boolean uncertaintyEnabled = false;
    private static final boolean uncertaintyEnabled = true;
    private static final double uncertaintyLogBase = Math.log(16);
    private static final double uncertaintyLogOffset = 2.5;
//    private static final double uncertaintyLogShift = 16200;
    private static final double uncertaintyLogShift = 8192;

    private static final double stochasticPower = 3.0;

    private static final double errorThreshold = 0.0;
//    private static final double errorThreshold = 0.001;
//    private static final double errorThreshold = 0.01;
//    private static final double errorThreshold = 0.05;
//    private static final double errorThreshold = 0.1;
//    private static final double errorThreshold = 0.15;


    private static final int[] emptyMoves = new int[0];
    private static final double[] emptyPredictions = new double[0];


    //-----------------------------------------------------------------------------------------------------------------
    private final int[] moves;
    private final double[] predictions;

    private final LongAdder visitCount;
    private final DoubleAdder valueSum;

    private final ChildList<MetaNode> childNodes;
    private final DeepOutcome deepOutcomeOrNull;

    private volatile double maxError;
    private volatile double maxValueCache = Double.NaN;


    //-----------------------------------------------------------------------------------------------------------------
    public MetaNode(int[] moves, double[] predictions, double winError)
    {
        this(moves, predictions, null, winError);
    }


    public MetaNode(Outcome outcome, State state)
    {
        this(emptyMoves,
                emptyPredictions,
                new DeepOutcome(outcome, 0),
                0);

        maxValueCache = outcome.valueFor(state.nextToAct());
    }


    public MetaNode(DeepOutcome deepOutcome, State state)
    {
        this(null,
                null,
                deepOutcome,
                0);

        maxValueCache = PuctUtils.deepOutcomeValue(state, deepOutcome);
    }


    private MetaNode(int[] moves, double[] predictions, DeepOutcome deepOutcomeOrNull, double winError)
    {
//        if (moves.length != predictions.length) {
//            System.out.println("foo");
//        }

        this.moves = moves;
        this.predictions = predictions;
        this.deepOutcomeOrNull = deepOutcomeOrNull;

        visitCount = new LongAdder();
        valueSum = new DoubleAdder();

        if (moves == null) {
            childNodes = null;
        }
        else {
            childNodes = new ChildList<>(moves.length);
        }

        maxError = winError;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public long visitCount() {
        return visitCount.longValue();
    }


    public long visitCount(int childIndex) {
        MetaNode child = childNodes.get(childIndex);
        return child == null
                ? 0
                : child.visitCount();
    }


    public double expectedValue(int moveIndex) {
        long count = visitCount(moveIndex);
        if (count == 0) {
            return initialPlayEstimate;
        }

        MetaNode child = childNodes.get(moveIndex);
        double childValueSum = child.valueSum.doubleValue();
        return childValueSum / count;
    }


    public int maxValueChild() {
        double maxValue = 0.0;
        int maxValueChild = 0;
        for (int i = 0; i < moves.length; i++) {
//            double value = expectedValue(i);
            MetaNode child = childNodes.get(i);
            double value =
                    child == null
                    ? initialPlayEstimate
                    : 1.0 - child.maxChildValue();

            if (value > maxValue) {
                maxValue = value;
                maxValueChild = i;
            }
        }
        return maxValueChild;
    }


    public double maxChildValue() {
        double cache = maxValueCache;
        if (! Double.isNaN(cache)) {
            return cache;
        }

        double maxValue = -1;
        for (int i = 0; i < moves.length; i++) {
            MetaNode child = childNodes.get(i);
            if (child == null) {
                continue;
            }

            double value = child.maxChildValue();
            if (value > maxValue) {
                maxValue = value;
            }
        }

        if (maxValue == -1) {
            long count = visitCount.longValue();
            double sum = valueSum.doubleValue();
            maxValue = count == 0
                    ? initialPlayEstimate
                    : 1.0 - sum / count;
        }

        maxValueCache = maxValue;

        return maxValue;
    }


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
            MetaContext context)
    {
        List<MetaNode> path = context.path;
        path.clear();

        path.add(this);
        visitCount.increment();

        boolean reachedTerminal = false;
        double estimatedValue = Double.NaN;
//        double errorValue = Double.NaN;

        while (! path.get( path.size() - 1 ).isUnvisitedVirtual())
        {
            MetaNode node = path.get( path.size() - 1 );

            int childIndex = node.puctChild(context);

            if (childIndex == -1) {
                // TODO: cleanup
                reachedTerminal = true;
                context.terminalHits.increment();
                break;
            }

            Move.apply(node.moves[childIndex], state);

            MetaNode existingChild = node.childNodes.get(childIndex);

            MetaNode child;
            boolean expanded;
            if (existingChild == null) {
                expanded = expandChildAndSetEstimatedValue(state, node, childIndex, context);
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
//                errorValue = context.estimatedError;
                break;
            }
            else if (child.deepOutcomeOrNull != null) {
//                estimatedValue = child.deepOutcomeValue(state, child.deepOutcomeOrNull);
                estimatedValue = child.maxValueCache;
//                errorValue = 0.0;
                if (child.moves == null) {
                    context.tablebaseHits.increment();
                }
                else {
                    context.terminalHits.increment();
                }
                break;
            }
        }

        if (Double.isNaN(estimatedValue)) {
            // NB: race condition with new node creation
            return;
        }

        double leafValue = reachedTerminal
                ? (state.isInCheck(state.nextToAct()) ? 0 : 0.5)
                : estimatedValue;

        backupValue(path, leafValue/*, errorValue*/);
    }


    private boolean expandChildAndSetEstimatedValue(
            State state, MetaNode parent, int childIndex, MetaContext context
    ) {
        int moveCount = state.legalMoves(context.movesA);
        if (moveCount == 0 || moveCount == -1) {
            Outcome knownOutcome = state.knownOutcome();
            MetaNode newChild = new MetaNode(knownOutcome, state);

            context.estimatedValue = newChild.maxValueCache;
//            context.estimatedError = 0.0;
            context.terminalHits.increment();

            return addChildIfRequired(newChild, parent, childIndex);
        }
        else if (state.isDrawnBy50MovesRule()) {
            MetaNode newChild = new MetaNode(DeepOutcome.DRAW, state);

            context.estimatedValue = 0.5;
//            context.estimatedError = 0.0;
            context.terminalHits.increment();

            return addChildIfRequired(newChild, parent, childIndex);
        }

        int[] legalMoves = Arrays.copyOf(context.movesA, moveCount);

        boolean tablebase =
                context.tablebase &&
                state.pieceCount() <= DeepOracle.instancePieceCount;

        if (tablebase) {
            DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(state);

            if (deepOutcome == null) {
                throw new IllegalStateException("Missing tablebase: " + state);
            }

            MetaNode newChild = new MetaNode(deepOutcome, state);

            context.estimatedValue = deepOutcome.outcome().valueFor(state.nextToAct());
//            context.estimatedError = 0.0;
            context.tablebaseHits.increment();

            return addChildIfRequired(newChild, parent, childIndex);
        }

        Long positionKey = state.staticHashCode();

        MetaEstimate cached = context.nnCache.get(positionKey);

        MetaEstimate estimate;
        if (cached == null) {
//            estimate = context.model.estimate(state, legalMoves);
//            estimate.postProcess(context.predictionUncertainty, guessRange, minimumGuess);

            estimate = context.pool.estimateBlocking(
                    state, legalMoves);

            context.nnCache.put(positionKey, estimate);
        }
        else {
            estimate = cached;
            context.cacheHits.increment();
        }

        MetaNode newChild = new MetaNode(
                legalMoves, estimate.moveProbabilities, estimate.winError);

        boolean wasSet = parent.childNodes.setIfAbsent(childIndex, newChild);
        if (! wasSet) {
            context.collisions.increment();
            return false;
        }
//        PuctNode existing = parent.childNodes.set(childIndex, newChild);
//        if (existing != null) {
//            parent.childNodes.set(childIndex, existing);
//            context.collisions.increment();
//            return false;
//        }

        double drawProximity =
                (double) state.reversibleMoves() / 250;
//                state.reversibleMoves() <= 30
//                ? 0
//                : state.reversibleMoves() <= 70
//                ? (double) state.reversibleMoves() / 100
//                : Math.pow((double) state.reversibleMoves() / 100, 2);
        double adjusted = drawProximity * 0.5 + (1 - drawProximity) * estimate.winProbability;

        context.estimatedValue = adjusted;
//        context.estimatedError = estimate.winError;

        return true;
    }


    private boolean addChildIfRequired(
            MetaNode newChild,
            MetaNode parent,
            int childIndex
    ) {
        return parent.childNodes.setIfAbsent(childIndex, newChild);

//        PuctNode existing = parent.childNodes.set(childIndex, newChild);
//        if (existing != null) {
//            parent.childNodes.set(childIndex, existing);
//            return false;
//        }
//
//        return true;
    }



    //-----------------------------------------------------------------------------------------------------------------
    private int puctChild(
            MetaContext context)
    {
//        int moveCount = childNodes.size();
        int moveCount = moves.length;
        if (moveCount == 0) {
            return -1;
        }

        if (maxError <= errorThreshold) {
            return maxValueChild();
        }

        double[] moveValueSums = context.valueSums;
        long[] moveVisitCounts = context.visitCounts;
        long parentVisitCount = 1;
        for (int i = 0; i < moveCount; i++) {
            MetaNode child = childNodes.get(i);

            long childVisitCount;
            double childValueSum;

            if (child == null) {
                childValueSum = 0;
                childVisitCount = 0;
            }
            else {
                childValueSum = child.valueSum.sum();
                childVisitCount = child.visitCount.sum();
            }

            moveValueSums[i] = childValueSum;
            moveVisitCounts[i] = childVisitCount;

            parentVisitCount += childVisitCount;
        }

        return puctChildSmear(moveCount, moveValueSums, moveVisitCounts, parentVisitCount, context);
    }


    private int puctChildSmear(
            int moveCount,
            double[] moveValueSums,
            long[] moveVisitCounts,
            long parentVisitCount,
            MetaContext context)
    {
        double firstPlayEstimate = 0;
        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;

        double moveUncertainty =
                uncertaintyEnabled
                        ? 1.0 - 1.0 / Math.max(1,
                        Math.log(parentVisitCount + uncertaintyLogShift) / uncertaintyLogBase - uncertaintyLogOffset)
                        : 0;
        double predictionDenominator = 1.0 + moveUncertainty * moveCount;

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
                    if (firstPlayEstimate == 0) {
                        firstPlayEstimate = childFirstPlayEstimate(context);
                    }
                    averageOutcome = firstPlayEstimate;
                }
                else {
                    averageOutcome = moveValueSums[i] / moveVisits;
                }

                prediction = predictions[i];
            }

            double smearedPrediction = (prediction + moveUncertainty) / predictionDenominator;

            double unvisitedBonus =
                    smearedPrediction *
                    (Math.sqrt(parentVisitCount) / (moveVisits + 1)) *
                    (context.exploration +
                            Math.log((parentVisitCount + context.explorationLog + 1) / context.explorationLog));

            double score = averageOutcome + unvisitedBonus;

            double adjustedScore =
                    context.randomize
                    ? context.random.nextDouble() *
                            context.threads *
                            (1.0 / Math.sqrt(Math.max(1, moveVisits - 10))) *
                            randomizationWeight +
                            score
                    : score;

            if (adjustedScore > maxScore ||
                    adjustedScore == maxScore && context.random.nextBoolean()) {
                maxScore = adjustedScore;
                maxScoreIndex = i;
            }
        }

        return maxScoreIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void backupValue(
            List<MetaNode> path,
            double leafPlayout)
    {
        double reward = leafPlayout;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            // NB: negating all rewards so that we're always maximizing
            reward = 1.0 - reward;

            MetaNode node = path.get(i);
            double nodeErr = node.maxError;

            if (i != path.size() - 1 &&
                    nodeErr <= errorThreshold)
            {
                double maxChildValue = node.maxChildValue();
                node.valueSum.add(1.0 - maxChildValue);
            }
            else
            {
                node.valueSum.add(reward);
            }

            if (i != 0)
            {
                MetaNode parent = path.get(i - 1);
                int maxChildIndex = parent.maxValueChild();
                if (node == parent.childNodes.get(maxChildIndex)) {
                    parent.maxError = nodeErr;

                    double maxChildValue = node.maxChildValue();
                    parent.maxValueCache = 1.0 - maxChildValue;
                }
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public int bestMove(
            State state,
            boolean stochastic,
            boolean isRepeat,
            long[] history,
            int historyCount
    ) {
        if (moves.length == 0) {
            return -1;
        }

        int contenderCount = 0;
        int[] moveContendersIndexes = new int[moves.length];
        int[] moveContendersCounts = new int[moves.length];
        int expectedCount = (int) Math.round((double) visitCount() / moves.length + 0.5);
        int contenderTotal = 0;

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

            if (stochastic && moveVisits < expectedCount) {
                continue;
            }

            contenderTotal += (int) moveScore;
            moveContendersIndexes[contenderCount] = i;
            moveContendersCounts[contenderCount] = (int) moveScore;
            contenderCount++;
        }

        int bestMoveIndex = 0;
        double bestMoveScore = 0;

        for (int i = 0; i < contenderCount; i++) {
            double moveScore;
            if (stochastic) {
                moveScore = Math.random() * Math.pow(
                        ((double) moveContendersCounts[i] / contenderTotal), stochasticPower);
            }
            else {
                moveScore = moveContendersCounts[i];
            }

            if (bestMoveScore < moveScore) {
                bestMoveScore = moveScore;
                bestMoveIndex = moveContendersIndexes[i];
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


    public long moveValue(int move/*, boolean visitMax*/) {
        int moveIndex = moveIndex(move);
        return (long) childVisitCount(moveIndex/*, visitMax*/);
    }


    public MetaNode childOrNull(int moveIndex) {
        return childNodes.get(moveIndex);
    }


    private double childVisitCount(int moveIndex/*, boolean visitMax*/) {
        MetaNode child = childNodes.get(moveIndex);

        if (child == null) {
            return 0;
        }

        return child.visitCount.longValue();
//        if (/*visitMax ||*/ visits == 0) {
//            return visits;
//        }

//        double certainty =
//                valueUncertainty
//                ? 1.0 - (1 / Math.sqrt(visits + 1))
//                : 1.0;
//
//        return certainty * child.valueSum.doubleValue() / visits * 10_000;
    }


    public double inverseValue()
    {
        if (maxError <= errorThreshold) {
            return maxChildValue();
        }

        long count = visitCount.longValue();
        double sum = valueSum.doubleValue();
        return count == 0
                ? initialPlayEstimate
                : 1.0 - sum / count;
    }


    private double childFirstPlayEstimate(MetaContext context)
    {
        long count = visitCount.longValue();
        if (count == 0) {
            return minimumGuess;
        }

        double sum = valueSum.doubleValue();
        double inverseValue = 1.0 - sum / count;

        return Math.max(minimumGuess, inverseValue - context.firstPlayDiscount);
    }


    public int maxDepth()
    {
        if (deepOutcomeOrNull != null) {
            return Math.abs(deepOutcomeOrNull.plyDistance());
        }

        int childMaxDepth = 0;

        for (var child : childNodes) {
            if (child == null) {
                continue;
            }
            childMaxDepth = Math.max(childMaxDepth, child.maxDepth());
        }

        return childMaxDepth + 1;
    }


    public double maxError()
    {
        return maxError;
//        double err = maxError;
//        if (! Double.isNaN(err)) {
//            return err;
//        }
////        else if (moves == null) {
////            return 0.0;
////        }
//
//        err = 0.0;
//
//        for (int i = 0; i < moves.length; i++) {
//            MetaNode child = childNodes.get(i);
//            if (child == null) {
//                continue;
//            }
//
//            double childErr = child.maxError();
//            if (err < childErr) {
//                err = childErr;
//            }
//        }
//
//        maxError = err;
//        return err;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public String toString(MetaContext context) {
        List<Integer> indexes = IntStream.range(0, moves.length).boxed().collect(Collectors.toList());

        long parentCount = 0;
        long[] counts = new long[moves.length];
        double[] values = new double[moves.length];
        for (int i = 0; i < counts.length; i++) {
            MetaNode node = childNodes.get(i);
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

//        double uncertainty = 1.0 - 1.0 / Math.max(1, Math.log(parentVisitCount) - 4);
//        double moveUncertainty = uncertainty / moves.length;
//        double predictionDenominator = 1.0 + uncertainty;

        double moveUncertainty =
                uncertaintyEnabled
                ? 1.0 - 1.0 / Math.max(1,
                        Math.log(parentVisitCount + uncertaintyLogShift) / uncertaintyLogBase - uncertaintyLogOffset)
                : 0;
        double predictionDenominator = 1.0 + moveUncertainty * moves.length;

        String childSummary = indexes
                .stream()
                .map(i -> String.format("%s %d %d %.4f +-%.4f %.4f %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
                        counts[i] == 0 ? 0 : childNodes.get(i).maxDepth(),
//                        counts[i] == 0 ? initialPlayEstimate : values[i] / counts[i],
                        counts[i] == 0 ? initialPlayEstimate : 1.0 - childNodes.get(i).inverseValue(),
                        counts[i] == 0 ? 1.0 : childNodes.get(i).maxError(),
                        predictions[i],
                        (predictions[i] + moveUncertainty) / predictionDenominator *
                                Math.sqrt(parentVisitCount) /
                                (counts[i] + 1) *
                                (context.exploration +
                                        Math.log((parentVisitCount + context.explorationLog + 1) / context.explorationLog))
                        ))
                .collect(Collectors.joining(" | "));

        return String.format("%d - %.4f +-%.4f - %.4f - %s",
                visitCount.longValue(),
                inverse,
                maxError(),
                Math.log((parentVisitCount + context.explorationLog + 1) / context.explorationLog),
                childSummary);
    }
}
