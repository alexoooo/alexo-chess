package ao.chess.v2.move;

/**
 * Date: Feb 6, 2009
 * Time: 4:37:00 AM
 */
public class Queen implements BoardPiece
{
    //--------------------------------------------------------------------
    private Queen() {}

    public static final BoardPiece MOVES = new Queen();


    //--------------------------------------------------------------------
    public long moves(long queen,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return Rook.MOVES.moves(
                    queen, occupied, notOccupied,
                    proponent, notProponent, opponent) |
               Bishop.MOVES.moves(
                    queen, occupied, notOccupied,
                    proponent, notProponent, opponent);
    }
}
