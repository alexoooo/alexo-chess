package ao.chess.v2.engine.mcts.player.neuro;

import ao.chess.v2.state.Move;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;


class PuctContext {
    public final MultiLayerNetwork nn;

    public final Random random = new Random();
    public final List<PuctNode> path = new ArrayList<>();

    public final int[] movesA = new int[Move.MAX_PER_PLY];
    public final int[] movesB = new int[Move.MAX_PER_PLY];
    public final double[] valueSums = new double[Move.MAX_PER_PLY];
    public final long[] visitCounts = new long[Move.MAX_PER_PLY];
//    public final double[] probabilityBuffer = new double[Move.MAX_PER_PLY];


    public final double exploration;
    public final int rollouts;
    public final boolean tablebase;
    public final double moveUncertainty;


    public double estimatedValue;


    public PuctContext(
            MultiLayerNetwork nn,
            double exploration,
            int rollouts,
            boolean tablebase,
            double moveUncertainty)
    {
        this.nn = nn;
        this.exploration = exploration;
        this.rollouts = rollouts;
        this.tablebase = tablebase;
        this.moveUncertainty = moveUncertainty;
    }
}
