package ao.chess.v2.engine.mcts.transposition;

import ao.chess.v2.engine.mcts.MctsValue;
import ao.chess.v2.engine.mcts.TranspositionTable;
import it.unimi.dsi.fastutil.longs.*;

/**
 * User: aostrovsky
 * Date: 11-Oct-2009
 * Time: 12:52:02 PM
 */
public class NativeTransTable<T extends MctsValue<T>>
        implements TranspositionTable<T>
{
    //--------------------------------------------------------------------
    private final MctsValue.Factory<T> VALUES;
    private final Long2ObjectMap<T>    TABLE;


    //--------------------------------------------------------------------
    public NativeTransTable(
            MctsValue.Factory<T> values)
    {
        VALUES = values;
        TABLE  = new Long2ObjectOpenHashMap<T>();
    }


    //--------------------------------------------------------------------
    @Override
    public void update(long stateHash, double winRate) {
        get(stateHash).update( winRate );
    }


    //--------------------------------------------------------------------
    @Override
    public T get(long stateHash) {
        T value = TABLE.get( stateHash );
        if (value == null) {
            value = VALUES.newValue();
            TABLE.put( stateHash, value );
        }
        return value;
    }


    //--------------------------------------------------------------------
    @Override
    public T getOrNull(long stateHash) {
        return TABLE.get( stateHash );
    }


    //--------------------------------------------------------------------
    @Override
    public boolean contains(long stateHash) {
        return TABLE.containsKey(stateHash);
    }


    //--------------------------------------------------------------------
    @Override
    public void retain(LongCollection stateHashes) {
        TABLE.keySet().retainAll( stateHashes );
    }
}
