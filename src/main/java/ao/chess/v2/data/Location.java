package ao.chess.v2.data;

import ao.chess.v2.state.State;

/**
 * Date: Feb 6, 2009
 * Time: 2:23:36 AM
 */
public class Location
{
    //--------------------------------------------------------------------
    private Location() {}


    //--------------------------------------------------------------------
    public static final int COUNT = 64;
    public static final int RANKS = 8;
    public static final int FILES = 8;


    //--------------------------------------------------------------------
    /**
     * @param rankIndex rank of square {0..7}
     * @param fileIndex file of square {0..7}
     * @return {0..63}
     */
    public static int squareIndex(int rankIndex,
                                  int fileIndex)
    {
        return 8 * rankIndex + fileIndex;
    }

    /**
     * @param squareIndex {0..63}
     * @return rank of square {0..7}
     */
    public static int rankIndex(int squareIndex)
    {
        return squareIndex / 8; // squareIndex >> 3;
    }

    /**
     * @param squareIndex {0..63}
     * @return file of square {0..7}
     */
    public static int fileIndex(int squareIndex)
    {
        return squareIndex % 8; // squareIndex & 7;
    }

    /**
     *   \f 0  1  2  3  4  5  6  7
        r_________________________
        7 | 7  6  5  4  3  2  1  0
        6 | 6  5  4  3  2  1  0 15
        5 | 5  4  3  2  1  0 15 14
        4 | 4  3  2  1  0 15 14 13
        3 | 3  2  1  0 15 14 13 12
        2 | 2  1  0 15 14 13 12 11
        1 | 1  0 15 14 13 12 11 10
        0 | 0 15 14 13 12 11 10  9

     * @param rankIndex {0..7}
     * @param fileIndex {0..7}
     * @return {0..7, 9..15} (or {0..15}?)
     */
    public static int diagonalIndex(int rankIndex,
                                    int fileIndex)
    {
        return (rankIndex - fileIndex) & 15;
    }


    /**
     *  \f  0  1  2  3  4  5  6  7
        r_________________________
        7 | 0 15 14 13 12 11 10  9
        6 | 1  0 15 14 13 12 11 10
        5 | 2  1  0 15 14 13 12 11
        4 | 3  2  1  0 15 14 13 12
        3 | 4  3  2  1  0 15 14 13
        2 | 5  4  3  2  1  0 15 14
        1 | 6  5  4  3  2  1  0 15
        0 | 7  6  5  4  3  2  1  0

     * @param rankIndex {0..7}
     * @param fileIndex {0..7}
     * @return {0..7, 9..15} (or {0..15}?)
     */
    public static int antiDiagonalIndex(int rankIndex,
                                        int fileIndex)
    {
        return (rankIndex + fileIndex) ^ 7; // xor
    }


    //--------------------------------------------------------------------
    public static String toString(int squareIndex)
    {
//        return "[" + (rankIndex(squareIndex) + 1) + ", " +
//                     (fileIndex(squareIndex) + 1) + "]";
        return String.valueOf(State.FILES.charAt(
                 fileIndex(squareIndex))) +
                (rankIndex(squareIndex) + 1);
    }
}
