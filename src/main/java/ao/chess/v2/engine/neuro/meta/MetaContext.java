package ao.chess.v2.engine.neuro.meta;

import ao.chess.v2.state.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;


class MetaContext {
    public final int index;
    public final int threads;

//    public final PuctModel model;
    public final MetaModelPool pool;

    public final Random random = new Random();
    public final List<MetaNode> path = new ArrayList<>();

    public final int[] movesA = new int[Move.MAX_PER_PLY];
    public final int[] movesB = new int[Move.MAX_PER_PLY];
    public final double[] valueSums = new double[Move.MAX_PER_PLY];
    public final long[] visitCounts = new long[Move.MAX_PER_PLY];
//    public final double[] probabilityBuffer = new double[Move.MAX_PER_PLY];


    public final double exploration;
    public final double explorationLog;
    public final double firstPlayDiscount;

    public final boolean randomize;
    public final boolean tablebase;
//    public final double predictionUncertainty;

    public double estimatedValue;
//    public double estimatedError;


    public final ConcurrentHashMap<Long, MetaEstimate> nnCache;
    public final LongAdder cacheHits;
    public final LongAdder collisions;
    public final LongAdder terminalHits;
    public final LongAdder tablebaseHits;


    public MetaContext(
            int index,
            int threads,
            MetaModelPool pool,
            double exploration,
            double explorationLog,
            double firstPlayDiscount,
            boolean randomize,
            boolean tablebase,
//            double predictionUncertainty,
            ConcurrentHashMap<Long, MetaEstimate> nnCache,
            LongAdder cacheHits,
            LongAdder collisions,
            LongAdder terminalHits,
            LongAdder tablebaseHits)
    {
        this.index = index;
        this.threads = threads;

//        this.model = model;
        this.pool = pool;

        this.exploration = exploration;
        this.explorationLog = explorationLog;
        this.randomize = randomize;
        this.tablebase = tablebase;
//        this.predictionUncertainty = predictionUncertainty;
        this.firstPlayDiscount = firstPlayDiscount;
        this.nnCache = nnCache;
        this.cacheHits = cacheHits;
        this.collisions = collisions;
        this.terminalHits = terminalHits;
        this.tablebaseHits = tablebaseHits;
    }
}
