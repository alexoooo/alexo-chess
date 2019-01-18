package ao.chess.v2.state;

/**
 * User: alexo
 * Date: Feb 22, 2009
 * Time: 12:06:22 AM
 */
public enum Status
{
    //--------------------------------------------------------------------
    IN_PROGRESS( null               ),
    WHITE_WINS ( Outcome.WHITE_WINS ),
    BLACK_WINS ( Outcome.BLACK_WINS ),
    DRAW       ( Outcome.DRAW       );


    //--------------------------------------------------------------------
    private final Outcome OUTCOME;


    //--------------------------------------------------------------------
    private Status(Outcome outcome)
    {
        OUTCOME = outcome;
    }


    //--------------------------------------------------------------------
    public Outcome toOutcome()
    {
        return OUTCOME;
    }
}
