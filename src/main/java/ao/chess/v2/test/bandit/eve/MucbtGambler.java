package ao.chess.v2.test.bandit.eve;

import ao.chess.v2.test.bandit.Bandit;
import ao.chess.v2.test.bandit.Gambler;

import java.util.Arrays;


/**
 * User: aostrovsky
 * Date: 25-Oct-2009
 * Time: 9:39:33 PM
 *
 * NB: this is a broken implementation of the ill-defined "Meta-UCBT" algorithm from "Anytime many-armed bandits"
 */
public class MucbtGambler implements Gambler
{
    //--------------------------------------------------------------------
    private final Bandit    MACHINE;
    private final double [] rewardSum;
    private final int    [] invocations;
    //    private final int    [] cumInvocations;
    private final Integer[] inWinrateOrder;
    private       int       totalInvocations;
    private       double    totalReward;


    //--------------------------------------------------------------------
    public MucbtGambler(Bandit machine)
    {
        MACHINE        = machine;
        rewardSum      = new double [ machine.armCount() ];
        invocations    = new int    [ machine.armCount() ];
//        cumInvocations = new int    [ machine.armCount() ];
        inWinrateOrder = new Integer[ machine.armCount() ];

        for (int i = 0; i < inWinrateOrder.length; i++) {
            inWinrateOrder[ i ] = i;
        }
    }


    //--------------------------------------------------------------------
    @Override public void play()
    {
        Arrays.sort(inWinrateOrder, (a, b) -> {
            double meanA =
                    (invocations[a] == 0)
                            ? Double.NEGATIVE_INFINITY
                            : (rewardSum[a] / invocations[a]);

            double meanB =
                    (invocations[b] == 0)
                            ? Double.NEGATIVE_INFINITY
                            : (rewardSum[b] / invocations[b]);

            return Double.compare(meanB, meanA); // descending
        });

        int nextArmIndex = -1;
        if (totalInvocations < 2) {
            if (totalInvocations == 0) {
                nextArmIndex = 0;
            }
            else {
                nextArmIndex = 1;
            }
            nextArmIndex = Math.min(
                    nextArmIndex, MACHINE.armCount() - 1);
        }
        else {
            double remainReward = totalReward;
            int restCount = totalInvocations;

            for (int i = 0; i < MACHINE.armCount(); i++) {
                int nextByMean = inWinrateOrder[ i ];
                double currMean =
                        invocations[nextByMean] == 0
                                ? Double.NEGATIVE_INFINITY
                                : (rewardSum[nextByMean] / invocations[nextByMean]);

                double currValue = currMean +
                        Math.sqrt((2.0 * Math.log(restCount)
                                / invocations[nextByMean]));

                int remainCount =
                        restCount - invocations[ nextByMean ];
                if (remainCount <= 0) {
                    nextArmIndex = nextByMean;
                    break;
                }

                remainReward -= rewardSum[nextByMean];
                double remainMean  =
                        remainReward / remainCount;
                double remainValue = remainMean +
                        Math.sqrt((2.0 * Math.log(restCount)
                                / remainCount));

                if (currValue > remainValue) {
                    nextArmIndex = nextByMean;
                    break;
                }

                restCount = remainCount;
            }
        }

        double reward = MACHINE.reward( nextArmIndex );

        totalReward += reward;
        rewardSum  [ nextArmIndex ] += reward;

//        for (int i = 0; i < MACHINE.armCount(); i++)
//        {
//            int armIndex = inWinrateOrder[ i ];
//            if (nextArmIndex == armIndex) break;
//        }

        totalInvocations++;
        invocations[ nextArmIndex ]++;
    }
}
