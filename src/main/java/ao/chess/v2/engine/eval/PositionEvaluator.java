package ao.chess.v2.engine.eval;


import ao.chess.v2.engine.neuro.rollout.RolloutContext;
import ao.chess.v2.state.State;


public interface PositionEvaluator
        extends AutoCloseable
{
    double evaluate(int topLevelMoveCount, State state, RolloutContext context);


    @Override
    void close();
}
