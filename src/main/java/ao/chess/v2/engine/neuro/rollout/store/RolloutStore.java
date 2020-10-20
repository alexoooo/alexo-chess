package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;


public interface RolloutStore extends AutoCloseable {
    //-----------------------------------------------------------------------------------------------------------------
    long rootIndex = 0;


    //-----------------------------------------------------------------------------------------------------------------
    /**
     * @param moveCount number of legal moves at root state
     * @return true if the root state was initialized, otherwise false if it already existed
     */
    boolean initRootIfRequired(int moveCount);


    void incrementVisitCount(long nodeIndex);


    void addValue(long nodeIndex, double value);


    void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome);


    long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount);


    long nextIndex();


    //-----------------------------------------------------------------------------------------------------------------
    long getVisitCount(long nodeIndex);


    KnownOutcome getKnownOutcome(long nodeIndex);


    long getChildIndex(long nodeIndex, int moveIndex);


    double getValueSum(long nodeIndex);


    double getValueSquareSum(long nodeIndex);


    double getAverageValue(long nodeIndex, double defaultValue);


    //-----------------------------------------------------------------------------------------------------------------
    TranspositionInfo getTranspositionOrNull(long hashHigh, long hashLow);


    void setTransposition(long hashHigh, long hashLow, double valueSum, long visitCount);


    //-----------------------------------------------------------------------------------------------------------------
    /**
     * @return estimated number of nodes flushed (optional value)
     */
    default long flush() {
        return 0;
    }
}
