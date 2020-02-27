package ao.chess.v2.engine.neuro.puct;


import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
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
class PuctNode {
    //-----------------------------------------------------------------------------------------------------------------
    public static final double minimumGuess = 0.1;
    private static final double maximumGuess = 0.9;
    public static final double guessRange = maximumGuess - minimumGuess;

//    private static final double initialPlayEstimate = 0.2;
    private static final double initialPlayEstimate = minimumGuess;

    private static final double underpromotionEstimate = 0;
    private static final double underpromotionPrediction = 0.001;
    private static final double randomizationWeight = 0.15;
//    private static final double randomizationWeight = 0.1;
//    private static final double randomizationWeight = 0.075;
//    private static final double randomizationWeight = 1 / 20.0;
//    private static final double randomizationWeight = 1 / 25.0;
//    private static final double randomizationWeight = 1 / 150.0;
//    private static final double randomizationWeight = 1 / 175.0;
//    private static final double randomizationWeight = 1 / 200.0;
//    private static final double randomizationWeight = 1 / 225.0;
//    private static final double randomizationWeight = 1 / 250.0;
//    private static final double randomizationWeight = 1 / 1000.0;

    private static final boolean uncertaintyEnabled = false;
//    private static final boolean uncertaintyEnabled = true;
    private static final double uncertaintyLogBase = Math.log(16);
    private static final double uncertaintyLogOffset = 2.5;
//    private static final double uncertaintyLogOffset = 2.75;
//    private static final double uncertaintyLogShift = 32512;
//    private static final double uncertaintyLogShift = 16200;
//    private static final double uncertaintyLogShift = 12288;
    private static final double uncertaintyLogShift = 8192;
//    private static final double uncertaintyMinimum = 0.001;
    private static final double uncertaintyMinimum = 0.0;

    private static final double stochasticPower = 3.0;

    private static final boolean epsilonGreedy = false;
    private static final double epsilonPerThread = 1 / 1000.0;

    private static final int[] emptyMoves = new int[0];
    private static final double[] emptyPredictions = new double[0];


    //-----------------------------------------------------------------------------------------------------------------
    private final int[] moves;
    private final double[] predictions;

    private final LongAdder visitCount;
    private final DoubleAdder valueSum;

    private final ChildList<PuctNode> childNodes;
    private final DeepOutcome deepOutcomeOrNull;

    private volatile double knownValue;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctNode(DeepOutcome deepOutcome, State state)
    {
        this(null,
                null,
                deepOutcome,
                PuctUtils.deepOutcomeValue(state, deepOutcome));
    }


    public PuctNode(Outcome outcome, State state)
    {
        this(emptyMoves,
                emptyPredictions,
                new DeepOutcome(outcome, 0),
                outcome.valueFor(state.nextToAct()));
    }


    public PuctNode(int[] moves, double[] predictions)
    {
        this(moves, predictions, null, Double.NaN);
    }


