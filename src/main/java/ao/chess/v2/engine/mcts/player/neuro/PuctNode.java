package ao.chess.v2.engine.mcts.player.neuro;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
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
    private static final double guessRange = maximumGuess - minimumGuess;

//    private static final double firstPlayEstimate = 0.45;
    private static final double firstPlayEstimate = minimumGuess;

    private static final double underpromotionEstimate = 0;
    private static final double underpromotionPrediction = 0.001;
    private static final double rolloutWeight = 0.25;

    private static final boolean valueUncertainty = false;


    //-----------------------------------------------------------------------------------------------------------------
    private final int[] moves;
    private final double[] predictions;

    private final LongAdder visitCount;
    private final DoubleAdder valueSum;

    private final CopyOnWriteArrayList<PuctNode> childNodes;
    private final DeepOutcome deepOutcomeOrNull;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctNode(int[] moves, double[] predictions, DeepOutcome deepOutcomeOrNull)
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
            childNodes = new CopyOnWriteArrayList<>(
                    Collections.nCopies(moves.length, null));
        }
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
                // TODO: cleanup
                reachedTerminal = true;
                break;
            }

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

            if (expanded) {
                estimatedValue = context.estimatedValue;
                break;
            }
            else if (child.deepOutcomeOrNull != null) {
                estimatedValue = child.deepOutcomeValue(state, child.deepOutcomeOrNull);
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

        backupValue(path, leafValue);
    }


    private boolean expandChildAndSetEstimatedValue(
            State state, PuctNode parent, int childIndex, PuctContext context
    ) {
        int moveCount = state.legalMoves(context.movesA);
        if (moveCount == 0 || moveCount == -1) {
            Outcome knownOutcome = state.knownOutcome();
            PuctNode newChild = new PuctNode(new int[0], new double[0],
                    new DeepOutcome(knownOutcome, 0));

            context.estimatedValue = knownOutcome.valueFor(state.nextToAct());

            return addChildIfRequired(newChild, parent, childIndex);
        }
        else if (state.isDrawnBy50MovesRule()) {
            PuctNode newChild = new PuctNode(null, null, DeepOutcome.DRAW);
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

            PuctNode newChild = new PuctNode(null, null, deepOutcome);

//            adjusted = newChild.deepOutcomeValue(state, deepOutcome);

            return addChildIfRequired(newChild, parent, childIndex);
        }

        Long positionKey = state.staticHashCode();

        PuctEstimate cached = context.nnCache.get(positionKey);

        PuctEstimate estimate;
        if (cached == null) {
            INDArray input = NeuralCodec.INSTANCE.encodeState(state);
            INDArray output = context.nn.output(input);

            double[] childPredictions = NeuralCodec.INSTANCE
                    .decodeMoveProbabilities(output, state, legalMoves);

            PuctUtils.smearProbabilities(
                    childPredictions, context.moveUncertainty);

            double childOutcome = NeuralCodec.INSTANCE.decodeOutcome(output);
            double scaleOutcome = guessRange * childOutcome + minimumGuess;

            estimate = new PuctEstimate(childPredictions, scaleOutcome);
            context.nnCache.put(positionKey, estimate);
        }
        else {
            estimate = cached;
            context.cacheHits.increment();
        }

        PuctNode newChild = new PuctNode(legalMoves, estimate.moveProbabilities, null);

        PuctNode existing = parent.childNodes.set(childIndex, newChild);
        if (existing != null) {
            parent.childNodes.set(childIndex, existing);
            return false;
        }

        double drawProximity =
                (double) state.reversibleMoves() / 200;
//                state.reversibleMoves() <= 30
//                ? 0
//                : state.reversibleMoves() <= 70
//                ? (double) state.reversibleMoves() / 100
//                : Math.pow((double) state.reversibleMoves() / 100, 2);
        double adjusted = drawProximity * 0.5 + (1 - drawProximity) * estimate.winProbability;

        if (context.rollouts == 0) {
            context.estimatedValue = adjusted;
        }
        else {
            double rolloutValue = randomRollouts(state, context);
            context.estimatedValue =
                    rolloutWeight * rolloutValue +
                    (1 - rolloutWeight) * adjusted;
        }

        return true;
    }


    private boolean addChildIfRequired(
            PuctNode newChild,
            PuctNode parent,
            int childIndex
    ) {
        PuctNode existing = parent.childNodes.set(childIndex, newChild);
        if (existing != null) {
            parent.childNodes.set(childIndex, existing);
            return false;
        }

        return true;
    }


    private double deepOutcomeValue(State state, DeepOutcome deepOutcome) {
        if (state.isDrawnBy50MovesRule()) {
            return 0.5;
        }

        double horizon =
                (double) Math.abs(deepOutcome.plyDistance()) / 400;

        Colour pov = state.nextToAct();
        Outcome outcome = deepOutcome.outcome();
        if (outcome == Outcome.DRAW) {
            return  0.5;
        }
        else if (outcome.winner() == pov) {
            return 1 - horizon;
        }
        else {
            return horizon;
        }
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
            return 0.5;
//            return MaterialEvaluation.evaluate(state, pov);
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
                averageOutcome =
                        moveVisits == 0
                        ? firstPlayEstimate
                        : moveValueSums[i] / moveVisits;

                prediction = predictions[i];
            }

            double unvisitedBonus =
                    prediction * Math.sqrt(parentVisitCount) / (moveVisits + 1);
//                    prediction * Math.pow(parentVisitCount, 0.75) / (moveVisits + 1);

            double score = averageOutcome + context.exploration * unvisitedBonus;

            if (score > maxScore ||
                    score == maxScore && context.random.nextBoolean()) {
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

            if (bestMoveScore < moveScore ||
                    bestMoveScore == moveScore &&
                            visitCount(bestMoveIndex) < visitCount(i)
            ) {
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

        double certainty =
                valueUncertainty
                ? 1.0 - (1 / Math.sqrt(visits + 1))
                : 1.0;

        return certainty * child.valueSum.doubleValue() / visits * 10_000;
    }


    public double inverseValue()
    {
        long count = visitCount.longValue();
        double sum = valueSum.doubleValue();
        return count == 0
                ? firstPlayEstimate
                : 1.0 - sum / count;
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
                .map(i -> String.format("%s %d %d %.4f %.4f %.4f",
                        Move.toInputNotation(moves[i]),
                        counts[i],
                        counts[i] == 0 ? 0 : childNodes.get(i).maxDepth(),
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
