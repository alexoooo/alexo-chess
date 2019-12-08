package ao.chess.v2.engine.mcts.player.neuro;

import ao.chess.v2.state.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;


class PuctContext {
    public final PuctModel model;

    public final Random random = new Random();
    public final List<PuctNode> path = new ArrayList<>();

    public final int[] movesA = new int[Move.MAX_PER_PLY];
    public final int[] movesB = new int[Move.MAX_PER_PLY];
    public final double[] valueSums = new double[Move.MAX_PER_PLY];
    public final long[] visitCounts = new long[Move.MAX_PER_PLY];
//    public final double[] probabilityBuffer = new double[Move.MAX_PER_PLY];


    public final double exploration;
    public final double explorationLog;
    public final double firstPlayDiscount;

    public final boolean randomize;
    public final int rollouts;
    public final boolean tablebase;
    public final double predictionUncertainty;

    public double estimatedValue;


    public final ConcurrentHashMap<Long, PuctEstimate> nnCache;
    public final LongAdder cacheHits;


    public PuctContext(
            PuctModel model,
            double exploration,
            double explorationLog,
            double firstPlayDiscount,
            boolean randomize,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            ConcurrentHashMap<Long, PuctEstimate> nnCache,
            LongAdder cacheHits)
    {
        this.model = model;
        this.exploration = exploration;
        this.explorationLog = explorationLog;
        this.randomize = randomize;
        this.rollouts = rollouts;
        this.tablebase = tablebase;
        this.predictionUncertainty = predictionUncertainty;
        this.firstPlayDiscount = firstPlayDiscount;
        this.nnCache = nnCache;
        this.cacheHits = cacheHits;
    }
}
