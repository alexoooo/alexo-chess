package ao.chess.v2.state;

import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Figure;

/**
 * Date: Feb 6, 2009
 * Time: 6:21:41 PM
 */
public class Move
{
    //--------------------------------------------------------------------
    private Move() {}


    //--------------------------------------------------------------------
    public static void main(String[] args) {
//        int move = mobility(Piece.WHITE_QUEEN,
//                            0, 1, (byte) 0xF, (byte) 10);
//
//        System.out.println("colour: " + colour(move));
//        System.out.println("type: " + moveType(move));
//        System.out.println("from: " + fromSquareIndex(move));
//        System.out.println("to: " + toSquareIndex(move));
//        System.out.println("main: " + figures(move).main() );
//        System.out.println("cap: " + figures(move).captured() );
//        System.out.println("en pass: " + enPassantRank(move) );
//        System.out.println("promo: " + promotion(move) );
//        System.out.println("castle: " + castleType(move) );
//        System.out.println("avail castle: " + availCastles(move) );
//        System.out.println("reverse: " + reversibleMoves(move) );

//        System.out.println(Integer.toBinaryString(
//                colourMask( Colour.WHITE )));
//
//        System.out.println(Integer.toBinaryString(
//                typeBits( MoveType.MOBILITY )));
//
//        System.out.println(Integer.toBinaryString(
//                figureMask( Figure.QUEEN )));
//
//        System.out.println(Integer.toBinaryString(
//                fromMask( 1 )));
//
//        System.out.println(Integer.toBinaryString(
//                toMask( 2 )));
//
//        System.out.println(Integer.toBinaryString(
//                availCastleMask( (byte) 0xF )));
//
//        System.out.println(Integer.toBinaryString(
//                reversibleMask( (byte) 10 )));
    }

    //--------------------------------------------------------------------
    public static final int MAX_PER_PLY = 128;


    //--------------------------------------------------------------------
    /*
     * Layout of move int is:
     *  [
     *    type {mobility, capture, en passant, castle}
     *      if mobility or capture lead to promotion, then
     *          the promoted to figure will be > 0
     *      lg 4  = 2 |
     *
     *    from index
     *      lg 64 = 6 |
     *
     *    to index
     *      lg 64 = 6 |
     *
     *    Figure
     *      lg 6 = 3  |
     *
     *    Capture {nil, pawn, knight, bishop, rook, queen}
     *          nil is auto inserted by offsetting the given figure
     *          value, used to indicate lack of capture figure info
     *      lg 6 = 3  |
     *
     *    promotion to figure {nil, knight, bishop, rook, queen}
     *          extra nil is used to indicate that this is not a promotion
     *      lg 5 = 3  |
     *
     *    castle type (if castle)
     *      lg 2 = 1  |
     *
     *    extra
     *      8 bits
     *          (prev en passants) lg 9 = 4
     *          (prev castles) 4
     *          (prev castle (path)) lg 3 = 2
     *          (prev reversible moves) 7
     *  ]
     */

    private static final int TYPE_SHIFT = 0;
    private static final int TYPE_SIZE  = 2;
    private static final int TYPE_MASK  = mask(TYPE_SIZE) << TYPE_SHIFT;

    private static final int FROM_SHIFT = TYPE_SHIFT + TYPE_SIZE;
    private static final int FROM_SIZE  = 6;
    private static final int FROM_MASK  = mask(FROM_SIZE) << FROM_SHIFT;

    private static final int TO_SHIFT = FROM_SHIFT + FROM_SIZE;
    private static final int TO_SIZE  = 6;
    private static final int TO_MASK  = mask(TO_SIZE) << TO_SHIFT;

    private static final int FIGURE_SHIFT = TO_SHIFT + TO_SIZE;
    private static final int FIGURE_SIZE  = 3;
    private static final int FIGURE_MASK  =
            mask(FIGURE_SIZE) << FIGURE_SHIFT;

    private static final int CAPTURE_SHIFT = FIGURE_SHIFT + FIGURE_SIZE;
    private static final int CAPTURE_SIZE  = 3;
    private static final int CAPTURE_MASK  =
            mask(CAPTURE_SIZE) << CAPTURE_SHIFT;
    //private static final int FIGURE_MASK_NOT = ~FIGURE_MASK;

//    private static final int EN_PASS_SHIFT = CAPTURE_SHIFT + CAPTURE_SIZE;
//    private static final int EN_PASS_SIZE  = 3;
//    private static final int EN_PASS_MASK  =
//            mask(EN_PASS_SIZE) << EN_PASS_SHIFT;

