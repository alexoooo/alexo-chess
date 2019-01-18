package ao.chess.v2.engine.mcts.scheduler;

import ao.chess.v2.engine.mcts.MctsScheduler;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 10:11:43 PM
 */
public class MctsSchedulerImpl implements MctsScheduler
{
    //--------------------------------------------------------------------
    public static class Factory implements MctsScheduler.Factory {
        @Override
        public MctsScheduler newScheduler(
                int timeLeft, int timePerMove, int timeIncrement) {
            return new MctsSchedulerImpl(timePerMove);
        }
    }



    //--------------------------------------------------------------------
    private final long startAt;
    private final long timePerMove;


    //--------------------------------------------------------------------
    public MctsSchedulerImpl(long perMoveMillis)
    {
        startAt     = System.currentTimeMillis();
        timePerMove = perMoveMillis;
    }


    //--------------------------------------------------------------------
    @Override
    public boolean shouldContinue() {
        return (System.currentTimeMillis() - startAt) < timePerMove;
    }
}
