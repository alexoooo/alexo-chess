package ao.chess.v2.engine.mcts;

import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.State;
import it.unimi.dsi.fastutil.longs.LongCollection;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 12:59:18 PM
 */
public interface MctsNode<V extends MctsValue<V>>
{
    //--------------------------------------------------------------------
    public void runTrajectory(
            State                 fromProtoState,
            MctsValue.Factory<V>  values,
            MctsRollout           mcRollout,
            TranspositionTable<V> transpositionTable,
            MctsHeuristic         heuristic);

    public MctsAction<V> bestMove(MctsSelector<V> selector);

    public MctsNode childMatching(int action);

    public void addStates(LongCollection to);


    //--------------------------------------------------------------------
    public static interface Factory
            <//N extends MctsNode<N>,
             V extends MctsValue<V>>
    {
        public MctsNode<V>/*N*/ newNode(
                State                state,
                MctsValue.Factory<V> valueFactory);
    }
}
