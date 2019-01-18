package ao.chess.v2.engine.mcts;

import it.unimi.dsi.fastutil.longs.LongCollection;

/**
 * User: aostrovsky
 * Date: 11-Oct-2009
 * Time: 12:48:54 PM
 */
public interface TranspositionTable<T extends MctsValue<T>>
{
    //--------------------------------------------------------------------
    public void update(long stateHash, double winRate);


    //--------------------------------------------------------------------
    public T get(long stateHash);


    //--------------------------------------------------------------------
    public T getOrNull(long stateHash);


    //--------------------------------------------------------------------
    public boolean contains(long stateHash);


    //--------------------------------------------------------------------
    public void retain(LongCollection stateHashes);
}
