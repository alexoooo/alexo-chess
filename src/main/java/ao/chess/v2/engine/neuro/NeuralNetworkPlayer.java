package ao.chess.v2.engine.neuro;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.learn.NeuralUtils;
import ao.chess.v2.state.State;
import org.deeplearning4j.nn.api.NeuralNetwork;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.deeplearning4j.nn.multilayer.MultiLayerNetwork;
import org.nd4j.linalg.api.ndarray.INDArray;

import java.nio.file.Path;


public class NeuralNetworkPlayer implements Player {
    public static NeuralNetworkPlayer load(
            Path savedNeuralNetwork,
            boolean computeGraph,
            boolean randomize)
    {
        NeuralNetwork nn = NeuralUtils.loadNeuralNetwork(savedNeuralNetwork, true, computeGraph);
        return new NeuralNetworkPlayer(nn, computeGraph, randomize);
    }


    private final NeuralNetwork nn;
    private final boolean computeGraph;
    private final boolean randomize;


    public NeuralNetworkPlayer(
            NeuralNetwork neuralNetwork,
            boolean computeGraph,
            boolean randomize
    ) {
        nn = neuralNetwork;
        this.computeGraph = computeGraph;
        this.randomize = randomize;
    }


    @Override
    public int move(
            State position,
            int timeLeft,
            int timePerMove,
            int timeIncrement
    ) {
        INDArray input = NeuralCodec.INSTANCE.encodeState(position);

        int[] legalMoves = position.legalMoves();
        if (legalMoves.length == 0) {
            return -1;
        }

        double[] moveProbabilities;
        if (computeGraph) {
            INDArray[] outputs = ((ComputationGraph) nn).output(input);
            moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveMultiProbabilities(
                            outputs[0],
                            outputs[1],
                            position,
                            legalMoves);
        }
        else {
            INDArray output = ((MultiLayerNetwork) nn).output(input);
            moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveProbabilities(output, position, legalMoves);
        }

        int maxMoveIndex = 0;
        double maxMoveProbability = 0;

        double smear = 0.2 / legalMoves.length;

        for (int i = 0; i < legalMoves.length; i++) {
            double probability = moveProbabilities[i];

//            double score = Math.random() * (probability + smear);
            double score =
                    randomize
                    ? Math.random() * (probability + smear)
                    : probability;

            if (score > maxMoveProbability) {
                maxMoveProbability = score;
                maxMoveIndex = i;
            }
        }

//        List<Integer> indexes = new ArrayList<>();
//        for (int i = 0; i < legalMoves.length; i++) {
//            indexes.add(i);
//        }
//        String moveStats = indexes.stream()
//                .sorted((a, b) -> -Double.compare(moveProbabilities[a], moveProbabilities[b]))
//                .map(i -> Move.toInputNotation(legalMoves[i]) + " (" + moveProbabilities[i] + ")")
//                .collect(Collectors.joining(" | "));
//        System.out.println("NN moves: " + moveStats);

        return legalMoves[maxMoveIndex];
    }


    @Override
    public void close() {}
}
