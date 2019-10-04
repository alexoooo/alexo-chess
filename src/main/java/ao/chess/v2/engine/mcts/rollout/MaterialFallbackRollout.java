package ao.chess.v2.engine.mcts.rollout;


import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.state.State;


public class MaterialFallbackRollout implements MctsRollout {
    private static final double epsilon = 0.000_001;

    private final MctsRollout delegate;


    public MaterialFallbackRollout(MctsRollout delegate) {
        this.delegate = delegate;
    }


    @Override
    public MctsRollout prototype() {
        return new MaterialFallbackRollout(
                delegate.prototype());
    }


    @Override
    public double monteCarloPlayout(
            State fromState,
            MctsHeuristic heuristitc
    ) {
        double value = delegate.monteCarloPlayout(fromState, heuristitc);

        return Math.abs(value - 0.5) < epsilon
                ? value
                : MaterialEvaluation.evaluate(fromState);
    }
}
