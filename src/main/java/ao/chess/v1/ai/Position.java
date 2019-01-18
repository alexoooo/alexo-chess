package ao.chess.v1.ai;

/**
 *
 */
public class Position
{
    //--------------------------------------------------------------------
    private final int  BOARD;
    private final long ZOBRIST;
    private final long PAWN_ZOBRIST;
    private final int  MOVES_FIFTY;
    private final long ETC;


    //--------------------------------------------------------------------
    public Position(
            int  board,
            long zobristKey,
            long pawnZobristKey,
            int  movesFifty,
            long etc)
    {
        BOARD = board;
        ZOBRIST = zobristKey;
        PAWN_ZOBRIST = pawnZobristKey;
        MOVES_FIFTY = movesFifty;
        ETC = etc;
    }


    //--------------------------------------------------------------------
    public boolean equals(Object o)
    {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Position position = (Position) o;

        return BOARD == position.BOARD &&
                MOVES_FIFTY == position.MOVES_FIFTY &&
                PAWN_ZOBRIST == position.PAWN_ZOBRIST &&
                ZOBRIST == position.ZOBRIST &&
                ETC == position.ETC;

    }

    public int hashCode()
    {
        int result;
        result = BOARD;
        result = 31 * result + (int) (ZOBRIST ^ (ZOBRIST >>> 32));
        result = 31 * result + (int) (PAWN_ZOBRIST ^ (PAWN_ZOBRIST >>> 32));
        result = 31 * result + MOVES_FIFTY;
        result = 31 * result + + (int) (ETC ^ (ETC >>> 32));
        return result;
    }
}
