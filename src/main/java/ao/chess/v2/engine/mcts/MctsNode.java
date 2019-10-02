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
    void runTrajectory(
            State                 fromProtoState,
            MctsValue.Factory<V>  values,
            MctsRollout           mcRollout,
            TranspositionTable<V> transpositionTable,
            MctsHeuristic         heuristic);

    MctsAction<V> bestMove(MctsSelector<V> selector);

    double moveScore(int action, MctsSelector<V> selector);

    MctsNode childMatching(int action);

    void addStates(LongCollection to);


    //--------------------------------------------------------------------
    interface Factory
            <V extends MctsValue<V>>
    {
        MctsNode<V>/*N*/ newNode(
                State                state,
                MctsValue.Factory<V> valueFactory);
    }
}
