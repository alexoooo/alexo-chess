package ao.chess.v2.engine.mcts.player;


import ao.chess.v2.engine.mcts.MctsScheduler;
import ao.chess.v2.state.State;


public interface BanditPlayer extends ScoredPlayer {
    BanditPlayer prototype();

    void clearInternal();

    void notifyMoveInternal(State position, int action);

    BanditNode moveInternal(
            State position,
            MctsScheduler scheduler);
}
