package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.data.Location;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public class PuctEnsembleModel
        implements PuctModel
{
    //-----------------------------------------------------------------------------------------------------------------
    private static class Partition {
        List<PartitionPointer> pointers = new ArrayList<>();
        int size = 0;

        public void add(PuctQuery query, int index) {
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
        PuctQuery query;
        int index = -1;

        public void set(PuctQuery query, int index) {
            this.query = query;
            this.index = index;
        }

        public void clear() {
            query = null;
            index = -1;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final ImmutableList<Path> savedNeuralNetworks;
//    private final int[] pieceCountToIndex;
    private final List<Partition> partitions;
    private final List<PuctEstimate> outputBuffer = new ArrayList<>();
    private final int[] hitCount;

    private final ComputationGraph[] nn;

    private INDArray features;
    private int[] propAttacks;
    private int[] oppAttacks;
    private double[] fromScores;
    private double[] toScores;
    private List<INDArray> batchFeatures;
    private int nextPartition;


    //-----------------------------------------------------------------------------------------------------------------
    public PuctEnsembleModel(
            List<Path> savedNeuralNetworks)
    {
        this.savedNeuralNetworks = ImmutableList.copyOf(savedNeuralNetworks);

//        pieceCountToIndex = new int[33];
//        Arrays.fill(pieceCountToIndex, -1);
//
//        int nnIndex = 0;
//        Map<Range<Integer>, Path> mapOfRanges = this.savedNeuralNetworks.asMapOfRanges();
//        for (var e : mapOfRanges.entrySet()) {
//            for (int pieceCount : ContiguousSet.create(e.getKey(), DiscreteDomain.integers())) {
//                pieceCountToIndex[pieceCount] = nnIndex;
//            }
//            nnIndex++;
//        }
//
//        checkArgument(nnIndex > 0);

        nn = new ComputationGraph[savedNeuralNetworks.size()];

        partitions = new ArrayList<>();
        for (int i = 0; i < savedNeuralNetworks.size(); i++) {
            partitions.add(new Partition());
        }

        hitCount = new int[savedNeuralNetworks.size()];
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public PuctModel prototype()
    {
        return new PuctEnsembleModel(savedNeuralNetworks);
    }


    @Override
    public void load()
    {
        if (nn[0] != null) {
            return;
        }

        for (int nnIndex = 0; nnIndex < savedNeuralNetworks.size(); nnIndex++) {
            Path nnPath = savedNeuralNetworks.get(nnIndex);
            nn[nnIndex] = (ComputationGraph) NeuralUtils.loadNeuralNetwork(
                    nnPath, true, true);
        }

        features = Nd4j.zeros(1, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
        propAttacks = new int[Location.COUNT];
        oppAttacks = new int[Location.COUNT];
        fromScores = new double[Location.COUNT];
        toScores = new double[Location.COUNT];

        batchFeatures = new ArrayList<>();
    }


    @Override
    public void prepare(int pieceCount)
    {
        nextPartition = (int) (Math.random() * savedNeuralNetworks.size());
    }


    @Override
    public PuctEstimate estimate(State state, int[] legalMoves)
    {
        double[] moveProbabilities;
        double winProbability;

        NeuralCodec.INSTANCE.encodeMultiState(
                state, features, propAttacks, oppAttacks);

//        int pieceCount = state.pieceCount();
        int index = nextPartition;
        nextPartition = (int) (Math.random() * savedNeuralNetworks.size());

        hitCount[index]++;

        INDArray[] outputs = nn[index].output(features);

        moveProbabilities = NeuralCodec.INSTANCE
                .decodeMoveMultiProbabilities(
                        outputs[0],
                        outputs[1],
                        state,
                        legalMoves,
                        fromScores,
                        toScores);

        winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2]);

        return new PuctEstimate(
                moveProbabilities,
                winProbability
        );
    }


    @Override
    public ImmutableList<PuctEstimate> estimateAll(
            List<PuctQuery> queries,
            double outcomeRange,
            double minOutcome)
    {
        int nnIndex = nextPartition;
        nextPartition = (int) (Math.random() * savedNeuralNetworks.size());

        Partition partition = partitions.get(nnIndex);

        for (int i = 0; i < queries.size(); i++) {
            PuctQuery query = queries.get(i);
            hitCount[nnIndex]++;

            partition.add(query, i);
            outputBuffer.add(null);
        }

        int size = partition.size;
        if (size == 0) {
            return  ImmutableList.of();
        }

        while (batchFeatures.size() < size) {
            int nextSize = batchFeatures.size() + 1;
            INDArray nextFeatures = Nd4j.zeros(
                    nextSize, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
            batchFeatures.add(nextFeatures);
        }

        INDArray sizedFeatures = batchFeatures.get(size - 1);

        for (int i = 0; i < size; i++) {
            PuctQuery query = partition.pointers.get(i).query;
            NeuralCodec.INSTANCE.encodeMultiState(
                    query.state, sizedFeatures, propAttacks, oppAttacks, i);
        }

        INDArray[] outputs = nn[nnIndex].output(sizedFeatures);

        for (int i = 0; i < size; i++) {
            PartitionPointer pointer = partition.pointers.get(i);
            PuctQuery query = pointer.query;

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

            double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2], i);

            PuctEstimate estimate = new PuctEstimate(
                    moveProbabilities, winProbability,
                    outcomeRange, minOutcome);

            outputBuffer.set(pointer.index, estimate);
        }

        partition.clear();

        ImmutableList<PuctEstimate> output = ImmutableList.copyOf(outputBuffer);
        outputBuffer.clear();
        return output;
    }


    @Override
    public int nextPartition() {
        return nextPartition;
    }


    @Override
    public String toString() {
        return Arrays.toString(hitCount);
    }
}