    //private static final int PROMO_SHIFT = EN_PASS_SHIFT + EN_PASS_SIZE;
    private static final int PROMO_SHIFT = CAPTURE_SHIFT + CAPTURE_SIZE;
    private static final int PROMO_SIZE  = 3;
    private static final int PROMO_MASK  =
            mask(PROMO_SIZE) << PROMO_SHIFT;
    private static final int PROMO_MASK_NOT = ~PROMO_MASK;

    private static final int CASTLE_SHIFT = PROMO_SHIFT + PROMO_SIZE;
    private static final int CASTLE_SIZE  = 1;
    private static final int CASTLE_MASK  =
            mask(CASTLE_SIZE) << CASTLE_SHIFT;

//    private static final int AVAIL_CASTLE_SHIFT =
//            CASTLE_SHIFT + CASTLE_SIZE;
//    private static final int AVAIL_CASTLE_SIZE  = 4;
//    private static final int AVAIL_CASTLE_MASK  =
//            mask(AVAIL_CASTLE_SIZE) << AVAIL_CASTLE_SHIFT;

//    private static final int REVERSE_SHIFT = CASTLE_SHIFT;
//    private static final int REVERSE_SIZE  = 7;
//    private static final int REVERSE_MASK  =
//            mask(REVERSE_SIZE) << REVERSE_SHIFT;


    //--------------------------------------------------------------------
    private static int mask(int size) {
        int mask = 0;
        for (int i = 0; i < size; i++) mask |= 1 << i;
        return mask;
    }


    //--------------------------------------------------------------------
//    private static int colourMask(Colour colour) {
//        return colour.ordinal();
//    }
    private static int typeBits(MoveType moveType) {
        return moveType.ordinal() << TYPE_SHIFT;
    }
    private static int fromBits(int squareIndex) {
        return squareIndex << FROM_SHIFT;
    }
    private static int toBits(int squareIndex) {
        return squareIndex << TO_SHIFT;
    }
    private static int figureBits(Figure figure) {

        return figure.ordinal() << FIGURE_SHIFT;
    }
//    private static int capturedBits(Figure captured) {
//        return captured.ordinal() << CAPTURE_SHIFT;
//    }
//    private static int enPassantBits(int enPassantFile) {
//        return enPassantFile << EN_PASS_SHIFT;
//    }
    private static int promotionBits(int promotingTo) {
        return promotingTo << PROMO_SHIFT;
    }
//    private static int promotionBits(Figure promotingTo) {
//        return promotionBits(promotingTo.ordinal());
//    }
    private static int castleBits(CastleType castle) {
        return castle.ordinal() << CASTLE_SHIFT;
    }
//    private static int availCastleMask(byte availeableCastles) {
//        return availeableCastles << AVAIL_CASTLE_SHIFT;
//    }
//    private static int reversibleMask(byte reversibleMoveCount) {
//        return reversibleMoveCount << REVERSE_SHIFT;
//    }


    //--------------------------------------------------------------------
//    private static Colour colour(int move) {
//        int index = (move & COLOUR_MASK) >>> COLOUR_SHIFT;
//        return Colour.VALUES[ index ];
//    }
    public static MoveType moveType(int move) {
        int index = (move & TYPE_MASK) >>> TYPE_SHIFT;
        return MoveType.VALUES[ index ];
    }
    public static int fromSquareIndex(int move) {
        return (move & FROM_MASK) >>> FROM_SHIFT;
    }
    public static int toSquareIndex(int move) {
        return (move & TO_MASK) >>> TO_SHIFT;
    }
    public static int figure(int move) {
        return (move & FIGURE_MASK) >>> FIGURE_SHIFT;
//        int index = (move & FIGURE_MASK) >>> FIGURE_SHIFT;
//        return Figure.VALUES[ index ];
    }
    public static int captured(int move) {
        return ((move & CAPTURE_MASK) >>> CAPTURE_SHIFT) - 1;
//        int index = (move & CAPTURE_MASK) >>> CAPTURE_SHIFT;
//        return Figure.VALUES[ index ];
    }
//    private static int enPassantRank(int move) {
//        return (move & EN_PASS_MASK) >>> EN_PASS_SHIFT;
//    }
    public static int enPassantCapture(int move) {
        return Location.squareIndex(
                Location.rankIndex(fromSquareIndex(move)),
                Location.fileIndex(  toSquareIndex(move)));
    }

