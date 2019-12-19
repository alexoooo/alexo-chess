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
    private final static int reportPeriod = 1_000;
//    private final static int reportPeriod = 5_000;


    //-----------------------------------------------------------------------------------------------------------------
    private final String id;
    private final PuctModel model;
    private final PuctModelPool pool;
    private final int threads;
    private final double exploration;
    private final double explorationLog;
    private final boolean randomize;
    private final int rollouts;
    private final boolean tablebase;
    private final double predictionUncertainty;
    private final double alpha;
    private final double signal;
    private final int minimumTrajectories;
    private final boolean useIo;
    private boolean train;

    private final ConcurrentHashMap<Long, PuctEstimate> nnCache = new ConcurrentHashMap<>();
    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder collisions = new LongAdder();

    private final CopyOnWriteArrayList<PuctContext> contexts;
    private ExecutorService executorService;

    private long previousPositionHash = -1;
    private PuctNode previousRoot;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctPlayer(
            PuctModel model,
            int threads,
            int minimumTrajectories)
    {
        this(model,
                threads,
                1.5,
                18432,
                false,
                7,
                true,
                0.4,
                minimumTrajectories);
    }


    public PuctPlayer(
            PuctModel model,
            int threads,
            double exploration,
            double explorationLog,
            boolean randomize,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories)
    {
        this(model,
                threads,
                exploration,
                explorationLog,
                randomize,
                rollouts,
                tablebase,
                predictionUncertainty,
                minumumTrajectories,
                0.3, 0.75, false);
    }


    public PuctPlayer(
            PuctModel model,
            int threads,
            double exploration,
            double explorationLog,
            boolean randomize,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories,
            boolean useIo)
    {
        this(model,
                threads,
                exploration,
                explorationLog,
                randomize,
                rollouts,
                tablebase,
                predictionUncertainty,
                minumumTrajectories,
                0.3, 0.75, false, useIo);
    }


    public PuctPlayer(
            PuctModel model,
            int threads,
            double exploration,
            double explorationLog,
            boolean randomize,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories,
            double alpha,
            double signal)
    {
        this(model,
                threads,
                exploration,
                explorationLog,
                randomize,
                rollouts,
                tablebase,
                predictionUncertainty,
                minumumTrajectories,
                alpha,
                signal,
                true);
    }


    public PuctPlayer(
            PuctModel model,
            int threads,
            double exploration,
            double explorationLog,
            boolean randomize,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories,
            double alpha,
            double signal,
            boolean train)
    {
        this(model,
                threads,
                exploration,
                explorationLog,
                randomize,
                rollouts,
                tablebase,
                predictionUncertainty,
                minumumTrajectories,
                alpha,
                signal,
                train,
                false);
    }


    public PuctPlayer(
            PuctModel model,
            int threads,
            double exploration,
            double explorationLog,
            boolean randomize,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories,
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
        this.rollouts = rollouts;
        this.tablebase = tablebase;
        this.predictionUncertainty = predictionUncertainty;
        this.minimumTrajectories = minumumTrajectories;
        this.alpha = alpha;
        this.signal = signal;
        this.train = train;
        this.useIo = useIo;

        pool = new PuctModelPool(
                threads, model,
                predictionUncertainty, PuctNode.guessRange, PuctNode.minimumGuess);

        contexts = new CopyOnWriteArrayList<>();

        id = Integer.toHexString((int) (Math.random() * 1024));
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
            return -1;
        }

        pool.restart();

        PuctNode root = getOrCreateRoot(position);

        if (root.legalMoves().length == 0) {
            return -1;
        }

        long episodeMillis = Math.min(reportPeriod, timePerMove);
        long deadline = System.currentTimeMillis() + timePerMove;

        if (threads == 1) {
            PuctContext context = contexts.get(0);
            do {
                thinkingEpisode(root, context, position, episodeMillis, true);
            }
            while (root.visitCount() < minimumTrajectories ||
                    System.currentTimeMillis() < deadline);
        }
        else {
            display("Submitting threads: " + threads);

            List<Future<?>> futures = new ArrayList<>();
            for (int thread = 0; thread < threads; thread++) {
                boolean progressThread = thread == 0;
                PuctContext context = contexts.get(thread);
                Future<?> future = executorService.submit(() -> {
                    do {
                        thinkingEpisode(root, context, position, episodeMillis, progressThread);
                    }
                    while (root.visitCount() < minimumTrajectories ||
                            System.currentTimeMillis() < deadline);
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

        return root.bestMove(true);
    }


    private void thinkingEpisode(
            PuctNode root,
            PuctContext context,
            State state,
            long episodeMillis,
            boolean progressThread
    ) {
        display("Thinking episode: " + context.index + " - " + episodeMillis);

        try {
            long episodeDeadline = System.currentTimeMillis() + episodeMillis;
            do {
                root.runTrajectory(state.prototype(), context);
            }
            while (System.currentTimeMillis() < episodeDeadline);
        }
        catch (Throwable t) {
            log("ERROR! " + t.getMessage());
            throw t;
        }

        if (progressThread) {
            reportProgress(root);
        }
    }


    private void initIfRequired() {
        nnCache.clear();
        cacheHits.reset();
        cacheHits.reset();

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
//                    modelProto,
                    pool,
                    exploration,
                    explorationLog,
                    0.2,
                    randomize,
                    rollouts,
                    tablebase,
                    predictionUncertainty,
                    nnCache,
                    cacheHits,
                    collisions));
        }

        MovePicker.init();
        if (tablebase) {
            DeepOracle.init();
        }
    }


    private PuctNode getOrCreateRoot(State state) {
        long positionHash = state.staticHashCode();

        if (previousRoot != null && previousPositionHash == positionHash) {
            display("Retrieved root" + previousRoot);
            return previousRoot;
        }

        int[] legalMoves = state.legalMoves();

        PuctEstimate estimate = contexts.get(0).pool.estimateRoot(state, legalMoves);

        if (train) {
            double[] buffer = new double[estimate.moveProbabilities.length];
            PuctUtils.smearProbabilities(estimate.moveProbabilities, alpha, signal, new Random(), buffer);
        }
        else {
            PuctUtils.smearProbabilities(
                    estimate.moveProbabilities, predictionUncertainty);
        }

        PuctNode root = new PuctNode(legalMoves, estimate.moveProbabilities, null);
        root.initRoot();

        previousRoot = root;
        previousPositionHash = positionHash;

        display("Created root: " + root);
        return root;
    }


    private void reportProgress(
            PuctNode root
    ) {
        int bestMove = root.bestMove(true);

        String generalPrefix = String.format(
                "%s - %s | %d / %.2f / %b / %d / %b / %.2f | %d / %d / %d | %s",
                id,
                model,
                threads,
                exploration,
                randomize,
                rollouts,
                tablebase,
                predictionUncertainty,
                nnCache.size(),
                cacheHits.longValue(),
                collisions.longValue(),
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
        return previousRoot.moveValue(move, true);
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
