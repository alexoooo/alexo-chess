package ao.chess.v2.test;


import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.learn.MoveHistory;
import ao.chess.v2.engine.heuristic.learn.MoveTrainer;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.neuro.puct.PuctPlayer;
import ao.chess.v2.engine.neuro.puct.PuctSingleModel;
import ao.chess.v2.engine.simple.RandomPlayer;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Outcome;
import com.google.common.collect.Lists;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;
import java.util.stream.Collectors;


public class LearningLoop {
    //-----------------------------------------------------------------------------------------------------------------
    private final static Path generationsDir = Paths.get("lookup/gen");

    private static final boolean computeGraph = false;
    private final static int coolDownMs = 5_000;
    private final static int selfPlayThreads = 1;
    private final static int gamesPerThread = 100;

    private final static int trainingIterations = 1;
    private final static int gamesInTest = 0;

//    private final static double thinkingExploration = 1.5;
    private final static double thinkingExploration = 1.5;
    private final static double thinkingExplorationLog = 18432;
//    private final static boolean thinkingMaxVisits = false;
    private final static double thinkingAlpha = 0.3;
    private final static double thinkingMoveUncertainty = 0.4;
    private final static double thinkingSignal = 0.75;
    private final static int thinkingMinimumTrajectories = 1000;
    private final static int thinkingRollounts = 7;
    private final static boolean thinkingTablebase = true;
//    private final static int thinkingTimeMs = 10_000;
    private final static int aThinkingTimeMs = 1_000;
    private final static int bThinkingTimeMs = 1_000;
    private final static boolean aVisitMax = false;
    private final static boolean bVisitMax = false;
    private final static int batchSize = 8_000;

    private final static String nnFilename = "nn.zip";
    private final static String historyFilename = "history.txt";

    private final static Random seededRandom = new Random(42);


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
        List<Path> generationDirs = listCompletedGenerationDirs();

        int lastGenerationNumber =
                generationDirs.isEmpty()
                ? -1
                : Integer.parseInt(generationDirs.get(generationDirs.size() - 1).getFileName().toString());

        int nextGenerationNumber = lastGenerationNumber + 1;

