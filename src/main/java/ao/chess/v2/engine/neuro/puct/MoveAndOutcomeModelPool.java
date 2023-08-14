package ao.chess.v2.engine.neuro.puct;


import ao.chess.v2.state.State;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;

import java.util.*;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.atomic.LongAdder;

import static com.google.common.base.Preconditions.checkState;


public class MoveAndOutcomeModelPool
        implements AutoCloseable
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final long accumulateDurationNanos = 50_000_000;
//    private static final long accumulateDurationNanos = 100_000_000;


    //-----------------------------------------------------------------------------------------------------------------
    private final int batchSize;
    private final MoveAndOutcomeModel model;

    private final BlockingDeque<MoveAndOutcomeQuery> queryQueue;
    private Thread worker;
    private volatile boolean isRoot = false;
    private int previousPieceCount = -1;

    private final Cache<CacheKey, MoveAndOutcomeProbability> cache = CacheBuilder.newBuilder()
            .concurrencyLevel(1)
//            .maximumSize(1024 * 1024)
//            .maximumSize(4 * 1024 * 1024)
//            .maximumSize(8 * 1024 * 1024)
            .maximumSize(32 * 1024 * 1024)
            .build();

    private final LongAdder cacheHits = new LongAdder();
    private final LongAdder localCacheHits = new LongAdder();


    //-----------------------------------------------------------------------------------------------------------------
    public MoveAndOutcomeModelPool(
            int batchSize,
            MoveAndOutcomeModel model)
    {
        this.batchSize = batchSize;
        this.model = model;

        queryQueue = new LinkedBlockingDeque<>();
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void restart(int pieceCount)
    {
        if (previousPieceCount == pieceCount && worker != null) {
            return;
        }
        previousPieceCount = pieceCount;

        if (worker != null) {
            close();
        }

        start(pieceCount);
    }


    public void start(int pieceCount)
    {
        model.prepare(pieceCount);

        worker = new Thread(this::workerLoop);
        worker.start();
    }


    @Override
    public void close() {
        if (worker == null) {
            return;
        }

        worker.interrupt();
        try {
            worker.join();
        }
        catch (InterruptedException e) {
            e.printStackTrace();
        }

        worker = null;
    }


    private void workerLoop() {
        model.load();

        List<MoveAndOutcomeQuery> buffer = new ArrayList<>();
        LinkedList<MoveAndOutcomeQuery> newQueries = new LinkedList<>();

        long spinStart = -1;

        while (! Thread.interrupted()) {
            queryQueue.drainTo(newQueries);

            boolean commit = false;
            if (newQueries.isEmpty()) {
                if (spinStart == -1) {
                    spinStart = System.nanoTime();
                }
                else {
                    long now = System.nanoTime();
                    if (now - spinStart > accumulateDurationNanos) {
                        if (! buffer.isEmpty()) {
                            spinStart = -1;
                            commit = true;
                        }
                    }
                }

                if (! commit) {
                    Thread.onSpinWait();
                    continue;
                }
            }

            if (isRoot) {
                buffer.addAll(newQueries);
                checkState(buffer.size() == 1);
                List<MoveAndOutcomeProbability> estimates = model.estimateAll(buffer);
                buffer.get(0).result.complete(estimates.get(0));
                buffer.clear();
            }

            if (commit) {
                processBuffer(buffer);
            }

            while (! newQueries.isEmpty()) {
                MoveAndOutcomeQuery nextQuery = newQueries.removeFirst();

//                if (buffer.contains(query)) {
//                    List<PuctEstimate> estimates = model.estimateAll(
//                            buffer, uncertainty, outcomeRange, minOutcome);
//                    buffer.clear();
//                }

                buffer.add(nextQuery);

                if (buffer.size() == batchSize) {
                    processBuffer(buffer);
                }
            }
        }
    }


    private void processBuffer(List<MoveAndOutcomeQuery> buffer) {
        List<MoveAndOutcomeProbability> estimates = model.estimateAll(buffer);

        for (int i = 0; i < buffer.size(); i++) {
            MoveAndOutcomeQuery query = buffer.get(i);
            MoveAndOutcomeProbability estimate = estimates.get(i);
            query.result.complete(estimate);
        }

        buffer.clear();
    }


    public MoveAndOutcomeProbability estimateRoot(
            State state, int[] legalMoves)
    {
        MoveAndOutcomeQuery query = new MoveAndOutcomeQuery(state, legalMoves);

        isRoot = true;
        queryQueue.add(query);

        try {
            return query.result.get();
        }
        catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
        finally {
            isRoot = false;
        }
    }


    public MoveAndOutcomeProbability estimateBlocking(
            State state, int[] legalMoves, int moveCount)
    {
        MoveAndOutcomeQuery query = new MoveAndOutcomeQuery(state, legalMoves, moveCount);

        queryQueue.add(query);

        try {
            return query.result.get();
        }
        catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }


    public MoveAndOutcomeProbability estimateBlockingCached(
            State state, int[] legalMoves, int moveCount)
    {
        CacheKey cacheKey = new CacheKey(state.staticHashCode(), model.nextPartition());
        MoveAndOutcomeProbability cached = cache.getIfPresent(cacheKey);
        if (cached != null) {
            cacheHits.increment();
            return cached;
        }

        MoveAndOutcomeQuery query = new MoveAndOutcomeQuery(state, legalMoves, moveCount);

        queryQueue.add(query);

        MoveAndOutcomeProbability result;
        try {
            result = query.result.get();
        }
        catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

        cache.put(cacheKey, result);

        return result;
    }


    public MoveAndOutcomeProbability estimateBlockingLocalCached(
            State state, int[] legalMoves, int moveCount, Map<CacheKey, MoveAndOutcomeProbability> localCache)
    {
        CacheKey cacheKey = new CacheKey(state.staticHashCode(), model.nextPartition());
        MoveAndOutcomeProbability cached = localCache.get(cacheKey);
        if (cached != null) {
            localCacheHits.increment();
            return cached;
        }

        MoveAndOutcomeQuery query = new MoveAndOutcomeQuery(state, legalMoves, moveCount);

        queryQueue.add(query);

        MoveAndOutcomeProbability result;
        try {
            result = query.result.get();
        }
        catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }

        localCache.put(cacheKey, result);

        return result;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public long cacheHits() {
//        return cache.stats().hitCount();
        return cacheHits.longValue();
    }

    public long localCacheHits() {
        return localCacheHits.longValue();
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static class CacheKey {
        private final long position;
        private final int partition;

        public CacheKey(long position, int partition) {
            this.position = position;
            this.partition = partition;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            CacheKey cacheKey = (CacheKey) o;
            return position == cacheKey.position &&
                    partition == cacheKey.partition;
        }

        @Override
        public int hashCode() {
//            return Objects.hash(position, partition);
            return (int) (31 * position + partition);
        }
    }
}