    private PuctNode(int[] moves, double[] predictions, DeepOutcome deepOutcomeOrNull, double knownValue)
    {
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

        this.knownValue = knownValue;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public long visitCount() {
        return visitCount.longValue();
    }


    public long visitCount(int childIndex) {
        PuctNode child = childNodes.get(childIndex);
        return child == null
                ? 0
                : child.visitCount();
    }


    public double expectedValue(int moveIndex) {
        long count = visitCount(moveIndex);
        if (count == 0) {
            return initialPlayEstimate;
        }

        PuctNode child = childNodes.get(moveIndex);
        double childValueSum = child.valueSum.doubleValue();
        return childValueSum / count;
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
            PuctContext context)
    {
        List<PuctNode> path = context.path;
        path.clear();

        path.add(this);
        visitCount.increment();

        double estimatedValue = Double.NaN;
//        boolean debug = false;

        while (! path.get( path.size() - 1 ).isUnvisitedVirtual())
        {
            PuctNode node = path.get( path.size() - 1 );

            int childIndex = node.puctChild(context);

            if (node.isValueKnown()) {
//                System.out.println(">> " + state.toFen() + " - " + node.knownValue);
                estimatedValue = node.knownValue;
                context.solutionHits.increment();
//                estimatedValue = 1.0 - node.knownValue;
                break;
            }

//            if (path.size() == 1) {
//                if (childIndex == 3) {
//                    debug = true;
//                }
//                System.out.println("!!!!! " + Move.toString(node.moves[childIndex]));
//            }

            Move.apply(node.moves[childIndex], state);

            PuctNode existingChild = node.childNodes.get(childIndex);

            PuctNode child;
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

            if (child.isValueKnown()) {
                estimatedValue = child.knownValue;

                if (child.moves == null) {
                    context.tablebaseHits.increment();
                }
                else if (child.moves.length == 0) {
                    context.terminalHits.increment();
                }
                else {
                    context.solutionHits.increment();
                }

//                if (debug && state.nextToAct() == Colour.WHITE && child.knownValue == 1.0) {
//                    System.out.println("??????");
//                }
//                System.out.println(">>> " + state.toFen() + " - " + child.knownValue);
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
            State state, PuctNode parent, int childIndex, PuctContext context
    ) {
        int moveCount = state.legalMoves(context.movesA);
        if (moveCount == 0 || moveCount == -1) {
            Outcome knownOutcome = state.knownOutcome();
            PuctNode newChild = new PuctNode(knownOutcome, state);
            context.estimatedValue = newChild.knownValue;
            context.terminalHits.increment();
            return addChildIfRequired(newChild, parent, childIndex);
        }
        else if (state.isDrawnBy50MovesRule()) {
            PuctNode newChild = new PuctNode(DeepOutcome.DRAW, state);
            context.estimatedValue = 0.5;
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

            PuctNode newChild = new PuctNode(deepOutcome, state);
            context.estimatedValue = newChild.knownValue;
            return addChildIfRequired(newChild, parent, childIndex);
        }

        Long positionKey = state.staticHashCode();

        PuctEstimate cached = context.nnCache.get(positionKey);

        PuctEstimate estimate;
        if (cached == null) {
            estimate = context.pool.estimateBlocking(
                    state, legalMoves);

            context.nnCache.put(positionKey, estimate);
        }
        else {
            estimate = cached;
            context.cacheHits.increment();
        }

        PuctNode newChild = new PuctNode(
                legalMoves, estimate.moveProbabilities);

        boolean wasSet = parent.childNodes.setIfAbsent(childIndex, newChild);
        if (! wasSet) {
            context.collisions.increment();
            return false;
        }

        double drawProximity =
                (double) state.reversibleMoves() / 250;

        double adjusted = drawProximity * 0.5 + (1 - drawProximity) * estimate.winProbability;

        context.estimatedValue = adjusted;

        return true;
    }


    private boolean addChildIfRequired(
            PuctNode newChild,
            PuctNode parent,
            int childIndex
    ) {
        return parent.childNodes.setIfAbsent(childIndex, newChild);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int puctChild(
            PuctContext context)
    {
        int moveCount = moves.length;
        if (moveCount == 0) {
            return -1;
        }

        int nonWinKnownCount = 0;

        double[] moveValueSums = context.valueSums;
        long[] moveVisitCounts = context.visitCounts;
        long parentVisitCount = 1;
        for (int i = 0; i < moveCount; i++) {
            PuctNode child = childNodes.get(i);

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
                        return i;
                    }

                    nonWinKnownCount++;
                    childValueSum = (1.0 - known) * childVisitCount;
//                    childValueSum = known * childVisitCount;
                }
            }

            moveValueSums[i] = childValueSum;
            moveVisitCounts[i] = childVisitCount;

            parentVisitCount += childVisitCount;
        }

        if (nonWinKnownCount == moveCount) {
            double childValue = 1.0;
//            double childValue = 0.0;
            int childIndex = 0;
            for (int i = 0; i < moveCount; i++) {
                PuctNode child = childNodes.get(i);
                double value = child.knownValue;
                if (value < childValue) {
//                if (value > childValue) {
                    childValue = value;
                    childIndex = i;
                }
            }
            knownValue = 1.0 - childValue;
//            knownValue = childValue;
            return childIndex;
        }

        return epsilonGreedy
                ? puctChildEpsilon(moveCount, moveValueSums, moveVisitCounts, parentVisitCount, context)
                : puctChildSmear(moveCount, moveValueSums, moveVisitCounts, parentVisitCount, context);
    }


    private int puctChildSmear(
            int moveCount,
            double[] moveValueSums,
            long[] moveVisitCounts,
            long parentVisitCount,
            PuctContext context)
    {
        double firstPlayEstimate = 0;
        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;

        double moveUncertainty =
                uncertaintyEnabled
                ? Math.max(uncertaintyMinimum, 1.0 - 1.0 / Math.max(1,
                        Math.log(parentVisitCount + uncertaintyLogShift) / uncertaintyLogBase - uncertaintyLogOffset))
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

            double adjustedScore;
            if (context.randomize && moveVisits < 256) {
                double adjustment =
                        randomizationWeight *
                        (1.0 - 1.0 / Math.sqrt(context.threads)) *
                        (1.0 / Math.sqrt(Math.max(1, moveVisits - 10))) *
                        context.random.nextDouble();
//                adjustedScore = score + adjustment;

                // TODO: test
                adjustedScore = score;
            }
            else {
                adjustedScore = score;
            }

            if (adjustedScore > maxScore ||
                    adjustedScore == maxScore && context.random.nextBoolean()) {
                maxScore = adjustedScore;
                maxScoreIndex = i;
            }
        }

        return maxScoreIndex;
    }


