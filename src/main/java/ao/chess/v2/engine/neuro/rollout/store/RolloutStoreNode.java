package ao.chess.v2.engine.neuro.rollout.store;


import java.util.Comparator;


public class RolloutStoreNode {
    //-----------------------------------------------------------------------------------------------------------------
    public static final Comparator<RolloutStoreNode> byIndex = Comparator.comparingLong(RolloutStoreNode::index);

    public static RolloutStoreNode emptyOfIndex(long index) {
        return new RolloutStoreNode(index, 0, 0, 0, KnownOutcome.Unknown, new long[0]);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final long index;
    private final long visitCount;
    private final double valueSum;
    private final double valueSquareSum;
    private final KnownOutcome knownOutcome;
    private final long[] childIndexes;


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutStoreNode(
            long index,
            long visitCount,
            double valueSum,
            double valueSquareSum,
            KnownOutcome knownOutcome,
            long[] childIndexes
    ) {
        this.index = index;
        this.visitCount = visitCount;
        this.valueSum = valueSum;
        this.valueSquareSum = valueSquareSum;
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


    public double valueSquareSum() {
        return valueSquareSum;
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
