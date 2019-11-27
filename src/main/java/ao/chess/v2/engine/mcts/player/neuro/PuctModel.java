package ao.chess.v2.engine.mcts.player.neuro;

import ao.chess.v2.state.State;


public interface PuctModel {
    PuctModel prototype();

    void load();

    PuctEstimate estimate(State state, int[] legalMoves);
}
