package ao.chess.v2.data;

/**
 * Date: Feb 6, 2009
 * Time: 2:09:15 AM
 *
 * Chess bitboard.
 * Little-Endian Rank-File Mapping.
 *
 * See http://chessprogramming.wikispaces.com/
 *      Efficient+Generation+of+Sliding+Piece+Attacks
 */
public class BitBoard
{
    //--------------------------------------------------------------------
    private BitBoard() {}


    //--------------------------------------------------------------------
    public static final long NIL = 0;
    public static final long ALL = ~NIL;


    //--------------------------------------------------------------------
    public static final long RANK_1 = 0x00000000000000FF;
    public static final long RANK_2 = 0x000000000000FF00;
    public static final long RANK_3 = 0x0000000000FF0000;
    public static final long RANK_4 = 0x00000000FF000000;
    public static final long RANK_5 = 0x000000FF00000000L;
    public static final long RANK_6 = 0x0000FF0000000000L;
    public static final long RANK_7 = 0x00FF000000000000L;
    public static final long RANK_8 = 0xFF00000000000000L;

    public static final long NOT_RANK_1 = ~RANK_1;
    public static final long NOT_RANK_8 = ~RANK_8;


    //--------------------------------------------------------------------
    public static long offset(long b, int deltaRank, int deltaFile) {
        for (; deltaRank > 0; deltaRank--) b = northOne(b);
        for (; deltaRank < 0; deltaRank++) b = southOne(b);
        for (; deltaFile > 0; deltaFile--) b = eastOne(b);
        for (; deltaFile < 0; deltaFile++) b = westOne(b);
        return b;
    }

    private static final long notAFile = 0xfefefefefefefefeL;
    private static final long notHFile = 0x7f7f7f7f7f7f7f7fL;

    public static long northOne(long b) {return (b <<  8) & NOT_RANK_1;}
    public static long southOne(long b) {return (b >>> 8) & NOT_RANK_8;}
    public static long eastOne (long b) {return (b <<  1) & notAFile;}
    public static long westOne (long b) {return (b >>> 1) & notHFile;}

    public static long noEaOne (long b) {return (b <<  9) & notAFile;}
    public static long soEaOne (long b) {return (b >>> 7) & notAFile;}
    public static long soWeOne (long b) {return (b >>> 9) & notHFile;}
    public static long noWeOne (long b) {return (b <<  7) & notHFile;}


    //--------------------------------------------------------------------
    public static long lowestOneBit(long b) {
        return Long.lowestOneBit(b);
    }


    /**
     *        x          &      (x-1)        =  x_with_reset_LS1B
        . . . . . . . .     . . . . . . . .     . . . . . . . .
        . . 1 . 1 . . .     . . 1 . 1 . . .     . . 1 . 1 . . .
        . 1 . . . 1 . .     . 1 . . . 1 . .     . 1 . . . 1 . .
        . . . . . . . .     . . . . . . . .     . . . . . . . .
        . 1 . . . 1 . .  &  . 1 . . . 1 . .  =  . 1 . . . 1 . .
        . . 1 . 1 . . .     1 1 . . 1 . . .     . . . . 1 . . .
        . . . . . . . .     1 1 1 1 1 1 1 1     . . . . . . . .
        . . . . . . . .     1 1 1 1 1 1 1 1     . . . . . . . .
     * @param b bitboard
     * @return bitboard with lowest one bit cleared
     */
    public static long clearLowestOneBit(long b) {
        return b & (b - 1);
    }


    //--------------------------------------------------------------------
    public static String toString(long bb)
    {
        StringBuilder str = new StringBuilder();
        for (int rank = 7; rank >= 0; rank--)
        {
            if (rank != 7) str.append("\n");

            for (int file = 0; file < 8; file++)
            {
                str.append(
                    ((BitLoc.locationToBitBoard(rank, file) & bb) == 0)
                    ? "_" : "#");
            }
        }
        return str.toString();
    }


    //--------------------------------------------------------------------
    private static final long DARK  = 0xAA55AA55AA55AA55L;
    private static final long LIGHT = ~DARK;

    public static boolean isLight(long bitBoardSquare)
    {
        return (LIGHT & bitBoardSquare) != 0;
    }
    public static boolean isDark(long bitBoardSquare)
    {
        return (DARK & bitBoardSquare) != 0;
    }



    //--------------------------------------------------------------------
//    public static int[] locations(long bb)
//    {
//        IntList locs = new IntList();
//        while (bb != 0)
//        {
//            // determine bit index of least significant one bit
//            locs.add(BitLoc.bitBoardToLocation(bb));
//
//            // reset LS1B
//            bb &= bb - 1;
//        }
//        return locs.toArray();
//    }
}
