package ao.chess.v2.engine.neuro;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import ao.chess.v2.engine.neuro.puct.PuctModel;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;


public class NeuralNetworkPlayer implements Player {
    private static final double estimateUncertainty = 0.01;


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
        if (legalMoves == null || legalMoves.length == 0) {
            return -1;
        }

        Colour pov = position.nextToAct();

        puctModel.prepare(position.pieceCount());
        PuctEstimate estimate = puctModel.estimate(position, legalMoves);

        double[] moveProbabilities = estimate.moveProbabilities;

        int maxMoveIndex = 0;
        double maxMoveProbability = 0;

        double moveUncertainty = estimateUncertainty / moveProbabilities.length;
        double denominator = 1.0 + estimateUncertainty;

        for (int i = 0; i < legalMoves.length; i++) {
            byte reversibleMoves = position.reversibleMoves();
            byte castles = position.castles();
            long castlePath = position.castlePath();

            int move = Move.apply(legalMoves[i], position);
            int[] opponentLegalMoves = position.legalMoves();
            int opponentMoveCount = opponentLegalMoves == null ? -1 : opponentLegalMoves.length;

            Outcome moveOutcome = position.knownOutcomeOrNull(opponentMoveCount);

            double scoreBonus = 0;
            if (moveOutcome == null && position.pieceCount() <= DeepOracle.instancePieceCount) {
                DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(position);
                if (deepOutcome == null) {
                    throw new IllegalStateException("Missing tablebase: " + position);
                }

                if (deepOutcome.outcome().winner() == pov) {
                    scoreBonus = 1000 - deepOutcome.plyDistance();
                }
                else {
                    moveOutcome = deepOutcome.outcome();
                }
            }

            Move.unApply(move, position);
            position.restore(reversibleMoves, castles, castlePath);

            if (moveOutcome != null) {
                if (moveOutcome.loser() == pov) {
                    // non-viable move, leads to self-mate
                    continue;
                }

                if (moveOutcome.winner() == pov) {
                    return legalMoves[i];
                }

                if (moveOutcome == Outcome.DRAW && estimate.winProbability > 0.5) {
                    continue;
                }
            }

            double probability = (estimate.moveProbabilities[i] + moveUncertainty) / denominator;
            double score = probability * probability * Math.random() + scoreBonus;
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
