package ao.chess.v2.test.bandit.eve;

import ao.chess.v2.test.bandit.Bandit;
import ao.chess.v2.test.bandit.Gambler;

/**
 * User: aostrovsky
 * Date: 25-Oct-2009
 * Time: 8:43:20 PM
 */
public class Ucb1Gambler implements Gambler
{
    //--------------------------------------------------------------------
    private final Bandit   MACHINE;
    private final double[] rewardSum;
    private final int   [] invocations;
    private       int      totalInvocations;


    //--------------------------------------------------------------------
    public Ucb1Gambler(Bandit machine)
    {
        MACHINE     = machine;
        rewardSum   = new double[ machine.armCount() ];
        invocations = new int   [ machine.armCount() ];
    }


    //--------------------------------------------------------------------
    @Override public void play()
    {
        double maxUcb1      = Double.NEGATIVE_INFINITY;
        int    maxUcb1Index = -1;
        
        for (int i = 0; i < MACHINE.armCount(); i++)
        {
            if (invocations[i] == 0)
            {
                maxUcb1Index = i;
                break;
            }

            double mean = (rewardSum[i] / invocations[i]);
            double ucb1 = mean + Math.sqrt(
                            (2.0 * Math.log(totalInvocations)) /
                            invocations[i]);
            if (ucb1 > maxUcb1) {
                maxUcb1      = ucb1;
                maxUcb1Index = i;
            }
        }

        double reward = MACHINE.reward( maxUcb1Index );

        totalInvocations++;
        rewardSum  [ maxUcb1Index ] += reward;
        invocations[ maxUcb1Index ]++;
    }
}
