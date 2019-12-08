package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.data.Location;
import ao.chess.v2.engine.neuro.NeuralCodec;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.learning.config.Adam;


// https://github.com/eclipse/deeplearning4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/convolution/alphagozero/DL4JAlphaGoZeroBuilder.java
public class NnBuilder {
    private ComputationGraphConfiguration.GraphBuilder conf;


    public NnBuilder() {
        this.conf = new NeuralNetConfiguration.Builder()
                .l2(0.0001)

//                .weightInit(WeightInit.XAVIER)
                .weightInit(WeightInit.RELU)

                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)

                .updater(new Adam())

                .graphBuilder()

                .addInputs("input")
                .setInputTypes(InputType.convolutional(
                        Location.FILES, Location.RANKS, NeuralCodec.inputChannels))

                .setOutputs("out-from", "out-to", "out-outcome");
    }


    public ComputationGraphConfiguration build() {
        return conf.build();
    }
}
