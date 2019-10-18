package ao.chess.v2.engine.mcts.player.par;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.mcts.MctsScheduler;
import ao.chess.v2.engine.mcts.player.BanditNode;
import ao.chess.v2.engine.mcts.player.BanditPlayer;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ParallelMctsPlayer
        implements BanditPlayer
{
    //-----------------------------------------------------------------------------------------------------------------
    private final static int reportPeriod = 60_000;


    //-----------------------------------------------------------------------------------------------------------------
    private final String name;
    private final int threads;

    private final double exploration;
    private final int rollouts;
    private final boolean material;

    private ExecutorService executorService;

    private long previousPositionHash = -1;
    private ParallelRoot previousRoot;


    //-----------------------------------------------------------------------------------------------------------------
    public ParallelMctsPlayer(
            String name,
            int threads,
            double exploration,
            int rollouts,
            boolean material)
    {
        this.name = name;
        this.threads = threads;

        this.exploration = exploration;
        this.rollouts = rollouts;
        this.material = material;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public int move(
            State position,
            int timeLeft,
            int timePerMove,
            int timeIncrement
    ) {
        ParallelRoot root = moveInternal(position, timePerMove);
        return root.bestMove();
    }


    private ParallelRoot moveInternal(
            State position,
            int timePerMove
    ) {
        if (executorService == null) {
            MovePicker.init();
            executorService = Executors.newFixedThreadPool(threads);
        }

        long positionHash = position.staticHashCode();
        ParallelRoot root;
        if (previousRoot != null && previousPositionHash == positionHash) {
            root = previousRoot;
        }
        else {
            root = new ParallelRoot(position);
            previousRoot = root;
            previousPositionHash = positionHash;
        }

        long episodeMillis = Math.min(reportPeriod, timePerMove);
        long deadline = System.currentTimeMillis() + timePerMove;

        List<Future<?>> futures = new ArrayList<>();
        for (int thread = 0; thread < threads; thread++) {
            boolean progressThread = thread == 0;
            Future<?> future = executorService.submit(() -> {
                ParallelContext context = new ParallelContext(
                        exploration, rollouts, material);
                do {
                    thinkingEpisode(root, context, episodeMillis, progressThread);
                }
                while (System.currentTimeMillis() < deadline);
            });
            futures.add(future);
        }

        for (Future<?> future : futures) {
            try {
                future.get();
            }
            catch (Exception e) {
                e.printStackTrace();
            }
        }

        root.validate();
        return root;
    }


    private void thinkingEpisode(
            ParallelRoot root,
            ParallelContext context,
            long episodeMillis,
            boolean progressThread
    ) {
        long episodeDeadline = System.currentTimeMillis() + episodeMillis;
        do {
            root.think(context);
        }
        while (System.currentTimeMillis() < episodeDeadline);

        if (progressThread) {
            reportProgress(root);
        }
    }


    private void reportProgress(
            ParallelRoot root
    ) {
        int bestMove = root.bestMove();

        String message = String.format(
                "%s | %d / %.2f / %d / %b | %s | %s",
                name,
                threads,
                exploration,
                rollouts,
                material,
                Move.toString(bestMove),
                root);
//        Io.display(message);
        System.out.println(message);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public BanditPlayer prototype() {
        return new ParallelMctsPlayer(
                name, threads, exploration, rollouts, material);
    }


    @Override
    public void clearInternal() {
        previousRoot = null;
        previousPositionHash = -1;
    }


    @Override
    public void notifyMoveInternal(State position, int action) {
        // TODO
    }


    @Override
    public double moveScoreInternal(BanditNode node, int move) {
        ParallelNode child = (ParallelNode) node.childMatching(move);
        return child.visitCount();
    }


    @Override
    public BanditNode moveInternal(State position, MctsScheduler scheduler) {
        ParallelRoot root = moveInternal(
                position,
                scheduler.timePerMove());

        return root.node();
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        if (executorService != null) {
            executorService.shutdown();
        }
    }


    @Override
    public String toString() {
        return name;
    }
}
