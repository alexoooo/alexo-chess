package ao.chess.v2.piece;

import ao.chess.v2.move.*;

/**
 * Date: Feb 6, 2009
 * Time: 5:32:19 PM
 */
public enum Piece implements BoardPiece
{
    //--------------------------------------------------------------------
    WHITE_PAWN  (Colour.WHITE, Figure.PAWN,   Pawns.WHITE_MOVES),
    WHITE_KNIGHT(Colour.WHITE, Figure.KNIGHT, Knight.MOVES),
    WHITE_BISHOP(Colour.WHITE, Figure.BISHOP, Bishop.MOVES),
    WHITE_ROOK  (Colour.WHITE, Figure.ROOK,   Rook.MOVES),
    WHITE_QUEEN (Colour.WHITE, Figure.QUEEN,  Queen.MOVES),
    WHITE_KING  (Colour.WHITE, Figure.KING,   King.MOVES),

    BLACK_PAWN  (Colour.BLACK, Figure.PAWN,   Pawns.BLACK_MOVES),
    BLACK_KNIGHT(Colour.BLACK, Figure.KNIGHT, Knight.MOVES),
    BLACK_BISHOP(Colour.BLACK, Figure.BISHOP, Bishop.MOVES),
    BLACK_ROOK  (Colour.BLACK, Figure.ROOK,   Rook.MOVES),
    BLACK_QUEEN (Colour.BLACK, Figure.QUEEN,  Queen.MOVES),
    BLACK_KING  (Colour.BLACK, Figure.KING,   King.MOVES);


    //--------------------------------------------------------------------
    public  static final Piece[]   VALUES           = values();
    private static final Piece[][] BY_COLOUR_FIGURE;
    static
    {
        BY_COLOUR_FIGURE = new Piece[ Colour.VALUES.length ]
                                    [ Figure.VALUES.length ];
        for (Colour c : Colour.VALUES)
        {
            for (Figure f : Figure.VALUES)
            {
                for (Piece p : VALUES)
                {
                    if (p.colour() == c && p.figure() == f)
                    {
                        BY_COLOUR_FIGURE[ c.ordinal() ]
                                        [ f.ordinal() ] = p;
                        break;
                    }
                }
            }
        }
    }

    public static Piece valueOf(Colour colour, Figure figure)
    {
        return BY_COLOUR_FIGURE[ colour.ordinal() ]
                               [ figure.ordinal() ];
    }

    public static Piece valueOf(char fen)
    {
        switch (fen) {
            case 'P': return valueOf(Colour.WHITE, Figure.PAWN);
            case 'p': return valueOf(Colour.BLACK, Figure.PAWN);

            case 'R': return valueOf(Colour.WHITE, Figure.ROOK);
            case 'r': return valueOf(Colour.BLACK, Figure.ROOK);

            case 'B': return valueOf(Colour.WHITE, Figure.BISHOP);
            case 'b': return valueOf(Colour.BLACK, Figure.BISHOP);

            case 'N': return valueOf(Colour.WHITE, Figure.KNIGHT);
            case 'n': return valueOf(Colour.BLACK, Figure.KNIGHT);

            case 'Q': return valueOf(Colour.WHITE, Figure.QUEEN);
            case 'q': return valueOf(Colour.BLACK, Figure.QUEEN);

            case 'K': return valueOf(Colour.WHITE, Figure.KING);
            case 'k': return valueOf(Colour.BLACK, Figure.KING);

            default: return null;
        }
    }


    //--------------------------------------------------------------------
    private final Colour     COLOUR;
    private final Figure     FIGURE;
    private final BoardPiece MOVES;

    private Piece(Colour     colour,
                  Figure     figure,
                  BoardPiece moves)
    {
        COLOUR = colour;
        FIGURE = figure;
        MOVES  = moves;
    }


    //--------------------------------------------------------------------
    public long moves(long pieceLocation,
                      long occupied,
                      long notOccupied,
                      long proponent,
                      long notProponent,
                      long opponent) {
        return MOVES.moves(
                    pieceLocation, occupied, notOccupied,
                    proponent, notProponent, opponent);
    }

    public Colour colour()
    {
        return COLOUR;
    }
    public boolean isWhite()
    {
        return COLOUR == Colour.WHITE;
    }

    public Figure figure()
    {
        return FIGURE;
    }


    //--------------------------------------------------------------------
    @Override public String toString()
    {
        return COLOUR == Colour.WHITE
               ? FIGURE.toString().toUpperCase()
               : FIGURE.toString().toLowerCase();
    }


}
