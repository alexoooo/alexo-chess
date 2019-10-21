package ao.chess.v2.engine.uct;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.concurrent.*;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 7:31:39 PM
 */
public class UctBgPlayer implements Player
{
    //--------------------------------------------------------------------
    private static final int MAX_EPISODES = 10 * 1000 * 1000;


    //--------------------------------------------------------------------
    private AbstractExecutorService exec =
            (AbstractExecutorService) Executors.newSingleThreadExecutor();
    private Runnable  curRunnable;
    private Future<?> preCalc = null;


    private final boolean OPTIMIZE;
    private final UctNode[] nextRoot = new UctNode[1];



    //--------------------------------------------------------------------
    public UctBgPlayer(boolean optimize)
    {
        OPTIMIZE = optimize;

        if (preCalc != null) {
            preCalc.cancel(true);
        }

        curRunnable = new Runnable() {
            public void run() {
                while (true) {
                    if (nextRoot[0] != null) {
                        int visits = nextRoot[0].visits();
                        if (visits > MAX_EPISODES) {
                            return;
                        }

                        if (visits != 0 && visits % 25000 == 0) {
                            System.out.println(
                                    "root size: " + nextRoot[0].visits() +
                                    "@" + nextRoot[0].depth() + " | " +
                                    Move.toString(
                                           nextRoot[0].optimize().act()));
                        }

                        nextRoot[0].strategize(null);
                    } else {
                        try {
                            Thread.sleep(100);
                        } catch (InterruptedException ignored) {}
                    }
                }
            }
        };
        preCalc = exec.submit( curRunnable );
    }


    //--------------------------------------------------------------------
    public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        UctNode curRoot = null;
        if (nextRoot[0] != null) {
            curRoot = nextRoot[0].childMatching( position );
        }

        if (curRoot == null) {
            curRoot = new UctNode(OPTIMIZE, position, null);
            nextRoot[0] = null;
        } else {
            Io.display("Recycling " + curRoot.visits() +
                        "@" + curRoot.depth());
        }

        nextRoot[0] = curRoot;
        try {
            try {
                preCalc.get(timePerMove, TimeUnit.MILLISECONDS);
            } catch (ExecutionException e) {
                e.printStackTrace();
            } catch (TimeoutException ignored) {}
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        UctNode retRoot = nextRoot[0];
        nextRoot[0]     = null;

        UctNode.Action nextRootNode = retRoot.optimize();
        nextRoot[0] = nextRootNode.node();

        preCalc = exec.submit(curRunnable);
        return nextRootNode.act();
    }


    @Override
    public void close() {
        exec.shutdownNow();
    }
}