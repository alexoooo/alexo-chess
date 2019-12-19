package ao.chess.v1.ai;

import ao.chess.v1.model.Board;

/**
 *
 */
public interface Bot
{
    int act(Board board);

    default void init() {}
}
