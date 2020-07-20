package ao.chess.v2.engine.neuro.rollout.store;


import it.unimi.dsi.fastutil.longs.LongIterator;

import java.nio.file.Path;


public class TieredRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final FileRolloutStore persistent;
    private final MapRolloutStore cache;
    private long nextIndex;


    //-----------------------------------------------------------------------------------------------------------------
    public TieredRolloutStore(Path file) {
        persistent = new FileRolloutStore(file);
        cache = new MapRolloutStore();
        nextIndex = persistent.nextIndex();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public boolean initRootIfRequired(int moveCount) {
        boolean empty = persistent.initRootIfRequired(moveCount);

        if (empty) {
            cache.initRootIfRequired(moveCount);
            nextIndex = FileRolloutStore.sizeOf(moveCount);
            return true;
        }

        return false;
    }


    @Override
    public void incrementVisitCount(long nodeIndex) {
        loadIfMissing(nodeIndex);
        cache.incrementVisitCount(nodeIndex);
    }


    @Override
    public void addValue(long nodeIndex, double value) {
        loadIfMissing(nodeIndex);
        cache.addValue(nodeIndex, value);
    }


    @Override
    public void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome) {
        loadIfMissing(nodeIndex);
        cache.setKnownOutcome(nodeIndex, knownOutcome);
    }


    @Override
    public long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount) {
        loadIfMissing(nodeIndex);

        long existingChildIndex = cache.getChildIndex(nodeIndex, moveIndex);
        if (existingChildIndex != -1) {
            return -(existingChildIndex + 1);
        }

        long newIndex = nextIndex;
        cache.addNode(nodeIndex, moveIndex, newIndex, childMoveCount);

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
        return cache.getVisitCount(nodeIndex);
    }


    @Override
    public KnownOutcome getKnownOutcome(long nodeIndex) {
        loadIfMissing(nodeIndex);
        return cache.getKnownOutcome(nodeIndex);
    }


    @Override
    public long getChildIndex(long nodeIndex, int moveIndex) {
        loadIfMissing(nodeIndex);
        return cache.getChildIndex(nodeIndex, moveIndex);
    }


    @Override
    public double getValueSum(long nodeIndex) {
        loadIfMissing(nodeIndex);
        return cache.getValueSum(nodeIndex);
    }


    @Override
    public double getAverageValue(long nodeIndex, double defaultValue) {
        loadIfMissing(nodeIndex);
        return cache.getAverageValue(nodeIndex, defaultValue);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void loadIfMissing(long nodeIndex) {
        if (! cache.contains(nodeIndex)) {
            RolloutStoreNode node = persistent.load(nodeIndex);
            cache.store(node);
        }
    }


    public void flush() {
//        long[] nodeIndexes = cache.nodeIndexes().toLongArray();
//        Arrays.sort(nodeIndexes);

        LongIterator iterator = cache.nodeIndexes().iterator();
        while (iterator.hasNext()) {
            long nodeIndex = iterator.nextLong();
            RolloutStoreNode node = cache.load(nodeIndex);
            persistent.store(node);
        }
        cache.clear();
    }


    @Override
    public void close() throws Exception {
        flush();
    }
}
