package ao.chess.v2.engine.neuro.meta;

import ao.chess.v2.state.State;

import java.util.concurrent.CompletableFuture;


class MetaQuery {
    public final State state;
    public final int[] legalMoves;
    public final CompletableFuture<MetaEstimate> result;


    public MetaQuery(State state, int[] legalMoves)
    {
        this.state = state;
        this.legalMoves = legalMoves;

        result = new CompletableFuture<>();
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MetaQuery puctQuery = (MetaQuery) o;
        return state.equalsIgnoringReversibleMovesAndCastlePath(puctQuery.state);
    }


    @Override
    public int hashCode() {
        return state.hashCode();
    }
}
