package ao.chess.v2.engine.mcts.player;

import ao.chess.v2.engine.Player;


public interface ScoredPlayer extends Player {
    double expectedValue();
    double moveScoreInternal(int move);
}
