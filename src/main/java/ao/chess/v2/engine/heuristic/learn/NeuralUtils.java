package ao.chess.v2.engine.heuristic.learn;


import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


public enum NeuralUtils {
    ;


    public static void saveNeuralNetwork(
            NeuralNetwork neuralNetwork,
            Path saveFile
    ) {
        try {
            Files.createDirectories(saveFile.getParent());

            if (neuralNetwork instanceof MultiLayerNetwork) {
                ((MultiLayerNetwork) neuralNetwork).save(saveFile.toFile());
            }
            else {
                ((ComputationGraph) neuralNetwork).save(saveFile.toFile());
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public static NeuralNetwork loadNeuralNetwork(
            Path loadFile,
            boolean readOnly,
            boolean computeGraph
    ) {
        try {
            System.out.println("> Loading: " + loadFile);
            long start = System.currentTimeMillis();

            if (computeGraph) {
                ComputationGraph nn = ComputationGraph.load(loadFile.toFile(), ! readOnly);
//                nn.setLearningRate();
//                nn.getUpdater();

                System.out.println("> Done, loading took " + (double) (System.currentTimeMillis() - start) / 1000);
                return nn;
            }
            else {
                MultiLayerNetwork nn = MultiLayerNetwork.load(loadFile.toFile(), ! readOnly);
                System.out.println("> Done, loading took " + (double) (System.currentTimeMillis() - start) / 1000);
                return nn;
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
