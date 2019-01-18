package ao.chess.v1.ai;

import ao.chess.v1.model.Board;
import ao.chess.v1.util.Condition;
import ao.chess.v1.util.Io;

import java.util.HashMap;
import java.util.Map;

/**
 *
 */
public class UctBot implements Bot
{
    //--------------------------------------------------------------------
    private final boolean opt;
    private final int     numRuns;
    private       Node    prevRoot;

    private       boolean   isFirstMove    = true;
    private final Condition keepPrecompute = new Condition(false);
    private final Condition donePrecompute = new Condition(true);


    //--------------------------------------------------------------------
    public UctBot()
    {
        this(128);
    }
    public UctBot(int numRuns)
    {
        this(numRuns, false);
    }
    public UctBot(int numRuns, boolean optimize)
    {
        this.numRuns = numRuns;
        this.opt     = optimize;
    }


    //--------------------------------------------------------------------
    public synchronized int act(final Board board)
    {
        final Map<Position, Node> transposition =
                new HashMap<Position, Node>(1 << 15);

        keepPrecompute.setFalse();
        donePrecompute.ignorantWaitForTrue();

        Node root = null;
        if (prevRoot != null)
        {
            root = prevRoot.childMatching(board);
        }
        if (root == null)
        {
            root = new Node(board);
            root.addLineageTo( transposition );
        }
        else
        {
            Io.display("recycled " + root.visits() +
                       " evaluations " /*+ root.depth() + " ply"*/);
        }

        for (int run = 0; run < numRuns; run++)
        {
            root.strategize(board, transposition, opt);
//            if (run % 100 == 0)
//            {
//                Io.display("run: " + run);
//                System.out.println(
//                        "run: " + run + " @ " +
//                        root.size() + " | " +
//                        transposition.size());
//            }
        }

        Io.display("analyzed " + //root.size() +
                   " (" + transposition.size() + ")" +
                   " positions, " + root.visits() + " visits, " +
                   root.depth() + " ply");

        Node.Action act = root.optimize();
        prevRoot = act.node();

        keepPrecompute.setTrue();
        if (isFirstMove) {
            isFirstMove = false;
            new Thread(new Runnable() {
                public void run() {
                    donePrecompute.setTrue();
                    keepPrecompute.ignorantWaitForTrue();

                    //noinspection InfiniteLoopStatement
                    while (true) {
                        while (keepPrecompute.isTrue()) {
                            prevRoot.strategize(board, transposition, opt);
                        }
                        donePrecompute.setTrue();
                        keepPrecompute.ignorantWaitForTrue();
                        donePrecompute.setFalse();
                    }
                }
            }).start();
        }

        return act.act();
    }
}

