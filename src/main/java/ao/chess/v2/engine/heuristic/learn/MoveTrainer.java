package ao.chess.v2.engine.heuristic.learn;

import ao.ai.classify.online.forest.OnlineRandomForest;
import ao.ai.classify.online.forest.Sample;
import ao.ai.ml.model.input.RealList;
import ao.ai.ml.model.output.MultiClass;
import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.MultiLayerConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
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
import org.nd4j.linalg.learning.config.AdaDelta;
import org.nd4j.linalg.learning.config.Nesterovs;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.function.Consumer;


public class MoveTrainer {
    private static final int modelSize = 512;

    private static final List<Path> inputs = List.of(
            Paths.get("lookup/think_train_10000.csv"),
            Paths.get("lookup/think_10000_20191021_122214_071.csv"));

    private static final Path test = Paths.get("lookup/think_test_10000.csv");


    public static void main(String[] args) {
        OnlineRandomForest forest = new OnlineRandomForest(modelSize);
//        var nn = createNeuralNetwork();
        var nn = createNeuralNetwork2();

        Consumer<MoveExample> randomLearner = example -> {};
        BiConsumer<MoveExample, Sample> randomTester = (example, sample) -> {
            randomSample(sample);
        };

        Consumer<MoveExample> forestLearner = example -> {
            RealList stateVector = example.stateInputVector();
            List<MultiClass> movePositionSample = example.movePositionSample();
            for (MultiClass output : movePositionSample) {
                forest.learn(stateVector, output);
            }
        };
        BiConsumer<MoveExample, Sample> forestTester = (example, sample) -> {
            RealList stateVector = example.stateInputVector();
            forest.sample(stateVector, sample);
        };

        Consumer<MoveExample> nnLearner = example -> {
            DataSet dataSet = convertToDataSet(example);
            nn.fit(dataSet);
        };
        BiConsumer<MoveExample, Sample> nnTester = (example, sample) -> {
            INDArray input = convertToDataSet(example).getFeatures();
            INDArray output = nn.output(input);
            for (int i = 0; i < Location.COUNT; i++) {
                double value = Math.max(0, output.getDouble(0, i));
                sample.set(i, value);
            }
        };

//        Consumer<MoveExample> learner = randomLearner;
//        BiConsumer<MoveExample, Sample> tester = randomTester;
//        Consumer<MoveExample> learner = forestLearner;
//        BiConsumer<MoveExample, Sample> tester = forestTester;
        Consumer<MoveExample> learner = nnLearner;
        BiConsumer<MoveExample, Sample> tester = nnTester;

        double min = Double.POSITIVE_INFINITY;
        for (int epoch = 0; epoch < 10_000; epoch++) {
            iterateInputs(epoch, learner);
            double error = testOutputs(tester);

            if (min > error) {
                min = error;
                System.out.println("^^^^ NEW BEST");
            }
        }
    }


