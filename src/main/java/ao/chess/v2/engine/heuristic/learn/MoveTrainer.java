package ao.chess.v2.engine.heuristic.learn;

import ao.chess.v2.data.Location;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.Move;
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
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
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
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;


public class MoveTrainer {
    private static final boolean bestAction = true;

    private static final List<Path> inputs = List.of(
//            Paths.get("lookup/think_train_10000.csv"),
//            Paths.get("lookup/think_10000_20191021_122214_071.csv"),
//            Paths.get("lookup/think_1000_20191021_180745_069.csv"),
//            Paths.get("lookup/think_1000_20191022_000732_249-a1-s.csv")
            Paths.get("lookup/think_1000_20191022_000732_249-a1.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a2.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a3.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a4.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a5.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a6.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a7.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a8.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a9.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a10.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-a11.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-b.csv"),
            Paths.get("lookup/think_1000_20191022_000732_249-c.csv"),
            Paths.get("lookup/think_1000_20191022_094039_884.csv"),
            Paths.get("lookup/think_1000_20191022_210003_723.csv")
    );

    private static final Path test =
//        Paths.get("lookup/think_test_10000.csv");
            Paths.get("lookup/think_1000_20191021_180745_069.csv");
//            Paths.get("lookup/think_1000_20191022_000732_249-a1.csv");
//            Paths.get("lookup/think_1000_20191022_000732_249-a1-s.csv");


    private static final Path saveFile =
            Paths.get("lookup/nn_2019-10-24.zip");


    public static void main(String[] args) {
        boolean saved = Files.exists(saveFile);

//        MultiLayerNetwork nn = createNeuralNetwork3();
        MultiLayerNetwork nn;
        if (saved) {
            nn = loadNeuralNetwork(saveFile, false);
        }
        else {
//            nn = createNeuralNetwork2();
            nn = createNeuralNetwork3();
        }

        Consumer<MoveExample> randomLearner = example -> {};
        Function<MoveExample, double[]> randomTester = MoveTrainer::randomSample;

        Consumer<MoveExample> nnLearner = example -> {
            DataSet dataSet = convertToDataSet(example);
            nn.fit(dataSet);
        };
        Function<MoveExample, double[]> nnTester = (example) -> {
            INDArray input = convertToDataSet(example).getFeatures();
            INDArray output = nn.output(input);

            return NeuralCodec.INSTANCE.decodeMoveProbabilities(output, example.state(), example.legalMoves());
        };

//        Consumer<MoveExample> learner = randomLearner;
//        Function<MoveExample, double[]> tester = randomTester;
        Consumer<MoveExample> learner = nnLearner;
        Function<MoveExample, double[]> tester = nnTester;

        if (saved) {
            testOutputs(tester);
        }

        double min = Double.POSITIVE_INFINITY;
        for (int epoch = 0; epoch < 2; epoch++) {
            for (Path input : inputs) {
                iterateInputs(epoch, input, learner);

                if (learner == nnLearner) {
                    saveNeuralNetwork(nn, saveFile);
                }

                double error = testOutputs(tester);

                if (min > error) {
                    min = error;
                    System.out.println("^^^^ NEW BEST");
                }
            }
        }
    }


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


