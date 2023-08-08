package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.util.List;


// TODO: rename and add Stockfish-based implementation
public interface PuctModel {
    PuctModel prototype();

    void load();

    void prepare(int pieceCount);

    PuctEstimate estimate(State state, int[] legalMoves);

    ImmutableList<PuctEstimate> estimateAll(List<PuctQuery> queries);


    default int nextPartition() {
        return 0;
    }
}
