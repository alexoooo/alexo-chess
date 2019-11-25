package ao.chess.v2.engine.heuristic.learn;


import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.learning.config.Adam;


// https://github.com/eclipse/deeplearning4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/convolution/alphagozero/DL4JAlphaGoZeroBuilder.java
public class NnBuilder {
    private ComputationGraphConfiguration.GraphBuilder conf;


    public NnBuilder() {
        this.conf = new NeuralNetConfiguration.Builder()
                .l2(0.0001)
                .weightInit(WeightInit.XAVIER)
                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)
                .updater(new Adam())
                .graphBuilder();
    }


    public void addInputs(String name) {
        conf.addInputs(name);
    }


    public void addOutputs(String... names) {
        conf.setOutputs(names);
    }


    public ComputationGraphConfiguration build() {
        return conf.build();
    }
}
