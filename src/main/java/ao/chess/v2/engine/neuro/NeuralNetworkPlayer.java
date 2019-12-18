package ao.chess.v2.engine.neuro;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import ao.chess.v2.engine.neuro.puct.PuctModel;
import ao.chess.v2.state.State;


public class NeuralNetworkPlayer implements Player {
    public static NeuralNetworkPlayer load(
            PuctModel puctModel,
            boolean randomize)
    {
        puctModel.load();
        return new NeuralNetworkPlayer(puctModel, randomize);
    }


    private final PuctModel puctModel;
    private final boolean randomize;


    public NeuralNetworkPlayer(
            PuctModel puctModel,
            boolean randomize
    ) {
        this.puctModel = puctModel;
        this.randomize = randomize;
    }


    @Override
    public int move(
            State position,
            int timeLeft,
            int timePerMove,
            int timeIncrement
    ) {
        int[] legalMoves = position.legalMoves();
        if (legalMoves.length == 0) {
            return -1;
        }

        PuctEstimate estimate = puctModel.estimate(position, legalMoves);

        double[] moveProbabilities = estimate.moveProbabilities;

        int maxMoveIndex = 0;
        double maxMoveProbability = 0;

        double smear =
//                0.2 / legalMoves.length;
                0.0;

        for (int i = 0; i < legalMoves.length; i++) {
            double probability = moveProbabilities[i];

            double score =
                    randomize
                    ? Math.random() * (probability + smear)
                    : probability;

            if (score > maxMoveProbability) {
                maxMoveProbability = score;
                maxMoveIndex = i;
            }
        }

        return legalMoves[maxMoveIndex];
    }


    @Override
    public void close() {}
}
