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
    private final String id;
    private final Path savedNeuralNetwork;
    private final int threads;
    private final double exploration;
    private final boolean visitMax;
    private final int rollouts;
    private final boolean tablebase;
    private final double alpha;
    private final double signal;
    private boolean train;

    private final CopyOnWriteArrayList<PuctContext> contexts;
    private ExecutorService executorService;

    private long previousPositionHash = -1;
    private PuctNode previousRoot;
//    private int[] previousTablebase;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            double exploration)
    {
        this(savedNeuralNetwork,
                threads,
                exploration,
                false,
                7,
                true,
                0.3,
                0.75,
                false);
    }


    public PuctPlayer(
            Path savedNeuralNetwork,
            int threads,
            double exploration,
            boolean visitMax,
            int rollouts,
            boolean tablebase,
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

//        int tablebaseMove = tablebaseMove(position, root);
//        if (tablebaseMove != -1) {
//            return tablebaseMove;
//        }

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
                    nn, exploration, rollouts, tablebase));
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
                    moveProbabilities, PuctNode.uncertainty);
        }

        PuctNode root = new PuctNode(legalMoves, moveProbabilities, null);
        root.initRoot();

        previousRoot = root;
        previousPositionHash = positionHash;
//        previousTablebase = null;

        return root;
    }


    private void reportProgress(
            PuctNode root
    ) {
//        boolean inTablebase = (previousTablebase != null);
        int bestMove = -1;
//        if (inTablebase) {
//            bestMove = tablebaseBestMove(root);
//        }
//        else {
            bestMove = root.bestMove(visitMax);
//        }

        String generalPrefix = String.format(
                "%s - %s | %d / %.2f / %d / %b | %s",
                id,
                savedNeuralNetwork,
                threads,
                exploration,
                rollouts,
                tablebase,
                Move.toString(bestMove));

        String moveSuffix =
                /*inTablebase
                ? "tablebase"
                :*/
                root.toString();

        System.out.println(generalPrefix + " | " + moveSuffix);
    }


    @Override
    public double expectedValue() {
//        if (previousTablebase != null) {
//            for (int value : previousTablebase) {
//                if (value == 0) {
//                    continue;
//                }
//
//                if (value < 500) {
//                    return 0;
//                } else if (value > 500) {
//                    return 1;
//                } else {
//                    return 0.5;
//                }
//            }
//        }

        return previousRoot.inverseValue();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double moveScoreInternal(int move) {
//        if (previousTablebase != null) {
//            int index = previousRoot.moveIndex(move);
//            return previousTablebase[index];
//        }

        return previousRoot.moveValue(move, visitMax);
    }


    //-----------------------------------------------------------------------------------------------------------------
//    private int tablebaseMove(State from, PuctNode root) {
//        if (previousTablebase != null) {
//            return tablebaseBestMove(root);
//        }
//        else if (! tablebase || from.pieceCount() > DeepOracle.instancePieceCount) {
//            previousTablebase = null;
//            return -1;
//        }
//
//        Colour pov = from.nextToAct();
//
//        int[] legalMoves = root.legalMoves();
//
//        boolean canDraw = false;
//        int bestOutcome = 0;
//        int bestMoveIndex = -1;
//
//        for (int i = 0; i < legalMoves.length; i++) {
//            int legalMove = legalMoves[i];
//
//            Move.apply(legalMove, from);
//            DeepOutcome outcome = DeepOracle.INSTANCE.see(from);
//            Move.unApply(legalMove, from);
//            if (outcome == null || outcome.isDraw()) {
//                canDraw = true;
//
//                if (bestMoveIndex == -1) {
//                    bestMoveIndex = i;
//                }
//                continue;
//            }
//
//            if (outcome.outcome().winner() == pov) {
//                if (bestOutcome <= 0 ||
//                        bestOutcome > outcome.plyDistance() ||
//                        (bestOutcome == outcome.plyDistance() &&
//                                Rand.nextBoolean())) {
//                    bestOutcome = outcome.plyDistance();
//                    bestMoveIndex = i;
//                }
//            }
//            else if (! canDraw && bestOutcome <= 0
//                    && bestOutcome > -outcome.plyDistance()) {
//                bestOutcome = -outcome.plyDistance();
//                bestMoveIndex = i;
//            }
//        }
//
//        int bestMoveValue;
//        if (bestOutcome < 0) {
//            bestMoveValue = -bestOutcome;
//        }
//        else if (bestOutcome > 0) {
//            bestMoveValue = 1000 - bestOutcome;
//        }
//        else {
//            bestMoveValue = 500;
//        }
//        previousTablebase[bestMoveIndex] = bestMoveValue;
//
//        System.out.println("Tablebase in " + bestOutcome);
//
//        return legalMoves[bestMoveIndex];
//    }
//
//
//    private int tablebaseBestMove(PuctNode root) {
//        for (int i = 0; i < previousTablebase.length; i++) {
//            if (previousTablebase[i] != 0) {
//                return root.legalMoves()[i];
//            }
//        }
//        return -1;
//    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }
}
