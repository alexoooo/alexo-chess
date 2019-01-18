package ao.chess.v2.move;

import ao.chess.v2.data.BitBoard;
import ao.chess.v2.data.BitLoc;

/**
 * Date: Feb 6, 2009
 * Time: 3:30:53 AM
 */
public class King implements BoardPiece
{
    //--------------------------------------------------------------------
    private King() {}

    public static final BoardPiece MOVES = new King();


    //--------------------------------------------------------------------
    private static final long[] CACHE;

    static
    {
        CACHE = new long[64];

        for (int loc = 0; loc < 64; loc++) {
            CACHE[ loc ] = attacks(
                    BitLoc.locationToBitBoard(loc));
//            System.out.println(
//                    loc + "\n" +
//                    BitBoard.toString(CACHE[loc]) + "\n\n");
        }
    }


    //--------------------------------------------------------------------
    public static long attacks(long king) {
        return BitBoard.offset(king,  1,  1) |
               BitBoard.offset(king,  1,  0) |
               BitBoard.offset(king,  1, -1) |
               BitBoard.offset(king,  0,  1) |
               BitBoard.offset(king,  0, -1) |
               BitBoard.offset(king, -1,  1) |
               BitBoard.offset(king, -1,  0) |
               BitBoard.offset(king, -1, -1);
    }

    public long attacks(int pieceLocation) {
        return CACHE[ pieceLocation ];
    }


    //--------------------------------------------------------------------
    public long moves(long king,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent)
    {
        return attacks(BitLoc.bitBoardToLocation(king))
               //attacks(king)
                & notProponent;
    }
}
