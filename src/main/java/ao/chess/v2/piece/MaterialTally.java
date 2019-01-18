package ao.chess.v2.piece;

import java.util.Random;

/**
* User: aostrovsky
* Date: 15-Oct-2009
* Time: 5:12:43 PM
*/
public class MaterialTally
{
    //--------------------------------------------------------------------
    private static final int[][][] COLOUR_FIGURE_COUNT;

    static
    {
        COLOUR_FIGURE_COUNT = new int[ Colour.VALUES.length ]
                                     [ Figure.VALUES.length ]
                                     [ 10                   ];

        Random rand = new Random(666);
        for (int[][] colourCount : COLOUR_FIGURE_COUNT) {
            for (int[] count : colourCount) {
                //count[ 0 ] = 0; // happens automatically 
                for (int c = 1; c < count.length; c++) {
                    count[ c ] = rand.nextInt();
                }
            }
        }
    }


    //--------------------------------------------------------------------
    private int tally = 0;


    //--------------------------------------------------------------------
    public MaterialTally(boolean startWithKings) {
        if (startWithKings) {
            tally(Colour.WHITE, Figure.KING.ordinal(), 1);
            tally(Colour.BLACK, Figure.KING.ordinal(), 1);
        }
    }


    //--------------------------------------------------------------------
    public void clear() {
        tally = 0;
    }


    //--------------------------------------------------------------------
    public int tally(Colour colour, int figure, int count) {
        tally ^= COLOUR_FIGURE_COUNT
                   [ colour.ordinal() ][ figure ][ count ];
        return count;
    }


    //--------------------------------------------------------------------
    public static int tally(
            int tally, Colour colour, Figure figure, int count) {
        return tally ^ COLOUR_FIGURE_COUNT
                        [ colour.ordinal() ][ figure.ordinal() ][ count ];
    }

    public static int tally(
            int tally, Colour colour, int figure, int count) {
        return tally ^ COLOUR_FIGURE_COUNT
                         [ colour.ordinal() ][ figure ][ count ];
    }

    public static int tally(Piece... material) {
        int[] counts = new int[ Piece.VALUES.length ];
        for (Piece piece : material) {
            counts[ piece.ordinal() ]++;
        }

        int tally = 0;
        for (Piece piece : Piece.VALUES) {
            int count = counts[ piece.ordinal() ];
            if (count != 0) {
                tally = tally(tally,
                          piece.colour(), piece.figure(), count);
            }
        }
        return tally;
    }

    public static int tally(Iterable<Piece> material) {
        int[] counts = new int[ Piece.VALUES.length ];
        for (Piece piece : material) {
            counts[ piece.ordinal() ]++;
        }

        int tally = 0;
        for (Piece piece : Piece.VALUES) {
            int count = counts[ piece.ordinal() ];
            if (count != 0) {
                tally = tally(tally,
                          piece.colour(), piece.figure(), count);
            }
        }
        return tally;
    }


    //--------------------------------------------------------------------
    @Override public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MaterialTally that = (MaterialTally) o;
        return tally == that.tally;
    }

    @Override
    public int hashCode() {
        return tally;
    }
}
