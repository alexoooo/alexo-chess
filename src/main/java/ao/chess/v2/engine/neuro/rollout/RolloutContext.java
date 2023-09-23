package ao.chess.v2.engine.neuro.rollout;

import ao.chess.v2.engine.neuro.puct.MoveAndOutcomeModelPool;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStore;
import ao.chess.v2.state.Move;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.LongAdder;


public class RolloutContext {
    public final int index;
    public final int threads;
    public final boolean optimize;

    public final MoveAndOutcomeModelPool pool;
    public final RolloutStore store;
    public final int rolloutLength;
    public final boolean binerize;
    public final double certaintyLimit;
    public final double probabilityPower;
    public final double eGreedyProbability;
    public final RolloutSolutionThreshold rolloutSolutionThreshold;

    public final Random random = new Random();
    public final List<RolloutNode> path = new ArrayList<>();
    public final LongSet pathHashes = new LongOpenHashSet();
    public final LongSet pathHashesAlt = new LongOpenHashSet();

    public final int[] movesA = new int[Move.MAX_PER_PLY];
    public final int[] movesB = new int[Move.MAX_PER_PLY];
    public final int[] movesC = new int[Move.MAX_PER_PLY];
    public final double[] valueSums = new double[Move.MAX_PER_PLY];
    public final double[] valueSquareSums = new double[Move.MAX_PER_PLY];
    public final long[] visitCounts = new long[Move.MAX_PER_PLY];
    public final long[] childIndexes = new long[Move.MAX_PER_PLY];
    public final long[] history = new long[4096];

    public final LongAdder collisions;
    public final LongAdder terminalHits;
    public final LongAdder terminalRolloutHits;
    public final LongAdder tablebaseHits;
    public final LongAdder tablebaseRolloutHits;
    public final LongAdder solutionHits;
    public final LongAdder repetitionHits;
    public final LongAdder transpositionHits;
    public final LongAdder eGreedyHits;

    private final LongAdder trajectoryCount;
    private final LongAdder trajectoryLengthSum;

    public RolloutContext(
            int index,
            int threads,
            int rolloutLength,
            boolean optimize,
            MoveAndOutcomeModelPool pool,
            RolloutStore store,

            boolean binerize,
            double certaintyLimit,
            double probabilityPower,
            double eGreedyProbability,
            RolloutSolutionThreshold rolloutSolutionThreshold,

            LongAdder collisions,
            LongAdder terminalHits,
            LongAdder terminalRolloutHits,
            LongAdder tablebaseHits,
            LongAdder tablebaseRolloutHits,
            LongAdder solutionHits,
            LongAdder repetitionHits,
            LongAdder transpositionHits,
            LongAdder eGreedyHits,

            LongAdder trajectoryCount,
            LongAdder trajectoryLengthSum)
    {
        this.index = index;
        this.threads = threads;
        this.optimize = optimize;

        this.pool = pool;
        this.store = store;
        this.rolloutLength = rolloutLength;
        this.binerize = binerize;
        this.certaintyLimit = certaintyLimit;
        this.probabilityPower = probabilityPower;
        this.eGreedyProbability = eGreedyProbability;
        this.rolloutSolutionThreshold = rolloutSolutionThreshold;

        this.collisions = collisions;
        this.terminalHits = terminalHits;
        this.terminalRolloutHits = terminalRolloutHits;
        this.tablebaseHits = tablebaseHits;
        this.tablebaseRolloutHits = tablebaseRolloutHits;
        this.solutionHits = solutionHits;
        this.repetitionHits = repetitionHits;
        this.transpositionHits = transpositionHits;
        this.eGreedyHits = eGreedyHits;

        this.trajectoryCount = trajectoryCount;
        this.trajectoryLengthSum = trajectoryLengthSum;
    }


    public double currentAverageSearchDepth() {
        long currentTrajectoryCountValue = trajectoryCount.longValue();
        return currentTrajectoryCountValue == 0
                ? 0
                : (double) trajectoryLengthSum.longValue() / currentTrajectoryCountValue;
    }
}
