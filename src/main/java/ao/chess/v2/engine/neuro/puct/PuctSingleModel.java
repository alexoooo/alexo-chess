package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.data.Location;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class PuctSingleModel
        implements PuctModel
{
    private final Path savedNeuralNetwork;
    private final boolean computeGraph;

    private NeuralNetwork nn;
    private INDArray features;
    private int[] propAttacks;
    private int[] oppAttacks;
    private double[] fromScores;
    private double[] toScores;
    private List<INDArray> batchFeatures;


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

        features = Nd4j.zeros(1, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
        propAttacks = new int[Location.COUNT];
        oppAttacks = new int[Location.COUNT];
        fromScores = new double[Location.COUNT];
        toScores = new double[Location.COUNT];

        batchFeatures = new ArrayList<>();
    }


    @Override
    public PuctEstimate estimate(State state, int[] legalMoves)
    {
        double[] moveProbabilities;
        double winProbability;

        if (computeGraph) {
            NeuralCodec.INSTANCE.encodeMultiState(
                    state, features, propAttacks, oppAttacks);

            INDArray[] outputs = ((ComputationGraph) nn).output(features);

            moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveMultiProbabilities(
                            outputs[0],
                            outputs[1],
                            state,
                            legalMoves,
                            fromScores,
                            toScores);

            winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2]);
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
    public ImmutableList<PuctEstimate> estimateAll(
            List<PuctQuery> queries,
            double uncertainty,
            double outcomeRange,
            double minOutcome)
    {
        int size = queries.size();
        while (batchFeatures.size() < size)
        {
            int nextSize = batchFeatures.size() + 1;
            INDArray nextFeatures = Nd4j.zeros(
                    nextSize, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
            batchFeatures.add(nextFeatures);
        }

        ImmutableList.Builder<PuctEstimate> all = ImmutableList.builder();

        INDArray sizedFeatures = batchFeatures.get(size - 1);

        for (int i = 0; i < size; i++) {
            PuctQuery query = queries.get(i);
            NeuralCodec.INSTANCE.encodeMultiState(
                    query.state, sizedFeatures, propAttacks, oppAttacks, i);
        }

        INDArray[] outputs = ((ComputationGraph) nn).output(sizedFeatures);

        for (int i = 0; i < size; i++) {
            PuctQuery query = queries.get(i);

            double[] moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveMultiProbabilities(
                            outputs[0],
                            outputs[1],
                            query.state,
                            query.legalMoves,
                            fromScores,
                            toScores,
                            i);

            double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2], i);

            PuctEstimate estimate = new PuctEstimate(
                    moveProbabilities, winProbability,
                    uncertainty, outcomeRange, minOutcome);
            all.add(estimate);
        }

        return all.build();
    }


    @Override
    public String toString() {
        return savedNeuralNetwork.getFileName().toString();
    }
}
