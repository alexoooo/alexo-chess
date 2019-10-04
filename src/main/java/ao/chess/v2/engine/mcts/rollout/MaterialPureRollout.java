package ao.chess.v2.engine.mcts.rollout;

import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.state.State;


public class MaterialPureRollout implements MctsRollout {
    @Override
    public MctsRollout prototype() {
        return this;
    }


    @Override
    public double monteCarloPlayout(
            State fromState,
            MctsHeuristic heuristitc
    ) {
        return MaterialEvaluation.evaluate(fromState);
    }
}
