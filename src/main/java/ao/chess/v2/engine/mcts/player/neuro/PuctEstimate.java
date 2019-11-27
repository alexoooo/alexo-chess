package ao.chess.v2.engine.mcts.player.neuro;


public class PuctEstimate {
    public final double[] moveProbabilities;
    public double winProbability;


    public PuctEstimate(
            double[] moveProbabilities,
            double winProbability
    ) {
        this.moveProbabilities = moveProbabilities;
        this.winProbability = winProbability;
    }


    public void postProcess(
            double uncertainty,
            double outcomeRange,
            double minOutcome
    ) {
        PuctUtils.smearProbabilities(
                moveProbabilities, uncertainty);

        winProbability = outcomeRange * winProbability + minOutcome;
    }
}
