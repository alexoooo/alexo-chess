package ao.chess.v2.engine.neuro.rollout.store;


public class RolloutStoreNode {
    //-----------------------------------------------------------------------------------------------------------------
    private final long index;
    private final long visitCount;
    private final double valueSum;
    private final KnownOutcome knownOutcome;
    private final long[] childIndexes;


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutStoreNode(
            long index,
            long visitCount,
            double valueSum,
            KnownOutcome knownOutcome,
            long[] childIndexes
    ) {
        this.index = index;
        this.visitCount = visitCount;
        this.valueSum = valueSum;
        this.knownOutcome = knownOutcome;
        this.childIndexes = childIndexes;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public long index() {
        return index;
    }


    public long visitCount() {
        return visitCount;
    }


    public double valueSum() {
        return valueSum;
    }


    public KnownOutcome knownOutcome() {
        return knownOutcome;
    }


    public int moveCount() {
        return childIndexes.length;
    }


    public long childIndex(int moveIndex) {
        return childIndexes[moveIndex];
    }
}
