package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.util.List;


public interface PuctModel {
    PuctModel prototype();

    void load();

    void prepare(int pieceCount);

    PuctEstimate estimate(State state, int[] legalMoves);

    ImmutableList<PuctEstimate> estimateAll(
            List<PuctQuery> queries, double outcomeRange, double minOutcome);


    default int nextPartition() {
        return 0;
    }
}
