package ao.chess.v2.test.bandit.test;

import ao.chess.v2.test.bandit.Bandit;
import ao.chess.v2.test.bandit.Gambler;
import ao.chess.v2.test.bandit.arm.RandomArm;
import ao.chess.v2.test.bandit.eve.MucbtGambler;

/**
 * User: aostrovsky
 * Date: 25-Oct-2009
 * Time: 9:21:23 PM
 */
public class BanditTournament
{
    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        int           nArms   = 40;
        Bandit        bandit  = new Bandit(
                RandomArm.newRandom(nArms, 420));

//        RandomGambler gambler = new RandomGambler( bandit );
//        Gambler gambler = new Ucb1Gambler( bandit );
        Gambler gambler = new MucbtGambler( bandit );
        
        int nPlays = 10 * 1000;
//        int nPlays = nArms * 3;

        for (int i = 0; i < nPlays; i++) {
            gambler.play();
        }

        System.out.println(
                "completed " + nPlays + " plays, " +
                "totalling " + bandit.sumOfRewards() +
                " averaging " + (bandit.sumOfRewards() / nPlays));
    }
}
