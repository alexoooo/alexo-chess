package ao.chess.v2.engine.mcts;


/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 10:52:59 PM
 */
public interface MctsValue<T extends MctsValue<T>>
{
    //--------------------------------------------------------------------
    int visits();


    /**
     * @param winRate [0, 1]
     */
    void update(double winRate);

    double confidenceBound(
            T transpositionValue,
            T withRespectToParent);


    //--------------------------------------------------------------------
    interface Factory<T extends MctsValue<T>> {
        T newValue();
    }
}
