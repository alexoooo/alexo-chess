package ao.chess.v2.engine.mcts.player.par;


import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.Arrays;


class ParallelRoot
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final int batchSize = 100;


    //-----------------------------------------------------------------------------------------------------------------
    private final State state;
    private final ParallelNode node;


    //-----------------------------------------------------------------------------------------------------------------
    public ParallelRoot(State state)
    {
        this.state = state;

        int[] legalMoves = new int[Move.MAX_PER_PLY];
        int moveCount = state.legalMoves(legalMoves);
        node = new ParallelNode(Arrays.copyOf(legalMoves, moveCount));
        node.initTrajectory();
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void think(ParallelContext context) {
        for (int i = 0; i < batchSize; i++) {
            node.runTrajectory(state.prototype(), context);
        }
    }


    public int bestMove() {
        return node.bestMove();
    }

    public ParallelNode node() {
        return node;
    }

    public void validate() {
        node.validate();
    }


    @Override
    public String toString() {
        return node.moveStats();
    }
}
