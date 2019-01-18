package ao.chess.v2.engine.mcts.rollout;

import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:56:58 PM
 */
public class MctsDeepRolloutImpl
    implements MctsRollout
{
    //--------------------------------------------------------------------
//    public static class Factory implements MctsRollout.Factory {
//        @Override public MctsRollout newRollout() {
//            return new MctsRolloutImpl();
//        }
//    }


    //--------------------------------------------------------------------
    private final int nSims;


    //--------------------------------------------------------------------
    public MctsDeepRolloutImpl()
    {
        this(1);
    }

    public MctsDeepRolloutImpl(int accuracy)
    {
        nSims = accuracy;
    }


    //--------------------------------------------------------------------
    @Override public double monteCarloPlayout(
            State position, MctsHeuristic heuristic)
    {
        Colour fromPov = position.nextToAct();
        State  state   = position;
        while (! state.isDrawnBy50MovesRule())
        {
            int move = bestMove(state, heuristic);
            if (move == -1) {
                return state.knownOutcome()
                        .valueFor( fromPov );
            }
            Move.apply(move, state);
        }
        return 0.5;
    }


    //--------------------------------------------------------------------
    private int bestMove(State position, MctsHeuristic heuristic)
    {
        int[] moves  = new int[Move.MAX_PER_PLY];
        int   nMoves = position.legalMoves(moves);
        if (nMoves == 0) return -1;

        State    state       = position.prototype();
        int   [] count       = new int   [ nMoves ];
        double[] expectation = new double[ nMoves ];
        for (int i = 0; i < nSims; i++) {
            for (int m = 0; m < nMoves; m++)
            {
                int move = Move.apply(moves[m], state);
                expectation[m] += computeMonteCarloPlayout(
                        state, heuristic);
                Move.unApply(move, state);
                count[ m ]++;
            }
        }

        return optimize(nMoves, moves, count, expectation);
    }


    //--------------------------------------------------------------------
    private int optimize(
            int      nMoves,
            int   [] moves,
            int   [] count,
            double[] expectation)
    {
        double maxEv      = -1;
        int    maxEvIndex = -1;
        for (int m = 0; m < nMoves; m++) {
            double ev = expectation[ m ] / count[ m ];
            if (ev > maxEv) {
                maxEv      = ev;
                maxEvIndex = m;
            }
        }
        return moves[ maxEvIndex ];
    }


    //--------------------------------------------------------------------
    private double computeMonteCarloPlayout(
            State fromState, MctsHeuristic heuristic) {
        State   simState  = fromState.prototype();
        int     nextCount = 0;
        int[]   nextMoves = new int[ Move.MAX_PER_PLY ];
        int[]   moves     = new int[ Move.MAX_PER_PLY ];
        int     nMoves    = simState.moves(moves);
        Outcome outcome   = Outcome.DRAW;

        do
        {
            int     move;
            boolean madeMove = false;

//            int[] moveOrder = heuristic.orderMoves(
//                    simState, moves, nMoves);
//            for (int moveIndex : moveOrder)
            for (int moveIndex = 0; moveIndex < nMoves; moveIndex++)
            {
                move = Move.apply(moves[ moveIndex ], simState);

                // generate opponent moves
                nextCount = simState.moves(nextMoves);

                if (nextCount < 0) { // if leads to mate
                    Move.unApply(move, simState);
                } else {
                    madeMove = true;
                    break;
                }
            }
            if (! madeMove) {
                outcome = simState.isInCheck(simState.nextToAct())
                          ? Outcome.loses(simState.nextToAct())
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
        while (! simState.isDrawnBy50MovesRule());

        return outcome == null
               ? Double.NaN
               : outcome.valueFor( simState.nextToAct() );
    }
}