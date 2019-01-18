package ao.chess.v2.piece;

/**
 * User: alexo
 * Date: Feb 23, 2009
 * Time: 1:57:21 PM
 */
public enum FigurePair
{
    //--------------------------------------------------------------------
    P_P(Figure.PAWN, Figure.PAWN),
    P_N(Figure.PAWN, Figure.KNIGHT),
    P_B(Figure.PAWN, Figure.BISHOP),
    P_R(Figure.PAWN, Figure.ROOK),
    P_Q(Figure.PAWN, Figure.QUEEN),

    N_P(Figure.KNIGHT, Figure.PAWN),
    N_N(Figure.KNIGHT, Figure.KNIGHT),
    N_B(Figure.KNIGHT, Figure.BISHOP),
    N_R(Figure.KNIGHT, Figure.ROOK),
    N_Q(Figure.KNIGHT, Figure.QUEEN),

    B_P(Figure.BISHOP, Figure.PAWN),
    B_N(Figure.BISHOP, Figure.KNIGHT),
    B_B(Figure.BISHOP, Figure.BISHOP),
    B_R(Figure.BISHOP, Figure.ROOK),
    B_Q(Figure.BISHOP, Figure.QUEEN),

    R_P(Figure.ROOK, Figure.PAWN),
    R_N(Figure.ROOK, Figure.KNIGHT),
    R_B(Figure.ROOK, Figure.BISHOP),
    R_R(Figure.ROOK, Figure.ROOK),
    R_Q(Figure.ROOK, Figure.QUEEN),

    Q_P(Figure.QUEEN, Figure.PAWN),
    Q_N(Figure.QUEEN, Figure.KNIGHT),
    Q_B(Figure.QUEEN, Figure.BISHOP),
    Q_R(Figure.QUEEN, Figure.ROOK),
    Q_Q(Figure.QUEEN, Figure.QUEEN),

    K_P(Figure.KING, Figure.PAWN),
    K_N(Figure.KING, Figure.KNIGHT),
    K_B(Figure.KING, Figure.BISHOP),
    K_R(Figure.KING, Figure.ROOK),
    K_Q(Figure.KING, Figure.QUEEN);

    public static final FigurePair[] VALUES = values();


    //--------------------------------------------------------------------
    private static final FigurePair[][] BY_ATTACKER_CAPTURED;

    static {
        BY_ATTACKER_CAPTURED = new FigurePair[ 6 ][ 5 ];
        for (Figure attacker : Figure.VALUES) {
            for (Figure captured : Figure.VALUES) {
                if (captured == Figure.KING) continue;

                for (FigurePair c : VALUES) {
                    if (c.ATTACKER == attacker &&
                            c.CAPTURED == captured) {
                        BY_ATTACKER_CAPTURED
                                [ attacker.ordinal() ]
                                [ captured.ordinal() ] = c;
                        break;
                    }
                }
            }
        }
    }

    public static FigurePair valueOf(Figure attacker, Figure captured)
    {
        return BY_ATTACKER_CAPTURED[ attacker.ordinal() ]
                                   [ captured.ordinal() ];
    }
    public static FigurePair valueOf(Figure attacker)
    {
        return BY_ATTACKER_CAPTURED[ attacker.ordinal() ][ 0 ];
    }


    //--------------------------------------------------------------------
    private final Figure ATTACKER;
    private final Figure CAPTURED;


    //--------------------------------------------------------------------
    private FigurePair(Figure attacker, Figure captured)
    {
        ATTACKER = attacker;
        CAPTURED = captured;
    }


    //--------------------------------------------------------------------
    public Figure main()
    {
        return ATTACKER;
    }

    public Figure captured()
    {
        return CAPTURED;
    }


    //--------------------------------------------------------------------
    public FigurePair withCaptured(Figure captured)
    {
        return valueOf(ATTACKER, captured);
    }
}
