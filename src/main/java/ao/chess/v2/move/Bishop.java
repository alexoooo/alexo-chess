package ao.chess.v2.move;

import static ao.chess.v2.move.SlidingPieces.slide;

/**
 * Date: Feb 6, 2009
 * Time: 4:34:58 AM
 */
public class Bishop implements BoardPiece
{
    //--------------------------------------------------------------------
    private Bishop() {}

    public static final Bishop MOVES = new Bishop();


    //--------------------------------------------------------------------
    public static long attacks(
            long bishop,
            long notOccupied,
            long opponent
    ) {
        return slide(bishop,  1,  1, notOccupied, opponent) |
                slide(bishop,  1, -1, notOccupied, opponent) |
                slide(bishop, -1,  1, notOccupied, opponent) |
                slide(bishop, -1, -1, notOccupied, opponent);
    }


    //--------------------------------------------------------------------
    public long moves(long bishop,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return slide(bishop,  1,  1, notOccupied, opponent) |
               slide(bishop,  1, -1, notOccupied, opponent) |
               slide(bishop, -1,  1, notOccupied, opponent) |
               slide(bishop, -1, -1, notOccupied, opponent);
    }
}
