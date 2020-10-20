package ao.chess.v2.engine.neuro.rollout;

import ao.chess.v2.engine.neuro.puct.PuctModelPool;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStore;
import ao.chess.v2.state.Move;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;


class RolloutContext {
    public final int index;
    public final int threads;
    public final boolean optimize;

    public final PuctModelPool pool;
    public final RolloutStore store;
    public final int rolloutLength;
    public final boolean binerize;
    public final double probabilityPower;

    public final Random random = new Random();
    public final List<RolloutNode> path = new ArrayList<>();

    public final int[] movesA = new int[Move.MAX_PER_PLY];
    public final int[] movesB = new int[Move.MAX_PER_PLY];
    public final int[] movesC = new int[Move.MAX_PER_PLY];
    public final double[] valueSums = new double[Move.MAX_PER_PLY];
    public final double[] valueSquareSums = new double[Move.MAX_PER_PLY];
    public final long[] visitCounts = new long[Move.MAX_PER_PLY];
    public final long[] history = new long[4096];

//    public double estimatedValue;

    public final LongAdder collisions;
    public final LongAdder terminalHits;
    public final LongAdder tablebaseHits;
    public final LongAdder tablebaseRolloutHits;
    public final LongAdder solutionHits;
    public final LongAdder transpositionHits;


    public RolloutContext(
            int index,
            int threads,
            int rolloutLength,
            boolean optimize,
            PuctModelPool pool,
            RolloutStore store,

            boolean binerize,
            double probabilityPower,
            LongAdder collisions,
            LongAdder terminalHits,
            LongAdder tablebaseHits,
            LongAdder tablebaseRolloutHits,
            LongAdder solutionHits,
            LongAdder transpositionHits)
    {
        this.index = index;
        this.threads = threads;
        this.optimize = optimize;

        this.pool = pool;
        this.store = store;
        this.rolloutLength = rolloutLength;
        this.binerize = binerize;
        this.probabilityPower = probabilityPower;

        this.collisions = collisions;
        this.terminalHits = terminalHits;
        this.tablebaseHits = tablebaseHits;
        this.tablebaseRolloutHits = tablebaseRolloutHits;
        this.solutionHits = solutionHits;
        this.transpositionHits = transpositionHits;
    }
}
