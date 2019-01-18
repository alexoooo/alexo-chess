package ao.chess.v2.engine.trans;

import ao.chess.v2.engine.mcts.MctsNode;
import ao.chess.v2.state.Move;

/**
 * User: aostrovsky
 * Date: 12-Oct-2009
 * Time: 11:32:17 AM
 */
public class TransAction
{
    //--------------------------------------------------------------------
    private final int       action;
    private final TransNode node;


    //--------------------------------------------------------------------
    public TransAction(int act, TransNode transNode) {
        action = act;
        node   = transNode;
    }


    //--------------------------------------------------------------------
    public String information() {
        return Move.toString(action) + " | " + node.toString();
    }


    //--------------------------------------------------------------------
    public int action()
    {
        return action;
    }

    public TransNode node()
    {
        return node;
    }
}
