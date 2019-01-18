package ao.chess.v2.data;

/**
 * Date: Feb 6, 2009
 * Time: 4:50:00 AM
 */
public class BitLoc
{
    //--------------------------------------------------------------------
    private BitLoc() {}


    //--------------------------------------------------------------------
    private static final int[] deBruijnIndex64 = {
           63,  0, 58,  1, 59, 47, 53,  2,
           60, 39, 48, 27, 54, 33, 42,  3,
           61, 51, 37, 40, 49, 18, 28, 20,
           55, 30, 34, 11, 43, 14, 22,  4,
           62, 57, 46, 52, 38, 26, 32, 41,
           50, 36, 17, 19, 29, 10, 13, 21,
           56, 45, 25, 31, 35, 16,  9, 12,
           44, 24, 15,  8, 23,  7,  6,  5
        };

    private static final long debruijn64 = 0x07EDD5E59A4E28C2L;

    /**
     * bitScanForward
     * @author Charles E. Leiserson
     *         Harald Prokop
     *         Keith H. Randall
     * "Using de Bruijn Sequences to Index a 1 in a Computer Word"
     * @param bb bitboard to scan
     * @precondition bb != 0
     * @return index (0..63) of least significant one bit
     */
    public static int bitBoardToLocation(long bb) {
       assert (bb != 0);
       return deBruijnIndex64[(int)(
               ((bb & -bb) * debruijn64) >>> 58
              )];
    }


    //--------------------------------------------------------------------
    private static final long[] loc2bb;
    static
    {
        loc2bb = new long[64];
        for (int off = 0; off < loc2bb.length; off++) {
            long bb  = 1L << off;
            int  loc = bitBoardToLocation(bb);
            loc2bb[ loc ] = bb;
        }
    }

    public static long locationToBitBoard(int loc) {
        return loc2bb[ loc ];
    }

    public static long locationToBitBoard(
            int rankIndex, int fileIndex) {
        return locationToBitBoard(Location.squareIndex(
                rankIndex, fileIndex));
    }
}
