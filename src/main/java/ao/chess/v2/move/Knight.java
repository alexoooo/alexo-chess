package ao.chess.v2.move;

import ao.chess.v2.data.BitBoard;
import ao.chess.v2.data.BitLoc;

/**
 * Date: Feb 6, 2009
 * Time: 3:17:06 AM
 */
public class Knight implements BoardPiece
{
    //----------------------------------------------------------
    private Knight() {}

    public static final BoardPiece MOVES = new Knight();


    //--------------------------------------------------------------------
    private static final long[] CACHE;

    static
    {
        CACHE = new long[64];

        for (int loc = 0; loc < 64; loc++) {
            CACHE[ loc ] = attacks(
                    BitLoc.locationToBitBoard(loc));
        }
    }

    public static long attacks(int loc) {
        return CACHE[ loc ];
    }


    //----------------------------------------------------------
    public static long attacks(long knight) {
        return BitBoard.offset(knight,  2,  1) |
               BitBoard.offset(knight,  2, -1) |
               BitBoard.offset(knight, -2,  1) |
               BitBoard.offset(knight, -2, -1) |
               BitBoard.offset(knight,  1,  2) |
               BitBoard.offset(knight,  1, -2) |
               BitBoard.offset(knight, -1,  2) |
               BitBoard.offset(knight, -1, -2);
    }


    //----------------------------------------------------------
    public long moves(long knight,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return attacks(BitLoc.bitBoardToLocation(knight))
                & notProponent;
    }

//    public long moves(int pieceLocation) {
//        return CACHE[ pieceLocation ];
//    }
}
