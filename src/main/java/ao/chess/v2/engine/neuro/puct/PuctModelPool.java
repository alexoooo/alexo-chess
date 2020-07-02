package ao.chess.v2.engine.neuro.puct;


import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.LinkedBlockingDeque;

import static com.google.common.base.Preconditions.checkState;


public class PuctModelPool
        implements AutoCloseable
{
    private final int batchSize;
    private final PuctModel model;

//    private final double uncertainty;
    private final double outcomeRange;
    private final double minOutcome;

    private final BlockingDeque<PuctQuery> queryQueue;
    private Thread worker;
    private volatile boolean isRoot = false;


    public PuctModelPool(
            int batchSize,
            PuctModel model,
            double outcomeRange,
            double minOutcome)
    {
        this.batchSize = batchSize;
        this.model = model;

//        this.uncertainty = uncertainty;
        this.outcomeRange = outcomeRange;
        this.minOutcome = minOutcome;

        queryQueue = new LinkedBlockingDeque<>();
    }


    public void restart(int pieceCount)
    {
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

        List<PuctQuery> buffer = new ArrayList<>();
        LinkedList<PuctQuery> newQueries = new LinkedList<>();

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
                List<PuctEstimate> estimates = model.estimateAll(
                        buffer, /*0.0,*/ 1.0, 0.0);
                buffer.get(0).result.complete(estimates.get(0));
                buffer.clear();
            }

            if (commit) {
                processBuffer(buffer);
            }

            while (! newQueries.isEmpty()) {
                PuctQuery nextQuery = newQueries.removeFirst();

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


    private void processBuffer(List<PuctQuery> buffer) {
        List<PuctEstimate> estimates = model.estimateAll(
                buffer, /*uncertainty,*/ outcomeRange, minOutcome);

        for (int i = 0; i < buffer.size(); i++) {
            PuctQuery query = buffer.get(i);
            PuctEstimate estimate = estimates.get(i);
            query.result.complete(estimate);
        }

        buffer.clear();
    }


    public PuctEstimate estimateRoot(
            State state, int[] legalMoves)
    {
        PuctQuery query = new PuctQuery(state, legalMoves);

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


    public PuctEstimate estimateBlocking(
            State state, int[] legalMoves, int moveCount)
    {
        PuctQuery query = new PuctQuery(state, legalMoves, moveCount);

        queryQueue.add(query);

        try {
            return query.result.get();
        }
        catch (ExecutionException | InterruptedException e) {
            throw new IllegalStateException(e);
        }
    }
}