    private static void iterateInputs(
            int epoch,
            Consumer<MoveExample> consumer
    ) {
        long trainingStart = System.currentTimeMillis();

        for (Path input : inputs) {
            try (var lines = Files.lines(input)) {
                lines.forEach(line -> {
                    MoveExample example = new MoveExample(line);

                    consumer.accept(example);
                });
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        System.out.println((epoch + 1) +
                " - Training took: " +
                (double) (System.currentTimeMillis() - trainingStart) / 1000);
    }


    private static double testOutputs(BiConsumer<MoveExample, Sample> tester)
    {
        long predictStart = System.currentTimeMillis();
        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();

        try (var lines = Files.lines(test)) {
            Sample sample = new Sample();

            lines.forEach(line -> {
                MoveExample example = new MoveExample(line);

                sample.clear();

                tester.accept(example, sample);

                double error = measureError(sample, example);
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
            Sample sample,
            MoveExample example)
    {
        double sampleTotal = sample.totalCount();

        double[] locationScores = example.locationScores();

        int actualsCount = 0;
        double errorSumOfSquares = 0;

        for (int i = 0; i < locationScores.length; i++) {
            double actual = locationScores[i];
            if (actual == 0) {
                continue;
            }
            actualsCount++;

            double prediction = sample.get(i) / sampleTotal;
//                    double prediction = sample.get(i);
            double error = locationScores[i] - prediction;
            errorSumOfSquares += error * error;
        }
        return Math.sqrt(errorSumOfSquares / actualsCount);
    }



    private static void randomSample(Sample sample) {
        for (int i = 0; i < modelSize; i++) {
            sample.learn(MultiClass.create((int) (Math.random() * 64)));
        }
    }



    public static DataSet convertToDataSet(MoveExample example) {
//        INDArray features = Nd4j.zeros(Location.COUNT, Figure.VALUES.length); // figures
//        INDArray features = Nd4j.zeros(Location.COUNT * Figure.VALUES.length); // figures
        INDArray features = Nd4j.zeros(6, 8, 8); // figures

        INDArray labels = Nd4j.zeros(Location.COUNT); // from square

        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
//                int index = Location.squareIndex(rank, file);

                Piece piece = example.state().pieceAt(rank, file);
                if (piece == null) {
                    continue;
                }

                boolean isNextToAct = piece.colour() == example.state().nextToAct();
                double value = (isNextToAct ? 1 : -1);

                Figure figure = piece.figure();

//                features.put(index, figure.ordinal(), value);
//                features.putScalar(index * Figure.VALUES.length + figure.ordinal(), value);
                features.put(new int[] {figure.ordinal(), rank, file}, Nd4j.scalar(value));
//                features.put(new PointIndex[] {new PointIndex(index * Figure.VALUES.length + figure.ordinal())}, value);
            }
        }

        long[] shape = features.shape();
        features = features.reshape(1, shape[0], shape[1], shape[2]);

        double[] locationScores = example.locationScores();
        for (int i = 0; i < example.legalMoves().length; i++) {
            int move = example.legalMoves()[i];
            int from = Move.fromSquareIndex(move);

            double score = locationScores[i];

            labels.put(from, Nd4j.scalar(score));
        }
        labels = labels.reshape(1, 64);

        return new DataSet(features, labels);
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
//                .layer(2, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
//                        .activation(Activation.LEAKYRELU)
//                        .build())
//                .layer(3, new DenseLayer.Builder().nIn(numHiddenNodes).nOut(numHiddenNodes)
//                        .activation(Activation.LEAKYRELU)
//                        .build())
//                .layer(4, new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                .layer(2, new OutputLayer.Builder(LossFunctions.LossFunction.L2)
//                .layer(1, new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .activation(Activation.IDENTITY)
                        .nIn(numHiddenNodes).nOut(numOutputs).build())
//                .pretrain(false)
//                .backprop(true)
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }


    private static MultiLayerNetwork createNeuralNetwork2() {
        int numHiddenNodes = 128;
        int seed = 42;
        int height = Location.FILES;
        int width = Location.RANKS;
        int channels = Figure.VALUES.length;

        MultiLayerConfiguration conf = new NeuralNetConfiguration.Builder()
                .seed(seed)
//                .l2(0.0005)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new AdaDelta())
                .list()
//                .layer(0, convInit("cnn1", channels, 50 , new int[]{5, 5}, new int[]{1, 1}, new int[]{0, 0}, 0))
                .layer(0, new ConvolutionLayer.Builder(2, 2)
                        .nIn(6)
                        .stride(1, 1)
                        .nOut(20)
                        .activation(Activation.IDENTITY)
                        .build())
                .layer(1, new SubsamplingLayer.Builder(SubsamplingLayer.PoolingType.MAX)
                        .kernelSize(2, 2).stride(1, 1).build())
                .layer(2, new DenseLayer.Builder().activation(Activation.LEAKYRELU)
                        .nOut(numHiddenNodes).build())
                .layer(3, new OutputLayer.Builder(LossFunctions.LossFunction.L2)
                        .activation(Activation.IDENTITY)
                        .nIn(numHiddenNodes)
                        .nOut(64).build())
                .setInputType(InputType.convolutionalFlat(height, width, channels))
                .build();

        MultiLayerNetwork net = new MultiLayerNetwork(conf);
        net.init();

        return net;
    }


    private static ConvolutionLayer convInit(String name, int in, int out, int[] kernel, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(kernel, stride, pad).name(name).nIn(in).nOut(out).biasInit(bias).build();
    }

    private static ConvolutionLayer conv3x3(String name, int out, double bias) {
        return new ConvolutionLayer.Builder(new int[]{3,3}, new int[] {1,1}, new int[] {1,1}).name(name).nOut(out).biasInit(bias).build();
    }

    private static ConvolutionLayer conv5x5(String name, int out, int[] stride, int[] pad, double bias) {
        return new ConvolutionLayer.Builder(new int[]{5,5}, stride, pad).name(name).nOut(out).biasInit(bias).build();
    }

    private static SubsamplingLayer maxPool(String name,  int[] kernel) {
        return new SubsamplingLayer.Builder(kernel, new int[]{2,2}).name(name).build();
    }

//    private static  DenseLayer fullyConnected(String name, int out, double bias, double dropOut, Distribution dist) {
//        return new DenseLayer.Builder().name(name).nOut(out).biasInit(bias).dropOut(dropOut).dist(dist).build();
//    }
}
