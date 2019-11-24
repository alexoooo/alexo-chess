package ao.chess.v2.engine.mcts.player.neuro;

import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.mcts.player.ScoredPlayer;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.*;
import java.util.concurrent.atomic.LongAdder;


public class PuctPlayer
        implements ScoredPlayer
{
    //-----------------------------------------------------------------------------------------------------------------
    private final static int reportPeriod = 5_000;


    //-----------------------------------------------------------------------------------------------------------------
    private final String id;
    private final Path savedNeuralNetwork;
    private final int threads;
    private final double exploration;
    private final boolean visitMax;
    private final int rollouts;
    private final boolean tablebase;
    private final double predictionUncertainty;
    private final double alpha;
    private final double signal;
    private final int minimumTrajectories;
    private boolean train;

    private final ConcurrentHashMap<Long, PuctEstimate> nnCache = new ConcurrentHashMap<>();
    private final LongAdder cacheHits = new LongAdder();

    private final CopyOnWriteArrayList<PuctContext> contexts;
    private ExecutorService executorService;

    private long previousPositionHash = -1;
    private PuctNode previousRoot;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            int minimumTrajectories)
    {
        this(savedNeuralNetwork,
                threads,
                1.5,
                true,
                7,
                true,
                0.4,
                minimumTrajectories);
    }


    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            double exploration,
            boolean visitMax,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories)
    {
        this(savedNeuralNetwork,
                threads,
                exploration,
                visitMax,
                rollouts,
                tablebase,
                predictionUncertainty,
                minumumTrajectories,
                0.3, 0.75, false);
    }


    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            double exploration,
            boolean visitMax,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories,
            double alpha,
            double signal)
    {
        this(savedNeuralNetwork,
                threads,
                exploration,
                visitMax,
                rollouts,
                tablebase,
                predictionUncertainty,
                minumumTrajectories,
                alpha,
                signal,
                true);
    }


    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            double exploration,
            boolean visitMax,
            int rollouts,
            boolean tablebase,
            double predictionUncertainty,
            int minumumTrajectories,
            double alpha,
            double signal,
            boolean train)
    {
        this.savedNeuralNetwork = savedNeuralNetwork;
        this.threads = threads;
        this.exploration = exploration;
        this.visitMax = visitMax;
        this.rollouts = rollouts;
        this.tablebase = tablebase;
        this.predictionUncertainty = predictionUncertainty;
        this.minimumTrajectories = minumumTrajectories;
        this.alpha = alpha;
        this.signal = signal;
        this.train = train;

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

        return root.bestMove(visitMax);
    }


    private void thinkingEpisode(
            PuctNode root,
            PuctContext context,
            State state,
            long episodeMillis,
            boolean progressThread
    ) {
        long episodeDeadline = System.currentTimeMillis() + episodeMillis;
        do {
            root.runTrajectory(state.prototype(), context);
        }
        while (System.currentTimeMillis() < episodeDeadline);

        if (progressThread) {
            reportProgress(root);
        }
    }


    private void initIfRequired() {
        nnCache.clear();
        cacheHits.reset();

        if (! contexts.isEmpty()) {
            return;
        }

        if (threads != 1) {
            executorService = Executors.newFixedThreadPool(threads);
        }

        for (int i = 0; i < threads; i++) {
            MultiLayerNetwork nn;
            try {
                nn = MultiLayerNetwork.load(savedNeuralNetwork.toFile(), false);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
            contexts.add(new PuctContext(
                    nn,
                    exploration, rollouts, tablebase, predictionUncertainty,
                    nnCache, cacheHits));
        }

        MovePicker.init();
        if (tablebase) {
            DeepOracle.init();
        }
    }


    private PuctNode getOrCreateRoot(State state) {
        long positionHash = state.staticHashCode();

        if (previousRoot != null && previousPositionHash == positionHash) {
            return previousRoot;
        }

        int[] legalMoves = state.legalMoves();

        MultiLayerNetwork nn = contexts.get(0).nn;

        INDArray input = NeuralCodec.INSTANCE.encodeState(state);
        INDArray output = nn.output(input);

        double[] moveProbabilities = NeuralCodec.INSTANCE
                .decodeMoveProbabilities(output, state, legalMoves);

        if (train) {
            double[] buffer = new double[moveProbabilities.length];
            PuctUtils.smearProbabilities(moveProbabilities, alpha, signal, new Random(), buffer);
        }
        else {
            PuctUtils.smearProbabilities(
                    moveProbabilities, predictionUncertainty);
        }

        PuctNode root = new PuctNode(legalMoves, moveProbabilities, null);
        root.initRoot();

        previousRoot = root;
        previousPositionHash = positionHash;

        return root;
    }


    private void reportProgress(
            PuctNode root
    ) {
        int bestMove = root.bestMove(visitMax);

        String generalPrefix = String.format(
                "%s - %s | %d / %.2f / %b / %d / %b | %d / %d | %s",
                id,
                savedNeuralNetwork,
                threads,
                exploration,
                visitMax,
                rollouts,
                tablebase,
                nnCache.size(),
                cacheHits.longValue(),
                Move.toString(bestMove));

        String moveSuffix =
                root.toString();

        System.out.println(generalPrefix + " | " + moveSuffix);
    }


    @Override
    public double expectedValue() {
        return previousRoot.inverseValue();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double moveScoreInternal(int move) {
        return previousRoot.moveValue(move, visitMax);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
