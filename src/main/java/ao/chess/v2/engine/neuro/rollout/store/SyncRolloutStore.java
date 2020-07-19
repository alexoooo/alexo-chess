package ao.chess.v2.engine.neuro.rollout.store;


import com.google.common.base.Preconditions;
import it.unimi.dsi.fastutil.bytes.ByteBigArrayBigList;
import it.unimi.dsi.fastutil.bytes.ByteBigList;
import it.unimi.dsi.fastutil.doubles.DoubleBigArrayBigList;
import it.unimi.dsi.fastutil.doubles.DoubleBigList;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongBigList;


public class SyncRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final LongBigList counts = new LongBigArrayBigList();
    private final DoubleBigList valueSums = new DoubleBigArrayBigList();
    private final LongBigList childMoveOffsets = new LongBigArrayBigList();
    private final LongBigList childMoves = new LongBigArrayBigList();
    private final ByteBigList knownOutcomes = new ByteBigArrayBigList();


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized void initRoot(int moveCount) {
        Preconditions.checkArgument(counts.isEmpty());
        addNode(moveCount);
    }


    @Override
    public synchronized void incrementCount(long nodeIndex) {
        counts.set(nodeIndex, counts.getLong(nodeIndex) + 1);
    }


    @Override
    public synchronized void addValue(long nodeIndex, double value) {
        valueSums.set(nodeIndex, valueSums.getDouble(nodeIndex) + value);
    }


    @Override
    public synchronized void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome) {
        knownOutcomes.set(nodeIndex, (byte) knownOutcome.ordinal());
    }


    @Override
    public synchronized long expandChildIfMissing(long nodeIndex, int moveIndex, int moveCount) {
        long existingChildIndex = getChildIndex(nodeIndex, moveIndex);
        if (existingChildIndex != -1) {
            return -(existingChildIndex + 1);
        }

        long newIndex = addNode(moveCount);

        long parentOffset = childMoveOffsets.getLong(nodeIndex);
        childMoves.set(parentOffset + moveIndex, newIndex);

        return newIndex;
    }


    private long addNode(int moveCount) {
        long newIndex = counts.size64();
        long newMoveOffset = childMoves.size64();

        counts.add(0);
        valueSums.add(0.0);
        knownOutcomes.add((byte) 0);

        for (int i = 0; i < moveCount; i++) {
            childMoves.add(-1);
        }

        childMoveOffsets.add(newMoveOffset);

        return newIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized long getChildIndex(long nodeIndex, int moveIndex) {
        long offset = childMoveOffsets.getLong(nodeIndex);
        return childMoves.getLong(offset + moveIndex);
    }


    @Override
    public synchronized double getValueSum(long nodeIndex) {
        return valueSums.getDouble(nodeIndex);
    }


    @Override
    public synchronized double getAverageValue(long nodeIndex, double defaultValue) {
        long count = counts.getLong(nodeIndex);
        if (count == 0) {
            return defaultValue;
        }

        double valueSum = valueSums.getDouble(nodeIndex);

        return valueSum / count;
    }


    @Override
    public synchronized long getCount(long nodeIndex) {
        return counts.getLong(nodeIndex);
    }


    @Override
    public synchronized KnownOutcome getKnownOutcome(long nodeIndex) {
        return KnownOutcome.values.get(knownOutcomes.getByte(nodeIndex));
    }
}
