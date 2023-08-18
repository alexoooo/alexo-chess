package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;
import com.google.common.util.concurrent.Uninterruptibles;

import java.nio.file.Path;
import java.time.Duration;
import java.util.AbstractList;
import java.util.List;
import java.util.RandomAccess;
import java.util.concurrent.atomic.AtomicLong;

import static com.google.common.base.Preconditions.checkState;


public class TieredRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final FileRolloutWriteStore writer;
    private final FileRolloutReadStore reader;
    private final FileTranspositionStore transpositions;
    private final MapRolloutStore buffer;
    private final AtomicLong nextIndex;


    //-----------------------------------------------------------------------------------------------------------------
    public TieredRolloutStore(
            Path file,
            Path transpositionFile,
            int readHandleCount,
            int writeHandleCount
    ) {
        writer = new FileRolloutWriteStore(file, writeHandleCount);
        reader = new FileRolloutReadStore(file, readHandleCount);
        transpositions = new FileTranspositionStore(transpositionFile);
        buffer = new MapRolloutStore();

        reader.openIfRequired();
        nextIndex = new AtomicLong(reader.nextIndex());
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized boolean initRootIfRequired(int moveCount) {
        if (nextIndex.get() != RolloutStore.rootIndex) {
            return false;
        }

        reader.closeIfRequired();

        writer.initRoot(moveCount);

        buffer.initRootIfRequired(moveCount);
        nextIndex.set(FileRolloutStore.sizeOf(moveCount));

        reader.openIfRequired();

        return true;
    }


    @Override
    public void incrementVisitCount(long nodeIndex) {
        loadIfMissing(nodeIndex);
        buffer.incrementVisitCount(nodeIndex);
    }


    @Override
    public void decrementVisitCount(long nodeIndex) {
        // NB: decrementing an ephemeral increment
        checkState(buffer.contains(nodeIndex), "Decrement non-buffered: %s", nodeIndex);

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
    public synchronized long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount) {
        loadIfMissing(nodeIndex);

        long existingChildIndex = buffer.getChildIndex(nodeIndex, moveIndex);
        if (existingChildIndex != -1) {
            return -(existingChildIndex + 1);
        }

        long newIndex = nextIndex.get();
        buffer.addNode(nodeIndex, moveIndex, newIndex, childMoveCount);

        int childSize = FileRolloutStore.sizeOf(childMoveCount);
        nextIndex.set(newIndex + childSize);

        return newIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized long nextIndex() {
        return nextIndex.get();
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

        TranspositionInfo backingInfo = transpositions.getTranspositionOrNull(hashHigh, hashLow);
        if (backingInfo != null) {
            buffer.storeTransposition(hashHigh, hashHigh, backingInfo.nodeIndex(), backingInfo.valueSum(), backingInfo.visitCount());
        }
        return backingInfo;
    }


    @Override
    public void setTransposition(long hashHigh, long hashLow, long nodeIndex, double valueSum, long visitCount) {
        buffer.setTransposition(hashHigh, hashLow, nodeIndex, valueSum, visitCount);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void loadIfMissing(long nodeIndex) {
        if (! buffer.contains(nodeIndex)) {
            RolloutStoreNode node = reader.load(nodeIndex);
            buffer.storeIfRequired(node);
        }
    }


    @Override
    public synchronized long flush() {
        if (! buffer.modified()) {
            buffer.clear();
            return 0;
        }

        long[] nodeIndexes = buffer.sortedNodeIndexes();

        class SequenceNodeList extends AbstractList<RolloutStoreNode> implements RandomAccess {
            @Override
            public int size() {
                return nodeIndexes.length;
            }

            @Override
            public RolloutStoreNode get(int index) {
                return buffer.load(nodeIndexes[index]);
            }
        }
        List<RolloutStoreNode> sequencedNodes = new SequenceNodeList();

        reader.closeIfRequired();

        waitAfterDiskRead();

        writer.storeAll(sequencedNodes);

        transpositions.storeAllTranspositions(buffer::forEachTransposition);
        transpositions.flush();

        waitAfterDiskWrite();

        buffer.clear();
        reader.openIfRequired();

        return nodeIndexes.length;
    }


    @SuppressWarnings("UnstableApiUsage")
    private void waitAfterDiskRead() {
        Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(5));
    }


    @SuppressWarnings("UnstableApiUsage")
    private void waitAfterDiskWrite() {
        Uninterruptibles.sleepUninterruptibly(Duration.ofSeconds(10));
    }


    @Override
    public void close() {
        flush();
        reader.closeIfRequired();
        transpositions.close();
    }
}
