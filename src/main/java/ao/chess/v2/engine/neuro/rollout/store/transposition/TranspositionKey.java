package ao.chess.v2.engine.neuro.rollout.store.transposition;


import java.util.Objects;


public class TranspositionKey {
    private final long hashHigh;
    private final long hashLow;


    public TranspositionKey(long hashHigh, long hashLow) {
        this.hashHigh = hashHigh;
        this.hashLow = hashLow;
    }


    public long hashHigh() {
        return hashHigh;
    }

    public long hashLow() {
        return hashLow;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        TranspositionKey that = (TranspositionKey) o;
        return hashHigh == that.hashHigh &&
                hashLow == that.hashLow;
    }

    @Override
    public int hashCode() {
        return Objects.hash(hashHigh, hashLow);
    }
}
