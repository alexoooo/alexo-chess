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

        int[] legalMoves = state.legalMoves();
        node = new ParallelNode(legalMoves);
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


    public double inverseScore()
    {
        double value = node.visitCount() == 0
                ? 0.5
                : node.valueSum() / node.visitCount();

        return 1.0 - value;
    }


    @Override
    public String toString() {
        return node.moveStats();
    }
}
