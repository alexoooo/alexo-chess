package ao.chess.v2.engine.heuristic.learn;

import ao.chess.v2.engine.neuro.meta.MetaEstimate;
import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import com.google.common.collect.Lists;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.memory.conf.WorkspaceConfiguration;
import org.nd4j.linalg.api.memory.enums.LocationPolicy;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;
import java.util.zip.GZIPInputStream;


// https://towardsdatascience.com/deep-learning-which-loss-and-activation-functions-should-i-use-ac02f1c56aa8
public class MoveTrainer {
    //-----------------------------------------------------------------------------------------------------------------
    private static final boolean useCheckpoint = true;
//    private static final boolean useCheckpoint = false;

//    private static final boolean snapshot = false;
    private static final boolean snapshot = true;

    private static final boolean meta = false;
//    private static final boolean meta = true;

    private static final boolean defaultBestAction = true;
//    private static final boolean defaultValueAverage = true;
//    private static final boolean measureOutcome = false;

//    private static final int miniBatchSize = 64;
//    private static final int miniBatchSize = 128;
//    private static final int miniBatchSize = 192;
//    private static final int miniBatchSize = 256;
//    private static final int miniBatchSize = 320;
//    private static final int miniBatchSize = 384;
//    private static final int miniBatchSize = 448;
    private static final int miniBatchSize = 512;
//    public static final int miniBatchSize = 704;
//    private static final int miniBatchSize = 768;
//    private static final int miniBatchSize = 1024;

    private static final int saveOnceEvery = 1_000_000;

    private static final int maxTestCount = 10_000;

    private static final int trainingIterations = 0;
//    private static final int trainingIterations = 1;
//    private static final int trainingIterations = 100;

//    private static final boolean testInitial = false;
    private static final boolean testInitial = true;

    private static final int seed = 42;
    private static final Random seededRandom = new Random(seed);
    private static final MoveTrainerEncoder encoder = new MoveTrainerEncoder(miniBatchSize);


    public static final WorkspaceConfiguration wsConfig = WorkspaceConfiguration.builder()
            .policyLocation(LocationPolicy.RAM)
            .build();


    private static final Path checkpointPath = Paths.get("lookup/train/checkpoint.txt");
    private static final Path progressPath = Paths.get("lookup/train/progress.tsv");
    private static final int progressSteps = 1;

//    private static final List<Path> inputs =
//            mixRange(69, 2999);
//            mixRange(377, 2999);
//    private static final List<Path> inputs = List.of(
//            Paths.get("lookup/train/mix-small/champions_10000.txt")
//    );


