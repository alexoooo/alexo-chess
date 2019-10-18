package ao.chess.v2.engine.mcts.player;


import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.MctsScheduler;
import ao.chess.v2.state.State;


public interface BanditPlayer extends Player {
    BanditPlayer prototype();

    void clearInternal();

    void notifyMoveInternal(State position, int action);

    double moveScoreInternal(
            BanditNode node,
            int move);

    BanditNode moveInternal(
            State position,
            MctsScheduler scheduler);
}
