package ao.chess.v2.engine.neuro.meta;

import ao.chess.v2.data.Location;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;


public class MetaSingleModel
        implements MetaModel
{
    private final Path savedNeuralNetwork;

    private NeuralNetwork nn;
    private INDArray features;
    private int[] propAttacks;
    private int[] oppAttacks;
    private double[] fromScores;
    private double[] toScores;
    private List<INDArray> batchFeatures;


    public MetaSingleModel(
            Path savedNeuralNetwork)
    {
        this.savedNeuralNetwork = savedNeuralNetwork;
    }


    @Override
    public MetaModel prototype()
    {
        return new MetaSingleModel(savedNeuralNetwork);
    }


    @Override
    public void load()
    {
        if (nn != null) {
            return;
        }

        nn = NeuralUtils.loadNeuralNetwork(savedNeuralNetwork, true, true);

        features = Nd4j.zeros(1, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
        propAttacks = new int[Location.COUNT];
        oppAttacks = new int[Location.COUNT];
        fromScores = new double[Location.COUNT];
        toScores = new double[Location.COUNT];

        batchFeatures = new ArrayList<>();
    }


    @Override
    public MetaEstimate estimate(State state, int[] legalMoves)
    {
        NeuralCodec.INSTANCE.encodeMultiState(
                state, features, propAttacks, oppAttacks);

        INDArray[] outputs = ((ComputationGraph) nn).output(features);

        double[] moveProbabilities = NeuralCodec.INSTANCE
                .decodeMoveMultiProbabilities(
                        outputs[0],
                        outputs[1],
                        state,
                        legalMoves,
                        legalMoves.length,
                        fromScores,
                        toScores,
                        0);

        double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2]);
        double winError = outputs[3].getDouble(0, 0);

        return new MetaEstimate(
                moveProbabilities,
                winProbability,
                winError
        );
    }


    @Override
    public ImmutableList<MetaEstimate> estimateAll(
            List<MetaQuery> queries,
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

        ImmutableList.Builder<MetaEstimate> all = ImmutableList.builder();

        INDArray sizedFeatures = batchFeatures.get(size - 1);

        for (int i = 0; i < size; i++) {
            MetaQuery query = queries.get(i);
            NeuralCodec.INSTANCE.encodeMultiState(
                    query.state, sizedFeatures, propAttacks, oppAttacks, i);
        }

        INDArray[] outputs = ((ComputationGraph) nn).output(sizedFeatures);

        for (int i = 0; i < size; i++) {
            MetaQuery query = queries.get(i);

            double[] moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveMultiProbabilities(
                            outputs[0],
                            outputs[1],
                            query.state,
                            query.legalMoves,
                            query.legalMoves.length,
                            fromScores,
                            toScores,
                            i);

            double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2], i);
            double winError = outputs[3].getDouble(i, 0);

            MetaEstimate estimate = new MetaEstimate(
                    moveProbabilities, winProbability, winError);
            all.add(estimate);
        }

        return all.build();
    }


    @Override
    public String toString() {
        return savedNeuralNetwork.getFileName().toString();
    }
}
