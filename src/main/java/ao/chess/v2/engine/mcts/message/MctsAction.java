package ao.chess.v2.engine.mcts.message;

import ao.chess.v2.engine.mcts.MctsNode;
import ao.chess.v2.engine.mcts.MctsValue;
import ao.chess.v2.state.Move;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 9:45:04 PM
 */
public class MctsAction<V extends MctsValue<V>>
{
    //--------------------------------------------------------------------
    private final int         action;
    private final MctsNode<V> node;


    //--------------------------------------------------------------------
    public MctsAction(int act, MctsNode<V> mctsNode) {
        action = act;
        node   = mctsNode;
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

    public MctsNode<V> node()
    {
        return node;
    }
}
