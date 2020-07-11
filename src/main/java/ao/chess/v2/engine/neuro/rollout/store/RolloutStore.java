package ao.chess.v2.engine.neuro.rollout.store;


public interface RolloutStore {
    void incrementCount(long nodeIndex);


    void addValue(long nodeIndex, double value);


    long childIndex(long nodeIndex, int moveIndex);


    long addChild(long nodeIndex, int moveIndex, int moveCount);
}
