package ao.chess.v2.engine.neuro.meta;


public class MetaEstimate {
    public final double[] moveProbabilities;
    public final double winProbability;
    public final double winError;


    public MetaEstimate(
            double[] moveProbabilities,
            double winProbability,
            double winError
    ) {
        this.moveProbabilities = moveProbabilities;
        this.winProbability = winProbability;
        this.winError = winError;
    }
}