    private static int readCheckpoint() {
        if (! Files.exists(checkpointPath)) {
            return -1;
        }

        try {
            String checkpointBody = Files.readString(checkpointPath);
            return Integer.parseInt(checkpointBody);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static void writeCheckpoint(int number) {
        try {
            Files.writeString(checkpointPath, String.valueOf(number));
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static void writeProgress(
            int checkpoint,
            double valueError,
            double policyError,
            double errorError
    ) {
        try {
            Files.writeString(progressPath,
                    String.format("%s\t%s\t%s\t%s\n", checkpoint, valueError, policyError, errorError),
                    StandardOpenOption.CREATE, StandardOpenOption.APPEND);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static List<Path> mixRange(int fromInclusive, int toInclusive) {
        List<Path> range = new ArrayList<>();
        for (int i = fromInclusive; i <= toInclusive; i++) {
//            Path mixFile = Paths.get("lookup/train/mix-pgnmentor-2/" + i + ".txt.gz");
//            Path mixFile = Paths.get("lookup/train/mix-big/" + i + ".txt.gz");
//            Path mixFile = Paths.get("lookup/train/pieces/32/" + i + ".txt.gz");
//            Path mixFile = Paths.get("lookup/train/pieces/p_2_12/" + i + ".txt.gz");
//            Path mixFile = Paths.get("lookup/train/pieces/p_13_22/" + i + ".txt.gz");
            Path mixFile = Paths.get("lookup/train/pieces/p_23_32/" + i + ".txt.gz");
            range.add(mixFile);
        }
        return range;
    }

    private static final List<Path> test = List.of(
//            Paths.get("lookup/train/mix-small/champions_1000.txt")
//            Paths.get("lookup/train/mix-small/champions_1000.txt"),
//
//            Paths.get("lookup/train/pieces/test/2.txt.gz"),
//            Paths.get("lookup/train/pieces/test/3.txt.gz"),
//            Paths.get("lookup/train/pieces/test/4.txt.gz"),
//            Paths.get("lookup/train/pieces/test/5.txt.gz"),
//            Paths.get("lookup/train/pieces/test/6.txt.gz"),
//            Paths.get("lookup/train/pieces/test/7.txt.gz"),
//            Paths.get("lookup/train/pieces/test/8.txt.gz"),
//            Paths.get("lookup/train/pieces/test/9.txt.gz"),
//            Paths.get("lookup/train/pieces/test/10.txt.gz"),
//            Paths.get("lookup/train/pieces/test/11.txt.gz"),
//            Paths.get("lookup/train/pieces/test/12.txt.gz")
//            Paths.get("lookup/train/pieces/test/13.txt.gz"),
//            Paths.get("lookup/train/pieces/test/14.txt.gz"),
//            Paths.get("lookup/train/pieces/test/15.txt.gz"),
//            Paths.get("lookup/train/pieces/test/16.txt.gz"),
//            Paths.get("lookup/train/pieces/test/17.txt.gz"),
//            Paths.get("lookup/train/pieces/test/18.txt.gz"),
//            Paths.get("lookup/train/pieces/test/19.txt.gz"),
//            Paths.get("lookup/train/pieces/test/20.txt.gz"),
//            Paths.get("lookup/train/pieces/test/21.txt.gz"),
//            Paths.get("lookup/train/pieces/test/22.txt.gz")
//            Paths.get("lookup/train/pieces/test/12.txt.gz")
//            Paths.get("lookup/train/pieces/test/22.txt.gz")
//            Paths.get("lookup/train/pieces/test/32.txt.gz")

            Paths.get("lookup/train/mix-small/champions_10000.txt")
//            Paths.get("lookup/pgn/small/Adams.txt")
    );


    private static final Path saveFile =
//            Paths.get("lookup/nn/res_4h_20191215.zip");
//            Paths.get("lookup/nn/res_10_20191227.zip");
//            Paths.get("lookup/nn/res_5a_head.zip");
//            Paths.get("lookup/nn/res_5_p32_head.zip");
//            Paths.get("lookup/nn/res_5_p32_head.zip");
//            Paths.get("lookup/nn/res_5_p_2_12_head.zip");
//            Paths.get("lookup/nn/res_5_p_13_22_head.zip");
//            Paths.get("lookup/nn/res_7_p_23_32_head.zip");
            Paths.get("lookup/nn/res_14_head.zip");
//            Paths.get("lookup/nn/res_5m_head.zip");
//            Paths.get("lookup/nn/dense_5c_48_head.zip");
//            Paths.get("lookup/nn/dense_6_48_head.zip");
//            Paths.get("lookup/nn/dense_6_48b_head.zip");
//            Paths.get("lookup/nn/dense_7_48_head.zip");
//            Paths.get("lookup/nn/dense_7b_48_head.zip");
//            Paths.get("lookup/nn/res_32_20191227.zip");


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        boolean saved = Files.exists(saveFile);
        System.out.println("Training " + saveFile + " - " + saved);

        NeuralNetwork nn;
        if (saved) {
            nn = NeuralUtils.loadNeuralNetwork(saveFile, false, true);
        }
        else {
//            nn = createNeuralNetwork4();
//            nn = createNeuralNetwork6();
//            nn = createNeuralNetwork6b();
//            nn = createNeuralNetwork6c();
//            nn = createNeuralNetwork6d();
//            nn = createResidualNetwork2();
//            nn = createResidualNetwork3();
//            nn = createResidualNetwork4();
//            nn = createResidualNetwork5();
//            nn = createResidualNetwork5();
//            nn = createResidualNetwork7();
            nn = createResidualNetwork14();
//            nn = createDenseNetwork5x48();
//            nn = createDenseNetwork5x48c();
//            nn = createDenseNetwork6x48();
//            nn = createDenseNetwork6x48b();
//            nn = createDenseNetwork7x48();
//            nn = createDenseNetwork7x48b();
//            nn = createResidualNetwork5Meta();
//            nn = createResidualNetwork32();
        }

        int checkpoint =
                useCheckpoint
                ? readCheckpoint()
                : -1;
        List<Path> inputs =
                mixRange(checkpoint + 1, checkpoint + progressSteps);
//                List.of(
//                        Paths.get("lookup/train/mix-small/champions_10000.txt")
//                );

        if (testInitial) {
            testOutputs(nn, checkpoint, false);
        }

        if (trainingIterations == 0) {
            return;
        }

        for (int epoch = 0; epoch < trainingIterations; epoch++) {
            System.out.println("Training epoch = " + (epoch + 1));

            int nextCheckpoint = checkpoint + 1;
            for (var input : inputs) {
                List<MoveHistory> inputMoves = readMoves(input);

                System.out.println("input: " + input + " - " + inputMoves.size());

                performTraining(inputMoves, epoch, nn);

                testOutputs(nn, nextCheckpoint, true);

                if (useCheckpoint) {
                    writeCheckpoint(nextCheckpoint);
                }
                nextCheckpoint++;
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static void performTraining(
            List<MoveHistory> allMoves,
            int epoch,
            NeuralNetwork nn
    ) {
        int trainingCount = 0;
        Collections.shuffle(allMoves, seededRandom);

        for (var partition : Lists.partition(allMoves, saveOnceEvery)) {
            long trainingStart = System.currentTimeMillis();

            List<List<MoveHistory>> partitions = Lists.partition(partition, miniBatchSize);

            List<MetaEstimate> estimates;
            if (meta) {
                estimates = new ArrayList<>();
                for (var batch : partitions) {
                    if (batch.size() == miniBatchSize) {
                        estimates.addAll(encoder.estimateAllMeta(batch, (ComputationGraph) nn));
                    }
                    else {
                        break;
                    }
                }
            }

            try (MemoryWorkspace ignored = Nd4j.getWorkspaceManager()
                    .getAndActivateWorkspace(wsConfig, "MoveTrainer"))
            {
                for (int partitionIndex = 0; partitionIndex < partitions.size(); partitionIndex++) {
                    List<MoveHistory> miniBatch = partitions.get(partitionIndex);
                    if (miniBatch.size() != miniBatchSize) {
                        // https://stats.stackexchange.com/a/236186
                        continue;
                    }

                    MultiDataSet encoded;
                    if (meta) {
                        List<MetaEstimate> batchEstimates = estimates.subList(
                                partitionIndex * miniBatchSize, (partitionIndex + 1) * miniBatchSize);
                        encoded = encoder.encodeAllInPlaceMeta(miniBatch, batchEstimates);
                    }
                    else {
                        encoded = encoder.encodeAllInPlace(miniBatch);
                    }

                    nn.fit(encoded);
                }

                saveProgress(nn);
            }

            trainingCount += partition.size();
            System.out.println((epoch + 1) +
                    " - " + trainingCount + " of " + allMoves.size() +
                    " - Training took: " +
                    (double) (System.currentTimeMillis() - trainingStart) / 1000 +
                    " - " + LocalDateTime.now());

            Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
        }
    }


    private static void saveProgress(NeuralNetwork nn) {
        NeuralUtils.saveNeuralNetwork(nn, saveFile);

        if (snapshot) {
            String timeSuffix = DateTimeFormatter
                    .ofPattern("yyyy-MM-dd_HH-mm-ss")
                    .format(ZonedDateTime.now());
            Path snapshotFile = saveFile.resolveSibling("snapshot_" + timeSuffix + ".zip");

            try {
                Files.copy(saveFile, snapshotFile);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }


    public static List<MoveHistory> readMoves(Path input) {
        try {
            if (input.getFileName().toString().endsWith(".gz")) {
                List<MoveHistory> moves = new ArrayList<>();
                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new GZIPInputStream(
                                Files.newInputStream(input)))
                )) {
                    while (reader.ready()) {
                        String line = reader.readLine();
                        moves.add(new MoveHistory(line));
                    }
                }
                return moves;
            }
            else {
                try (var lines = Files.lines(input)) {
                    return lines.map(MoveHistory::new).collect(Collectors.toList());
                }
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static void testOutputs(
            NeuralNetwork nn, int checkpoint, boolean write)
    {
        DoubleSummaryStatistics globalOutcomeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics globalActionStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics globalErrorStats = new DoubleSummaryStatistics();

        for (var testPath : test) {
            long predictStart = System.currentTimeMillis();
            DoubleSummaryStatistics outcomeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics actionStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics errorStats = new DoubleSummaryStatistics();

            List<MoveHistory> inputMovesAll = readMoves(testPath);
            List<MoveHistory> inputMoves = inputMovesAll.subList(0, Math.min(maxTestCount, inputMovesAll.size()));

            if (meta) {
                List<MetaEstimate> predictions = testExampleAllMeta((ComputationGraph) nn, inputMoves);
                for (int i = 0; i < inputMoves.size(); i++)
                {
                    double[] errors = measureErrorMeta(predictions.get(i), inputMoves.get(i));

                    outcomeStats.accept(errors[0]);
                    globalOutcomeStats.accept(errors[0]);

                    actionStats.accept(errors[1]);
                    globalActionStats.accept(errors[1]);

                    errorStats.accept(errors[2]);
                    globalErrorStats.accept(errors[2]);
                }
            }
            else {
                List<PuctEstimate> predictions = testExampleAll((ComputationGraph) nn, inputMoves);
                for (int i = 0; i < inputMoves.size(); i++)
                {
                    double[] errors = measureError(predictions.get(i), inputMoves.get(i));

                    outcomeStats.accept(errors[0]);
                    globalOutcomeStats.accept(errors[0]);

                    actionStats.accept(errors[1]);
                    globalActionStats.accept(errors[1]);
                }
            }

            double took = (double) (System.currentTimeMillis() - predictStart) / 1000;
            System.out.println("Path error:\t" +
                    outcomeStats.getAverage() + "\t" +
                    actionStats.getAverage() + "\t" +
                    errorStats.getAverage() + "\t" +
                    "took: " + took + " - " + testPath);

            if (write && useCheckpoint) {
                writeProgress(
                        checkpoint,
                        outcomeStats.getAverage(),
                        actionStats.getAverage(),
                        errorStats.getAverage());
            }

            Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
        }

        System.out.println("Average error: " +
                globalOutcomeStats.getAverage() + " / " +
                globalActionStats.getAverage() + " / " +
                globalErrorStats.getAverage());
    }


    private static PuctEstimate testExample(NeuralNetwork nn, MoveHistory example) {
        return testExampleGraph((ComputationGraph) nn, example);
    }


    private static PuctEstimate testExampleGraph(ComputationGraph nn, MoveHistory example) {
        return encoder.estimate(example, nn);
    }


    private static List<PuctEstimate> testExampleAll(ComputationGraph nn, List<MoveHistory> examples) {
        List<PuctEstimate> all = new ArrayList<>();
        for (var batch : Lists.partition(examples, miniBatchSize))
        {
            if (batch.size() == miniBatchSize) {
                all.addAll(encoder.estimateAll(batch, nn));
            }
            else {
                for (var example : batch) {
                    all.add(encoder.estimate(example, nn));
                }
            }
        }
        return all;
    }


    private static List<MetaEstimate> testExampleAllMeta(ComputationGraph nn, List<MoveHistory> examples) {
        List<MetaEstimate> all = new ArrayList<>();
        for (var batch : Lists.partition(examples, miniBatchSize))
        {
            if (batch.size() == miniBatchSize) {
                all.addAll(encoder.estimateAllMeta(batch, nn));
            }
            else {
                for (var example : batch) {
                    all.add(encoder.estimateMeta(example, nn));
                }
            }
        }
        return all;
    }


    private static double[] measureError(
            PuctEstimate prediction,
            MoveHistory example)
    {
        double predictedOutcome = prediction.winProbability;
        double actualOutcome = example.outcome().valueFor(example.state().nextToAct());
        double outcomeError = Math.abs(predictedOutcome - actualOutcome);

        double actionError;
        double[] predictedMoveProbabilities = prediction.moveProbabilities;
        if (defaultBestAction) {
            double bestActualScore = 0;
            int bestActualIndex = 0;
            double bestPredictedScore = 0;
            int bestPredictedIndex = 0;

            for (int i = 0; i < example.moveScores().length; i++) {
                double actual = example.moveScores()[i];
                if (actual > bestActualScore) {
                    bestActualScore = actual;
                    bestActualIndex = i;
                }

                double movePrediction = predictedMoveProbabilities[i];
                if (movePrediction > bestPredictedScore) {
                    bestPredictedScore = movePrediction;
                    bestPredictedIndex = i;
                }
            }

            actionError = bestActualIndex == bestPredictedIndex
                    ? 0 : 1;
        }
        else {
            double squaredErrorSum = 0;
            for (int i = 0; i < example.legalMoves().length; i++) {
                double movePrediction = predictedMoveProbabilities[i];
                double actual = example.moveScores()[i];
                double error = actual - movePrediction;
                squaredErrorSum += error * error;
            }
            actionError = Math.sqrt(squaredErrorSum / example.legalMoves().length);
        }

        return new double[] {outcomeError, actionError};
    }


    private static double[] measureErrorMeta(
            MetaEstimate prediction,
            MoveHistory example)
    {
        double predictedOutcome = prediction.winProbability;
        double actualOutcome = example.outcome().valueFor(example.state().nextToAct());
        double outcomeError = Math.abs(predictedOutcome - actualOutcome);
        double errorError = Math.abs(prediction.winError - outcomeError);

        double actionError;
        double[] predictedMoveProbabilities = prediction.moveProbabilities;

        double bestActualScore = 0;
        int bestActualIndex = 0;
        double bestPredictedScore = 0;
        int bestPredictedIndex = 0;

        for (int i = 0; i < example.moveScores().length; i++) {
            double actual = example.moveScores()[i];
            if (actual > bestActualScore) {
                bestActualScore = actual;
                bestActualIndex = i;
            }

            double movePrediction = predictedMoveProbabilities[i];
            if (movePrediction > bestPredictedScore) {
                bestPredictedScore = movePrediction;
                bestPredictedIndex = i;
            }
        }

        actionError = bestActualIndex == bestPredictedIndex
                ? 0 : 1;

        return new double[] {outcomeError, actionError, errorError};
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static ComputationGraph createResidualNetwork2() {
        NnResBuilder builder = new NnResBuilder(192, Activation.LEAKYRELU);
//        NnBuilder builder = new NnBuilder(256, Activation.LEAKYRELU);

//        builder.addInitialSingleton();
        builder.addInitialConvolution();

        String body = builder.addResidualTower(2, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 32);
//        builder.addPolicyHead(body, 64);

//        builder.addValueHead(body, 16);
        builder.addValueHead(body, 32, 192);
//        builder.addValueHead(body, 64);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork3() {
        NnResBuilder builder = new NnResBuilder(256, Activation.LEAKYRELU);

        builder.addInitialSingleton();
//        builder.addInitialConvolution();

        String body = builder.addResidualTower(3, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 256);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork4() {
        NnResBuilder builder = new NnResBuilder(256, Activation.LEAKYRELU);

        builder.addInitialSingleton();
//        builder.addInitialConvolution();

        String body = builder.addResidualTower(4, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 256);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork5() {
        NnResBuilder builder = new NnResBuilder(256, Activation.RELU);

        builder.addInitialConvolution();

        String body = builder.addResidualTower(5, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 256);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork7() {
        NnResBuilder builder = new NnResBuilder(256, Activation.RELU);

        builder.addInitialConvolution();

        String body = builder.addResidualTower(7, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 256);
//        builder.addPolicyHead(body, 96);
//        builder.addValueHead(body, 64, 384);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork14() {
        NnResBuilder builder = new NnResBuilder(256, Activation.RELU);

        builder.addInitialConvolution();

        String body = builder.addResidualTower(14, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 256);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


//    public static ComputationGraph createDenseNetwork5x48() {
//        NnDenseBuilder builder = new NnDenseBuilder(Activation.RELU);
//
//        List<String> body = builder.addDenseTower(5, 48, NnDenseBuilder.layerInput);
//
//        builder.addPolicyHead(body, 64);
//        builder.addValueHead(body, 64, 256);
//
//        ComputationGraph net = new ComputationGraph(builder.build());
//        net.init();
//
//        return net;
//    }


    public static ComputationGraph createDenseNetwork5x48b() {
        NnDenseBuilder builder = new NnDenseBuilder(Activation.RELU);

        List<String> body = builder.addDenseTower(5, 48,48, NnDenseBuilder.layerInput);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 32, 384);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createDenseNetwork5x48c() {
        NnDenseBuilder builder = new NnDenseBuilder(Activation.RELU);

        List<String> body = builder.addDenseTower(5, 48,48, NnDenseBuilder.layerInput);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 384);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createDenseNetwork6x48() {
        NnDenseBuilder builder = new NnDenseBuilder(Activation.RELU);

        List<String> body = builder.addDenseTower(6, 48,48, NnDenseBuilder.layerInput);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 384);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createDenseNetwork6x48b() {
        NnDenseBuilder builder = new NnDenseBuilder(Activation.RELU);

        List<String> body = builder.addDenseTower(
                6, 256,48, NnDenseBuilder.layerInput);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 384);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createDenseNetwork7x48() {
        NnDenseBuilder builder = new NnDenseBuilder(Activation.RELU);

        List<String> body = builder.addDenseTower(
                7, 256,48, NnDenseBuilder.layerInput);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 384);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createDenseNetwork7x48b() {
        NnDenseBuilder builder = new NnDenseBuilder(Activation.RELU);

        List<String> body = builder.addDenseTower(
                7, 288,48, NnDenseBuilder.layerInput);

        builder.addPolicyHead(body, 96);
        builder.addValueHead(body, 64, 384);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork5Meta() {
        NnResBuilder builder = new NnResBuilder(256, Activation.RELU, true);

        builder.addInitialConvolution();

        String body = builder.addResidualTower(5, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 256);
        builder.addErrorHead(body, 64);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork10() {
        NnResBuilder builder = new NnResBuilder(256, Activation.LEAKYRELU);

        builder.addInitialConvolution();

        String body = builder.addResidualTower(10, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 256);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }


    public static ComputationGraph createResidualNetwork32() {
        NnResBuilder builder = new NnResBuilder(64, Activation.LEAKYRELU);

        builder.addInitialConvolution();

        String body = builder.addResidualTower(32, NnResBuilder.layerInitial);

        builder.addPolicyHead(body, 64);
        builder.addValueHead(body, 64, 64);

        ComputationGraph net = new ComputationGraph(builder.build());
        net.init();

        return net;
    }
}
