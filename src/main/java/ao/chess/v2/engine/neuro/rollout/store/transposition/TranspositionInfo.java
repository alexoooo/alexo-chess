package ao.chess.v2.engine.neuro.rollout.store.transposition;


public class TranspositionInfo {
    private final double valueSum;
    private final long visitCount;


    public TranspositionInfo(double valueSum, long visitCount) {
        this.valueSum = valueSum;
        this.visitCount = visitCount;
    }


    public double valueSum() {
        return valueSum;
    }

    public long visitCount() {
        return visitCount;
    }
}
