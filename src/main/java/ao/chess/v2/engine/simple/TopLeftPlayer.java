package ao.chess.v2.engine.simple;

import ao.chess.v2.engine.PlayerImpl;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

/**
 * User: aostrovsky
 * Date: 14-Sep-2009
 * Time: 4:13:18 PM
 */
public class TopLeftPlayer extends PlayerImpl
{
    //--------------------------------------------------------------------
    private final int[] moves = new int[ Move.MAX_PER_PLY ];
    private final int[] pseudoMoves = new int[ Move.MAX_PER_PLY ];


    //--------------------------------------------------------------------
    public int move(
            State position)
    {
        int nMoves = position.legalMoves(moves, pseudoMoves);
        if (nMoves <= 0) {
            return -1;
        }

        return moves[0];
    }
}
