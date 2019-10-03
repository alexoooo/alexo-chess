package ao.chess.v2.piece;

/**
 * Date: Feb 6, 2009
 * Time: 5:31:31 PM
 */
public enum Figure
{
    //--------------------------------------------------------------------
    PAWN  ("P"),
    KNIGHT("N"),
    BISHOP("B"),
    ROOK  ("R"),
    QUEEN ("Q"),
    KING  ("K");

    public static Figure[] VALUES = values();


    //--------------------------------------------------------------------
    private final String SYMBOL;


    //--------------------------------------------------------------------
    Figure(String symbol)
    {
        SYMBOL = symbol;
    }


    //--------------------------------------------------------------------
    @Override public String toString()
    {
        return SYMBOL;
    }
}
