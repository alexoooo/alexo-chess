package ao.chess.v2.engine.mcts;

import ao.chess.v2.engine.mcts.value.Ucb1Value;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 10:52:59 PM
 */
public interface MctsValue<T extends MctsValue<T>>
{
    //--------------------------------------------------------------------
    /**
     * @param winRate [0, 1]
     */
    public void   update(double winRate);

    public double confidenceBound(
            T transpositionValue,
            T withRespectToParent);


    //--------------------------------------------------------------------
    public static interface Factory<T extends MctsValue<T>> {
        public T newValue();
    }
}