    public static int promotion(int move) {
        return (move & PROMO_MASK) >>> PROMO_SHIFT;
//        int index = (move & PROMO_MASK) >>> PROMO_SHIFT;
//        return Figure.VALUES[ index ];
    }
    public static CastleType castleType(int move) {
        int index = (move & CASTLE_MASK) >>> CASTLE_SHIFT;
        return CastleType.VALUES[ index ];
    }
//    private static int availCastles(int move) {
//        return (move & AVAIL_CASTLE_MASK) >>> AVAIL_CASTLE_SHIFT;
//    }
//    private static byte reversibleMoves(int move) {
//        return (byte) ((move & REVERSE_MASK) >>> REVERSE_SHIFT);
//    }


    //--------------------------------------------------------------------
    private static int addCaptured(
            int toMove, int captured) {
        return toMove | ((captured + 1) << CAPTURE_SHIFT);
    }

    public static int setPromotion(
            int toMove, int toFigure) {
        return (toMove & PROMO_MASK_NOT) | promotionBits(toFigure);
    }
//    public static int setPromotion(
//            int toMove, Figure toFigure) {
//        return setPromotion(toMove, toFigure.ordinal());
//    }

    public static boolean isCapture(int move) {
        return moveType(move) == MoveType.CAPTURE;
    }
    public static boolean isEnPassant(int move) {
        return moveType(move) == MoveType.EN_PASSANT;
    }
    public static boolean isCastle(int move) {
        return moveType(move) == MoveType.CASTLE;
    }
    public static boolean isPromotion(int move) {
        return promotion(move) != 0;
    }
    public static boolean isMobility(int move) {
        return moveType(move) == MoveType.MOBILITY;
    }


    //--------------------------------------------------------------------
    public static int mobility(
            Figure moving,
            int    fromSquareIndex,
            int      toSquareIndex)
    {
        return   typeBits( MoveType.MOBILITY ) |
               figureBits( moving            ) |
                 fromBits( fromSquareIndex   ) |
                   toBits( toSquareIndex     );
    }

    public static int capture(
            Figure attacker,
            int    fromSquareIndex,
            int      toSquareIndex)
    {
        return   typeBits( MoveType.CAPTURE  ) |
               figureBits( attacker          ) |
                 fromBits( fromSquareIndex   ) |
                   toBits( toSquareIndex     );
    }

    public static int enPassant(
            int fromSquareIndex,
            int   toSquareIndex)
    {
        return typeBits( MoveType.EN_PASSANT ) |
               fromBits( fromSquareIndex     ) |
                 toBits( toSquareIndex       );
    }

    public static int castle(
            int        fromSquareIndex,
            int          toSquareIndex,
            CastleType type)
    {
        return   fromBits( fromSquareIndex ) |
                   toBits( toSquareIndex   ) |
                 typeBits( MoveType.CASTLE ) |
               castleBits( type            );
    }


    //--------------------------------------------------------------------
    /**
     * @param move Move to apply
     * @param toState State (board) on which to apply the move
     * @return Move that can be undone
     */
    public static int apply(int move, State toState)
    {
//        Colour colour = colour(move);
        switch (moveType(move))
        {
            case MOBILITY: {
                int figure = figure(move);
                int from   = fromSquareIndex(move);
                int to     = toSquareIndex(move);

                int promoteTo;
                if (figure == 0 &&
                        (promoteTo = promotion(move)) != 0) {
                    toState.pushPromote(from, to, promoteTo);
                } else {
                    toState.mobalize(figure, from, to);
                }
                return move;
            }

            case CAPTURE: {
                int figure = figure(move);
                int from   = fromSquareIndex(move);
                int to     = toSquareIndex(move);

                int promoteTo = promotion(move);
                int captured  = captured(move);
                if (captured == -1) {
                    captured =
                        (promoteTo != 0)
                       ? toState.capturePromote(
                            from, to, promoteTo)
                       : toState.capture(figure, from, to);
                    return addCaptured(move, captured);
                } else {
                    if (promoteTo != 0) {
                        toState.capturePromote(
                            from, to, promoteTo, captured);
                    } else {
                        toState.capture(
                            figure, from, to, captured);
                    }
                    return move;
                }
            }

            case EN_PASSANT: {
                int from =  fromSquareIndex(move);
                int to   =    toSquareIndex(move);
                int cap  = enPassantCapture(move);

                toState.enPassantCapture(from, to, cap);
                return move;
            }

            case CASTLE: {
                toState.castle(castleType(move));
                return move;
            }

            default:
                throw new IllegalStateException();
        }
    }