    private static void saveNeuralNetwork(
            MultiLayerNetwork neuralNetwork,
            Path saveFile
    ) {
        try {
            neuralNetwork.save(saveFile.toFile());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static MultiLayerNetwork loadNeuralNetwork(
            Path loadFile,
            boolean readOnly
    ) {
        try {
            System.out.println("> Loading: " + loadFile);
            long start = System.currentTimeMillis();
            MultiLayerNetwork nn = MultiLayerNetwork.load(loadFile.toFile(), ! readOnly);
            System.out.println("> Done, loading took " + (double) (System.currentTimeMillis() - start) / 1000);
            return nn;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static void iterateInputs(
            int epoch,
            Path input,
            Consumer<MoveExample> consumer
    ) {
        long trainingStart = System.currentTimeMillis();

        try (var lines = Files.lines(input)) {
            lines.forEach(line -> {
                MoveExample example = new MoveExample(line);

                consumer.accept(example);
            });
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        System.out.println((epoch + 1) +
                " - " + input +
                " - Training took: " +
                (double) (System.currentTimeMillis() - trainingStart) / 1000);
    }


    private static double testOutputs(Function<MoveExample, double[]> tester)
    {
        long predictStart = System.currentTimeMillis();
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

        try (var lines = Files.lines(test)) {
            lines.forEach(line -> {
                MoveExample example = new MoveExample(line);

                double[] prediction = tester.apply(example);

                double error = measureError(prediction, example);
                stats.accept(error);
            });
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        double took = (double) (System.currentTimeMillis() - predictStart) / 1000;
        System.out.println("Average error: " + stats.getAverage() + " - took: " + took);

        return stats.getAverage();
    }


    private static double measureError(
            double[] predictedMoveProbabilities,
            MoveExample example)
    {
        if (bestAction) {
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

                double prediction = predictedMoveProbabilities[i];
                if (prediction > bestPredictedScore) {
                    bestPredictedScore = prediction;
                    bestPredictedIndex = i;
                }
            }

            return bestActualIndex == bestPredictedIndex
                    ? 0 : 1;
        }
        else {
            double squaredErrorSum = 0;
            for (int i = 0; i < example.legalMoves().length; i++) {
                double prediction = predictedMoveProbabilities[i];
                double actual = example.moveScores()[i];
                double error = actual - prediction;
                squaredErrorSum += error * error;
            }
            return Math.sqrt(squaredErrorSum / example.legalMoves().length);
        }
    }


    private static double[] randomSample(MoveExample example) {
        double[] prediction = new double[example.legalMoves().length];

        double total = 0;
        for (int i = 0; i < prediction.length; i++) {
            double value = Math.random();
            prediction[i] = value;
            total += value;
        }

        for (int i = 0; i < prediction.length; i++) {
            prediction[i] /= total;
        }

        return prediction;
    }


    public static DataSet convertToDataSet(MoveExample example) {
        INDArray reshapedFeatures = NeuralCodec.INSTANCE.encodeState(example.state());

        INDArray reshapedLabels =
                bestAction
                ? bestActionLabels(example)
                : allActionLabels(example);

        return new DataSet(reshapedFeatures, reshapedLabels);
    }


    private static INDArray allActionLabels(MoveExample example)
    {
        // from square and to square, independent probabilities
        INDArray labels = Nd4j.zeros(Location.COUNT * 2);

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

        return labels.reshape(1, Location.COUNT * 2);
    }


    private static INDArray bestActionLabels(MoveExample example)
    {
        // from square and to square, independent probabilities
        INDArray labels = Nd4j.zeros(Location.COUNT * 2);

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

        return labels.reshape(1, Location.COUNT * 2);
    }


    private static MultiLayerNetwork createNeuralNetwork() {
        int seed = 42;
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
        int seed = 42;
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
                        .nOut(Location.COUNT * 2).activation(Activation.IDENTITY).build())
                .setInputType(InputType.convolutionalFlat(height, width, channels))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }


    // see: https://pdfs.semanticscholar.org/28a9/fff7208256de548c273e96487d750137c31d.pdf
    private static MultiLayerNetwork createNeuralNetwork3() {
        int seed = 42;
        int height = Location.FILES;
        int width = Location.RANKS;
        int channels = Figure.VALUES.length;

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

//                .layer(new ConvolutionLayer.Builder(3, 3)
//                        .stride(1, 1).padding(1, 1).nOut(256).activation(Activation.LEAKYRELU).build())
//
//                .layer(new ConvolutionLayer.Builder(3, 3)
//                        .stride(1, 1).padding(1, 1).nOut(384).activation(Activation.LEAKYRELU).build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU).nOut(384).build())

                .layer(new DenseLayer.Builder()
                        .activation(Activation.LEAKYRELU).nOut(384).build())

                .layer(new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .nOut(Location.COUNT * 2).activation(Activation.IDENTITY).build())

                .setInputType(InputType.convolutionalFlat(height, width, channels))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }
}
