package ao.chess.v2.engine.mcts;

import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:54:41 PM
 */
public interface MctsRollout
{
    //--------------------------------------------------------------------
    double monteCarloPlayout(
            State fromState, MctsHeuristic heuristitc);


    //--------------------------------------------------------------------
//    public static interface Factory {
//        public MctsRollout newRollout();
//    }
}
