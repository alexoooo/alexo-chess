package ao.chess.v2.engine.simple;

import ao.chess.v2.engine.PlayerImpl;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

/**
 * User: aostrovsky
 * Date: 14-Sep-2009
 * Time: 4:13:18 PM
 */
public class RandomPlayer extends PlayerImpl
{
    //--------------------------------------------------------------------
    private final int[] moves = new int[ Move.MAX_PER_PLY ];


    //--------------------------------------------------------------------
    public int move(
            State position)
    {
        int nMoves = position.legalMoves(moves);
        if (nMoves <= 0) return -1;

        return moves[(int)(Math.random() * nMoves)];
    }
}
