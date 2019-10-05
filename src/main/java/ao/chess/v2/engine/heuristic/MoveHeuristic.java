package ao.chess.v2.engine.heuristic;

import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 11:27:53 AM
 */
public interface MoveHeuristic
{
    //--------------------------------------------------------------------
    double evaluate(State state, int move);


    //--------------------------------------------------------------------
    void update(State fromState, int move, Outcome outcome);


    //--------------------------------------------------------------------
    void persist();
}