    private int puctChildEpsilon(
            int moveCount,
            double[] moveValueSums,
            long[] moveVisitCounts,
            long parentVisitCount,
            PuctContext context)
    {
        double epsilon = context.random.nextDouble();

        if (epsilon < epsilonPerThread * context.threads)
        {
            return context.random.nextInt(moveCount);
        }

        double firstPlayEstimate = 0;
        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = 0;

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

            double unvisitedBonus =
                    prediction *
                    (Math.sqrt(parentVisitCount) / (moveVisits + 1)) *
                    (context.exploration +
                            Math.log((parentVisitCount + context.explorationLog + 1) / context.explorationLog));

            double score = averageOutcome + unvisitedBonus;

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
            double leafValue,
            PuctContext context)
    {
        double inverseValue = 1.0 - leafValue;
        // NB: negating all rewards so that we're always maximizing
        boolean reverse = true;
//        boolean reverse = false;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            double negaMaxValue = reverse ? inverseValue : leafValue;

            PuctNode node = path.get(i);

//            if (i == 1) {
//                System.out.println(" ## " + negaMaxValue + " - " + node.toString(context));
//            }

            node.valueSum.add(negaMaxValue);

            reverse = ! reverse;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public boolean isValueKnown()
    {
        return ! Double.isNaN(knownValue);
    }


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
        if (! Double.isNaN(knownValue)) {
            double minChildValue = 1.0;
            int minChildIndex = 0;
            for (int i = 0; i < moves.length; i++) {
                PuctNode child = childOrNull(i);
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


    public PuctNode childOrNull(int moveIndex) {
        return childNodes.get(moveIndex);
    }


    private double childVisitCount(int moveIndex/*, boolean visitMax*/) {
        PuctNode child = childNodes.get(moveIndex);

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
        if (! Double.isNaN(knownValue)) {
            return knownValue;
        }

        long count = visitCount.longValue();
        double sum = valueSum.doubleValue();
        return count == 0
                ? initialPlayEstimate
                : 1.0 - sum / count;
    }


    private double childFirstPlayEstimate(PuctContext context)
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


    //-----------------------------------------------------------------------------------------------------------------
    public String toString(PuctContext context) {
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

        double moveUncertainty =
                uncertaintyEnabled
                ? 1.0 - 1.0 / Math.max(1,
                        Math.log(parentVisitCount + uncertaintyLogShift) / uncertaintyLogBase - uncertaintyLogOffset)
                : 0;
        double predictionDenominator = 1.0 + moveUncertainty * moves.length;

        String childSummary = indexes
                .stream()
                .map(i -> String.format("%s %d %d %.4f %.4f %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
                        counts[i] == 0 ? 0 : childNodes.get(i).maxDepth(),
                        counts[i] == 0 ? initialPlayEstimate : values[i] / counts[i],
                        predictions[i],
                        (predictions[i] + moveUncertainty) / predictionDenominator *
                                Math.sqrt(parentVisitCount) /
                                (counts[i] + 1) *
                                (context.exploration +
                                        Math.log((parentVisitCount + context.explorationLog + 1) / context.explorationLog))
                        ))
                .collect(Collectors.joining(" | "));

        return String.format("%d - %.4f - %.4f - %s",
                visitCount.longValue(),
                inverse,
                Math.log((parentVisitCount + context.explorationLog + 1) / context.explorationLog),
                childSummary);
    }
}
