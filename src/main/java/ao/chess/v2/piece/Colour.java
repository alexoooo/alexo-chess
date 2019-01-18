package ao.chess.v2.piece;

/**
 * Date: Feb 6, 2009
 * Time: 5:31:54 PM
 *
 * white = 0,
 * black = 1
 */
public enum Colour
{
    //--------------------------------------------------------------------
    WHITE, BLACK;


    //--------------------------------------------------------------------
    public static final Colour[] VALUES = values();


    //--------------------------------------------------------------------
    public Colour invert()
    {
        return this == WHITE
               ? BLACK : WHITE;
    }
}
