package ao.chess.v2.engine.mcts.player.par;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.DoubleAdder;
import java.util.concurrent.atomic.LongAdder;


class ParallelNode {
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
        visitCount.increment();

        while (! path.get( path.size() - 1 ).isUnvisitedVirtual())
        {
            ParallelNode node = path.get( path.size() - 1 );

            ParallelNode selectedChild =
                    node.descendByBandit(state, context);

            if (selectedChild == null) {
                break;
            }

            path.add( selectedChild );
            selectedChild.visitCount.increment();
        }

        double leafValue = monteCarloPlayout(state, context);
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

        long currentVisitCount = visitCount.sum();

        double greatestValue = Double.NEGATIVE_INFINITY;
        int greatestValueIndex = -1;
        for (int i = 0; i < moveCount; i++) {
            ParallelNode child = childNodes.get( i );

            double banditValue;
            if (child == null || child.isUnvisited()) {
                if (context.material) {
                    int move = moves[i];
                    banditValue = (! Move.isMobility(move) || Move.isPromotion(move) ? 1500 : 1000) +
                            Math.random();
                }
                else {
                    banditValue = 1000 + context.random.nextDouble();
                }
            }
            else {
                banditValue = child.confidenceBound(currentVisitCount, context);
            }

            if (banditValue > greatestValue) {
                greatestValue      = banditValue;
                greatestValueIndex = i;
            }
        }
        if (greatestValueIndex == -1) {
            return null;
        }

        Move.apply(moves[greatestValueIndex], state);

        ParallelNode child = childNodes.get(greatestValueIndex);
        if (child == null) {
            int childMoveCount = state.legalMoves(context.movesA);
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
            ParallelContext context
    ) {
        double currentValueSum = valueSum.sum();
        long currentVisitCount = visitCount.sum();

        double averageReward = currentValueSum / currentVisitCount;

        return averageReward +
                context.exploration *
                        Math.sqrt(2 * Math.log(parentVisitCount) / currentVisitCount);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private double monteCarloPlayout(
            State state,
            ParallelContext context)
    {
        double sum = 0;
        for (int i = context.rollouts; i >= 0; i--) {
            State freshState =
                    (i == 0)
                    ? state
                    : state.prototype();

            sum += computeMonteCarloPlayout(freshState, context);
        }
        return sum / context.rollouts;
    }


    private double computeMonteCarloPlayout(
            State state,
            ParallelContext context
    ) {
        Colour pov = state.nextToAct();

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

            path.get(i).valueSum.add(reward);
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
        for (int i = 0; i < moves.length; i++) {
            indexes.add(i);

            ParallelNode child = childNodes.get(i);
            visitCounts[i] = (child == null ? 0 : child.visitCount.sum());
        }

        indexes.sort(Comparator.comparingLong(a -> -visitCounts[a]));

        List<String> moveStats = new ArrayList<>();
        for (int index : indexes) {
            moveStats.add(Move.toInputNotation(moves[index]) + " (" + visitCounts[index] + ")");
        }

        return visitCount.sum() + " - " + String.join(" | ", moveStats);
    }
}
