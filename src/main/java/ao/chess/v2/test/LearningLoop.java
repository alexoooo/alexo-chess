package ao.chess.v2.test;


import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.learn.MoveHistory;
import ao.chess.v2.engine.heuristic.learn.MoveTrainer;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.mcts.player.ScoredPlayer;
import ao.chess.v2.engine.mcts.player.neuro.PuctPlayer;
import ao.chess.v2.engine.simple.RandomPlayer;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Outcome;
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
import java.util.stream.Collectors;


public class LearningLoop {
    //-----------------------------------------------------------------------------------------------------------------
    private final static Path generationsDir = Paths.get("lookup/gen");
    private final static int gamesInGeneration = 100;
    private final static int trainingIterations = 2;
    private final static int gamesInTest = 1;

    private final static int thinkingThreads = 1;
    private final static double thinkingExploration = 4;
    private final static double thinkingAlpha = 4;
    private final static int thinkingTimeMs = 1000;

    private final static String nnFilename = "nn.zip";
    private final static String historyFilename = "history.txt";

    private final static Random seededRandom = new Random(42);


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) throws IOException {
        List<Path> generationDirs = listGenerationDirs();

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


    //-----------------------------------------------------------------------------------------------------------------
    private static List<Path> listGenerationDirs() throws IOException {
        if (! Files.exists(generationsDir)) {
            return new ArrayList<>();
        }

        try (var stream = Files.list(generationsDir)) {
            return stream.collect(Collectors.toList());
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
        MultiLayerNetwork emptyNn = MoveTrainer.createNeuralNetwork3();

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
        MultiLayerNetwork emptyNn = MoveTrainer.createNeuralNetwork3();

        Path nnFile = generationDir.resolve(nnFilename);

        trainNeuralNetwork(emptyNn, previousGenerationDirs, nnFile);

        recordSelfPlay(nnFile);
    }


    private static void trainNeuralNetwork(
            MultiLayerNetwork nn,
            List<Path> generationDirs,
            Path nnFile
    ) {
        for (int i = 0; i < trainingIterations; i++) {
            System.out.println("Training iteration: " + (i + 1));

            for (Path generationDir : generationDirs) {
                Path generationHistory = generationDir.resolve(historyFilename);
                trainNeuralNetwork(nn, generationHistory);
            }
        }

        NeuralUtils.saveNeuralNetwork(nn, nnFile);
    }


    private static void trainNeuralNetwork(
            MultiLayerNetwork nn,
            Path generationHistory
    ) {
        long startTime = System.currentTimeMillis();

        List<MoveHistory> moveHistories;
        try (var lines = Files.lines(generationHistory)) {
            moveHistories = lines.map(MoveHistory::new).collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        Collections.shuffle(moveHistories, seededRandom);

        for (var example : moveHistories) {
            DataSet dataSet = MoveTrainer.convertToDataSet(example);
            nn.fit(dataSet);
        }

        Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();

        long delta = System.currentTimeMillis() - startTime;
        System.out.println("Trained (" + (delta / 1000) + "): " + generationHistory);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static void recordSelfPlay(
            Path nnFile
    ) {
        ScoredPlayer a = new PuctPlayer(
                nnFile,
                thinkingThreads,
                thinkingExploration,
                thinkingAlpha);

        ScoredPlayer b = new PuctPlayer(
                nnFile,
                thinkingThreads,
                thinkingExploration,
                thinkingAlpha);

        Path historyFile = nnFile.resolveSibling(historyFilename);
        try {
            Files.createDirectories(historyFile.getParent());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        GameLoop gameLoop = new GameLoop();
        try (PrintWriter historyOut = new PrintWriter(historyFile.toFile()))
        {
            for (int i = 0; i < gamesInGeneration; i++)
            {
                List<MoveHistory> history = gameLoop.playWithHistory(a, b, thinkingTimeMs);

                for (var move : history) {
                    historyOut.println(move.asString());
                }

                historyOut.flush();

                System.out.println("recorded game " + (i + 1) + ": " + history.get(0).outcome());

                Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
            }
        }
        catch (FileNotFoundException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static void testPlayer(
            List<Path> previousGenerationDirs,
            Path generationDir
    ) {
        Path nnFile = generationDir.resolve(nnFilename);

        Player a = new PuctPlayer(
                nnFile,
                thinkingThreads,
                thinkingExploration,
                thinkingAlpha);

        Player b;
        if (previousGenerationDirs.isEmpty()) {
            b = new RandomPlayer();
        }
        else {
            Path previousNnFile = previousGenerationDirs
                    .get(previousGenerationDirs.size() - 1)
                    .resolve(nnFilename);

            b = new PuctPlayer(
                    previousNnFile,
                    thinkingThreads,
                    thinkingExploration,
                    thinkingAlpha);
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

            Outcome outcome = gameLoop.play(white, black, thinkingTimeMs);

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
        }
    }
}
