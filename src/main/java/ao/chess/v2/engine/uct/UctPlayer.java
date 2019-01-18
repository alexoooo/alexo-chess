package ao.chess.v2.engine.uct;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.HashMap;
import java.util.Map;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 7:31:39 PM
 */
public class UctPlayer implements Player
{
    //--------------------------------------------------------------------
    private final boolean OPTIMIZE;
    private       UctNode prevRoot;


    //--------------------------------------------------------------------
    public UctPlayer(boolean optimize)
    {
        OPTIMIZE = optimize;
        prevRoot = null;
    }


    //--------------------------------------------------------------------
    public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        final Map<State, UctNode> transposition =
                new HashMap<State, UctNode>();

        UctNode root = null;
        if (prevRoot != null) {
            root = prevRoot.childMatching(position);
        }
        if (root == null) {
            root = new UctNode(OPTIMIZE, position, transposition);
        } else {
//            root.addLineageTo( transposition );
        }

//        Io.display("Recycling " + root.visits() +
//                    "@" + (OPTIMIZE ? "?" : root.depth()));
        Io.display("Recycling " + root.visits() +
                    "@" + root.depth());

        int  count  = 0;
        long before = System.currentTimeMillis();
        long prev   = before;
        while ((System.currentTimeMillis() - before) < timePerMove) {
            root.strategize(transposition);

            if (count++ != 0 && count % 25000 == 0) {
                System.out.println(
                        "root size: " + root.visits() +
                        "@" + root.depth() +
                        " in " + (System.currentTimeMillis() - prev) +
                        " | " + Move.toString(root.optimize().act()));
                prev = System.currentTimeMillis();
            }
        }

        UctNode.Action act = root.optimize();
        prevRoot = act.node();
        return act.act();
    }
}
