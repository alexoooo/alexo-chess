package ao.chess.v2.engine.mcts.player.par;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.Player;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;


public class ParallelMctsPlayer
        implements Player
{
    //-----------------------------------------------------------------------------------------------------------------
    private final static int reportPeriod = 60_000;


    //-----------------------------------------------------------------------------------------------------------------
    private final String name;
    private final int threads;

    private final double exploration;
    private final int rollouts;
    private final boolean material;

    private final ExecutorService executorService;


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

        executorService = Executors.newFixedThreadPool(threads);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public int move(
            State position,
            int timeLeft,
            int timePerMove,
            int timeIncrement
    ) {
        MovePicker.init();
        ParallelRoot root = new ParallelRoot(position);

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

        return root.bestMove();
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
                "%s | %d / %.2f / %b | %s | %s",
                name,
                threads,
                exploration,
                material,
                Move.toString(bestMove),
                root);
//        Io.display(message);
        System.out.println(message);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        executorService.shutdown();
    }


    @Override
    public String toString() {
        return name;
    }
}
