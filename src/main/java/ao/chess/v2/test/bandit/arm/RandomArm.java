package ao.chess.v2.test.bandit.arm;

import ao.util.math.rand.Rand;

import java.util.Random;

/**
 * User: aostrovsky
 * Date: 25-Oct-2009
 * Time: 7:41:39 PM
 */
public class RandomArm implements BanditArm
{
    //--------------------------------------------------------------------
    public static BanditArm[] newRandom(int nArms, long seed) {
        BanditArm[] arms = new BanditArm[ nArms ];
        
        Random rand = new Random(seed);
        for (int i = 0; i < arms.length; i++) {
            arms[ i ] = new RandomArm( rand.nextDouble() );
        }

        return arms;
    }


    //--------------------------------------------------------------------
    private final double MAXIMUM;


    //--------------------------------------------------------------------
    public RandomArm(double maximum) {
        MAXIMUM = maximum;
    }


    //--------------------------------------------------------------------
    @Override public double reward() {
        return Rand.nextDouble( MAXIMUM );
    }
}
