package ao.chess.v2.engine.neuro.puct;

import ao.chess.v1.util.Io;
import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.mcts.player.ScoredPlayer;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;


public class PuctPlayer
        implements ScoredPlayer
{
    //-----------------------------------------------------------------------------------------------------------------
//    private final static int reportPeriod = 0;
//    private final static int reportPeriod = 1_000;
    private final static int reportPeriod = 5_000;


    //-----------------------------------------------------------------------------------------------------------------
    public static class Builder
    {
        private PuctModel model;
        private int threads = 1;
        private double exploration = 1.25;
        private double explorationLog = 18432;
        private boolean randomize = true;
        private boolean tablebase = true;
        private int minumumTrajectories = 0;
        private double alpha = 0.3;
        private double signal = 0.75;
        private boolean stochastic = false;
        private boolean train = false;
        private boolean useIo = false;


        public Builder(PuctModel model) {
            this.model = model;
        }


        public Builder threads(int threads) {
            this.threads = threads;
            return this;
        }

        public Builder useIo(boolean useIo) {
            this.useIo = useIo;
            return this;
        }

        public Builder stochastic(boolean stochastic) {
            this.stochastic = stochastic;
            return this;
        }

        public Builder train(boolean train) {
            this.train = train;
            return this;
        }


        public PuctPlayer build()
        {
            return new PuctPlayer(
                    model,
                    threads,
                    exploration,
                    explorationLog,
                    randomize,
                    tablebase,
                    minumumTrajectories,
                    stochastic,
                    alpha,
                    signal,
                    true,
                    useIo);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final String id;
    private final PuctModel model;
    private final PuctModelPool pool;
    private final int threads;
    private final double exploration;
    private final double explorationLog;
    private final boolean randomize;
    private final boolean tablebase;
    private final double alpha;
    private final double signal;
    private final int minimumTrajectories;
    private final boolean stochastic;
    private final boolean useIo;
    private boolean train;

    private final ConcurrentHashMap<Long, PuctEstimate> nnCache = new ConcurrentHashMap<>();
    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder collisions = new LongAdder();
    private final LongAdder terminalHits = new LongAdder();
    private final LongAdder tablebaseHits = new LongAdder();
    private final LongAdder solutionHits = new LongAdder();

    private final CopyOnWriteArrayList<PuctContext> contexts;
    private ExecutorService executorService;

    private long previousPositionHash = -1;
    private PuctNode previousRoot;

    private final long[] history;
    private int historyIndex;

    private State prevState = null;
    private PuctNode prevPlay  = null;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctPlayer(
            PuctModel model,
            int threads,
            double exploration,
            double explorationLog,
            boolean randomize,
            boolean tablebase,
            int minumumTrajectories,
            boolean stochastic,
            double alpha,
            double signal,
            boolean train,
            boolean useIo)
    {
        this.model = model;
        this.threads = threads;
        this.exploration = exploration;
        this.explorationLog = explorationLog;
        this.randomize = randomize;
        this.tablebase = tablebase;
        this.minimumTrajectories = minumumTrajectories;
        this.stochastic = stochastic;
        this.alpha = alpha;
        this.signal = signal;
        this.train = train;
        this.useIo = useIo;

        pool = new PuctModelPool(
                threads, model,
                PuctNode.guessRange, PuctNode.minimumGuess);

        contexts = new CopyOnWriteArrayList<>();

        id = Integer.toHexString((int) (Math.random() * 1024));

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

        if (position.knownOutcome() != null) {
            prevPlay = null;
            prevState = null;
            return -1;
        }

        pool.restart(position.pieceCount());

        long positionHash = position.staticHashCode();
        PuctNode root = getOrCreateRoot(position, positionHash);

        if (prevState != null && prevPlay != null) {
            int move = Move.findMove(prevState, position);
            if (move != -1) {
                int index = prevPlay.moveIndex(move);
                if (index != -1) {
                    PuctNode child = prevPlay.childOrNull(index);
                    if (child != null) {
                        log(id + " - restored: " + child.visitCount());
                        root = child;
                    }
                }
            }
        }

        if (root.legalMoves() == null ||
                root.legalMoves().length == 0) {
            prevPlay = null;
            prevState = null;
            return -1;
        }

        boolean isRepeat = isHistoryRepeat(position, positionHash);

        long episodeMillis = Math.min(reportPeriod, timePerMove);
        long deadline = System.currentTimeMillis() + timePerMove;

        if (threads == 1) {
            PuctContext context = contexts.get(0);
            do {
                thinkingEpisode(
                        root, context, position, isRepeat, episodeMillis, true);
            }
            while ((root.visitCount() < minimumTrajectories ||
                    System.currentTimeMillis() <= deadline) &&
                    ! root.isValueKnown());
        }
        else {
            display("Submitting threads: " + threads);

            List<Future<?>> futures = new ArrayList<>();
            for (int thread = 0; thread < threads; thread++) {
                boolean progressThread = thread == 0;
                PuctContext context = contexts.get(thread);
                PuctNode currentRoot = root;
                Future<?> future = executorService.submit(() -> {
                    do {
                        thinkingEpisode(
                                currentRoot, context, position, isRepeat, episodeMillis, progressThread);
                    }
                    while ((currentRoot.visitCount() < minimumTrajectories ||
                            System.currentTimeMillis() <= deadline) &&
                            ! currentRoot.isValueKnown());
                });
                futures.add(future);
            }

            for (Future<?> future : futures) {
                try {
                    future.get();
                }
                catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        int bestMove = root.bestMove(
                position, stochastic, isRepeat, history, historyIndex + 1);

        int bestMoveIndex = root.moveIndex(bestMove);
        prevPlay = root.childOrNull(bestMoveIndex);
        prevState = position.prototype();
        Move.apply(bestMove, prevState);
        addHistoryMoveIfAbsent(prevState);

        return bestMove;
    }


    private void thinkingEpisode(
            PuctNode root,
            PuctContext context,
            State state,
            boolean isRepeat,
            long episodeMillis,
            boolean progressThread
    ) {
//        display("Thinking episode: " + context.index + " - " + episodeMillis);

        try {
            long episodeDeadline = System.currentTimeMillis() + episodeMillis;
            do {
                root.runTrajectory(
                        state.prototype(), context);
            }
            while (System.currentTimeMillis() < episodeDeadline &&
                    ! root.isValueKnown());
        }
        catch (Throwable t) {
            log("ERROR! " + t.getMessage());
            throw t;
        }

        if (progressThread) {
            reportProgress(root, state, isRepeat);
        }
    }


    private void initIfRequired() {
        nnCache.clear();
        cacheHits.reset();
        collisions.reset();

        if (! contexts.isEmpty()) {
            return;
        }

        if (threads != 1) {
            executorService = Executors.newFixedThreadPool(threads);
        }

        for (int i = 0; i < threads; i++) {
//            PuctModel modelProto = model.prototype();
//            modelProto.load();

            contexts.add(new PuctContext(
                    i,
                    threads,
//                    modelProto,
                    pool,
                    exploration,
                    explorationLog,
                    0.2,
                    randomize,
                    tablebase,
//                    predictionUncertainty,
                    nnCache,
                    cacheHits,
                    collisions,
                    terminalHits,
                    tablebaseHits,
                    solutionHits));
        }

        MovePicker.init();
        if (tablebase) {
            DeepOracle.init();
        }
    }


    private boolean isHistoryRepeat(State state, long positionHash) {
        if (State.isInitial(state)) {
            historyIndex = 0;
            history[historyIndex] = positionHash;
            return false;
        }

        for (int i = 0; i <= historyIndex; i++) {
            if (history[i] == positionHash) {
                return true;
            }
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


    private PuctNode getOrCreateRoot(State state, long positionHash) {
        if (previousRoot != null && previousPositionHash == positionHash) {
//            display("Retrieved root" + previousRoot);
            return previousRoot;
        }

        int[] legalMoves = state.legalMoves();

        PuctEstimate estimate = contexts.get(0).pool.estimateRoot(state, legalMoves);

        if (train) {
            double[] buffer = new double[estimate.moveProbabilities.length];
            PuctUtils.smearProbabilities(estimate.moveProbabilities, alpha, signal, new Random(), buffer);
        }
//        else {
//            PuctUtils.smearProbabilities(
//                    estimate.moveProbabilities, predictionUncertainty);
//        }

        PuctNode root = new PuctNode(legalMoves, estimate.moveProbabilities);
        root.initRoot();

        previousRoot = root;
        previousPositionHash = positionHash;

//        display("Created root: " + root);
        return root;
    }


    private void reportProgress(
            PuctNode root,
            State state,
            boolean isRepeat
    ) {
        int bestMove = root.bestMove(state, stochastic, isRepeat, history, historyIndex);

        String generalPrefix = String.format(
                "%s - %s | %d / %.2f / %b / %b | u%d / c%d / x%d / t%d / e%d / s%d | %s",
                id,
                model,
                threads,
                exploration,
                randomize,
                tablebase,
                nnCache.size(),
                cacheHits.longValue(),
                collisions.longValue(),
                terminalHits.longValue(),
                tablebaseHits.longValue(),
                solutionHits.longValue(),
                Move.toString(bestMove));

        String moveSuffix =
                root.toString(contexts.get(0));

        log(generalPrefix + " | " + moveSuffix);
    }


    @Override
    public double expectedValue() {
        return previousRoot.inverseValue();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double moveScoreInternal(int move) {
        return previousRoot.moveValue(move);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
            pool.close();
        }
    }


    private void log(String message)
    {
        System.out.println(message);
        display(message);
    }


    private void display(String message)
    {
        if (useIo)
        {
            Io.display(message);
        }
    }
}
