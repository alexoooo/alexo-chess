package ao.chess.v2.engine;

import ao.chess.v2.state.State;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 12:13:07 AM
 */
public abstract class PlayerImpl implements Player
{
    //--------------------------------------------------------------------
    public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        return move(position);
    }


    //--------------------------------------------------------------------
    public abstract int move(State position);
}
