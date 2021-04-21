package ao.chess.v2.engine.neuro.puct;


public class PuctEstimate {
    public final double[] moveProbabilities;

    public final double winProbability;
    public final double drawProbability;

//    public final double winProbability;


    public PuctEstimate(
            double[] moveProbabilities,
            double winProbability,
            double drawProbability
    ) {
        this.moveProbabilities = moveProbabilities;
        this.winProbability = winProbability;
        this.drawProbability = drawProbability;
    }


    public double expectedValue() {
        return winProbability + 0.5 * drawProbability;
    }


    public double certainty() {
        return Math.max(winProbability, Math.max(drawProbability, 1.0 - winProbability - drawProbability));
    }


//    public PuctEstimate(
//            double[] moveProbabilities,
//            double winProbability,
//            double outcomeRange,
//            double minOutcome
//    ) {
//        this.moveProbabilities = moveProbabilities;
//
//        this.winProbability = outcomeRange * winProbability + minOutcome;
//    }
}
