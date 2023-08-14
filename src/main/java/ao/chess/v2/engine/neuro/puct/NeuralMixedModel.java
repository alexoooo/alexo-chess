package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.data.Location;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.State;
import com.google.common.collect.*;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import static com.google.common.base.Preconditions.checkArgument;


public class NeuralMixedModel
        implements MoveAndOutcomeModel
{
    //-----------------------------------------------------------------------------------------------------------------
    private static class Partition {
        List<PartitionPointer> pointers = new ArrayList<>();
        int size = 0;

        public void add(MoveAndOutcomeQuery query, int index) {
            PartitionPointer pointer;
            if (pointers.size() <= size) {
                pointer = new PartitionPointer();
                pointers.add(pointer);
            }
            else {
                pointer = pointers.get(size);
            }

            pointer.set(query, index);

            size++;
        }

        public void clear() {
            for (int i = 0; i < size; i++) {
                pointers.get(i).clear();
            }
            size = 0;
        }
    }


    private static class PartitionPointer {
        MoveAndOutcomeQuery query;
        int index = -1;

        public void set(MoveAndOutcomeQuery query, int index) {
            this.query = query;
            this.index = index;
        }

        public void clear() {
            query = null;
            index = -1;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final ImmutableRangeMap<Integer, Path> savedNeuralNetworks;
    private final int[] pieceCountToIndex;
    private final List<Partition> partitions;
    private final List<MoveAndOutcomeProbability> outputBuffer = new ArrayList<>();
    private final int[] hitCount;

    private final ComputationGraph[] nn;

    private INDArray features;
    private int[] propAttacks;
    private int[] oppAttacks;
    private double[] fromScores;
    private double[] toScores;
    private List<INDArray> batchFeatures;
//    private int pieceCountIndex = -1;


    //-----------------------------------------------------------------------------------------------------------------
    public NeuralMixedModel(
            RangeMap<Integer, Path> savedNeuralNetworks)
    {
        this.savedNeuralNetworks = ImmutableRangeMap.copyOf(savedNeuralNetworks);

        pieceCountToIndex = new int[33];
        Arrays.fill(pieceCountToIndex, -1);

        int nnIndex = 0;
        Map<Range<Integer>, Path> mapOfRanges = this.savedNeuralNetworks.asMapOfRanges();
        for (var e : mapOfRanges.entrySet()) {
            for (int pieceCount : ContiguousSet.create(e.getKey(), DiscreteDomain.integers())) {
                pieceCountToIndex[pieceCount] = nnIndex;
            }
            nnIndex++;
        }

        checkArgument(nnIndex > 0);

        nn = new ComputationGraph[nnIndex];

        partitions = new ArrayList<>();
        for (int i = 0; i < nnIndex; i++) {
            partitions.add(new Partition());
        }

        hitCount = new int[nnIndex];
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public MoveAndOutcomeModel prototype()
    {
        return new NeuralMixedModel(savedNeuralNetworks);
    }


    @Override
    public void load()
    {
        if (nn[0] != null) {
            return;
        }

        int nnIndex = 0;
        for (var nnPath : savedNeuralNetworks.asMapOfRanges().values()) {
            nn[nnIndex++] = (ComputationGraph) NeuralUtils.loadNeuralNetwork(
                    nnPath, true, true);
        }

        features = Nd4j.zeros(1, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
        propAttacks = new int[Location.COUNT];
        oppAttacks = new int[Location.COUNT];
        fromScores = new double[Location.COUNT];
        toScores = new double[Location.COUNT];

        batchFeatures = new ArrayList<>();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void prepare(int pieceCount)
    {
//        pieceCountIndex = pieceCountToIndex[pieceCount];
    }


    private int pieceCountIndex(int pieceCount)
    {
//        return pieceCountIndex;
        return pieceCountToIndex[pieceCount];
    }


    @Override
    public MoveAndOutcomeProbability estimate(State state, int[] legalMoves, int moveCount)
    {
        NeuralCodec.INSTANCE.encodeMultiState(
                state, features, propAttacks, oppAttacks);

        int pieceCount = state.pieceCount();
        int index = pieceCountIndex(pieceCount);
        hitCount[index]++;

        INDArray[] outputs = nn[index].output(features);

        double[] moveProbabilities = NeuralCodec.INSTANCE.decodeMoveMultiProbabilities(
                outputs[0],
                outputs[1],
                state,
                legalMoves,
                moveCount,
                fromScores,
                toScores,
                0);

        double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcomeWin(outputs[2]);
        double drawProbability = NeuralCodec.INSTANCE.decodeMultiOutcomeDraw(outputs[2]);

        return new MoveAndOutcomeProbability(
                moveProbabilities,
                winProbability,
                drawProbability
        );
    }


    @Override
    public ImmutableList<MoveAndOutcomeProbability> estimateAll(
            List<MoveAndOutcomeQuery> queries)
    {
        for (int i = 0; i < queries.size(); i++) {
            MoveAndOutcomeQuery query = queries.get(i);
            int pieceCount = query.state.pieceCount();
            int nnIndex = pieceCountIndex(pieceCount);
            hitCount[nnIndex]++;

            Partition partition = partitions.get(nnIndex);
            partition.add(query, i);
            outputBuffer.add(null);
        }

        for (int nnIndex = 0; nnIndex < nn.length; nnIndex++) {
            Partition partition = partitions.get(nnIndex);
            int size = partition.size;
            if (size == 0) {
                continue;
            }

            while (batchFeatures.size() < size) {
                int nextSize = batchFeatures.size() + 1;
                INDArray nextFeatures = Nd4j.zeros(
                        nextSize, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
                batchFeatures.add(nextFeatures);
            }

            INDArray sizedFeatures = batchFeatures.get(size - 1);

            for (int i = 0; i < size; i++) {
                MoveAndOutcomeQuery query = partition.pointers.get(i).query;
                NeuralCodec.INSTANCE.encodeMultiState(
                        query.state, sizedFeatures, propAttacks, oppAttacks, i);
            }

            INDArray[] outputs = nn[nnIndex].output(sizedFeatures);

            for (int i = 0; i < size; i++) {
                PartitionPointer pointer = partition.pointers.get(i);
                MoveAndOutcomeQuery query = pointer.query;

                double[] moveProbabilities = NeuralCodec.INSTANCE
                        .decodeMoveMultiProbabilities(
                                outputs[0],
                                outputs[1],
                                query.state,
                                query.legalMoves,
                                query.moveCount,
                                fromScores,
                                toScores,
                                i);

                double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcomeWin(outputs[2], i);
                double drawProbability = NeuralCodec.INSTANCE.decodeMultiOutcomeDraw(outputs[2], i);

                MoveAndOutcomeProbability estimate = new MoveAndOutcomeProbability(
                        moveProbabilities, winProbability, drawProbability);

                outputBuffer.set(pointer.index, estimate);
            }

            partition.clear();
        }

        ImmutableList<MoveAndOutcomeProbability> output = ImmutableList.copyOf(outputBuffer);
        outputBuffer.clear();
        return output;
    }


    @Override
    public String toString() {
        return Arrays.toString(hitCount);
    }
}
