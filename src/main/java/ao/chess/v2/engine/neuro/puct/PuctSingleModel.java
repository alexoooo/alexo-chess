package ao.chess.v2.engine.neuro.puct;

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


public class PuctSingleModel
        implements PuctModel
{
    private final Path savedNeuralNetwork;

    private NeuralNetwork nn;
    private INDArray features;
    private int[] propAttacks;
    private int[] oppAttacks;
    private double[] fromScores;
    private double[] toScores;
    private List<INDArray> batchFeatures;


    public PuctSingleModel(
            Path savedNeuralNetwork)
    {
        this.savedNeuralNetwork = savedNeuralNetwork;
    }


    @Override
    public PuctModel prototype()
    {
        return new PuctSingleModel(savedNeuralNetwork);
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
    public void prepare(int pieceCount) {
        // NB: NOOP
    }


    @Override
    public PuctEstimate estimate(State state, int[] legalMoves)
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
                        fromScores,
                        toScores);

        double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcomeWin(outputs[2]);
        double drawProbability = NeuralCodec.INSTANCE.decodeMultiOutcomeDraw(outputs[2]);

        return new PuctEstimate(moveProbabilities, winProbability, drawProbability);
    }


    @Override
    public ImmutableList<PuctEstimate> estimateAll(
            List<PuctQuery> queries)
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

            double[] moveProbabilities = NeuralCodec.INSTANCE.decodeMoveMultiProbabilities(
                    outputs[0],
                    outputs[1],
                    query.state,
                    query.legalMoves,
                    query.moveCount,
                    fromScores,
                    toScores,
                    i);

            double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2], i);
            double drawProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2], i);

            PuctEstimate estimate = new PuctEstimate(
                    moveProbabilities, winProbability, drawProbability);
            all.add(estimate);
        }

        return all.build();
    }


    @Override
    public String toString() {
        return savedNeuralNetwork.getFileName().toString();
    }
}
