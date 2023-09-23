package ao.chess.v2.engine.neuro.rollout;


import ao.chess.v2.engine.neuro.rollout.store.KnownOutcome;

import static com.google.common.base.Preconditions.checkArgument;


public record RolloutSolutionThreshold(
        long minimumVisitCount,
        double uncertaintyPercent
) {
    public static final RolloutSolutionThreshold zero =
            new RolloutSolutionThreshold(Long.MAX_VALUE, 0);


    public RolloutSolutionThreshold {
        checkArgument(uncertaintyPercent >= 0);
        checkArgument(uncertaintyPercent <= 1.0/3);
    }


    public KnownOutcome checkKnownOutcome(double valueOrNan) {
        if (Double.isNaN(valueOrNan)) {
            return KnownOutcome.Unknown;
        }

        if (valueOrNan < uncertaintyPercent) {
            return KnownOutcome.Loss;
        }

        double winLowerBound = 1.0 - uncertaintyPercent;
        if (valueOrNan > winLowerBound) {
            return KnownOutcome.Win;
        }

        double halfUncertainty = uncertaintyPercent / 2;
        double drawLowerBound = 0.5 - halfUncertainty;
        double drawUpperBound = 0.5 + halfUncertainty;
        if (drawLowerBound < valueOrNan && valueOrNan < drawUpperBound) {
            return KnownOutcome.Draw;
        }

        return KnownOutcome.Unknown;
    }
}
