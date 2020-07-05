package ao.chess.v2.engine.mcts.player.par;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.mcts.player.BanditNode;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;


class ParallelNode implements BanditNode {
    //-----------------------------------------------------------------------------------------------------------------
//    private static final double exploration = 0.5;
//    private static final int rolloutCount = 32;


    //-----------------------------------------------------------------------------------------------------------------
    private final LongAdder visitCount;
    private final DoubleAdder valueSum;

    private final int[] moves;
    private final CopyOnWriteArrayList<ParallelNode> childNodes;


    //-----------------------------------------------------------------------------------------------------------------
    public ParallelNode(int[] moves)
    {
        visitCount = new LongAdder();
        valueSum = new DoubleAdder();

        this.moves = moves;
        childNodes = new CopyOnWriteArrayList<>(
                Collections.nCopies(moves.length, null));
    }


    //-----------------------------------------------------------------------------------------------------------------
    private boolean isUnvisited() {
        return visitCount.longValue() == 0;
    }

    private boolean isUnvisitedVirtual() {
        return visitCount.longValue() < 2;
    }


    public void validate()
    {
        // NB: doesn't work when it's about to lose
//        long childVisits = 0;
//        for (var child : childNodes) {
//            childVisits += child.visitCount.sum();
//        }
//        long delta = visitCount.sum() - childVisits;
//        if (delta != 1) {
//            throw new IllegalStateException("delta = " + delta);
//        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void initTrajectory() {
        visitCount.increment();
    }


    public void runTrajectory(
            State state,
            ParallelContext context)
    {
        List<ParallelNode> path = context.path;
        path.clear();

        path.add(this);
//        visitCount.increment();

//        Colour initialPov = state.nextToAct();
//        double initialDefaultValue = defaultValue(state, initialPov, context);

        while (! path.get( path.size() - 1 ).isUnvisited())
        {
            ParallelNode node = path.get( path.size() - 1 );

            ParallelNode selectedChild =
                    node.descendByBandit(state, context);

            if (selectedChild == null) {
                break;
            }

            path.add( selectedChild );
//            selectedChild.visitCount.increment();
        }

        double leafValue = monteCarloPlayout(
                state/*, initialPov, initialDefaultValue*/, context);
        if (! (0 <= leafValue && leafValue <= 1.0)) {
            throw new IllegalStateException("Bad value: " + leafValue);
        }

        backupMcValue(path, leafValue);
    }


    //--------------------------------------------------------------------
    private ParallelNode descendByBandit(
            State state,
            ParallelContext context)
    {
        int moveCount = childNodes.size();
        if (moveCount == 0) {
            return null;
        }

        double[] moveValueSums = context.valueSums;
        long[] moveVisitCounts = context.visitCounts;
        long parentVisitCount = 0;
        for (int i = 0; i < moveCount; i++) {
            ParallelNode child = childNodes.get(i);
            if (child != null) {
                moveValueSums[i] = child.valueSum.sum();

                long childVisitCount = child.visitCount.sum();
                moveVisitCounts[i] = childVisitCount;
                parentVisitCount += childVisitCount;
            }
        }

        double greatestValue = Double.NEGATIVE_INFINITY;
        int greatestValueIndex = -1;

        int[] moveOrder = MovePicker.pickRandom(moveCount);
        for (int i : moveOrder) {
            ParallelNode child = childNodes.get( i );

            double banditValue;
            if (child == null || child.isUnvisited()) {
//                if (context.material) {
//                    int move = moves[i];
//                    banditValue = (! Move.isMobility(move) || Move.isPromotion(move) ? 1500 : 1000);
//                }
//                else {
                    banditValue = 1000;
//                }
            }
            else {
//                banditValue = child.confidenceBound(parentVisitCount, context);
                banditValue = child.confidenceBound(
                        parentVisitCount,
                        moveValueSums[i],
                        moveVisitCounts[i],
                        context);
            }

            if (banditValue > greatestValue) {
                greatestValue = banditValue;
                greatestValueIndex = i;
            }
        }
        if (greatestValueIndex == -1) {
            return null;
        }

        Move.apply(moves[greatestValueIndex], state);

        ParallelNode child = childNodes.get(greatestValueIndex);
        if (child == null) {
            int childMoveCount = state.legalMoves(context.movesA, context.movesC);
            ParallelNode newChild = new ParallelNode(Arrays.copyOf(context.movesA, childMoveCount));
            ParallelNode existing = childNodes.set(greatestValueIndex, newChild);
            if (existing != null) {
                childNodes.set(greatestValueIndex, existing);
                return existing;
            }
            else {
                return newChild;
            }
        }
        else {
            return child;
        }
    }


//    @SuppressWarnings("unchecked")
//    private void initiateKids(State fromState) {
//        acts = fromState.legalMoves();
//        kids = (acts == null)
//                ? null : new MctsNodeImpl[ acts.length ];
//    }


