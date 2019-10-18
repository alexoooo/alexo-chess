package ao.chess.v2.engine.mcts.player.par;


import ao.chess.v2.state.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class ParallelContext {
    public final Random random = new Random();
    public final List<ParallelNode> path = new ArrayList<>();

    public final int[] movesA = new int[Move.MAX_PER_PLY];
    public final int[] movesB = new int[Move.MAX_PER_PLY];
    public final double[] valueSums = new double[Move.MAX_PER_PLY];
    public final long[] visitCounts = new long[Move.MAX_PER_PLY];


    public final double exploration;
    public final int rollouts;
    public final boolean material;


    public ParallelContext(
            double exploration,
            int rollouts,
            boolean material)
    {
        this.exploration = exploration;
        this.rollouts = rollouts;
        this.material = material;
    }
}
