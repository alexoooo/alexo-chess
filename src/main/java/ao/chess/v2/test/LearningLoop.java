package ao.chess.v2.test;


import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.learn.MoveHistory;
import ao.chess.v2.engine.heuristic.learn.MoveTrainer;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.mcts.player.neuro.PuctPlayer;
import ao.chess.v2.engine.simple.RandomPlayer;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Outcome;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.dataset.DataSet;

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
    private final static int gamesInGeneration = 10;
    private final static int trainingIterations = 5;
    private final static int gamesInTest = 0;

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

        NeuralUtils.saveNeuralNetwork(emptyNn, nnFile);

        recordSelfPlay(nnFile);
    }


    private static void runNextGeneration(
            List<Path> previousGenerationDirs,
            Path generationDir)
    {
        MultiLayerNetwork emptyNn = MoveTrainer.createNeuralNetwork3();

        trainNeuralNetwork(emptyNn, previousGenerationDirs);

        Path nnFile = generationDir.resolve(nnFilename);

        recordSelfPlay(nnFile);
    }


    private static void trainNeuralNetwork(
            MultiLayerNetwork nn,
            List<Path> generationDirs
    ) {
        for (int i = 0; i < trainingIterations; i++) {
            for (Path generationDir : generationDirs) {
                Path generationHistory = generationDir.resolve(historyFilename);
                trainNeuralNetwork(nn, generationHistory);
            }
        }
    }


    private static void trainNeuralNetwork(
            MultiLayerNetwork nn,
            Path generationHistory
    ) {
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

        System.out.println("Trained: " + generationHistory);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static void recordSelfPlay(
            Path nnFile
    ) {
        Player a = new PuctPlayer(
                nnFile,
                thinkingThreads,
                thinkingExploration,
                thinkingAlpha);

        Player b = new PuctPlayer(
                nnFile,
                thinkingThreads,
                thinkingExploration,
                thinkingAlpha);

        GameLoop gameLoop = new GameLoop();
        try (PrintWriter historyOut = new PrintWriter(
                nnFile.resolveSibling(historyFilename).toFile()))
        {
            for (int i = 0; i < gamesInGeneration; i++)
            {
                List<MoveHistory> history = gameLoop.playWithHistory(a, b, thinkingTimeMs);

                for (var move : history) {
                    historyOut.println(move.asString());
                }

                historyOut.flush();

                System.out.println("recorded game " + (i + 1) + ": " + history.get(0).outcome());
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

            System.out.println("win: " + aWins + " | loss: " + bWins + " | draw: " + draws);
        }
    }
}
