package ao.chess.v2.engine.endgame.common;

import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.CastleType;
import ao.chess.v2.state.State;
import ao.util.pass.Traverser;

import java.util.Arrays;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 13-Oct-2009
 * Time: 11:17:46 PM
 */
public class PositionTraverser
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        long before = System.currentTimeMillis();

        final long[] count = {0};

        new PositionTraverser().traverse(
                Arrays.asList(
                        Piece.WHITE_KING,
                        Piece.BLACK_KING,
                        Piece.WHITE_PAWN,
                        Piece.WHITE_PAWN),
                state -> {
                    String fen = state.toFen();
                    if (fen.startsWith("8/4k3/8/5K1P/7P/8/8/8 b ")) {
                        System.out.println();
                        System.out.println(fen);
                        System.out.println(state);
                    }

                    count[0]++;
                }
        );

        System.out.println("found: " + count[0]);
        System.out.println(
                "took " + (System.currentTimeMillis() - before));
    }


    //--------------------------------------------------------------------
    private final Piece[][] BOARD;


    //--------------------------------------------------------------------
    public PositionTraverser()
    {
        BOARD = new Piece[ Location.RANKS ][ Location.FILES ];
    }


    //--------------------------------------------------------------------
    public synchronized void traverse(
            List<Piece> pieces,
            Traverser<State> visitor)
    {
        place(pieces.get(0),
              pieces.subList(1, pieces.size()),
              visitor);
    }


    //--------------------------------------------------------------------
    private void place(
            Piece            first,
            List     <Piece> rest,
            Traverser<State> visitor)
    {
        for (int rank = 0; rank < Location.RANKS; rank++) {
            if (first.figure() == Figure.PAWN &&
                    (rank == 0 || rank == 7)) {
                continue;
            }

            for (int file = 0; file < Location.FILES; file++) {
                if (BOARD[rank][file] != null) {
                    continue;
                }

                BOARD[rank][file] = first;

                if (rest.isEmpty()) {
                    check(Colour.WHITE, visitor);
                    check(Colour.BLACK, visitor);
                }
                else {
                    place(rest.get(0),
                          rest.subList(1, rest.size()),
                          visitor);
                }

                BOARD[rank][file] = null;
            }
        }
    }


    //--------------------------------------------------------------------
    private void check(
            Colour           nextToAct,
            Traverser<State> visitor)
    {
        checkValid(new State(BOARD, nextToAct,
                (byte) 0, CastleType.Set.NONE, State.EP_NONE), visitor);

        byte enPassant = enPassant( nextToAct.invert() );
        if (enPassant == -1) {
            return;
        }

        checkValid(new State(BOARD, nextToAct,
                (byte) 0, CastleType.Set.NONE, enPassant), visitor);
    }


    private void checkValid(
            State            state,
            Traverser<State> visitor)
    {
        if (! state.checkPieces()) {
            return;
        }

        boolean lastToActInCheck =
                state.isInCheck( state.nextToAct().invert() );

        // cannot move into check
        if (lastToActInCheck) return;

        visitor.traverse(state);
    }


    //--------------------------------------------------------------------
    private byte enPassant(Colour by) {
        if (by == Colour.WHITE) {
            for (byte file = 0; file < Location.FILES; file++) {
                if (BOARD[3][file] == Piece.WHITE_PAWN &&
                        BOARD[2][file] == null &&
                        BOARD[1][file] == null) {
                    return file;
                }
            }
        }
        else {
            for (byte file = 0; file < Location.FILES; file++) {
                if (BOARD[4][file] == Piece.BLACK_PAWN &&
                        BOARD[5][file] == null &&
                        BOARD[6][file] == null) {
                    return file;
                }
            }
        }
        return State.EP_NONE;
    }
}
