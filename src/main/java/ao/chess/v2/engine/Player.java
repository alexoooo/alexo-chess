package ao.chess.v2.engine;

import ao.chess.v2.state.State;

/**
 * User: aostrovsky
 * Date: 14-Sep-2009
 * Time: 3:58:17 PM
 */
public interface Player extends AutoCloseable
{
    int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement);


    @Override
    default void close() {}

    default void flush() {}

    default boolean isSolved(State position) {
        return false;
    }

    default void showSolution(State position) {}
}
