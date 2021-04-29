package ao.chess.v2.engine.neuro.rollout;

import ao.chess.v1.util.Io;
import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.endgame.v2.EfficientDeepOracle;
import ao.chess.v2.engine.neuro.puct.PuctModel;
import ao.chess.v2.engine.neuro.puct.PuctModelPool;
import ao.chess.v2.engine.neuro.rollout.store.KnownOutcome;
import ao.chess.v2.engine.neuro.rollout.store.MapRolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.SynchronizedRolloutStore;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.math.rand.Rand;
import ao.util.time.Sched;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.LongAdder;


public class RolloutPlayer
        implements Player
{
    //-----------------------------------------------------------------------------------------------------------------
    private final static int initDelayMillis = 1;
//    private final static int initDelayMillis = 11;
//    private final static int initDelayMillis = 110;

//    private final static int reportPeriod = 0;
//    private final static int reportPeriod = 1_000;
//    private final static int reportPeriod = 5_000;
    private final static int reportPeriod = 30_000;
    private final static boolean tablebase = true;

////    private final static double explorationMin = 0.2;
////    private final static double explorationMin = 0.5;
////    private final static double explorationMin = 0.75;
//    private final static double explorationMin = 1.0;
////    private final static double explorationVariance = 1.0;
////    private final static double explorationVariance = 1.5;
//    private final static double explorationVariance = 1.75;
////    private final static double explorationVariance = 2.0;

//    private final static double probabilityPowerMin = 0.75;
//    private final static double probabilityPowerMin = 1.0;
//    private final static double probabilityPowerMin = 1.25;
    private final static double probabilityPowerMin = 2.0;
//    private final static double probabilityPowerMax = 1.0;
//    private final static double probabilityPowerMax = 2.0;
//    private final static double probabilityPowerMax = 2.5;
    private final static double probabilityPowerMax = 3.0;


    private final static int progressThreadIndex = 0;


    //-----------------------------------------------------------------------------------------------------------------
    public static class Builder
    {
        private final PuctModel model;
//        private RolloutStore store = new SynchronizedRolloutStore(new BigArrayRolloutStore());
        private RolloutStore store = new SynchronizedRolloutStore(new MapRolloutStore());
        private int threads = 1;
        private int rolloutLength = 0;
//        private int rolloutLength = 1;
        private int minimumTrajectories = 0;
        private boolean optimize = false;
        private boolean binerize = false;
        private boolean useIo = false;


        public Builder(PuctModel model) {
            this.model = model;
        }


        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder rolloutLength(int rolloutLength) {
            this.rolloutLength = rolloutLength;
            return this;
        }

        public Builder useIo(boolean useIo) {
            this.useIo = useIo;
            return this;
        }

        public Builder minimumTrajectories(int minimumTrajectories) {
            this.minimumTrajectories = minimumTrajectories;
            return this;
        }

        public Builder store(RolloutStore store) {
            this.store = store;
            return this;
        }

        public Builder optimize(boolean optimize) {
            this.optimize = optimize;
            return this;
        }


        // see: http://game.c.u-tokyo.ac.jp/ja/wp-content/uploads/2015/08/ieeecig2015.pdf
        public Builder binerize(boolean binerize) {
            this.binerize = binerize;
            return this;
        }


        public RolloutPlayer build()
        {
            return new RolloutPlayer(
                    model,
                    store,
                    threads,
                    rolloutLength,
                    minimumTrajectories,
                    binerize,
                    optimize,
                    useIo);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final String id;
    private final RolloutStore store;
    private final PuctModel model;
    private final PuctModelPool pool;
    private final int threads;
    private final int rolloutLength;
    private final int minimumTrajectories;
    private final boolean useIo;
    private final boolean binerize;
    private final boolean optimize;
    private boolean train;

    private final LongAdder collisions = new LongAdder();
    private final LongAdder terminalHits = new LongAdder();
    private final LongAdder terminalRolloutHits = new LongAdder();
    private final LongAdder tablebaseHits = new LongAdder();
    private final LongAdder tablebaseRolloutHits = new LongAdder();
    private final LongAdder solutionHits = new LongAdder();
    private final LongAdder transpositionHits = new LongAdder();
    private final LongAdder trajectoryCount = new LongAdder();
    private final LongAdder trajectoryLengthSum = new LongAdder();
    private final Random random = new Random();

    private final CopyOnWriteArrayList<RolloutContext> contexts;
    private ExecutorService executorService;

    private long previousPositionHash = -1;
    private RolloutNode previousRoot;

    private final long[] history;
    private int historyIndex;

    private State prevState = null;
    private RolloutNode prevPlay  = null;


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutPlayer(
            PuctModel model,
            RolloutStore store,
            int threads,
            int rolloutLength,
            int minumumTrajectories,
            boolean binerize,
            boolean optimize,
            boolean useIo)
    {
        this.model = model;
        this.store = store;
        this.threads = threads;
        this.rolloutLength = rolloutLength;
        this.minimumTrajectories = minumumTrajectories;
        this.binerize = binerize;
        this.optimize = optimize;
        this.useIo = useIo;

        pool = new PuctModelPool(
                threads, model);

        contexts = new CopyOnWriteArrayList<>();

        id = Integer.toHexString((int) (random.nextDouble() * 1024));

        history = new long[4096];
        historyIndex = -1;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void setTrain(boolean train)
    {
        this.train = train;
    }


    public boolean getTrain()
    {
        return train;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public int move(
            State position,
            int timeLeft,
            int timePerMove,
            int timeIncrement
    ) {
        initIfRequired();

        if (position.knownOutcomeOrNull() != null) {
            prevPlay = null;
            prevState = null;
            return -1;
        }

        int oracleAction = oracleAction(position);
        if (oracleAction != -1) {
            prevState = null;
            prevPlay = null;

            // NB: some kind of UI race condition?
            Sched.sleep(100);

            return oracleAction;
        }

        pool.restart(position.pieceCount());

        long positionHash = position.staticHashCode();
        RolloutNode root = getOrCreateRoot(position, positionHash);

        int[] legalMoves = position.legalMoves();
        if (prevState != null && prevPlay != null) {
            int move = Move.findMove(prevState, position);
            if (move != -1) {
                int index = Ints.indexOf(prevState.legalMoves(), move);
                if (index != -1) {
                    RolloutNode child = prevPlay.childOrNull(index, store);
                    if (child != null) {
                        log(id + " - restored: " + child.visitCount(store));
                        root = child;
                    }
                }
            }
        }

        if (legalMoves == null || legalMoves.length == 0) {
            prevPlay = null;
            prevState = null;
            return -1;
        }

        boolean isRepeat = isHistoryRepeat(position, positionHash);

        long episodeMillis = Math.min(reportPeriod, timePerMove);
        long deadline = System.currentTimeMillis() + timePerMove;

        display("Submitting threads: " + threads);

        List<Future<?>> futures = new ArrayList<>();
        for (int thread = 0; thread < threads; thread++) {
            boolean progressThread = thread == progressThreadIndex;
            RolloutContext context = contexts.get(thread);
            RolloutNode currentRoot = root;
            Future<?> future = executorService.submit(() -> {
                State original = State.fromFen(position.toFen());

                do {
                    thinkingEpisode(
                            currentRoot, context, original, isRepeat, episodeMillis, progressThread);
                }
                while ((currentRoot.visitCount(store) < minimumTrajectories ||
                        System.currentTimeMillis() <= deadline) &&
                        ! currentRoot.isValueKnown(store));
            });

            futures.add(future);

            try {
                // NB: avoid pile-up of collisions
                Thread.sleep(initDelayMillis);
            }
            catch (InterruptedException e) {
                break;
            }
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        int bestMove = root.bestMove(
                position, isRepeat, history, historyIndex + 1, store);

        int bestMoveIndex = Ints.indexOf(legalMoves, bestMove);
        prevPlay = root.childOrNull(bestMoveIndex, store);
        prevState = position.prototype();
        Move.apply(bestMove, prevState);
        addHistoryMoveIfAbsent(prevState);

        log(id + " - PV: " + root.principalVariation(bestMove, position, store));
        flush();
        return bestMove;
    }


    private void thinkingEpisode(
            RolloutNode root,
            RolloutContext context,
            State state,
            boolean isRepeat,
            long episodeMillis,
            boolean progressThread
    ) {
//        display("Thinking episode: " + context.index + " - " + episodeMillis);

        try {
            long episodeDeadline = System.currentTimeMillis() + episodeMillis;
            do {
                int length = root.runTrajectory(
                        state.prototype(), context);

                if (length == 0) {
                    break;
                }

                trajectoryCount.increment();
                trajectoryLengthSum.add(length - 1);
            }
            while (System.currentTimeMillis() < episodeDeadline &&
                    ! root.isValueKnown(store));
        }
        catch (Throwable t) {
            t.printStackTrace();
            log("ERROR! " + t.getMessage());
            throw t;
        }

        if (progressThread) {
//            State original = state.prototype();
            reportProgress(root, state, isRepeat);
//            if (! original.equals(state)) {
//                System.out.println(original.equals(state));
//            }
        }
    }


    private void initIfRequired() {
//        nnCache.clear();
//        cacheHits.reset();
//        collisions.reset();

        if (! contexts.isEmpty()) {
            return;
        }

        executorService = Executors.newFixedThreadPool(threads);

        for (int i = 0; i < threads; i++) {
//            PuctModel modelProto = model.prototype();
//            modelProto.load();

//            double exploration = explorationMin + Math.abs(random.nextGaussian() * explorationVariance);
            double probabilityPower =
                    probabilityPowerMin + (probabilityPowerMax - probabilityPowerMin) * random.nextDouble();

            contexts.add(new RolloutContext(
                    i,
                    threads,
                    rolloutLength,
                    optimize,
                    pool,
                    store,
                    binerize,
                    probabilityPower,
                    collisions,
                    terminalHits,
                    terminalRolloutHits,
                    tablebaseHits,
                    tablebaseRolloutHits,
                    solutionHits,
                    transpositionHits));
        }

        MovePicker.init();
        if (tablebase) {
//            DeepOracle.init();
            EfficientDeepOracle.init();
        }
    }


    private boolean isHistoryRepeat(State state, long positionHash) {
        if (State.isInitial(state)) {
            historyIndex = 0;
            history[historyIndex] = positionHash;
            return false;
        }

        for (int i = 0; i < historyIndex; i++) {
            if (history[i] == positionHash) {
                return true;
            }
        }

        if (historyIndex != -1 && history[historyIndex] == positionHash) {
            // NB: repeated call to play the same move
            return false;
        }

        historyIndex++;
        history[historyIndex] = positionHash;

        return false;
    }


    private void addHistoryMoveIfAbsent(State moveState) {
        long positionHash = moveState.staticHashCode();
        for (int i = 0; i <= historyIndex; i++) {
            if (history[i] == positionHash) {
                return;
            }
        }

        historyIndex++;
        history[historyIndex] = positionHash;
    }


    private RolloutNode getOrCreateRoot(State state, long positionHash) {
        if (previousRoot != null && previousPositionHash == positionHash) {
//            display("Retrieved root" + previousRoot);
            return previousRoot;
        }

        int[] legalMoves = state.legalMoves();
//        PuctEstimate estimate = contexts.get(0).pool.estimateRoot(state, legalMoves);
//        int moveCount = estimate.moveProbabilities.length;
        int moveCount = legalMoves.length;

        boolean empty = store.initRootIfRequired(moveCount);

        RolloutNode root = new RolloutNode(RolloutStore.rootIndex);
        root.initRoot(store);

        previousRoot = root;
        previousPositionHash = positionHash;

        if (! empty) {
            log(id + " > restored root: " + store.nextIndex());
            reportProgress(root, state, false);
        }

//        display("Created root: " + root);
        return root;
    }


    private void reportProgress(
            RolloutNode root,
            State state,
            boolean isRepeat
    ) {
        int bestMove = root.bestMove(state, isRepeat, history, historyIndex, store);

        long trajectoryCountValue = trajectoryCount.longValue();
        double averageSearchDepth =
                trajectoryCountValue == 0
                ? 0
                : (double) trajectoryLengthSum.longValue() / trajectoryCountValue;

        String generalPrefix = String.format(
                "%s - %s %d | c%d cL%d x%d t%d tR%d e%d eR%d s%d T%d n%d l%.2f | %s",
                id,
                model,
                threads,
                pool.cacheHits(),
                pool.localCacheHits(),
                collisions.longValue(),
                terminalHits.longValue(),
                terminalRolloutHits.longValue(),
                tablebaseHits.longValue(),
                tablebaseRolloutHits.longValue(),
                solutionHits.longValue(),
                transpositionHits.longValue(),
                trajectoryCountValue,
                averageSearchDepth,
                Move.toString(bestMove));

        String moveSuffix =
                root.toString(state, store);

        log(generalPrefix + " | " + moveSuffix);

        if (bestMove != -1) {
            log(id + " - PV: " + root.principalVariation(bestMove, state, store));
            log(id + " - UCB: " + root.ucbVariation(state, /*store,*/ contexts.get(progressThreadIndex)));
        }
    }


//    @Override
//    public double expectedValue() {
//        return previousRoot.rootValue();
//    }


    //-----------------------------------------------------------------------------------------------------------------
//    @Override
//    public double moveScoreInternal(int move) {
//        return previousRoot.moveValue(move);
//    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
            pool.close();
        }

        try {
            store.close();
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public void flush() {
        try {
            log(id + " - flushing store: " + LocalTime.now());
            Stopwatch stopwatch = Stopwatch.createStarted();
            long flushed = store.flush();
            log(id + " - flushed " + flushed + " - took: " + stopwatch);
        }
        catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    @Override
    public boolean isSolved(State position) {
        return store.getKnownOutcome(RolloutStore.rootIndex) != KnownOutcome.Unknown;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int oracleAction(State from) {
        if (from.pieceCount() > EfficientDeepOracle.pieceCount) {
            return -1;
        }

        boolean canDraw     = false;
        int     bestOutcome = 0;
        int     bestMove    = -1;
        for (int legalMove : from.legalMoves()) {
            Move.apply(legalMove, from);
            DeepOutcome outcome = EfficientDeepOracle.getOrNull(from);
            Move.unApply(legalMove, from);
            if (outcome == null || outcome.isDraw()) {
                canDraw = true;
                continue;
            }

            if (outcome.outcome().winner() == from.nextToAct()) {
                if (bestOutcome <= 0 ||
                        bestOutcome > outcome.plyDistance() ||
                        (bestOutcome == outcome.plyDistance() &&
                                Rand.nextBoolean())) {
                    bestOutcome = outcome.plyDistance();
                    bestMove    = legalMove;
                }
            }
            else if (! canDraw && bestOutcome <= 0
                    && bestOutcome > -outcome.plyDistance()) {
                bestOutcome = -outcome.plyDistance();
                bestMove    = legalMove;
            }
        }

        if (bestOutcome <= 0 && canDraw) {
            return -1;
        }

        return bestMove;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void log(String message)
    {
        System.out.println(message);
        display(message);
    }


    private void display(String message)
    {
        if (useIo) {
            Io.display(message);
        }
    }
}
