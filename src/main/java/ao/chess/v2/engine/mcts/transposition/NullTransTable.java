package ao.chess.v2.engine.mcts.transposition;

import ao.chess.v2.engine.mcts.MctsValue;
import ao.chess.v2.engine.mcts.TranspositionTable;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.longs.LongCollection;

/**
 * User: aostrovsky
 * Date: 11-Oct-2009
 * Time: 12:52:02 PM
 */
public class NullTransTable<T extends MctsValue<T>>
        implements TranspositionTable<T>
{
    //--------------------------------------------------------------------
    public NullTransTable() {}


    //--------------------------------------------------------------------
    @Override
    public void update(long stateHash, double winRate) {}


    //--------------------------------------------------------------------
    @Override
    public T get(long stateHash) {
        return null;
    }


    //--------------------------------------------------------------------
    @Override
    public T getOrNull(long stateHash) {
        return null;
    }

    
    //--------------------------------------------------------------------
    @Override
    public boolean contains(long stateHash) {
        return false;
    }


    //--------------------------------------------------------------------
    @Override
    public void retain(LongCollection stateHashes) {}
}