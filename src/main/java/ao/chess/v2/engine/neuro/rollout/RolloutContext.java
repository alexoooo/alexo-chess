package ao.chess.v2.engine.neuro.rollout;

import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import ao.chess.v2.engine.neuro.puct.PuctModelPool;
import ao.chess.v2.state.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.LongAdder;


class RolloutContext {
    public final int index;
    public final int threads;

//    public final PuctModel model;
    public final PuctModelPool pool;

    public final Random random = new Random();
    public final List<RolloutNode> path = new ArrayList<>();

    public final int[] movesA = new int[Move.MAX_PER_PLY];
    public final int[] movesB = new int[Move.MAX_PER_PLY];
    public final int[] movesC = new int[Move.MAX_PER_PLY];
    public final double[] valueSums = new double[Move.MAX_PER_PLY];
    public final long[] visitCounts = new long[Move.MAX_PER_PLY];
//    public final double[] probabilityBuffer = new double[Move.MAX_PER_PLY];


//    public final double exploration;
//    public final double explorationLog;
//    public final double firstPlayDiscount;

//    public final boolean randomize;
//    public final boolean tablebase;
//    public final double predictionUncertainty;

    public double estimatedValue;


//    public final ConcurrentHashMap<Long, PuctEstimate> nnCache;
//    public final LongAdder cacheHits;
    public final LongAdder collisions;
    public final LongAdder terminalHits;
    public final LongAdder tablebaseHits;
    public final LongAdder tablebaseRolloutHits;
    public final LongAdder solutionHits;


    public RolloutContext(
            int index,
            int threads,
            PuctModelPool pool,
//            double exploration,
//            double explorationLog,
//            double firstPlayDiscount,
//            boolean randomize,
//            boolean tablebase,
////            double predictionUncertainty,
//            ConcurrentHashMap<Long, PuctEstimate> nnCache,
//            LongAdder cacheHits,
            LongAdder collisions,
            LongAdder terminalHits,
            LongAdder tablebaseHits,
            LongAdder tablebaseRolloutHits,
            LongAdder solutionHits)
    {
        this.index = index;
        this.threads = threads;

//        this.model = model;
        this.pool = pool;

//        this.exploration = exploration;
//        this.explorationLog = explorationLog;
//        this.randomize = randomize;
//        this.tablebase = tablebase;
////        this.predictionUncertainty = predictionUncertainty;
//        this.firstPlayDiscount = firstPlayDiscount;
//        this.nnCache = nnCache;
//        this.cacheHits = cacheHits;
        this.collisions = collisions;
        this.terminalHits = terminalHits;
        this.tablebaseHits = tablebaseHits;
        this.tablebaseRolloutHits = tablebaseRolloutHits;
        this.solutionHits = solutionHits;
    }
}
