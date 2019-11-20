package ao.chess.v2.engine.heuristic.learn;

import ao.chess.v2.data.Location;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.Move;
import com.google.common.collect.Collections2;
import com.google.common.collect.Lists;
import org.deeplearning4j.datasets.iterator.impl.ListDataSetIterator;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.distribution.UniformDistribution;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.ConvolutionLayer;
import org.deeplearning4j.nn.conf.layers.DenseLayer;
import org.deeplearning4j.nn.conf.layers.OutputLayer;
import org.deeplearning4j.nn.conf.layers.SubsamplingLayer;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.api.memory.MemoryWorkspace;
import org.nd4j.linalg.api.memory.conf.WorkspaceConfiguration;
import org.nd4j.linalg.api.memory.enums.LocationPolicy;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.dataset.api.iterator.DataSetIterator;
import org.nd4j.linalg.factory.Nd4j;
import org.nd4j.linalg.learning.config.AdaGrad;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


public class MoveTrainer {
    //-----------------------------------------------------------------------------------------------------------------
    private static final boolean defaultBestAction = true;
    private static final boolean defaultValueAverage = true;
//    private static final boolean measureOutcome = false;

    private static final int miniBatchSize = 256;
    private static final int saveOnceEvery = 1_000_000;

    private static final int trainingIterations = 0;
//    private static final int trainingIterations = 1;
//    private static final int trainingIterations = 100;

//    private static final boolean testInitial = false;
    private static final boolean testInitial = true;

    private static final int seed = 42;
    private static final Random seededRandom = new Random(seed);


    public static final WorkspaceConfiguration wsConfig = WorkspaceConfiguration.builder()
            .policyLocation(LocationPolicy.RAM)
            .build();


    private static final List<Path> inputs =
//            mixRange(128, 999);
            mixRange(692, 999);
//    private static final List<Path> inputs = List.of(
//            Paths.get("lookup/mix/0.txt"),
//            Paths.get("lookup/mix/1.txt")
////            Paths.get("lookup/history/mix/champions_10000.txt")
////            Paths.get("lookup/history/mix/champions_1000.txt")
////            Paths.get("lookup/history/mix/champions_100.txt")
//    );

    private static List<Path> mixRange(int fromInclusive, int toInclusive) {
        List<Path> range = new ArrayList<>();
        for (int i = fromInclusive; i <= toInclusive; i++) {
            Path mixFile = Paths.get("lookup/mix/" + i + ".txt");
            range.add(mixFile);
        }
        return range;
    }

    private static final List<Path> test = List.of(
//            Paths.get("lookup/test_1000_20191024_190016_544.csv"),
//            Paths.get("lookup/think_1000_20191024_110657_082.csv"),
//            Paths.get("lookup/think_1000_20191024_135020_955.csv"),
//            Paths.get("lookup/think_1000_20191024_202610_148.csv"),
//            Paths.get("lookup/think_1000_20191025_152515_865.csv"),
//            Paths.get("lookup/think_1000_20191025_194455_822.csv"),
//            Paths.get("lookup/think_1000_20191026_185627_150.csv"),
//            Paths.get("lookup/think_1000_20191102_112411_359.csv"),
//            Paths.get("lookup/think_5000_20191107_161637_470.csv")
            Paths.get("lookup/history/mix/champions_10000.txt"),
            Paths.get("lookup/history/Adams.txt")
//            Paths.get("lookup/history/mix/champions_1000.txt")
//            Paths.get("lookup/history/mix/champions_100.txt")
    );


    private static final Path saveFile =
//            Paths.get("lookup/gen/3/nn.zip");
//            Paths.get("lookup/gen/5/nn.zip");
//            Paths.get("lookup/gen/5/nn-2.zip");
//            Paths.get("lookup/gen/6/nn-x.zip");
//            Paths.get("lookup/gen/8/nn.zip");
//            Paths.get("lookup/history/Carlsen-100.zip");
//            Paths.get("lookup/history/mix/champions-1000.zip");
//            Paths.get("lookup/history/mix/champions_2019-11-12.zip");
//            Paths.get("lookup/history/mix/champions-m4_2019-11-14.zip");
//            Paths.get("lookup/history/mix/all_mid_20191116.zip");
//            Paths.get("lookup/history/mix/all_mid_20191117b.zip");
//            Paths.get("lookup/history/mix/all_mid_batch_20191118b.zip");
            Paths.get("lookup/history/mix/all_mid_batch_20191120.zip");
//            Paths.get("lookup/history/mix/all_deep_20191119.zip");


