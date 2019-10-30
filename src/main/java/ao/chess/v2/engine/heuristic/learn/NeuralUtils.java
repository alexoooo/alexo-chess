package ao.chess.v2.engine.heuristic.learn;


import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


public enum NeuralUtils {
    ;


    public static void saveNeuralNetwork(
            MultiLayerNetwork neuralNetwork,
            Path saveFile
    ) {
        try {
            Files.createDirectories(saveFile.getParent());

            neuralNetwork.save(saveFile.toFile());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public static MultiLayerNetwork loadNeuralNetwork(
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

}
