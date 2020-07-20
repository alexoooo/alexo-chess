package ao.chess.v2.engine.neuro.rollout.store;


import com.google.common.collect.AbstractIterator;
import it.unimi.dsi.fastutil.longs.LongIterator;

import java.nio.file.Path;
import java.util.Arrays;
import java.util.Iterator;


public class TieredRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final FileRolloutStore backing;
    private final MapRolloutStore buffer;
    private long nextIndex;


    //-----------------------------------------------------------------------------------------------------------------
    public TieredRolloutStore(Path file) {
        backing = new FileRolloutStore(file);
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
    public double getAverageValue(long nodeIndex, double defaultValue) {
        loadIfMissing(nodeIndex);
        return buffer.getAverageValue(nodeIndex, defaultValue);
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

//        long size = buffer.nodeIndexes().size();

        long[] nodeIndexes = buffer.nodeIndexes().toLongArray();
        Arrays.parallelSort(nodeIndexes);

        LongIterator nodeIndexIterator = buffer.nodeIndexes().iterator();
        Iterator<RolloutStoreNode> nodeIterator = new AbstractIterator<>() {
            @Override
            protected RolloutStoreNode computeNext() {
                if (! nodeIndexIterator.hasNext()) {
                    return endOfData();
                }
                long nodeIndex = nodeIndexIterator.nextLong();
                return buffer.load(nodeIndex);
            }
        };

        backing.storeAll(nodeIterator);
//        while (nodeIndexIterator.hasNext()) {
//            long nodeIndex = nodeIndexIterator.nextLong();
//            RolloutStoreNode node = buffer.load(nodeIndex);
//            backing.store(node);
//        }
        buffer.clear();
        backing.flush();

        return nodeIndexes.length;
    }


    @Override
    public void close() throws Exception {
        flush();
    }
}
