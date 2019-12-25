package ao.chess.v2.engine.neuro.puct;


public class PuctEstimate {
    public final double[] moveProbabilities;
    public final double winProbability;


    public PuctEstimate(
            double[] moveProbabilities,
            double winProbability
    ) {
        this.moveProbabilities = moveProbabilities;
        this.winProbability = winProbability;
    }


    public PuctEstimate(
            double[] moveProbabilities,
            double winProbability,
//            double uncertainty,
            double outcomeRange,
            double minOutcome
    ) {
//        PuctUtils.smearProbabilities(
//                moveProbabilities, uncertainty);
        this.moveProbabilities = moveProbabilities;

        this.winProbability = outcomeRange * winProbability + minOutcome;
    }
}
