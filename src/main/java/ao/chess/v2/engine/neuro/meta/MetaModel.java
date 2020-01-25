package ao.chess.v2.engine.neuro.meta;

import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.util.List;


public interface MetaModel {
    MetaModel prototype();

    void load();

    MetaEstimate estimate(State state, int[] legalMoves);

    ImmutableList<MetaEstimate> estimateAll(
            List<MetaQuery> queries, double outcomeRange, double minOutcome);
}
