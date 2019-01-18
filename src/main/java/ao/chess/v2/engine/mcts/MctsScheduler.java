package ao.chess.v2.engine.mcts;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 9:58:24 PM
 */
public interface MctsScheduler
{
    //--------------------------------------------------------------------
    public boolean shouldContinue();


    //--------------------------------------------------------------------
    public static interface Factory
    {
        public MctsScheduler newScheduler(
                                int timeLeft,
                                int timePerMove,
                                int timeIncrement);
    }
}
