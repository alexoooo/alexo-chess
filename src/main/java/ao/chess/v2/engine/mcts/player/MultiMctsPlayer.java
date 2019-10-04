package ao.chess.v2.engine.mcts.player;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.mcts.MctsNode;
import ao.chess.v2.engine.mcts.MctsScheduler;
import ao.chess.v2.engine.mcts.scheduler.MctsSchedulerImpl;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.math.rand.Rand;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;


public class MultiMctsPlayer implements Player
{
    //-----------------------------------------------------------------------------------------------------------------
//    private final static int reportPeriod = 300_000;
    private final static int reportPeriod = 60_000;

    private final List<MctsPlayer> players;
    private final ExecutorService executor;

//    private double cumulativeScore = 0;


    //-----------------------------------------------------------------------------------------------------------------
    public MultiMctsPlayer(List<MctsPlayer> players)
    {
        this.players = players;
        executor = Executors.newFixedThreadPool(players.size());
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public int move(
            State position,
            int timeLeft,
            int timePerMove,
            int timeIncrement)
    {
        int[] legalMoves = position.legalMoves();
        if (legalMoves == null || legalMoves.length == 0) {
            players.forEach(MctsPlayer::clearInternal);
            return -1;
        }

        int oracleAction = oracleAction(position, legalMoves);
        if (oracleAction != -1) {
            players.forEach(MctsPlayer::clearInternal);
            return oracleAction;
        }

//        int bestMove = searchForBestMove(position, timePerMove, legalMoves);

        MctsScheduler scheduler = new MctsSchedulerImpl.Factory()
              .newScheduler(timeLeft, timePerMove, timeIncrement);
        int bestMove;
        do {
            bestMove = searchForBestMove(position, Math.min(reportPeriod, timePerMove), legalMoves);
        }
        while (scheduler.shouldContinue());

        for (var player : players) {
            player.notifyMoveInternal(position, bestMove);
        }
        return bestMove;
    }


    @SuppressWarnings("SameParameterValue")
    private int searchForBestMove(
            State position,
            int searchTime,
            int[] legalMoves
    ) {
        MctsScheduler scheduler = new MctsSchedulerImpl.Factory().newScheduler(
                searchTime, searchTime, searchTime);

        List<Callable<MctsNode>> internalMoves = new ArrayList<>();
        for (MctsPlayer player : players) {
            internalMoves.add(() ->
                    player.moveInternal(position, scheduler));
        }

        List<MctsNode> rootNodes = new ArrayList<>();
        try {
            List<Future<MctsNode>> roots = executor.invokeAll(internalMoves);
            for (Future<MctsNode> root : roots) {
                rootNodes.add(root.get());
            }
        }
        catch (InterruptedException | ExecutionException e) {
            throw new Error(e);
        }

        return selectBestMove(legalMoves, rootNodes);
    }


    private int selectBestMove(
            int[] legalMoves,
            List<MctsNode> rootNodes
    ) {
//        double totalScore = 0;
        int totalNodes = 0;
        int minDepth = Integer.MAX_VALUE;
        int maxDepth = 0;

        double[][] scores = new double[players.size()][legalMoves.length];
        for (int playerIndex = 0; playerIndex < players.size(); playerIndex++) {
            MctsNode rootNode = rootNodes.get(playerIndex);

            double playerMoveScoreSum = 0;
            for (int moveIndex = 0; moveIndex < legalMoves.length; moveIndex++) {
                int action = legalMoves[moveIndex];

                MctsNode actionNode = rootNode.childMatching(action);

                int playerMinDepth = actionNode.minDepth();
                minDepth = Math.min(minDepth, playerMinDepth);

                int playerMaxDepth = actionNode.maxDepth();
                maxDepth = Math.max(maxDepth, playerMaxDepth);

                double moveScore = players.get(playerIndex)
                        .moveScoreInternal(rootNode, legalMoves[moveIndex]);
                scores[playerIndex][moveIndex] = moveScore;
                playerMoveScoreSum += moveScore;
            }

            if (playerMoveScoreSum > 0) {
                for (int moveIndex = 0; moveIndex < legalMoves.length; moveIndex++) {
                    scores[playerIndex][moveIndex] /= playerMoveScoreSum;
                }
            }

//            totalScore += playerMoveScoreSum;
            totalNodes += rootNode.nodeCount();
        }

        double maxMoveScore = 0;
        int maxMoveIndex = 0;
        for (int moveIndex = 0; moveIndex < legalMoves.length; moveIndex++) {
            double moveScoreSum = 0;
            for (int playerIndex = 0; playerIndex < rootNodes.size(); playerIndex++) {
                moveScoreSum += scores[playerIndex][moveIndex];
            }

            if (moveScoreSum > maxMoveScore) {
                maxMoveScore = moveScoreSum;
                maxMoveIndex = moveIndex;
            }

//            System.out.println(" > " +
//                    (moveScoreSum / players.size()) + " | " + Move.toString(legalMoves[moveIndex]));
        }

        int bestMove = legalMoves[maxMoveIndex];

//        cumulativeScore += totalScore;
        String message = String.format(
//                "threads %d | positions %,d | max move %,d | cumulative %,d | depth %d - %d | %s",
                "threads %d | nodes %,d | confidence %.2f | depth %d - %d | %s",
                players.size(),
                totalNodes,
                (maxMoveScore / players.size()) * 100,
//                (long) cumulativeScore,
                minDepth,
                maxDepth,
                Move.toString(bestMove));
        Io.display(message);
        System.out.println(message);

        return bestMove;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int oracleAction(State from, int[] legalMoves) {
        if (from.pieceCount() > 5) {
            return -1;
        }

        boolean canDraw     = false;
        int     bestOutcome = 0;
        int     bestMove    = -1;
        for (int legalMove : legalMoves) {
            Move.apply(legalMove, from);
            DeepOutcome outcome = DeepOracle.INSTANCE.see(from);
            Move.unApply(legalMove, from);
            if (outcome == null || outcome.isDraw()) {
                canDraw = true;
                continue;
            }

            if (outcome.outcome().winner() == from.nextToAct()) {
                if (bestOutcome <= 0 ||
                        bestOutcome > outcome.plyDistance() ||
                        (bestOutcome == outcome.plyDistance() &&
                                Rand.nextBoolean())) {
                    Io.display(outcome.outcome() + " in " +
                            outcome.plyDistance() + " with " +
                            Move.toString(legalMove));
                    bestOutcome = outcome.plyDistance();
                    bestMove    = legalMove;
                }
            } else if (! canDraw && bestOutcome <= 0
                    && bestOutcome > -outcome.plyDistance()) {
                Io.display(outcome.outcome() + " in " +
                        outcome.plyDistance() + " with " +
                        Move.toString(legalMove));
                bestOutcome = -outcome.plyDistance();
                bestMove    = legalMove;
            }
        }

        return (bestOutcome <= 0 && canDraw)
                ? -1 : bestMove;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {
        for (var player : players) {
            player.close();
        }

        executor.shutdown();
    }
}
