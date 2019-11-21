package ao.chess.v2.engine.mcts.player.neuro;


class PuctEstimate {
    public final double[] moveProbabilities;
    public final double winProbability;


    public PuctEstimate(
            double[] moveProbabilities,
            double winProbability
    ) {
        this.moveProbabilities = moveProbabilities;
        this.winProbability = winProbability;
    }
}
