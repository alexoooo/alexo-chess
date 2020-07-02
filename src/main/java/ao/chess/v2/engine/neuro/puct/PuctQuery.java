package ao.chess.v2.engine.neuro.puct;

import ao.chess.v2.state.State;
import com.google.common.base.Preconditions;

import java.util.concurrent.CompletableFuture;

import static com.google.common.base.Preconditions.checkArgument;


class PuctQuery {
    public final State state;
    public final int[] legalMoves;
    public final int moveCount;
    public final CompletableFuture<PuctEstimate> result;


    public PuctQuery(State state, int[] legalMoves)
    {
        this(state, legalMoves, legalMoves.length);
    }

    public PuctQuery(State state, int[] legalMoves, int moveCount)
    {
        this.state = state;
        this.legalMoves = legalMoves;
        this.moveCount = moveCount;

        checkArgument(moveCount <= legalMoves.length);

        result = new CompletableFuture<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        PuctQuery puctQuery = (PuctQuery) o;
        return state.equalsIgnoringReversibleMovesAndCastlePath(puctQuery.state);
    }


    @Override
    public int hashCode() {
        return state.hashCode();
    }
}
