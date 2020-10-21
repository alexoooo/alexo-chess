package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;


public class SynchronizedRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private final RolloutStore delegate;


    //-----------------------------------------------------------------------------------------------------------------
    public SynchronizedRolloutStore(RolloutStore delegate) {
        this.delegate = delegate;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public synchronized boolean initRootIfRequired(int moveCount) {
        return delegate.initRootIfRequired(moveCount);
    }


    @Override
    public synchronized void incrementVisitCount(long nodeIndex) {
        delegate.incrementVisitCount(nodeIndex);
    }


    @Override
    public synchronized void addValue(long nodeIndex, double value) {
        delegate.addValue(nodeIndex, value);
    }


    @Override
    public synchronized void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome) {
        delegate.setKnownOutcome(nodeIndex, knownOutcome);
    }


    @Override
    public synchronized long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount) {
        return delegate.expandChildIfMissing(nodeIndex, moveIndex, childMoveCount);
    }


    @Override
    public synchronized long nextIndex() {
        return delegate.nextIndex();
    }


    @Override
    public synchronized long getVisitCount(long nodeIndex) {
        return delegate.getVisitCount(nodeIndex);
    }


    @Override
    public synchronized KnownOutcome getKnownOutcome(long nodeIndex) {
        return delegate.getKnownOutcome(nodeIndex);
    }


    @Override
    public synchronized long getChildIndex(long nodeIndex, int moveIndex) {
        return delegate.getChildIndex(nodeIndex, moveIndex);
    }


    @Override
    public synchronized double getValueSum(long nodeIndex) {
        return delegate.getValueSum(nodeIndex);
    }


    @Override
    public synchronized double getValueSquareSum(long nodeIndex) {
        return delegate.getValueSquareSum(nodeIndex);
    }


    @Override
    public synchronized double getAverageValue(long nodeIndex, double defaultValue) {
        return delegate.getAverageValue(nodeIndex, defaultValue);
    }

    @Override
    public synchronized TranspositionInfo getTranspositionOrNull(long hashHigh, long hashLow) {
        return delegate.getTranspositionOrNull(hashHigh, hashLow);
    }


    @Override
    public synchronized void setTransposition(
            long hashHigh, long hashLow, long nodeIndex, double valueSum, long visitCount) {
        delegate.setTransposition(hashHigh, hashLow, nodeIndex, valueSum, visitCount);
    }


    @Override
    public synchronized void close() throws Exception {
        delegate.close();
    }


    @Override
    public synchronized long flush() {
        return delegate.flush();
    }
}
