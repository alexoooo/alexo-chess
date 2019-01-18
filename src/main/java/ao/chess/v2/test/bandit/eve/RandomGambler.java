package ao.chess.v2.test.bandit.eve;

import ao.chess.v2.test.bandit.Bandit;
import ao.chess.v2.test.bandit.Gambler;
import ao.util.math.rand.Rand;

/**
 * User: aostrovsky
 * Date: 25-Oct-2009
 * Time: 8:56:11 PM
 */
public class RandomGambler implements Gambler
{
    //--------------------------------------------------------------------
    private final Bandit MACHINE;


    //--------------------------------------------------------------------
    public RandomGambler(Bandit machine) {
        MACHINE = machine;
    }


    //--------------------------------------------------------------------
    @Override public void play() {
        MACHINE.reward(Rand.nextInt(MACHINE.armCount()));
    }
}
