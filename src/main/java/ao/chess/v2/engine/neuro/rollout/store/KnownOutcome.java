package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Outcome;
import com.google.common.collect.ImmutableList;


public enum KnownOutcome {
    //-----------------------------------------------------------------------------------------------------------------
    // NB: as default value, 'Unknown' must be first (have ordinal zero)
    Unknown,

    Win,
    Loss,
    Draw;


    //-----------------------------------------------------------------------------------------------------------------
    public static final ImmutableList<KnownOutcome> values = ImmutableList.copyOf(values());


    //-----------------------------------------------------------------------------------------------------------------
    public static KnownOutcome ofOutcome(Outcome outcome, Colour fromPov) {
        if (outcome.winner() == fromPov) {
            return Win;
        }

        if (outcome.loser() == fromPov) {
            return Loss;
        }

        return Draw;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public double toValue() {
        switch (this) {
            case Unknown:
                return Double.NaN;

            case Win:
                return 1.0;

            case Loss:
                return 0.0;

            case Draw:
                return 0.5;

            default:
                throw new IllegalStateException();
        }
    }


    public KnownOutcome reverse() {
        switch (this) {
            case Unknown:
                return Unknown;

            case Win:
                return Loss;

            case Loss:
                return Win;

            case Draw:
                return Draw;

            default:
                throw new IllegalStateException();
        }
    }
}
