package ao.chess.v2.move;

import ao.chess.v2.data.BitBoard;
import ao.chess.v2.data.BitLoc;

/**
 * Date: Feb 6, 2009
 * Time: 2:33:17 AM
 *
 * See http://chessprogramming.wikispaces.com/Pawn+Attacks
 */
public class Pawns
{
    //--------------------------------------------------------------------
    private Pawns() {}


    //--------------------------------------------------------------------
    private static final long WHITE_ATTACK[];
    private static final long BLACK_ATTACK[];

    private static final long WHITE_PUSH[];
    private static final long BLACK_PUSH[];

    static
    {
        WHITE_ATTACK = new long[64];
        BLACK_ATTACK = new long[64];
        WHITE_PUSH   = new long[64];
        BLACK_PUSH   = new long[64];

        for (int loc = 0; loc < 64; loc++) {
            WHITE_ATTACK[ loc ] = whiteAttacks(
                                BitLoc.locationToBitBoard(loc));
            BLACK_ATTACK[ loc ] = blackAttacks(
                                BitLoc.locationToBitBoard(loc));

            WHITE_PUSH  [ loc ] =
                    BitBoard.northOne(BitLoc.locationToBitBoard(loc));
            BLACK_PUSH  [ loc ] =
                    BitBoard.southOne(BitLoc.locationToBitBoard(loc));
        }
    }


    //--------------------------------------------------------------------
    public static long whiteAttacks(long wPawn) {
        return BitBoard.noEaOne(wPawn) |
               BitBoard.noWeOne(wPawn);
    }

    public static long blackAttacks(long bPawn) {
        return BitBoard.soEaOne(bPawn) |
               BitBoard.soWeOne(bPawn);
    }


    //--------------------------------------------------------------------
    public static final BoardPiece WHITE_MOVES = new BoardPiece() {
        public long moves(
                long whitePawn,
                long occupied,
                long notOccupied,
                long proponent,
                long notProponent,
                long opponent
        ) {
            int loc = BitLoc.bitBoardToLocation(whitePawn);

            long mobility;
            mobility = WHITE_PUSH[loc] & notOccupied;
            if ((whitePawn & BitBoard.RANK_2) != 0 &&
                    mobility != 0) {
                mobility |= WHITE_PUSH[loc + 8] & notOccupied;
            }

            return mobility |
                   WHITE_ATTACK[loc] & opponent;
        }
    };


    //--------------------------------------------------------------------
    public static final BoardPiece BLACK_MOVES = new BoardPiece() {
        public long moves(
                long blackPawn,
                long occupied,
                long notOccupied,
                long proponent,
                long notProponent,
                long opponent
        ) {
            int loc = BitLoc.bitBoardToLocation(blackPawn);

            long mobility;
            mobility = BLACK_PUSH[loc] & notOccupied;
            if ((blackPawn & BitBoard.RANK_7) != 0 &&
                    mobility != 0) {
                mobility |= BLACK_PUSH[loc - 8] & notOccupied;
            }

            return mobility |
                   BLACK_ATTACK[loc] & opponent;
        }
    };
}
