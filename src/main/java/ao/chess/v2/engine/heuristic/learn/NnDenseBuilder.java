package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.data.Location;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.state.Outcome;
import com.google.common.collect.ImmutableList;
import org.deeplearning4j.nn.api.OptimizationAlgorithm;
import org.deeplearning4j.nn.conf.ComputationGraphConfiguration;
import org.deeplearning4j.nn.conf.InputPreProcessor;
import org.deeplearning4j.nn.conf.NeuralNetConfiguration;
import org.deeplearning4j.nn.conf.inputs.InputType;
import org.deeplearning4j.nn.conf.layers.*;
import org.deeplearning4j.nn.conf.preprocessor.CnnToFeedForwardPreProcessor;
import org.deeplearning4j.nn.weights.WeightInit;
import org.nd4j.linalg.activations.Activation;
import org.nd4j.linalg.learning.config.Adam;
import org.nd4j.linalg.lossfunctions.LossFunctions;

import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class NnDenseBuilder {
    //-----------------------------------------------------------------------------------------------------------------
    public static final String layerInput = "input";
//    public static final String layerInitial = "init";
    public static final String layerHeadFrom = "out-from";
    public static final String layerHeadTo = "out-to";
    public static final String layerHeadOutcome = "out-outcome";
    public static final String layerHeadError = "out-error";


    //-----------------------------------------------------------------------------------------------------------------
//    private final int bodyFilters;
    private final Activation bodyActivation;
    private final ComputationGraphConfiguration.GraphBuilder conf;


    //-----------------------------------------------------------------------------------------------------------------
    public NnDenseBuilder(
//            int bodyFilters,
            Activation bodyActivation
    ) {
        this(/*bodyFilters,*/ bodyActivation, false);
    }


    public NnDenseBuilder(
//            int bodyFilters,
            Activation bodyActivation,
            boolean meta
    ) {
//        this.bodyFilters = bodyFilters;
        this.bodyActivation = bodyActivation;

        conf = new NeuralNetConfiguration.Builder()
                .l2(0.0001)

//                .weightInit(WeightInit.XAVIER)
                .weightInit(WeightInit.RELU)

                .optimizationAlgo(OptimizationAlgorithm.STOCHASTIC_GRADIENT_DESCENT)

                .updater(new Adam())

                .graphBuilder()

                .addInputs(layerInput)
                .setInputTypes(InputType.convolutional(
                        Location.FILES, Location.RANKS, NeuralCodec.inputChannels));

        if (meta) {
            conf.setOutputs(layerHeadFrom, layerHeadTo, layerHeadOutcome, layerHeadError);
        }
        else {
            conf.setOutputs(layerHeadFrom, layerHeadTo, layerHeadOutcome);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public String addNormActivationConvolution(
            String blockName, List<String> inNames, int filterCount
    ) {
        String normName = blockName + "-bn";
        String activationName = blockName + "-act";
        String convolutionName = blockName + "-conv";

        conf.addLayer(normName,
                new BatchNormalization.Builder().build(),
                inNames.toArray(String[]::new));

        conf.addLayer(activationName,
                new ActivationLayer.Builder()
                        .activation(bodyActivation)
                        .build(),
                normName);

        conf.addLayer(convolutionName,
                new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(filterCount)
                        .activation(Activation.IDENTITY)
                        .build(),
                activationName);

        return convolutionName;
    }


    public String addBlock(
            int blockNumber,
            List<String> inNames,
            int internalFilterCount,
            int outputFilterCount
    ) {
        String firstBlock = blockNumber + "-res-1" ;
        String secondBlock = blockNumber + "-res-2";

        String firstBlockOut = addNormActivationConvolution(firstBlock, inNames, internalFilterCount);
        return addNormActivationConvolution(secondBlock, List.of(firstBlockOut), outputFilterCount);
    }


    public List<String> addDenseTower(
            int numBlocks,
            int internalFilters,
            int filtersPerBlock,
            String inName
    ) {
        ImmutableList.Builder<String> builder = ImmutableList.builder();

        builder.add(inName);

        for (int i = 0; i < numBlocks; i++) {
            String name = addBlock(i, builder.build(), internalFilters, filtersPerBlock);
            builder.add(name);
        }

        return builder.build();
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void addPolicyHead(List<String> inNames, int headFilters) {
        String convName = "policy_head_conv";
        String bnName = "policy_head_batch_norm";
        String actName = "policy_head_activation";

        conf.addLayer(convName,
                new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(headFilters)
                        .activation(Activation.IDENTITY)
                        .build(),
                inNames.toArray(String[]::new));

        conf.addLayer(bnName,
                new BatchNormalization.Builder().build(),
                convName);

        conf.addLayer(actName,
                new ActivationLayer.Builder()
                        .activation(bodyActivation)
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


    public void addValueHead(List<String> inNames, int headFilters, int affineSize) {
        String convName = "value_head_conv";
        String convBnName = "value_head_conv_bn";
        String actName = "value_head_activation";
        String denseName = "value_head_dense";
        String denseBnName = "value_head_dense_bn";

        conf.addLayer(convName,
                new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(headFilters)
                        .activation(Activation.IDENTITY)
                        .build(),
                inNames.toArray(String[]::new));

        conf.addLayer(convBnName,
                new BatchNormalization.Builder().build(),
                convName);

        conf.addLayer(actName,
                new ActivationLayer.Builder()
                        .activation(bodyActivation)
                        .build(),
                convBnName);

        conf.addLayer(denseName,
                new DenseLayer.Builder()
                        .nOut(affineSize)
                        .activation(bodyActivation)
                        .build(),
                actName);

        conf.addLayer(denseBnName,
                new BatchNormalization.Builder().build(),
                denseName);

        conf.addLayer(layerHeadOutcome,
                new OutputLayer.Builder(LossFunctions.LossFunction.NEGATIVELOGLIKELIHOOD)
                        .activation(Activation.SOFTMAX)
                        .nOut(Outcome.values.length)
                        .weightInit(WeightInit.XAVIER)
                        .build(),
                denseBnName);

        Map<String, InputPreProcessor> preProcessorMap = new HashMap<>();
        preProcessorMap.put(denseName, new CnnToFeedForwardPreProcessor(
                Location.FILES, Location.RANKS, headFilters));
        conf.setInputPreProcessors(preProcessorMap);
    }


    public void addErrorHead(List<String> inNames, int headFilters, int affineSize) {
        String convName = "error_head_conv";
        String convBnName = "error_head_conv_bn";
        String actName = "error_head_activation";
        String denseName = "error_head_dense";
        String denseBnName = "error_head_dense_bn";

        conf.addLayer(convName,
                new ConvolutionLayer.Builder()
                        .kernelSize(3, 3)
                        .stride(1, 1)
                        .padding(1, 1)
                        .nOut(headFilters)
                        .activation(Activation.IDENTITY)
                        .build(),
                inNames.toArray(String[]::new));

        conf.addLayer(convBnName,
                new BatchNormalization.Builder().build(),
                convName);

        conf.addLayer(actName,
                new ActivationLayer.Builder()
                        .activation(bodyActivation)
                        .build(),
                convBnName);

        conf.addLayer(denseName,
                new DenseLayer.Builder()
                        .nOut(affineSize)
                        .activation(bodyActivation)
                        .build(),
                actName);

        conf.addLayer(denseBnName,
                new BatchNormalization.Builder().build(),
                denseName);

        conf.addLayer(layerHeadError,
                new OutputLayer.Builder(LossFunctions.LossFunction.MSE)
                        .activation(Activation.SIGMOID)
                        .nOut(1)
                        .weightInit(WeightInit.XAVIER)
                        .build(),
                denseBnName);

        Map<String, InputPreProcessor> preProcessorMap = new HashMap<>();
        preProcessorMap.put(denseName, new CnnToFeedForwardPreProcessor(
                Location.FILES, Location.RANKS, headFilters));
        conf.setInputPreProcessors(preProcessorMap);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public ComputationGraphConfiguration build() {
        return conf.build();
    }
}
