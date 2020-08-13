package ao.chess.v2.state;

import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Piece;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;

/**
 * User: alex
 * Date: 17-Oct-2009
 * Time: 1:54:47 PM
 *
 * See http://en.wikipedia.org/wiki/Board_representation_(chess)
 */
public class Representation
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        State test = State.fromFen(
                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 13 2");

        System.out.println(test);

        byte[] packed = packStream(test);
        State unpacked = unpackStream(packed);
        System.out.println(unpacked);        
    }


    //--------------------------------------------------------------------
    private Representation() {}


    //--------------------------------------------------------------------
    public static State unpackStream(byte[] stream)
    {
        return unpackStream(stream, 0, stream.length);
    }


    public static State unpackStream(byte[] stream, int offset, int length)
    {
        Colour nextToAct =
                ((stream[offset] >>> 7) > 0 ? Colour.WHITE : Colour.BLACK);

        byte reversibleMoves = (byte) (stream[offset] & 0x7F);

        CastleType.Set castles =
                new CastleType.Set((byte)(stream[offset + 1] >>> 4));

        byte enPassantFile = (byte)(stream[offset + 1] & 0xF);
        if (enPassantFile > 7) {
            enPassantFile = -1;
        }

        int nextLocation = 0;
        Piece[][] board = new Piece[8][8];
        for (int i = 2; i < length; i++) {
            if (stream[offset + i] < 0) {
                nextLocation -= stream[offset + i];
            }
            else {
                board[ Location.rankIndex(nextLocation) ]
                     [ Location.fileIndex(nextLocation) ] =
                         Piece.VALUES[ stream[offset + i] ];

                nextLocation++;
            }
        }

        return new State(
                board, nextToAct,
                reversibleMoves, castles, enPassantFile);
    }


    //--------------------------------------------------------------------
    public static byte[] packStream(State state)
    {
        ByteList packed = new ByteArrayList();
        packStream(state, packed);
        return packed.toByteArray();
    }


    public static void packStream(State state, ByteList packed)
    {
        // next to act (1)
        // reversible moves (7)
        int nextToAct = (state.nextToAct() == Colour.WHITE ? 1 : 0);
        packed.add((byte)(nextToAct << 7 |
                          state.reversibleMoves()));

        // castles (4)
        // en passant (4)
        packed.add((byte)(state.castlesAvailable().toBits() << 4 |
                          state.enPassantFile() & 0xF));

        byte empties = 0;
        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                Piece p = state.pieceAt(rank, file);
                if (p == null) {
                    empties++;
                }
                else {
                    if (empties > 0) {
                        packed.add( (byte) -empties );
                    }
                    packed.add((byte) p.ordinal());
                    empties = 0;
                }
            }
        }
    }


    //--------------------------------------------------------------------
    public static Piece[][] board(State of) {
        Piece[][] board =
                new Piece[ Location.RANKS ]
                         [ Location.FILES ];

        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                board[ rank ][ file ] = of.pieceAt(rank, file);
            }
        }

        return board;
    }


    //--------------------------------------------------------------------
    public static String displayPosition(
            Piece[][]      board,
            Colour         nextToAct,
            int            reversibleMoves,
            CastleType.Set castles,
            int            enPassant
    ) {
        StringBuilder str = new StringBuilder();

        str.append("Next to Act: ").append(nextToAct);
        str.append("\nReversible Moves: ").append(reversibleMoves);

        if (! castles.noneAvailable()) {
            str.append("\nCastles Available: ");

            if (castles.whiteAvailable()) {
                str.append("[white: ");
                if (castles.allWhiteAvailable()) {
                    str.append("O-O, O-O-O");
                }
                else if (castles.whiteQueenSide()) {
                    str.append("O-O-O");
                }
                else {
                    str.append("O-O");
                }
                str.append("] ");
            }
            if (castles.blackAvailable()) {
                str.append("[black: ");
                if (castles.allBlackAvailable()) {
                    str.append("O-O, O-O-O");
                }
                else if (castles.blackQueenSide()) {
                    str.append("O-O-O");
                }
                else {
                    str.append("O-O");
                }
                str.append("]");
            }
        }

        if (enPassant != State.EP_NONE) {
            str.append("\nEn Passants: ");
            str.append(enPassant);
        }

        for (int rank = 7; rank >= 0; rank--) {
            str.append("\n");
            for (int file = 0; file < 8; file++) {
                Piece p = board[rank][file];
                str.append((p == null) ? "." : p);
            }
        }

        return str.toString();
    }


    //--------------------------------------------------------------------
    public static String fen(
            Piece[][]      board,
            Colour         nextToAct,
            int            reversibleMoves,
            CastleType.Set castles,
            int            enPassant)
    {
        StringBuilder str = new StringBuilder();

        for (int rank = Location.RANKS - 1; rank >= 0; rank--) {
            int emptySquares = 0;
            for (int file = 0; file < Location.FILES; file++) {
                Piece p = board[rank][file];
                if (p == null) {
                    emptySquares++;
                } else {
                    if (emptySquares > 0) {
                        str.append(emptySquares);
                        emptySquares = 0;
                    }
                    str.append(p.toString());
                }
            }
            if (emptySquares > 0) {
                str.append(emptySquares);
            }

            if (rank != 0 ) {
                str.append("/");
            }
        }

        str.append(" ");
        str.append(nextToAct == Colour.WHITE
                   ? "w" : "b");
        str.append(" ");

        // castles
        if (castles == null) {
            str.append("-");
        } else {
            str.append(castles.toFen());
        }

        str.append(" ");

		// En passant square
        if (enPassant == -1) {
            str.append("-");
        } else {
            str.append(State.FILES.charAt(enPassant));
//            str.append(" ");
            if (nextToAct == Colour.WHITE) {
                str.append(6);
            } else {
                str.append(3);
            }
        }
        str.append(" ");

        // reversible moves
//        str.append("0");
        str.append(reversibleMoves);

        str.append(" ");

        // full moves since start of game
        str.append("n");

		return str.toString();
    }


    //--------------------------------------------------------------------
    public static State fromFen(String fen) {
        Piece[][] board =
                new Piece[ Location.RANKS ]
                         [ Location.FILES ];

        String[] parts = fen.split(" ");
        String[] ranks = parts[0].split("/");

        for (int rank = 7; rank >= 0; rank--) {
            int file = 0;
            for (char fenPiece : ranks[7 - rank].toCharArray()) {
                if (Character.isDigit(fenPiece)) {
                    int emptyFiles = Character.digit(fenPiece, 10);
                    file += emptyFiles;
                } else {
                    board[rank][file++] = Piece.valueOf(fenPiece);
                }
            }
        }

        Colour nextToAct = parts[1].equals("w")
                    ? Colour.WHITE : Colour.BLACK;

        byte castleBits = 0;
        if (parts.length >= 3 && (! parts[2].equals("-"))) {
            for (char castle : parts[2].toCharArray()) {
                switch (castle) {
                    case 'K': castleBits |= State.WHITE_K_CASTLE; break;
                    case 'Q': castleBits |= State.WHITE_Q_CASTLE; break;
                    case 'k': castleBits |= State.BLACK_K_CASTLE; break;
                    case 'q': castleBits |= State.BLACK_Q_CASTLE; break;
                }
            }
        }

        byte enPassant = State.EP_NONE;
        if (parts.length >= 4 && (! parts[3].equals("-"))) {
            enPassant = (byte) State.FILES.indexOf(parts[3].charAt(0));
        }

        byte reversibleMoves = 0;
        if (parts.length >= 5 && (! parts[4].equals("-"))) {
            reversibleMoves = Byte.parseByte(parts[4]);
        }

        return new State(
                board,
                nextToAct,
                reversibleMoves,
                new CastleType.Set(castleBits),
                enPassant);
    }
}
