package ao.chess.v2.engine.mcts.rollout;


import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.state.State;


public class MaterialFallbackRollout implements MctsRollout {
    private final MctsRollout delegate;


    public MaterialFallbackRollout(MctsRollout delegate) {
        this.delegate = delegate;
    }


    @Override
    public double monteCarloPlayout(
            State fromState,
            MctsHeuristic heuristitc
    ) {
        double value = delegate.monteCarloPlayout(fromState, heuristitc);

        return value != 0.5
                ? value
                : MaterialEvaluation.evaluate(fromState);
    }
}
