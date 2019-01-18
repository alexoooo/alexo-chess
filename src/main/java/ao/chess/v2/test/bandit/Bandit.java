package ao.chess.v2.test.bandit;

import ao.chess.v2.test.bandit.arm.BanditArm;

/**
 * User: aostrovsky
 * Date: 25-Oct-2009
 * Time: 7:40:31 PM
 */
public class Bandit
{
    //--------------------------------------------------------------------
    private final BanditArm[] ARMS;
    private       double      sum;


    //--------------------------------------------------------------------
    public Bandit(BanditArm... arms) {
        ARMS = arms.clone();
    }


    //--------------------------------------------------------------------
    public double reward(int arm) {
        double reward = ARMS[arm].reward();
        sum += reward;
        return reward;
    }


    //--------------------------------------------------------------------
    public double sumOfRewards() {
        return sum;
    }


    //--------------------------------------------------------------------
    public int armCount() {
        return ARMS.length;
    }
}