    private double confidenceBound(
            long parentVisitCount,
            double valueSumSnapshot,
            long visitCountSnapshot,
            ParallelContext context
    ) {
//        double currentValueSum = valueSum.sum();
//        long currentVisitCount = visitCount.sum();

        double averageReward = valueSumSnapshot / visitCountSnapshot;

        return averageReward +
                context.exploration *
                        Math.sqrt(2 * Math.log(parentVisitCount) / visitCountSnapshot);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private double monteCarloPlayout(
            State state,
//            Colour initialPov,
//            double initialDefaultValue,
            ParallelContext context)
    {
        Colour pov = state.nextToAct();
//        double povInitialDefaultValue =
//                initialPov == pov
//                ? initialDefaultValue
//                : 1.0 - initialDefaultValue;

        double sum = 0;
        int count = 0;
        for (int i = context.rollouts - 1; i >= 0; i--) {
            State freshState =
                    (i == 0)
                    ? state
                    : state.prototype();

            double value = computeMonteCarloPlayout(
                    freshState, pov/*, povInitialDefaultValue*/, context);

//            if (value != 0.5) {
            {
                sum += value;
                count++;
            }
        }
        double expectedValue = count == 0 ? 0.5 : sum / count;
        return expectedValue;
//        return Math.abs(expectedValue - 0.5) < 0.0001
//                ? 0.5
//                : expectedValue > 0.5
//                ? 1
//                : 0;

//        return Math.abs(expectedValue - 0.5) < 0.0001
//                ? 0.5
//                : expectedValue > 0.5
//                ? Math.max(expectedValue, 0.75)
//                : Math.min(expectedValue, 0.25);
    }


    private double computeMonteCarloPlayout(
            State state,
            Colour pov,
//            double povInitialDefaultValue,
            ParallelContext context
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
            return defaultValue(state, pov, context);
//            double defaultValue = defaultValue(state, pov, context);
//            double defaultValueDelta = defaultValue - povInitialDefaultValue;
//            double ex = Math.exp(defaultValueDelta);
//            return ex / (ex + 1);
        }

        return outcome.valueFor( pov );
    }


    private double defaultValue(
            State state,
            Colour pov,
            ParallelContext context
    ) {
        if (context.material) {
            return MaterialEvaluation.evaluate(state, pov);
        }
        else {
            return 0.5;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void backupMcValue(
            List<ParallelNode> path,
            double leafPlayout)
    {
        double reward = leafPlayout;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            // NB: negating all rewards so that UCB1 is always maximizing
            reward = 1.0 - reward;

            ParallelNode node = path.get(i);
            node.visitCount.increment();
            node.valueSum.add(reward);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public long visitCount() {
        return visitCount.sum();
    }

    public double valueSum() {
        return valueSum.sum();
    }


    public int bestMove() {
        if (moves.length == 0) {
            return -1;
        }

        int bestMoveIndex = 0;
        long bestMoveVisits = 0;
        for (int i = 0; i < moves.length; i++) {
            ParallelNode kid = childNodes.get(i);
            if (kid == null) {
                continue;
            }

            long moveVisits = kid.visitCount.longValue();
            if (bestMoveVisits < moveVisits) {
                bestMoveVisits = moveVisits;
                bestMoveIndex = i;
            }
        }
        return moves[bestMoveIndex];
    }


    public String moveStats()
    {
        List<Integer> indexes = new ArrayList<>();
        long[] visitCounts = new long[moves.length];
        double[] rewardSums = new double[moves.length];
        for (int i = 0; i < moves.length; i++) {
            indexes.add(i);

            ParallelNode child = childNodes.get(i);
            visitCounts[i] = (child == null ? 0 : child.visitCount.sum());
            rewardSums[i] = (child == null ? 0 : child.valueSum.sum() / visitCounts[i]);
        }

        indexes.sort(Comparator.comparingLong(a -> -visitCounts[a]));

        List<String> moveStats = new ArrayList<>();
        for (int index : indexes) {
            moveStats.add(String.format(
                    "%s (%,d / %.5f)",
                    Move.toInputNotation(moves[index]),
                    visitCounts[index],
                    rewardSums[index]
            ));
        }

        return String.format("%,d - %s",
                visitCount.sum(),
                String.join(" | ", moveStats));
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public BanditNode childMatching(int action) {
        for (int i = 0; i < moves.length; i++) {
            if (moves[i] == action) {
                return childNodes.get(i);
            }
        }
        return null;
    }

    @Override
    public int maxDepth() {
        if (childNodes.isEmpty()) {
            return 0;
        }

        int depth = 0;
        for (BanditNode child : childNodes) {
            if (child == null) {
                continue;
            }
            depth = Math.max(depth, child.maxDepth());
        }
        return depth + 1;
    }


    @Override
    public int minDepth() {
        if (childNodes.isEmpty()) {
            return 0;
        }

        int minDepth = Integer.MAX_VALUE;
        for (BanditNode child : childNodes) {
            if (child == null) {
                return 1;
            }
            minDepth = Math.min(minDepth, child.minDepth());
        }
        return minDepth + 1;
    }


    @Override
    public int nodeCount() {
        if (childNodes.isEmpty()) {
            return 1;
        }

        int size = 1;
        for (BanditNode kid : childNodes) {
            if (kid == null) {
                continue;
            }
            size += kid.nodeCount();
        }
        return size;
    }
}
