package ao.chess.v2.engine.mcts;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 9:58:24 PM
 */
public interface MctsScheduler
{
    //--------------------------------------------------------------------
    boolean shouldContinue();


    //--------------------------------------------------------------------
    interface Factory
    {
        MctsScheduler newScheduler(
                                int timeLeft,
                                int timePerMove,
                                int timeIncrement);
    }
}
