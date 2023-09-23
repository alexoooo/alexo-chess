package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;
import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionKey;
import it.unimi.dsi.fastutil.longs.*;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;


// thread safe
public class MapRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final Long2LongMap counts = new Long2LongOpenHashMap();
    private final Long2DoubleMap valueSums = new Long2DoubleOpenHashMap();
    private final Long2DoubleMap valueSquareSums = new Long2DoubleOpenHashMap();
    private final Long2ByteMap knownOutcomes = new Long2ByteOpenHashMap();
    private final Long2ByteMap childMoveCounts = new Long2ByteOpenHashMap();
    private final Long2LongMap childMoveOffsets = new Long2LongOpenHashMap();
    private final LongBigList childMoves = new LongBigArrayBigList();
    private long maxIndex = -1;
    private boolean modified = false;

    private final Map<TranspositionKey, TranspositionInfo> transposition = new HashMap<>();


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized boolean initRootIfRequired(int moveCount) {
        if (! counts.isEmpty()) {
            return false;
        }

        addNode(moveCount);
        return true;
    }


    @Override
    public synchronized void incrementVisitCount(long nodeIndex) {
        long previousCount = counts.get(nodeIndex);
        counts.put(nodeIndex, previousCount + 1);
        modified = true;
    }


    @Override
    public synchronized void decrementVisitCount(long nodeIndex) {
        long previousCount = counts.get(nodeIndex);
        counts.put(nodeIndex, previousCount - 1);
        modified = true;
    }


    @Override
    public synchronized void addValue(long nodeIndex, double value) {
        valueSums.put(nodeIndex, valueSums.get(nodeIndex) + value);
        valueSquareSums.put(nodeIndex, valueSquareSums.get(nodeIndex) + value * value);
        modified = true;
    }


    @Override
    public synchronized void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome) {
        knownOutcomes.put(nodeIndex, (byte) knownOutcome.ordinal());
        modified = true;
    }


    @Override
    public synchronized long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount) {
        long existingChildIndex = getChildIndex(nodeIndex, moveIndex);
        if (existingChildIndex != -1) {
            return -(existingChildIndex + 1);
        }

        long newIndex = nextIndex();
        addNode(nodeIndex, moveIndex, newIndex, childMoveCount);

        return newIndex;
    }


    @Override
    public synchronized long nextIndex() {
        return maxIndex + 1;
    }


    private long addNode(int moveCount) {
        long newIndex = nextIndex();
        addNode(newIndex, moveCount);
        return newIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized long getChildIndex(long nodeIndex, int moveIndex) {
        long offset = childMoveOffsets.get(nodeIndex);
        if (childMoves.size64() == (offset + moveIndex)) {
            return -1;
        }
        if (childMoves.size64() < (offset + moveIndex)) {
            System.out.println("foo");
        }
        return childMoves.getLong(offset + moveIndex);
    }


    @Override
    public synchronized double getValueSum(long nodeIndex) {
        return valueSums.get(nodeIndex);
    }


    @Override
    public synchronized double getValueSquareSum(long nodeIndex) {
        return valueSquareSums.get(nodeIndex);
    }


    @Override
    public synchronized double getAverageValue(long nodeIndex, double defaultValue) {
        long count = counts.get(nodeIndex);
        if (count == 0) {
            return defaultValue;
        }

        double valueSum = valueSums.get(nodeIndex);

        return valueSum / count;
    }

    @Override
    public synchronized long getVisitCount(long nodeIndex) {
        return counts.get(nodeIndex);
    }


    @Override
    public synchronized KnownOutcome getKnownOutcome(long nodeIndex) {
        return KnownOutcome.values.get(knownOutcomes.get(nodeIndex));
    }


    //-----------------------------------------------------------------------------------------------------------------
//    public boolean containsTransposition(long hashHigh, long hashLow) {
//        return transposition.containsKey(new TranspositionKey(hashHigh, hashLow));
//    }


    @Override
    public synchronized TranspositionInfo getTranspositionOrNull(long hashHigh, long hashLow) {
        return transposition.get(new TranspositionKey(hashHigh, hashLow));
    }


    @Override
    public synchronized void setTransposition(long hashHigh, long hashLow, long nodeIndex, double valueSum, long visitCount) {
        storeTransposition(hashHigh, hashLow, nodeIndex, valueSum, visitCount);
        modified = true;
    }


    public synchronized void storeTransposition(long hashHigh, long hashLow, long nodeIndex, double valueSum, long visitCount) {
        transposition.put(
                new TranspositionKey(hashHigh, hashLow),
                new TranspositionInfo(nodeIndex, valueSum, visitCount));
    }


//    public synchronized Iterator<Map.Entry<TranspositionKey, TranspositionInfo>> transpositionIterator() {
//        return transposition.entrySet().iterator();
//    }
    public synchronized void forEachTransposition(Consumer<Map.Entry<TranspositionKey, TranspositionInfo>> consumer) {
        transposition.entrySet().forEach(consumer);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public synchronized boolean contains(long nodeIndex) {
        return counts.containsKey(nodeIndex);
    }


    public synchronized void storeIfRequired(
            RolloutStoreNode node
    ) {
        if (contains(node.index())) {
            return;
        }

        long newMoveOffset = childMoves.size64();

        counts.put(node.index(), node.visitCount());
        valueSums.put(node.index(), node.valueSum());
        valueSquareSums.put(node.index(), node.valueSquareSum());
        knownOutcomes.put(node.index(), (byte) node.knownOutcome().ordinal());

        for (int i = 0; i < node.moveCount(); i++) {
            childMoves.add(node.childIndex(i));
        }

        childMoveCounts.put(node.index(), (byte) node.moveCount());
        childMoveOffsets.put(node.index(), newMoveOffset);

//        modified = true;
    }


    public synchronized void addNode(long parentIndex, int parentMoveIndex, long nodeIndex, int moveCount) {
        addNode(nodeIndex, moveCount);

        long parentOffset = childMoveOffsets.get(parentIndex);
        childMoves.set(parentOffset + parentMoveIndex, nodeIndex);
    }


    private void addNode(long nodeIndex, int moveCount) {
        long newMoveOffset = childMoves.size64();

        counts.put(nodeIndex, 0);
        valueSums.put(nodeIndex, 0.0);
        knownOutcomes.put(nodeIndex, (byte) 0);

        for (int i = 0; i < moveCount; i++) {
            childMoves.add(-1);
        }

        childMoveCounts.put(nodeIndex, (byte) moveCount);
        childMoveOffsets.put(nodeIndex, newMoveOffset);

        maxIndex = Math.max(maxIndex, nodeIndex);
        modified = true;
    }


    public synchronized long[] sortedNodeIndexes() {
        long[] nodeIndexes = counts.keySet().toLongArray();
        Arrays.parallelSort(nodeIndexes);
        return nodeIndexes;
    }


    public synchronized RolloutStoreNode load(long nodeIndex) {
        int moveCount = Byte.toUnsignedInt(childMoveCounts.get(nodeIndex));
        long[] childIndexes = new long[moveCount];

        long childMoveOffset = childMoveOffsets.get(nodeIndex);
        for (int i = 0; i < moveCount; i++) {
            long childIndex = childMoves.getLong(childMoveOffset + i);
            childIndexes[i] = childIndex;
        }

        return new RolloutStoreNode(
                nodeIndex,
                getVisitCount(nodeIndex),
                getValueSum(nodeIndex),
                getValueSquareSum(nodeIndex),
                getKnownOutcome(nodeIndex),
                childIndexes);
    }


    public synchronized void clear() {
        counts.clear();
        valueSums.clear();
        valueSquareSums.clear();
        knownOutcomes.clear();
        childMoveCounts.clear();
        childMoveOffsets.clear();
        childMoves.clear();
        maxIndex = -1;
        modified = false;
        transposition.clear();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized void close() {}

    public synchronized boolean modified() {
        return modified;
    }
}
