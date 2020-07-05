package ao.chess.v2.state;

import ao.chess.v2.piece.Colour;


/**
 * User: alexo
 * Date: Feb 22, 2009
 * Time: 12:34:57 PM
 */
public enum Outcome
{
    //--------------------------------------------------------------------
    DRAW,
    WHITE_WINS,
    BLACK_WINS;

    public static final Outcome[] values = values();


    //--------------------------------------------------------------------
    public double valueFor(Colour nextToAct) {
        if (this == DRAW) {
            return 0.5;
        }
        else {
            if (this == WHITE_WINS) {
                if (nextToAct == Colour.WHITE) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
            else /*if (this == BLACK_WINS)*/ {
                if (nextToAct == Colour.BLACK) {
                    return 1;
                }
                else {
                    return 0;
                }
            }
        }
    }


    //--------------------------------------------------------------------
    public static Outcome wins(Colour c)
    {
        return c == Colour.WHITE
               ? WHITE_WINS : BLACK_WINS;
    }

    public static Outcome loses(Colour c)
    {
        return wins(c.invert());
    }

    public Colour winner() {
        return (this == WHITE_WINS)
               ? Colour.WHITE
               : (this == BLACK_WINS)
                 ? Colour.BLACK
                 : null;
    }

    public Colour loser() {
        return (this == WHITE_WINS)
               ? Colour.BLACK
               : (this == BLACK_WINS)
                 ? Colour.WHITE
                 : null;
    }
}
