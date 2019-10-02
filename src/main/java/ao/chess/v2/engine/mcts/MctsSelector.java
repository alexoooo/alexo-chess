package ao.chess.v2.engine.mcts;

import java.util.Comparator;

/**
 * User: aostrovsky
 * Date: 29-Sep-2009
 * Time: 3:55:45 PM
 */
public interface MctsSelector<V extends MctsValue<V>>
        extends Comparator<V>
{
    double asDouble(V value);
}