    //--------------------------------------------------------------------
    public static void unApply(int move, State toState)
    {
//        Colour colour = colour(move);
        switch (moveType(move))
        {
            case MOBILITY: {
                int figure = figure(move);
                int from   = fromSquareIndex(move);
                int to     = toSquareIndex(move);

                int promoteTo;
                if (figure == 0 &&
                        (promoteTo = promotion(move)) != 0) {
                    toState.unPushPromote(from, to, promoteTo);
                } else {
                    toState.unMobalize(figure, from, to);
                }
                return;
            }

            case CAPTURE: {
                int figure   = figure(move);
                int captured = captured(move);
                int from     = fromSquareIndex(move);
                int to       = toSquareIndex(move);

                int promoteTo;
                if (figure == 0 &&
                        (promoteTo = promotion(move)) != 0) {
                    toState.unCapturePromote(
                            from, to, promoteTo, captured);
                } else {
                    toState.unCapture(figure, captured, from, to);
                }
                return;
            }

            case EN_PASSANT: {
                int from =  fromSquareIndex(move);
                int to   =    toSquareIndex(move);
                int cap  = enPassantCapture(move);

                toState.unEnPassantCapture(from, to, cap);
                return;
            }

            case CASTLE: {
                toState.unCastle(castleType(move));
                return;
            }

            default:
                throw new IllegalStateException();
        }
    }


    //--------------------------------------------------------------------
    public static String toString(int move)
    {
        switch (moveType(move))
        {
            case MOBILITY: {
                int figure = figure(move);
                int from   = fromSquareIndex(move);
                int to     = toSquareIndex(move);
                int promo  = promotion(move);

                return "move with " + Figure.VALUES[figure] +
                         " from " + Location.toString(from) + " to " +
                                    Location.toString(to) +
                         (promo != 0
                          ? " promo " + Figure.VALUES[promo] : "");
            }

            case CAPTURE: {
                int figure = figure(move);
                int from   = fromSquareIndex(move);
                int to     = toSquareIndex(move);
                int promo  = promotion(move);

                return "capture with " + Figure.VALUES[figure] +
                         " from " + Location.toString(from) + " to " +
                                    Location.toString(to)   +
                         (promo != 0
                          ? " promo " + Figure.VALUES[promo] : "");
            }

            case EN_PASSANT: {
                int from =  fromSquareIndex(move);
                int to   =    toSquareIndex(move);
                int cap  = enPassantCapture(move);

                return "en passant capture from " +
                        Location.toString(from) + " to " +
                        Location.toString(to  ) + " capturing " +
                        Location.toString(cap );
            }

            case CASTLE: {
                return castleType(move) + " castle";
            }
        }

        return "Unknown";
    }


    //--------------------------------------------------------------------
    public static String toInputNotation(int move)
    {
        return toInputNotationSource(move) + toInputNotationDestination(move);
    }


    public static String toInputNotationSource(int move)
    {
        int from = fromSquareIndex(move);
        return String.valueOf(State.FILES.charAt(Location.fileIndex(from))) +
                (Location.rankIndex(from) + 1);
    }


    public static String toInputNotationDestination(int move)
    {
        int to = toSquareIndex(move);
        int promoteTo = promotion(move);

        String promoteToInput =
                (promoteTo == 0)
                ? "" : Figure.VALUES[ promoteTo ]
                .toString().substring(0, 1).toLowerCase();

        return String.valueOf(State.FILES.charAt(Location.fileIndex(to))) +
                (Location.rankIndex(to) + 1) +
                promoteToInput;
    }


    public static int findMove(State from, State to)
    {
        int[] legalMoves = from.legalMoves();

        for (int legalMove : legalMoves) {
            int move = Move.apply(legalMove, from);
            if (from.equals(to)) {
                Move.unApply(move, from);
                return move;
            }
            Move.unApply(move, from);
        }

        return -1;
    }
}
