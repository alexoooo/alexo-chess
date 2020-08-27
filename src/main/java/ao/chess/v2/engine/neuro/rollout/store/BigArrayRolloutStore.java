package ao.chess.v2.engine.neuro.rollout.store;


import it.unimi.dsi.fastutil.bytes.ByteBigArrayBigList;
import it.unimi.dsi.fastutil.bytes.ByteBigList;
import it.unimi.dsi.fastutil.doubles.DoubleBigArrayBigList;
import it.unimi.dsi.fastutil.doubles.DoubleBigList;
import it.unimi.dsi.fastutil.longs.LongBigArrayBigList;
import it.unimi.dsi.fastutil.longs.LongBigList;


public class BigArrayRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final LongBigList counts = new LongBigArrayBigList();
    private final DoubleBigList valueSums = new DoubleBigArrayBigList();
    private final DoubleBigList valueSquareSums = new DoubleBigArrayBigList();
    private final ByteBigList knownOutcomes = new ByteBigArrayBigList();
    private final LongBigList childMoveOffsets = new LongBigArrayBigList();
    private final LongBigList childMoves = new LongBigArrayBigList();


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public boolean initRootIfRequired(int moveCount) {
        if (counts.isEmpty()) {
            return false;
        }

        addNode(moveCount);
        return true;
    }


    @Override
    public void incrementVisitCount(long nodeIndex) {
        counts.set(nodeIndex, counts.getLong(nodeIndex) + 1);
    }


    @Override
    public void addValue(long nodeIndex, double value) {
        valueSums.set(nodeIndex, valueSums.getDouble(nodeIndex) + value);
        valueSquareSums.set(nodeIndex, valueSquareSums.getDouble(nodeIndex) + value * value);
    }


    @Override
    public void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome) {
        knownOutcomes.set(nodeIndex, (byte) knownOutcome.ordinal());
    }


    @Override
    public long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount) {
        long existingChildIndex = getChildIndex(nodeIndex, moveIndex);
        if (existingChildIndex != -1) {
            return -(existingChildIndex + 1);
        }

        long newIndex = addNode(childMoveCount);

        long parentOffset = childMoveOffsets.getLong(nodeIndex);
        childMoves.set(parentOffset + moveIndex, newIndex);

        return newIndex;
    }


    private long addNode(int moveCount) {
        long newIndex = nextIndex();
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
    public long getChildIndex(long nodeIndex, int moveIndex) {
        long offset = childMoveOffsets.getLong(nodeIndex);
        return childMoves.getLong(offset + moveIndex);
    }


    @Override
    public double getValueSum(long nodeIndex) {
        return valueSums.getDouble(nodeIndex);
    }


    @Override
    public double getValueSquareSum(long nodeIndex) {
        return valueSquareSums.getDouble(nodeIndex);
    }


    @Override
    public double getAverageValue(long nodeIndex, double defaultValue) {
        long count = counts.getLong(nodeIndex);
        if (count == 0) {
            return defaultValue;
        }

        double valueSum = valueSums.getDouble(nodeIndex);

        return valueSum / count;
    }


    @Override
    public long getVisitCount(long nodeIndex) {
        return counts.getLong(nodeIndex);
    }


    @Override
    public KnownOutcome getKnownOutcome(long nodeIndex) {
        return KnownOutcome.values.get(knownOutcomes.getByte(nodeIndex));
    }


    @Override
    public long nextIndex() {
        return counts.size64() ;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {}
}
