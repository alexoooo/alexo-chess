package ao.chess.v2.state;

import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Piece;

import java.util.Random;

/**
 * User: aostrovsky
 * Date: 7-Oct-2009
 * Time: 7:27:25 PM
 *
 */
public class Zobrist
{
    //--------------------------------------------------------------------
    private Zobrist() {}


    //--------------------------------------------------------------------
    private static final long[][] PIECES =
            new long[Piece.VALUES.length][Location.COUNT];

    private static final long[] EN_PASSANTS =
            new long[Location.FILES];

    private static final long[][] CASTLES =
            new long[Colour.VALUES.length][CastleType.VALUES.length];

//    private static final long WHITE_NEXT;

    private static final long[] REV_MOVES =
            new long[Byte.MAX_VALUE];


    private static final long[][] PIECES_ALT = new long[Piece.VALUES.length][Location.COUNT];
    private static final long[] EN_PASSANTS_ALT = new long[Location.FILES];
    private static final long[][] CASTLES_ALT = new long[Colour.VALUES.length][CastleType.VALUES.length];
    private static final long[] REV_MOVES_ALT = new long[Byte.MAX_VALUE];


    static {
        Random rand = new Random(420);

        // legacy
//        WHITE_NEXT = rand.nextLong();
        rand.nextLong();

        for (Piece piece : Piece.VALUES) {
            populateRandomly(
                    PIECES[ piece.ordinal() ], rand);
        }

        populateRandomly(EN_PASSANTS, rand);

        for (Colour colour : Colour.VALUES) {
            populateRandomly(
                    CASTLES[ colour.ordinal() ], rand);
        }

        populateRandomly(REV_MOVES, rand);


        Random randAlt = new Random(666);
        for (Piece piece : Piece.VALUES) {
            populateRandomly(PIECES_ALT[piece.ordinal()], randAlt);
        }
        populateRandomly(EN_PASSANTS_ALT, randAlt);
        for (Colour colour : Colour.VALUES) {
            populateRandomly(CASTLES_ALT[colour.ordinal()], randAlt);
        }
        populateRandomly(REV_MOVES_ALT, randAlt);
    }


    private static void populateRandomly(
            long[] values, Random random) {
        for (int i = 0; i < values.length; i++) {
            values[ i ] = random.nextLong();
        }
    }


    //--------------------------------------------------------------------
    public static long togglePiece(
            long zobrist, Piece piece, int locationIndex) {
        return zobrist ^ PIECES[ piece.ordinal() ]
                               [ locationIndex   ];
    }


    public static long toggleEnPassant(
            long zobrist, byte enPassantFile) {
        return zobrist ^ EN_PASSANTS[enPassantFile];
    }


    public static long toggleCastle(
            long zobrist, Colour forSide, CastleType castle) {
        return zobrist ^ CASTLES[ forSide.ordinal() ]
                                [ castle.ordinal()  ];
    }


//    public static long toggleWhiteToAct(
//            long zobrist) {
//        return zobrist ^ WHITE_NEXT;
//    }


    public static long toggleReversibleMoves(
            long zobrist, byte reversibleMoves) {
        return zobrist ^ REV_MOVES[ reversibleMoves ];
    }


    public static long togglePieceAlt(long zobrist, Piece piece, int locationIndex) {
        return zobrist ^ PIECES_ALT[piece.ordinal()][locationIndex];
    }


    public static long toggleEnPassantAlt(long zobrist, byte enPassantFile) {
        return zobrist ^ EN_PASSANTS_ALT[enPassantFile];
    }


    public static long toggleCastleAlt(long zobrist, Colour forSide, CastleType castle) {
        return zobrist ^ CASTLES_ALT[forSide.ordinal()][castle.ordinal()];
    }

    public static long toggleReversibleMovesAlt(long zobrist, byte reversibleMoves) {
        return zobrist ^ REV_MOVES_ALT[reversibleMoves];
    }
}
