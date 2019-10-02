package ao.chess.v2.engine.mcts;

import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:22:34 PM
 */
public interface MctsHeuristic
{
    //--------------------------------------------------------------------
    double firstPlayUrgency(int move);


    //--------------------------------------------------------------------
    int[] orderMoves(State fromState, int[] moves, int nMoves);


    //--------------------------------------------------------------------
//    public static interface Factory {
//        public MctsHeuristic newHeuristic();
//    }
}