        while (true)
        {
            System.out.println("Starting generation: " + nextGenerationNumber);

            Path generationDir = generationsDir.resolve(Integer.toString(nextGenerationNumber));
            runGeneration(generationDirs, nextGenerationNumber, generationDir);

            generationDirs.add(generationDir);

            nextGenerationNumber++;
        }
    }


    private static void coolDown() {
        try {
            // NB: let the PC cool down
            Thread.sleep(coolDownMs);
        }
        catch (InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static List<Path> listCompletedGenerationDirs() throws IOException {
        if (! Files.exists(generationsDir)) {
            return new ArrayList<>();
        }

        try (var stream = Files.list(generationsDir)) {
            return stream
                    .filter(dir -> Files.exists(dir.resolve(historyFilename)))
                    .collect(Collectors.toList());
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static void runGeneration(
            List<Path> previousGenerationDirs,
            int generationNumber,
            Path generationDir
    ) {
        if (generationNumber == 0) {
            runFirstGeneration(generationDir);
        }
        else {
            runNextGeneration(previousGenerationDirs, generationDir);
        }

        testPlayer(previousGenerationDirs, generationDir);
    }


    private static void runFirstGeneration(
            Path generationDir
    ) {
        MultiLayerNetwork emptyNn = emptyNeuralNetwork();

        Path nnFile = generationDir.resolve(nnFilename);

        try {
            Files.createDirectories(generationDir);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        NeuralUtils.saveNeuralNetwork(emptyNn, nnFile);

        recordSelfPlay(nnFile);
    }


    private static void runNextGeneration(
            List<Path> previousGenerationDirs,
            Path generationDir)
    {
        MultiLayerNetwork emptyNn = emptyNeuralNetwork();

        Path nnFile = generationDir.resolve(nnFilename);

        if (! Files.exists(nnFile)) {
            trainNeuralNetwork(emptyNn, previousGenerationDirs, nnFile);
        }

        recordSelfPlay(nnFile);
    }


    private static MultiLayerNetwork emptyNeuralNetwork() {
        return MoveTrainer.createNeuralNetwork4();
    }


    private static void trainNeuralNetwork(
            MultiLayerNetwork nn,
            List<Path> generationDirs,
            Path nnFile
    ) {
        int horizon = Math.min(generationDirs.size(), Math.max(generationDirs.size() / 2, 100));
//        int horizon = generationDirs.size();
        List<Path> trainingDirs = generationDirs.subList(generationDirs.size() - horizon, generationDirs.size());

        List<MoveHistory> allTrainingData = new ArrayList<>();

        for (Path generationDir : trainingDirs) {
            Path generationHistory = generationDir.resolve(historyFilename);
            List<MoveHistory> generationTrainingData = readTrainingData(generationHistory);
            allTrainingData.addAll(generationTrainingData);
        }

        for (int i = 0; i < trainingIterations; i++) {
            Collections.shuffle(allTrainingData, seededRandom);

            System.out.println("Training iteration: " + (i + 1));

            int total = 0;
            for (var trainingBatch : Lists.partition(allTrainingData, batchSize)) {
                long startTime = System.currentTimeMillis();

                trainNeuralBatch(nn, trainingBatch);
                total += trainingBatch.size();

                long delta = System.currentTimeMillis() - startTime;
                System.out.println("Trained (" + (delta / 1000) + ") " + total + " of " + allTrainingData.size());

                coolDown();
            }
        }

        NeuralUtils.saveNeuralNetwork(nn, nnFile);
    }


    private static List<MoveHistory> readTrainingData(
            Path generationHistory
    ) {
        try (var lines = Files.lines(generationHistory)) {
            return lines.map(MoveHistory::new).collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }



    private static void trainNeuralBatch(
            MultiLayerNetwork nn,
            List<MoveHistory> moveHistories
    ) {
        for (var example : moveHistories) {
            DataSet dataSet = MoveTrainer.convertToDataSet(example);
            nn.fit(dataSet);
        }

        Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static void recordSelfPlay(
            Path nnFile
    ) {
        Path historyFile = nnFile.resolveSibling(historyFilename);
        try {
            Files.createDirectories(historyFile.getParent());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        List<Thread> threads = new ArrayList<>();

        try (PrintWriter historyOut = new PrintWriter(historyFile.toFile()))
        {
            if (selfPlayThreads == 1) {
                PuctPlayer a = new PuctPlayer(
                        new PuctSingleModel(
                                nnFile,
                                computeGraph
                        ),
                        1,
                        thinkingExploration,
                        thinkingExplorationLog,
                        aVisitMax,
                        thinkingRollounts,
                        thinkingTablebase,
                        thinkingMoveUncertainty,
                        thinkingMinimumTrajectories,
                        thinkingAlpha,
                        thinkingSignal);

                PuctPlayer b = new PuctPlayer(
                        new PuctSingleModel(
                                nnFile,
                                computeGraph
                        ),
                        1,
                        thinkingExploration,
                        thinkingExplorationLog,
                        bVisitMax,
                        thinkingRollounts,
                        thinkingTablebase,
                        thinkingMoveUncertainty,
                        thinkingMinimumTrajectories,
                        thinkingAlpha,
                        thinkingSignal);

                int whiteThinkingMs = aThinkingTimeMs;
                int blackThinkingMs = bThinkingTimeMs;

                GameLoop gameLoop = new GameLoop();
                for (int i = 0; i < gamesPerThread; i++) {
                    List<MoveHistory> history = gameLoop.playWithHistory(
                            a, b, whiteThinkingMs, blackThinkingMs);

                    Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();

                    for (var move : history) {
                        historyOut.println(move.asString());
                    }

                    historyOut.flush();

                    {
                        int tempMs = whiteThinkingMs;
                        whiteThinkingMs = blackThinkingMs;
                        blackThinkingMs = tempMs;

                        PuctPlayer tempPlayer = a;
                        a = b;
                        b = tempPlayer;
                    }

                    System.out.println("recorded game " + (i + 1) + ": " + history.get(0).outcome());
                    coolDown();
                }
            }
            else {
                Object monitor = new Object();
                AtomicInteger gameCount = new AtomicInteger();
                for (int i = 0; i < selfPlayThreads; i++) {
                    Thread thread = selfPlayInThread(nnFile, (history) -> {
                        synchronized (monitor) {
                            for (var move : history) {
                                historyOut.println(move.asString());
                            }

                            historyOut.flush();

                            int currentGameNumber = gameCount.incrementAndGet();
                            System.out.println("recorded game " + currentGameNumber + ": " + history.get(0).outcome());
                        }
                    });

                    threads.add(thread);
                }

                for (Thread t : threads) {
                    t.join();
                }

                Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
            }
        }
        catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private static Thread selfPlayInThread(
            Path nnFile,
            Consumer<List<MoveHistory>> historyCallback
    ) {
        Thread thread = new Thread(() -> {
            PuctPlayer a = new PuctPlayer(
                    new PuctSingleModel(
                            nnFile,
                            computeGraph
                    ),
                    1,
                    thinkingExploration,
                    thinkingExplorationLog,
                    aVisitMax,
                    thinkingRollounts,
                    thinkingTablebase,
                    thinkingMoveUncertainty,
                    thinkingMinimumTrajectories,
                    thinkingAlpha,
                    thinkingSignal);

            PuctPlayer b = new PuctPlayer(
                    new PuctSingleModel(
                            nnFile,
                            computeGraph
                    ),
                    1,
                    thinkingExploration,
                    thinkingExplorationLog,
                    bVisitMax,
                    thinkingRollounts,
                    thinkingTablebase,
                    thinkingMoveUncertainty,
                    thinkingMinimumTrajectories,
                    thinkingAlpha,
                    thinkingSignal);

            int whiteThinkingMs = aThinkingTimeMs;
            int blackThinkingMs = bThinkingTimeMs;

            GameLoop gameLoop = new GameLoop();
            for (int i = 0; i < gamesPerThread; i++) {
                List<MoveHistory> history = gameLoop.playWithHistory(
                        a, b, whiteThinkingMs, blackThinkingMs);

                Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();

                historyCallback.accept(history);

                {
                    int tmp = whiteThinkingMs;
                    whiteThinkingMs = blackThinkingMs;
                    blackThinkingMs = tmp;

                    PuctPlayer tempPlayer = a;
                    a = b;
                    b = tempPlayer;
                }

                coolDown();
            }
        });

        thread.start();

        return thread;
    }


    private static void testPlayer(
            List<Path> previousGenerationDirs,
            Path generationDir
    ) {
        if (gamesInTest == 0) {
            return;
        }

        Path nnFile = generationDir.resolve(nnFilename);

        Player a = new PuctPlayer(
                new PuctSingleModel(
                        nnFile,
                        computeGraph
                ),
                1,
                500);

        Player b;
        if (previousGenerationDirs.isEmpty()) {
            b = new RandomPlayer();
        }
        else {
            Path previousNnFile = previousGenerationDirs
                    .get(previousGenerationDirs.size() - 1)
                    .resolve(nnFilename);

            b = new PuctPlayer(
                    new PuctSingleModel(
                            previousNnFile,
                            computeGraph
                    ),
                    1,
                    500);
        }

        GameLoop gameLoop = new GameLoop();

        int aWins = 0;
        int bWins = 0;
        int draws = 0;

        System.out.println("TEST: " + generationDir);

        for (int i = 0; i < gamesInTest; i++)
        {
            Colour aColour = i % 2 == 0 ? Colour.WHITE : Colour.BLACK;
            Colour bColour = i % 2 == 0 ? Colour.BLACK : Colour.WHITE;

            Player white = aColour == Colour.WHITE ? a : b;
            Player black = aColour == Colour.WHITE ? b : a;

            Outcome outcome = gameLoop.play(white, black, 1000);

            Colour winner = outcome.winner();
            if (winner == aColour) {
                aWins++;
            }
            else if (winner == bColour) {
                bWins++;
            }
            else {
                draws++;
            }

            Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();

            System.out.println("win: " + aWins + " | loss: " + bWins + " | draw: " + draws);
            coolDown();
        }
    }
}
