package ao.chess.v2.engine.heuristic.learn;

import ao.ai.classify.online.forest.OnlineRandomForest;
import ao.ai.classify.online.forest.Sample;
import ao.ai.ml.model.input.RealList;
import ao.ai.ml.model.output.MultiClass;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.DoubleSummaryStatistics;
import java.util.List;


public class MoveTrainer {
    private static final int modelSize = 512;


    public static void main(String[] args) throws IOException {
        List<Path> inputs = List.of(
                Paths.get("lookup/think_20191019_221110_888.csv"));
        Path test = Paths.get("lookup/think_20191019_203033_132.csv");

        OnlineRandomForest learner = new OnlineRandomForest(modelSize);

        for (Path input : inputs) {
            try (var lines = Files.lines(input)) {
                lines.forEach(line -> {
                    MoveExample example = new MoveExample(line);
                    RealList stateVector = example.stateInputVector();
                    List<MultiClass> movePositionSample = example.movePositionSample();
                    for (MultiClass output : movePositionSample) {
                        learner.learn(stateVector, output);
                    }
                });
            }
        }

        DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
        try (var lines = Files.lines(test)) {
            Sample sample = new Sample();

            lines.forEach(line -> {
                MoveExample example = new MoveExample(line);
                RealList stateVector = example.stateInputVector();

                sample.clear();

                randomSample(sample);
//                learner.sample(stateVector, sample);

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
                    double error = locationScores[i] - prediction;
                    errorSumOfSquares += error * error;
                }
                double meanRootSquareError = Math.sqrt(errorSumOfSquares / actualsCount);

                stats.accept(meanRootSquareError);
            });
        }

        System.out.println("Average error: " + stats.getAverage());
    }


    private static void randomSample(Sample sample) {
        for (int i = 0; i < modelSize; i++) {
            sample.learn(MultiClass.create((int) (Math.random() * 64)));
        }
    }
}
