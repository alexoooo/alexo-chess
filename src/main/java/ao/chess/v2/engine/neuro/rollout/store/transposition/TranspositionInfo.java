package ao.chess.v2.engine.neuro.rollout.store.transposition;


public class TranspositionInfo {
    private final long nodeIndex;
    private final double valueSum;
    private final long visitCount;


    public TranspositionInfo(long nodeIndex, double valueSum, long visitCount) {
        this.nodeIndex = nodeIndex;
        this.valueSum = valueSum;
        this.visitCount = visitCount;
    }


    public long nodeIndex() {
        return nodeIndex;
    }


    public double valueSum() {
        return valueSum;
    }


    public long visitCount() {
        return visitCount;
    }
}
