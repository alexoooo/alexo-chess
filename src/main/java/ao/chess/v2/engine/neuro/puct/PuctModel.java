package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.util.List;


public interface PuctModel {
    PuctModel prototype();

    void load();

    PuctEstimate estimate(State state, int[] legalMoves);

    ImmutableList<PuctEstimate> estimateAll(
            List<PuctQuery> queries, double uncertainty, double outcomeRange, double minOutcome);
}
