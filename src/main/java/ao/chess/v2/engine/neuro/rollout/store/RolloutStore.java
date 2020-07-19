package ao.chess.v2.engine.neuro.rollout.store;


public interface RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    long rootIndex = 0;


    //-----------------------------------------------------------------------------------------------------------------
    void initRoot(int moveCount);


    void incrementCount(long nodeIndex);


    void addValue(long nodeIndex, double value);


    void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome);


    long expandChildIfMissing(long nodeIndex, int moveIndex, int moveCount);


    //-----------------------------------------------------------------------------------------------------------------
    long getCount(long nodeIndex);


    KnownOutcome getKnownOutcome(long nodeIndex);


    long getChildIndex(long nodeIndex, int moveIndex);


    double getValueSum(long nodeIndex);


    double getAverageValue(long nodeIndex, double defaultValue);
}
