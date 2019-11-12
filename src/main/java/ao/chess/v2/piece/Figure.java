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

    public static Figure ofSymbol(String symbol) {
        for (var figure : VALUES) {
            if (figure.symbol().equals(symbol)) {
                return figure;
            }
        }
        throw new IllegalArgumentException("Unknown: " + symbol);
    }


    //--------------------------------------------------------------------
    private final String SYMBOL;


    //--------------------------------------------------------------------
    Figure(String symbol)
    {
        SYMBOL = symbol;
    }


    //--------------------------------------------------------------------
    public String symbol() {
        return SYMBOL;
    }


    //--------------------------------------------------------------------
    @Override public String toString()
    {
        return SYMBOL;
    }
}
