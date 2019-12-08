package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.data.Location;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.state.Outcome;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.graph.ElementWiseVertex;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.preprocessor.CnnToFeedForwardPreProcessor;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.HashMap;
import java.util.Map;


// https://github.com/eclipse/deeplearning4j-examples/blob/master/dl4j-examples/src/main/java/org/deeplearning4j/examples/convolution/alphagozero/DL4JAlphaGoZeroBuilder.java
public class NnBuilder {
    //-----------------------------------------------------------------------------------------------------------------
    public static final String layerInput = "input";
    public static final String layerInitialActivation = "init-act";
    public static final String layerHeadFrom = "out-from";
    public static final String layerHeadTo = "out-to";
    public static final String layerHeadOutcome = "out-outcome";

    private static final Activation bodyActivation = Activation.LEAKYRELU;


    //-----------------------------------------------------------------------------------------------------------------
    private final int bodyFilters;
    private final int headFilters;
    private final ComputationGraphConfiguration.GraphBuilder conf;


    public NnBuilder(
            int bodyFilters,
            int headFilters
    ) {
        this.bodyFilters = bodyFilters;
        this.headFilters = headFilters;

        this.conf = new NeuralNetConfiguration.Builder()
                .l2(0.0001)

//                .weightInit(WeightInit.XAVIER)
                .weightInit(WeightInit.RELU)

                .activation(bodyActivation)

                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)

                .updater(new Adam())

                .graphBuilder()

                .addInputs(layerInput)
                .setInputTypes(InputType.convolutional(
                        Location.FILES, Location.RANKS, NeuralCodec.inputChannels))

                .setOutputs(layerHeadFrom, layerHeadTo, layerHeadOutcome);
    }


    public void addInitialConvolution() {
        String convName = "init-conv";
        String bnName = "init-bn";

        conf.addLayer(convName,
                new ConvolutionLayer.Builder().kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(bodyFilters)
                        .build(),
                layerInput);

        conf.addLayer(bnName, new BatchNormalization.Builder().nOut(bodyFilters).build(), convName);

        conf.addLayer(layerInitialActivation,
                new ActivationLayer.Builder()
//                        .activation(bodyActivation)
                        .build(),
                bnName);
    }


    public String addNormActivationConvolution(String blockName, String inName) {
        String normName = blockName + "-bn";
        String activationName = blockName + "-act";
        String convolutionName = blockName + "-conv";

        conf.addLayer(normName, new BatchNormalization.Builder().build(), inName);

        conf.addLayer(activationName,
                new ActivationLayer.Builder()
//                        .activation(bodyActivation)
                        .build(),
                normName);

        conf.addLayer(convolutionName,
                new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(bodyFilters)
                        .build(),
                activationName);

        return convolutionName;
    }


    public String addResidual(int blockNumber, String inName) {
        String firstBlock = blockNumber + "-res-1" ;
        String secondBlock = blockNumber + "-res-2";
        String merge = blockNumber + "-add";

        String firstBlockOut = addNormActivationConvolution(firstBlock, inName);
        String secondBlockOut = addNormActivationConvolution(secondBlock, firstBlockOut);

        conf.addVertex(merge, new ElementWiseVertex(ElementWiseVertex.Op.Add), inName, secondBlockOut);

        return merge;
    }


    public String addResidualTower(int numBlocks, String inName) {
        String name = inName;
        for (int i = 0; i < numBlocks; i++) {
            name = addResidual(i, name);
        }
        return name;
    }


    public void addPolicyHead(String inName) {
        String convName = "policy_head_conv";
        String bnName = "policy_head_batch_norm";
        String actName = "policy_head_activation";

        conf.addLayer(convName,
                new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(headFilters)
//                        .nIn(bodyFilters)
                        .build(),
                inName);

        conf.addLayer(bnName,
                new BatchNormalization.Builder()
//                        .nOut(2)
                        .build(),
                convName);

        conf.addLayer(actName,
                new ActivationLayer.Builder()
//                        .activation(bodyActivation)
                        .build(),
                bnName);

        conf.addLayer(layerHeadFrom,
                new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nOut(Location.COUNT)
                        .weightInit(WeightInit.XAVIER)
                        .build(),
                actName);

        conf.addLayer(layerHeadTo,
                new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nOut(Location.COUNT)
                        .weightInit(WeightInit.XAVIER)
                        .build(),
                actName);

        Map<String, InputPreProcessor> preProcessorMap = new HashMap<>();
        preProcessorMap.put(layerHeadFrom, new CnnToFeedForwardPreProcessor(
                Location.FILES, Location.RANKS, headFilters));
        preProcessorMap.put(layerHeadTo, new CnnToFeedForwardPreProcessor(
                Location.FILES, Location.RANKS, headFilters));
        conf.setInputPreProcessors(preProcessorMap);
    }


    public void addValueHead(String inName) {
        String convName = "value_head_conv";
        String bnName = "value_head_batch_norm";
        String actName = "value_head_activation";
        String denseName = "value_head_dense";

        conf.addLayer(convName,
                new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(headFilters)
                        .build(),
                inName);

        conf.addLayer(bnName, new BatchNormalization.Builder().build(), convName);

        conf.addLayer(actName,
                new ActivationLayer.Builder()
//                        .activation(bodyActivation)
                        .build(),
                bnName);

        conf.addLayer(denseName, new DenseLayer.Builder().nOut(bodyFilters).build(), actName);

        conf.addLayer(layerHeadOutcome,
                new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nOut(Outcome.values.length)
                        .weightInit(WeightInit.XAVIER)
                        .build(),
                denseName);

        Map<String, InputPreProcessor> preProcessorMap = new HashMap<>();
        preProcessorMap.put(denseName, new CnnToFeedForwardPreProcessor(
                Location.FILES, Location.RANKS, headFilters));
        conf.setInputPreProcessors(preProcessorMap);
    }


    public ComputationGraphConfiguration build() {
        return conf.build();
    }
}
