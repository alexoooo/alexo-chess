package ao.chess.v2.engine.neuro.rollout.store;


import it.unimi.dsi.fastutil.doubles.DoubleBigArrayBigList;
import it.unimi.dsi.fastutil.doubles.DoubleBigList;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongBigList;


public class SyncRolloutStore implements RolloutStore {
    private final LongBigList counts = new LongBigArrayBigList();
    private final DoubleBigList valueSums = new DoubleBigArrayBigList();
    private final LongBigList childMoveOffsets = new LongBigArrayBigList();
    private final LongBigList childMoves = new LongBigArrayBigList();


    @Override
    public synchronized void incrementCount(long nodeIndex) {
        counts.set(nodeIndex, counts.getLong(nodeIndex) + 1);
    }


    @Override
    public synchronized void addValue(long nodeIndex, double value) {
        valueSums.set(nodeIndex, valueSums.getDouble(nodeIndex) + value);
    }


    @Override
    public synchronized long childIndex(long nodeIndex, int moveIndex) {
        long offset = childMoveOffsets.getLong(nodeIndex);
        return childMoves.getLong(offset + moveIndex);
    }


    @Override
    public synchronized long addChild(long nodeIndex, int moveIndex, int moveCount) {
        long newIndex = counts.size64();
        long newMoveOffset = childMoves.size64();

        counts.add(0);
        valueSums.add(0.0);

        for (int i = 0; i < moveCount; i++) {
            childMoves.add(-1);
        }

        childMoveOffsets.add(newMoveOffset);

        long parentOffset = childMoveOffsets.getLong(nodeIndex);
        childMoves.set(parentOffset + moveIndex, newIndex);

        return newIndex;
    }
}
