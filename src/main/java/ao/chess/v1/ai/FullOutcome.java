package ao.chess.v1.ai;

/**
 *
 */
public enum FullOutcome
{
    //--------------------------------------------------------------------
    BLACK_MATES(false, true,  "0-1 (Black mates)"),
    WHITE_MATES(true,  false, "1-0 (White mates)"),
    STALE_MATE (true,  true,  "1/2-1/2 (Stalemate)"),
    FIFTY_MOVES(true,  true,  "1/2-1/2 (50 moves rule)"),
    REPETITION (true,  true,  "1/2-1/2 (Drawn by repetition)"),
    MATERIAL   (true,  true,  "1/2-1/2 (Drawn by material)"),
    UNDECIDED  (false, false, "undecided");


    //--------------------------------------------------------------------
    private final boolean WHITE_SCORES;
    private final boolean BLACK_SCORES;
    private final String  DISPLAY;


    //--------------------------------------------------------------------
    FullOutcome(boolean whiteScores,
                    boolean blackScores,
                    String  display)
    {
        WHITE_SCORES = whiteScores;
        BLACK_SCORES = blackScores;
        DISPLAY      = display;
    }


    //--------------------------------------------------------------------
    public boolean whiteScores()
    {
        return WHITE_SCORES;
    }

    public boolean blackScores()
    {
        return BLACK_SCORES;
    }

    public boolean isDraw()
    {
        return WHITE_SCORES && BLACK_SCORES;
    }

    public boolean scores(boolean isWhite)
    {
        return isWhite
                ? WHITE_SCORES : BLACK_SCORES;
    }


    //--------------------------------------------------------------------
    @Override public String toString()
    {
        return DISPLAY;
    }
}
