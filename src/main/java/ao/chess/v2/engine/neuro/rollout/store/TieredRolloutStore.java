package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;
import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionKey;
import com.google.common.collect.AbstractIterator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Map;


public class TieredRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final FileRolloutStore backing;
    private final MapRolloutStore buffer;
    private long nextIndex;


    //-----------------------------------------------------------------------------------------------------------------
    public TieredRolloutStore(Path file, Path transpositionFile) {
        backing = new FileRolloutStore(file, transpositionFile);
        buffer = new MapRolloutStore();
        nextIndex = backing.nextIndex();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public boolean initRootIfRequired(int moveCount) {
        boolean empty = backing.initRootIfRequired(moveCount);

        if (empty) {
            buffer.initRootIfRequired(moveCount);
            nextIndex = FileRolloutStore.sizeOf(moveCount);
            return true;
        }

        return false;
    }


    @Override
    public void incrementVisitCount(long nodeIndex) {
        loadIfMissing(nodeIndex);
        buffer.incrementVisitCount(nodeIndex);
    }


    @Override
    public void addValue(long nodeIndex, double value) {
        loadIfMissing(nodeIndex);
        buffer.addValue(nodeIndex, value);
    }


    @Override
    public void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome) {
        loadIfMissing(nodeIndex);
        buffer.setKnownOutcome(nodeIndex, knownOutcome);
    }


    @Override
    public long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount) {
        loadIfMissing(nodeIndex);

        long existingChildIndex = buffer.getChildIndex(nodeIndex, moveIndex);
        if (existingChildIndex != -1) {
            return -(existingChildIndex + 1);
        }

        long newIndex = nextIndex;
        buffer.addNode(nodeIndex, moveIndex, newIndex, childMoveCount);

        int childSize = FileRolloutStore.sizeOf(childMoveCount);
        nextIndex += childSize;

        return newIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public long nextIndex() {
        return nextIndex;
    }


    @Override
    public long getVisitCount(long nodeIndex) {
        loadIfMissing(nodeIndex);
        return buffer.getVisitCount(nodeIndex);
    }


    @Override
    public KnownOutcome getKnownOutcome(long nodeIndex) {
        loadIfMissing(nodeIndex);
        return buffer.getKnownOutcome(nodeIndex);
    }


    @Override
    public long getChildIndex(long nodeIndex, int moveIndex) {
        loadIfMissing(nodeIndex);
        return buffer.getChildIndex(nodeIndex, moveIndex);
    }


    @Override
    public double getValueSum(long nodeIndex) {
        loadIfMissing(nodeIndex);
        return buffer.getValueSum(nodeIndex);
    }


    @Override
    public double getValueSquareSum(long nodeIndex) {
        loadIfMissing(nodeIndex);
        return buffer.getValueSquareSum(nodeIndex);
    }


    @Override
    public double getAverageValue(long nodeIndex, double defaultValue) {
        loadIfMissing(nodeIndex);
        return buffer.getAverageValue(nodeIndex, defaultValue);
    }


    @Override
    public TranspositionInfo getTranspositionOrNull(long hashHigh, long hashLow) {
        TranspositionInfo info = buffer.getTranspositionOrNull(hashHigh, hashLow);
        if (info != null) {
            return info;
        }

        TranspositionInfo backingInfo = backing.getTranspositionOrNull(hashHigh, hashLow);
        if (backingInfo != null) {
            buffer.storeTransposition(hashHigh, hashHigh, backingInfo.valueSum(), backingInfo.visitCount());
        }
        return backingInfo;
    }


    @Override
    public void setTransposition(long hashHigh, long hashLow, double valueSum, long visitCount) {
        buffer.setTransposition(hashHigh, hashLow, valueSum, visitCount);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void loadIfMissing(long nodeIndex) {
        if (! buffer.contains(nodeIndex)) {
            RolloutStoreNode node = backing.load(nodeIndex);
            buffer.store(node);
        }
    }


    @Override
    public long flush() {
        if (! buffer.modified()) {
            buffer.clear();
            return 0;
        }

        long[] nodeIndexes = buffer.nodeIndexes().toLongArray();

        Arrays.parallelSort(nodeIndexes);

        Iterator<RolloutStoreNode> nodeIterator = new AbstractIterator<>() {
            private int next = 0;

            @Override
            protected RolloutStoreNode computeNext() {
                if (next >= nodeIndexes.length) {
                    return endOfData();
                }
                long nodeIndex = nodeIndexes[next++];
                return buffer.load(nodeIndex);
            }
        };

        backing.storeAll(nodeIterator);

        Iterator<Map.Entry<TranspositionKey, TranspositionInfo>> transpositionIterator = buffer.transpositionIterator();
        backing.storeAllTranspositions(transpositionIterator);

        buffer.clear();
        backing.flush();

        return nodeIndexes.length;
    }


    @Override
    public void close() {
        flush();
    }
}
