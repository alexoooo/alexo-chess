package ao.chess.v2.engine.neuro.meta;


import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

import static com.google.common.base.Preconditions.checkState;


class MetaModelPool
        implements AutoCloseable
{
    private final int batchSize;
    private final MetaModel model;

//    private final double uncertainty;
    private final double outcomeRange;
    private final double minOutcome;

    private final BlockingDeque<MetaQuery> queryQueue;
    private Thread worker;
    private volatile boolean isRoot = false;


    public MetaModelPool(
            int batchSize,
            MetaModel model,
            double outcomeRange,
            double minOutcome)
    {
        this.batchSize = batchSize;
        this.model = model;

        this.outcomeRange = outcomeRange;
        this.minOutcome = minOutcome;

        queryQueue = new LinkedBlockingDeque<>();
    }


    public void restart()
    {
        if (worker != null) {
            close();
        }

        start();
    }


    public void start()
    {
        worker = new Thread(this::workerLoop);
        worker.start();
    }


    @Override
    public void close() {
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

        List<MetaQuery> buffer = new ArrayList<>();
        LinkedList<MetaQuery> newQueries = new LinkedList<>();

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
                    if (now - spinStart > 100_000_000) {
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
                List<MetaEstimate> estimates = model.estimateAll(
                        buffer, /*0.0,*/ 1.0, 0.0);
                buffer.get(0).result.complete(estimates.get(0));
                buffer.clear();
            }

            if (commit) {
                processBuffer(buffer);
            }

            while (! newQueries.isEmpty()) {
                MetaQuery nextQuery = newQueries.removeFirst();

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

    private void processBuffer(List<MetaQuery> buffer) {
        List<MetaEstimate> estimates = model.estimateAll(
                buffer, /*uncertainty,*/ outcomeRange, minOutcome);

        for (int i = 0; i < buffer.size(); i++) {
            MetaQuery query = buffer.get(i);
            MetaEstimate estimate = estimates.get(i);
            query.result.complete(estimate);
        }

        buffer.clear();
    }


    public MetaEstimate estimateRoot(
            State state, int[] legalMoves)
    {
        MetaQuery query = new MetaQuery(state, legalMoves);

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


    public MetaEstimate estimateBlocking(
            State state, int[] legalMoves)
    {
        MetaQuery query = new MetaQuery(state, legalMoves);

        queryQueue.add(query);

        try {
            return query.result.get();
        }
        catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
