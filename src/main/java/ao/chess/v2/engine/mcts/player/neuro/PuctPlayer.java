package ao.chess.v2.engine.mcts.player.neuro;

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
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class PuctPlayer
        implements ScoredPlayer
{
    //-----------------------------------------------------------------------------------------------------------------
    private final static int reportPeriod = 1_000;


    //-----------------------------------------------------------------------------------------------------------------
    private final Path savedNeuralNetwork;
    private final int threads;
    private final double exploration;
    private final double alpha;
    private final double signal;
    private final boolean train;

    private final CopyOnWriteArrayList<PuctContext> contexts;
    private ExecutorService executorService;

    private long previousPositionHash = -1;
    private PuctNode previousRoot;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            double exploration)
    {
        this(savedNeuralNetwork, threads, exploration, 1, 0.75, false);
    }


    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            double exploration,
            double alpha,
            double signal,
            boolean train)
    {
        this.savedNeuralNetwork = savedNeuralNetwork;
        this.threads = threads;
        this.exploration = exploration;
        this.alpha = alpha;
        this.signal = signal;
        this.train = train;

        contexts = new CopyOnWriteArrayList<>();
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

        PuctNode root = getOrCreateRoot(position);

        long episodeMillis = Math.min(reportPeriod, timePerMove);
        long deadline = System.currentTimeMillis() + timePerMove;

        if (threads == 1) {
            PuctContext context = contexts.get(0);
            do {
                thinkingEpisode(root, context, position, episodeMillis, true);
            }
            while (System.currentTimeMillis() < deadline);
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
                    while (System.currentTimeMillis() < deadline);
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

        return root.bestMove();
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
                    nn, exploration, alpha, signal));
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

        PuctNode root = new PuctNode(legalMoves, moveProbabilities);
        root.initRoot();

        previousRoot = root;
        previousPositionHash = positionHash;

        return root;
    }


    private void reportProgress(
            PuctNode root
    ) {
        int bestMove = root.bestMove();

        String message = String.format(
                "%s | %d / %.2f / %.2f | %s | %s",
                savedNeuralNetwork,
                threads,
                exploration,
                alpha,
                Move.toString(bestMove),
                root);

//        Io.display(message);
        System.out.println(message);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double moveScoreInternal(int move) {
        return previousRoot.moveVisits(move);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
