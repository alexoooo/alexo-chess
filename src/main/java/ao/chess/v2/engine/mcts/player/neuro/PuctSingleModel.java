package ao.chess.v2.engine.mcts.player.neuro;

import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.state.State;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.nio.file.Path;


public class PuctSingleModel
        implements PuctModel
{
    private final Path savedNeuralNetwork;
    private final boolean computeGraph;

    private NeuralNetwork nn;


    public PuctSingleModel(
            Path savedNeuralNetwork,
            boolean computeGraph)
    {
        this.savedNeuralNetwork = savedNeuralNetwork;
        this.computeGraph = computeGraph;
    }


    @Override
    public PuctModel prototype()
    {
        return new PuctSingleModel(savedNeuralNetwork, computeGraph);
    }


    @Override
    public void load()
    {
        nn = NeuralUtils.loadNeuralNetwork(savedNeuralNetwork, true, computeGraph);
    }


    @Override
    public PuctEstimate estimate(State state, int[] legalMoves)
    {
        double[] moveProbabilities;
        double winProbability;

        if (computeGraph) {
            INDArray input = NeuralCodec.INSTANCE.encodeMultiState(state);

            INDArray[] outputs = ((ComputationGraph) nn).output(input);
            moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveMultiProbabilities(
                            outputs[0],
                            outputs[1],
                            state,
                            legalMoves);

            winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2]);
//            winProbability = NeuralCodec.INSTANCE.decodeMultiOutcomeMax(outputs[2]);
        }
        else {
            INDArray input = NeuralCodec.INSTANCE.encodeState(state);

            INDArray output = ((MultiLayerNetwork) nn).output(input);

            moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveProbabilities(output, state, legalMoves);

            winProbability = NeuralCodec.INSTANCE.decodeOutcome(output);
        }

        return new PuctEstimate(
                moveProbabilities,
                winProbability
        );
    }


    @Override
    public String toString() {
        return savedNeuralNetwork.getFileName().toString();
    }
}
