package ao.chess.v2.engine.mcts.player.neuro;

import ao.chess.v2.state.State;

import java.util.concurrent.CompletableFuture;


class PuctQuery {
    public final State state;
    public final int[] legalMoves;
    public final CompletableFuture<PuctEstimate> result;


    public PuctQuery(State state, int[] legalMoves)
    {
        this.state = state;
        this.legalMoves = legalMoves;

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
