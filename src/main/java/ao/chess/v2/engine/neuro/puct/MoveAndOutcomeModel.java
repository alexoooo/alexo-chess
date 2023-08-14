package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.util.List;


public interface MoveAndOutcomeModel {
    MoveAndOutcomeModel prototype();

    void load();

    void prepare(int pieceCount);

    MoveAndOutcomeProbability estimate(State state, int[] legalMoves, int moveCount);

    ImmutableList<MoveAndOutcomeProbability> estimateAll(List<MoveAndOutcomeQuery> queries);


    default int nextPartition() {
        return 0;
    }
}