    private static class Prediction {
        public final double[] actionProbabilities;
        public final double outcome;

        public Prediction(double[] actionProbabilities, double outcome) {
            this.actionProbabilities = actionProbabilities;
            this.outcome = outcome;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        boolean saved = Files.exists(saveFile);

        MultiLayerNetwork nn;
        if (saved) {
            nn = NeuralUtils.loadNeuralNetwork(saveFile, false);
        }
        else {
//            nn = createNeuralNetwork2();
//            nn = createNeuralNetwork3();
            nn = createNeuralNetwork4();
//            nn = createNeuralNetwork5();
        }

        if (saved && testInitial) {
            testOutputs(nn);
        }

        if (trainingIterations == 0) {
            return;
        }
//        List<MoveHistory> allMoves = readAllMoves();
//        System.out.println("Loaded moves: " + allMoves.size());

        for (int epoch = 0; epoch < trainingIterations; epoch++) {
            System.out.println("Training epoch = " + (epoch + 1));

            for (var input : inputs) {
                List<MoveHistory> inputMoves = readMoves(input);

                System.out.println("input: " + input + " - " + inputMoves.size());

                performTraining(inputMoves, epoch, nn);
            }

            testOutputs(nn);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static int flipIndexIfRequired(
            int locationIndex,
            boolean flip
    ) {
        int rank = Location.rankIndex(locationIndex);
        int file = Location.fileIndex(locationIndex);
        int adjustedRank = (flip ? Location.RANKS - rank - 1 : rank);
        int adjustedFile = (flip ? Location.FILES - file - 1 : file);
        return Location.squareIndex(adjustedRank, adjustedFile);
    }


    private static void performTraining(
            List<MoveHistory> allMoves,
            int epoch,
            MultiLayerNetwork nn
    ) {
        int trainingCount = 0;
        Collections.shuffle(allMoves, seededRandom);

        for (var partition : Lists.partition(allMoves, saveOnceEvery)) {
            long trainingStart = System.currentTimeMillis();

            try (MemoryWorkspace ignored = Nd4j.getWorkspaceManager()
                    .getAndActivateWorkspace(wsConfig, "MoveTrainer")
            ) {
                for (var miniBatch : Lists.partition(partition, miniBatchSize)) {
                    DataSetIterator miniBatchIterator = new ListDataSetIterator<>(
                            Collections2.transform(miniBatch, MoveTrainer::convertToDataSet),
                            miniBatch.size());

                    nn.fit(miniBatchIterator);
                }

                NeuralUtils.saveNeuralNetwork(nn, saveFile);
            }

            trainingCount += partition.size();
            System.out.println((epoch + 1) +
                    " - " + trainingCount + " of " + allMoves.size() +
                    " - Training took: " +
                    (double) (System.currentTimeMillis() - trainingStart) / 1000);

//            Nd4j.getWorkspaceManager().destroyAllWorkspacesForCurrentThread();
        }
    }


    public static List<MoveHistory> readMoves(Path input) {
        try (var lines = Files.lines(input)) {
            return lines.map(MoveHistory::new).collect(Collectors.toList());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static void testOutputs(MultiLayerNetwork nn)
    {
        DoubleSummaryStatistics globalOutcomeStats = new DoubleSummaryStatistics();
        DoubleSummaryStatistics globalActionStats = new DoubleSummaryStatistics();

        for (var testPath : test) {
            long predictStart = System.currentTimeMillis();
            DoubleSummaryStatistics outcomeStats = new DoubleSummaryStatistics();
            DoubleSummaryStatistics actionStats = new DoubleSummaryStatistics();

            try (var lines = Files.lines(testPath);
//                 MemoryWorkspace ignored = Nd4j.getWorkspaceManager()
//                         .getAndActivateWorkspace(wsConfig, "MoveTrainer")
            ) {
                lines.forEach(line -> {
                    MoveHistory example = new MoveHistory(line);

                    Prediction prediction = testExample(nn, example);

                    double[] errors = measureError(prediction, example);

                    outcomeStats.accept(errors[0]);
                    globalOutcomeStats.accept(errors[0]);

                    actionStats.accept(errors[1]);
                    globalActionStats.accept(errors[1]);
                });
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            double took = (double) (System.currentTimeMillis() - predictStart) / 1000;
            System.out.println("Path error: " +
                    outcomeStats.getAverage() + " / " + actionStats.getAverage() +
                    " - took: " + took + " - " + testPath);
        }

        System.out.println("Average error: " +
                globalOutcomeStats.getAverage() + " / " + globalActionStats.getAverage());
    }


    private static Prediction testExample(MultiLayerNetwork nn, MoveHistory example) {
        INDArray input = convertToDataSet(example).getFeatures();
        INDArray output = nn.output(input);

        double[] moveProbabilities = NeuralCodec.INSTANCE
                .decodeMoveProbabilities(output, example.state(), example.legalMoves());

        double outcome = NeuralCodec.INSTANCE
                .decodeOutcome(output);

        return new Prediction(moveProbabilities, outcome);
    }


    private static double[] measureError(
            Prediction prediction,
            MoveHistory example)
    {
        double predictedOutcome = prediction.outcome;
        double actualOutcome = example.outcome().valueFor(example.state().nextToAct());
        double outcomeError = Math.abs(predictedOutcome - actualOutcome);

        double actionError;
        double[] predictedMoveProbabilities = prediction.actionProbabilities;
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


    public static DataSet convertToDataSet(
            MoveHistory example
    ) {
        return convertToDataSet(example, defaultBestAction, defaultValueAverage);
    }


    public static DataSet convertToDataSet(
            MoveHistory example,
            boolean bestAction,
            boolean averageValue
    ) {
        INDArray reshapedFeatures = NeuralCodec.INSTANCE.encodeState(example.state());

        INDArray reshapedLabels =
                bestAction
                ? bestActionLabels(example, averageValue)
                : allActionLabels(example, averageValue);

        return new DataSet(reshapedFeatures, reshapedLabels);
    }


    private static INDArray allActionLabels(
            MoveHistory example, boolean averageValue)
    {
        // from square and to square, independent probabilities
        INDArray labels = Nd4j.zeros(Location.COUNT * 2 + 1);

        boolean flip = example.state().nextToAct() == Colour.BLACK;

        double[] fromLocationScores = example.fromLocationScores();
        double[] toLocationScores = example.toLocationScores();

        for (int i = 0; i < example.legalMoves().length; i++) {
            int move = example.legalMoves()[i];

            int fromSquareIndex = Move.fromSquareIndex(move);
            double fromScore =
                    fromLocationScores.length > fromSquareIndex
                            ? fromLocationScores[fromSquareIndex]
                            : 0;
            int adjustedFrom = flipIndexIfRequired(fromSquareIndex, flip);
            labels.put(adjustedFrom, Nd4j.scalar(fromScore));

            int toSquareIndex = Move.toSquareIndex(move);
            double toScore =
                    toLocationScores.length > toSquareIndex
                            ? toLocationScores[toSquareIndex]
                            : 0;
            int adjustedTo = flipIndexIfRequired(toSquareIndex, flip);
            labels.put(adjustedTo + Location.COUNT, Nd4j.scalar(toScore));
        }

        labels.put(Location.COUNT * 2, Nd4j.scalar(
                averageValue
                ? (example.outcomeScore() + example.outcomeScore()) / 2
                : example.outcomeScore()));

        return labels.reshape(1, Location.COUNT * 2 + 1);
    }


    private static INDArray bestActionLabels(
            MoveHistory example, boolean averageValue)
    {
        // from square and to square, independent probabilities
        INDArray labels = Nd4j.zeros(Location.COUNT * 2 + 1);

        boolean flip = example.state().nextToAct() == Colour.BLACK;

        double bestMoveScore = 0;
        int bestMoveIndex = 0;

        for (int i = 0; i < example.moveScores().length; i++) {
            double moveScore = example.moveScores()[i];

            if (moveScore > bestMoveScore) {
                bestMoveScore = moveScore;
                bestMoveIndex = i;
            }
        }

        int bestMove = example.legalMoves()[bestMoveIndex];

        int fromSquareIndex = Move.fromSquareIndex(bestMove);
        int adjustedFrom = flipIndexIfRequired(fromSquareIndex, flip);
        labels.put(adjustedFrom, Nd4j.scalar(1));

        int toSquareIndex = Move.toSquareIndex(bestMove);
        int adjustedTo = flipIndexIfRequired(toSquareIndex, flip);
        labels.put(adjustedTo + Location.COUNT, Nd4j.scalar(1));

        labels.put(Location.COUNT * 2, Nd4j.scalar(
                averageValue
                ? (example.outcomeScore() + example.expectedValueScore()) / 2
                : example.outcomeScore()));

        return labels.reshape(1, Location.COUNT * 2 + 1);
    }


    private static MultiLayerNetwork createNeuralNetwork() {
        double learningRate = 0.02;
        int numInputs = Figure.VALUES.length;
        int numHiddenNodes = 200;
        int numOutputs = 1;
        double momentum = 0.8;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
                .weightInit(WeightInit.XAVIER)
                .updater(new Nesterovs(learningRate, momentum))
                .list()
                .layer(0, new DenseLayer.Builder().nIn(numInputs).nOut(numHiddenNodes)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(1, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
                        .activation(Activation.LEAKYRELU)
                        .build())
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .activation(Activation.IDENTITY)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }


    // see: https://gist.github.com/maxpumperla/ec437ebc5cc70fee910d15278337ef41
    private static MultiLayerNetwork createNeuralNetwork2() {
        int height = Location.FILES;
        int width = Location.RANKS;
        int channels = Figure.VALUES.length;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
//                .l2(0.0005)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new AdaGrad(0.1))
                .list()
                .layer(0, new ConvolutionLayer.Builder(3, 3)
                        .nIn(channels).stride(1, 1).nOut(50).activation(Activation.LEAKYRELU).build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2).stride(1, 1).build())
                .layer(2, new ConvolutionLayer.Builder(3, 3)
                        .stride(1, 1).nOut(20).activation(Activation.LEAKYRELU).build())
                .layer(3, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2).stride(1, 1).build())
                .layer(4, new DenseLayer.Builder().activation(Activation.LEAKYRELU)
                        .nOut(500).build())
                .layer(5, new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .nOut(Location.COUNT * 2 + 1).activation(Activation.IDENTITY).build())
                .setInputType(InputType.convolutionalFlat(height, width, channels))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }


    // see: https://pdfs.semanticscholar.org/28a9/fff7208256de548c273e96487d750137c31d.pdf
    public static MultiLayerNetwork createNeuralNetwork3() {
        int height = Location.FILES;
        int width = Location.RANKS;
        int channels = Figure.VALUES.length + 2;

        //RmsProp
        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
//                .l2(0.0001)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)

//                .updater(new RmsProp(0.0015))
                .updater(new Adam())
//                .updater(new AdaDelta())
//                .updater(new AdaGrad(0.1))

                .list()

                .layer(new ConvolutionLayer.Builder(3, 3)
                        .nIn(channels)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(96)
                        .activation(Activation.RELU)
                        .weightInit(new UniformDistribution(-1.5e-7, 1.5e-7))
                        .build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU).nOut(384).build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU).nOut(384).build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .nOut(Location.COUNT * 2 + 1).activation(Activation.IDENTITY).build())

                .setInputType(InputType.convolutionalFlat(height, width, channels))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }


    public static MultiLayerNetwork createNeuralNetwork4() {
        int height = Location.FILES;
        int width = Location.RANKS;
        int channels = Figure.VALUES.length + 2;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)

                .l2(0.0001)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)

//                .updater(new RmsProp(0.0015, 0.9999, RmsProp.DEFAULT_RMSPROP_EPSILON))
                .updater(new Adam())
//                .updater(new AdaDelta())
//                .updater(new AdaGrad(0.1))

                .list()

                .layer(new ConvolutionLayer.Builder(1, 1)
                        .nIn(channels)
                        .stride(1, 1)
                        .padding(0, 0)
//                        .nOut(32)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .weightInit(new UniformDistribution(-1.5e-7, 1.5e-7))
                        .build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU)
                        .nOut(256)
                        .build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU)
                        .nOut(256)
                        .build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .nOut(Location.COUNT * 2 + 1)
                        .activation(Activation.IDENTITY)
                        .build())

                .setInputType(InputType.convolutional(height, width, channels))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }


    public static MultiLayerNetwork createNeuralNetwork5() {
        int height = Location.FILES;
        int width = Location.RANKS;
        int channels = Figure.VALUES.length + 2;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)

                .l2(0.0001)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)

//                .updater(new RmsProp(0.0015, 0.9999, RmsProp.DEFAULT_RMSPROP_EPSILON))
                .updater(new Adam())
//                .updater(new AdaDelta())
//                .updater(new AdaGrad(0.1))

                .list()

                .layer(new ConvolutionLayer.Builder(1, 1)
                        .nIn(channels)
                        .stride(1, 1)
                        .padding(0, 0)
//                        .nOut(32)
                        .nOut(64)
                        .activation(Activation.RELU)
                        .weightInit(new UniformDistribution(-1.5e-7, 1.5e-7))
                        .build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU)
                        .nOut(256)
                        .build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU)
                        .nOut(256)
                        .build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU)
                        .nOut(256)
                        .build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .nOut(Location.COUNT * 2 + 1)
                        .activation(Activation.IDENTITY)
                        .build())

                .setInputType(InputType.convolutional(height, width, channels))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }
}
