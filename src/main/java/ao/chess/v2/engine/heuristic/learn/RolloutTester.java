package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.neuro.puct.MoveAndOutcomeProbability;
import ao.chess.v2.engine.neuro.puct.MoveAndOutcomeModel;
import ao.chess.v2.engine.neuro.puct.MoveAndOutcomeModelPool;
import ao.chess.v2.engine.neuro.puct.NeuralMultiModel;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.DoubleSummaryStatistics;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;


public class RolloutTester {
//    private static final double rolloutValueWeight = 0.0;
    private static final double rolloutValueWeight = 0.25;
//    private static final double rolloutValueWeight = 0.5;
//    private static final double rolloutValueWeight = 1.0;
    private static final double rolloutValueDenominator = 1.0 + rolloutValueWeight;

//    private static final double rolloutProbabilityPower = 1.0;
    private static final double rolloutProbabilityPower = 2.0;
//    private static final double rolloutProbabilityPower = 3.0;

    //    private static final double estimateUncertainty = 0.01;
    private static final double estimateUncertainty = 0.025;
    private static final double estimateUncertaintyDenominator = 1.0 + estimateUncertainty;

//    private static final int maxTestCount = 1;
//    private static final int maxTestCount = 32;
//    private static final int maxTestCount = 1_000;
    private static final int maxTestCount = 10_000;
//    private static final int maxTestCount = 100_000;

    private static final int loggingFrequency = 1;
//    private static final int loggingFrequency = 100;
//    private static final int loggingFrequency = 250;
//    private static final int loggingFrequency = 1_000;
//    private static final int loggingFrequency = 10_000;

//    private static final int rolloutCount = 1;
//    private static final int rolloutCount = 2;
//    private static final int rolloutCount = 4;
//    private static final int rolloutCount = 8;
//    private static final int rolloutCount = 16;
//    private static final int rolloutCount = 32;
//    private static final int rolloutCount = 64;
    private static final int rolloutCount = 256;

//    private static final int modelBatchSize = 1;
//    private static final int modelBatchSize = 8;
//    private static final int modelBatchSize = 16;
//    private static final int modelBatchSize = 24;
//    private static final int modelBatchSize = 32;
//    private static final int modelBatchSize = 64;
//    private static final int modelBatchSize = 128;
    private static final int modelBatchSize = 256;
    private static final int threadCount = modelBatchSize;


    private static final List<Path> test = List.of(
//            Paths.get("lookup/train/pieces/test/2.txt.gz"),
//            Paths.get("lookup/train/pieces/test/3.txt.gz"),
//            Paths.get("lookup/train/pieces/test/4.txt.gz"),
//            Paths.get("lookup/train/pieces/test/5.txt.gz"),
//            Paths.get("lookup/train/pieces/test/6.txt.gz"),
//            Paths.get("lookup/train/pieces/test/7.txt.gz"),
//            Paths.get("lookup/train/pieces/test/8.txt.gz"),
//            Paths.get("lookup/train/pieces/test/9.txt.gz"),
//            Paths.get("lookup/train/pieces/test/10.txt.gz"),
//            Paths.get("lookup/train/pieces/test/11.txt.gz"),
//            Paths.get("lookup/train/pieces/test/12.txt.gz"),
//            Paths.get("lookup/train/pieces/test/13.txt.gz"),
//            Paths.get("lookup/train/pieces/test/14.txt.gz"),
//            Paths.get("lookup/train/pieces/test/15.txt.gz"),
//            Paths.get("lookup/train/pieces/test/16.txt.gz"),
//            Paths.get("lookup/train/pieces/test/17.txt.gz"),
//            Paths.get("lookup/train/pieces/test/18.txt.gz"),
//            Paths.get("lookup/train/pieces/test/19.txt.gz"),
//            Paths.get("lookup/train/pieces/test/20.txt.gz"),
//            Paths.get("lookup/train/pieces/test/21.txt.gz"),
//            Paths.get("lookup/train/pieces/test/22.txt.gz"),
//            Paths.get("lookup/train/pieces/test/23.txt.gz"),
//            Paths.get("lookup/train/pieces/test/24.txt.gz"),
//            Paths.get("lookup/train/pieces/test/25.txt.gz"),
//            Paths.get("lookup/train/pieces/test/26.txt.gz"),
//            Paths.get("lookup/train/pieces/test/27.txt.gz"),
//            Paths.get("lookup/train/pieces/test/28.txt.gz"),
//            Paths.get("lookup/train/pieces/test/29.txt.gz"),
//            Paths.get("lookup/train/pieces/test/30.txt.gz"),
//            Paths.get("lookup/train/pieces/test/31.txt.gz"),
            Paths.get("lookup/train/pieces/test/32.txt.gz")

//            Paths.get("lookup/train/mix-small/champions_10000.txt")
    );


    private static final ExecutorService executor = Executors.newFixedThreadPool(threadCount);


    public static void main(String[] args) throws InterruptedException
    {
        MoveAndOutcomeModel model = new NeuralMultiModel(ImmutableRangeMap.<Integer, Path>builder()
                .put(Range.closed(2, 22),
                        Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
                .put(Range.closed(23, 32),
                        Paths.get("lookup/nn/res_20_n1307.zip"))
                .build());

        MoveAndOutcomeModelPool pool = new MoveAndOutcomeModelPool(modelBatchSize, model);
        pool.restart(32);

        DoubleSummaryStatistics globalOutcomeStats = new DoubleSummaryStatistics();

        for (var testPath : test) {
            long predictStart = System.currentTimeMillis();

            List<MoveHistory> inputMovesAll = MoveTrainer.readMoves(testPath);
            List<MoveHistory> inputMoves = inputMovesAll.subList(0, Math.min(maxTestCount, inputMovesAll.size()));

            DoubleSummaryStatistics outcomeStats = test(pool, inputMoves);

            double took = (double) (System.currentTimeMillis() - predictStart) / 1000;
            System.out.println("Path error:\t" +
                    outcomeStats.getAverage() + "\t" +
                    "took: " + took + " - " + testPath);

            globalOutcomeStats.combine(outcomeStats);
        }

        System.out.println("Average error: " +
                globalOutcomeStats.getAverage());

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);
        pool.close();
    }


    private static DoubleSummaryStatistics test(MoveAndOutcomeModelPool model, List<MoveHistory> examples) {
        DoubleSummaryStatistics outcomeStats = new DoubleSummaryStatistics();

        List<Future<Double>> rolloutValues = new ArrayList<>();
        for (int i = 0; i < examples.size(); i++) {
            MoveHistory example = examples.get(i);

            int index = i;
            Future<Double> rollout = executor.submit(() ->
                    rollout(index, example.state(), model));

            rolloutValues.add(rollout);
        }

        com.google.common.base.Stopwatch totalTimer = Stopwatch.createStarted();
        com.google.common.base.Stopwatch timer = Stopwatch.createStarted();
        for (int i = 0; i < examples.size(); i++) {
            MoveHistory example = examples.get(i);
            Future<Double> rolloutValue = rolloutValues.get(i);

            double rollout;
            try {
                rollout = rolloutValue.get();
            }
            catch (Exception e) {
                throw new Error(e);
            }

            double error = Math.abs(rollout - example.outcomeValue());
            outcomeStats.accept(error);

            if ((i + 1) % loggingFrequency == 0) {
                System.out.println("Tested: " + (i + 1) + " | took: " + timer + " at " +
                        ((double) loggingFrequency * 1_000.0 / timer.elapsed().toMillis()) + "/s | " +
                        "total: " + totalTimer + " at " +
                        ((double) (i + 1) * 1_000.0 / totalTimer.elapsed().toMillis()) + "/s | " +
                        outcomeStats.getAverage() + " average");
                timer.reset();
                timer.start();
            }
        }

        return outcomeStats;
    }


    private static double rollout(int index, State state, MoveAndOutcomeModelPool model) {
        double valueSum = 0;
        for (int i = 0; i < rolloutCount; i++) {
//            System.out.println("Started " + index + "-" + i);
            Stopwatch timer = Stopwatch.createStarted();

            valueSum += rolloutOne(state.prototype(), model);
//            valueSum += rolloutOneRandom(state.prototype());

//            System.out.println("Finished " + index + "-" + i + " | took: " + timer.toString());
        }
        return valueSum / rolloutCount;
    }


    private static double rolloutOne(State state, MoveAndOutcomeModelPool model) {
        Colour fromPov = state.nextToAct();

        int[] pseudoMoves = new int[Move.MAX_PER_PLY];

        int[] moves = new int[Move.MAX_PER_PLY];
        int nMoves = state.legalMoves(moves, pseudoMoves);
        int nextCount;
        int[] nextMoves = new int[Move.MAX_PER_PLY];
        Outcome outcome;

        if (nMoves < 1) {
            return Double.NaN;
        }

        int rolloutLength = 0;
        double rolloutValueSum = 0;

        rollout:
        while (true) {
            MoveAndOutcomeProbability estimate = model.estimateBlocking(state, moves, nMoves);

            rolloutLength++;
            rolloutValueSum +=
                    state.nextToAct() == fromPov
                    ? estimate.expectedValue()
                    : 1.0 - estimate.expectedValue();

            int bestMoveIndex = 0;
            double bestMoveScore = Double.NEGATIVE_INFINITY;

            double moveUncertainty = estimateUncertainty / nMoves;

            for (int i = 0; i < nMoves; i++) {
                byte reversibleMoves = state.reversibleMoves();
                byte castles = state.castles();
                long castlePath = state.castlePath();

                int move = Move.apply(moves[ i ], state);
                int opponentMoveCount = state.legalMoves(nextMoves, pseudoMoves);
                Outcome moveOutcome = state.knownOutcomeOrNull(opponentMoveCount);
                Move.unApply(move, state);

                state.restore(reversibleMoves, castles, castlePath);

                if (moveOutcome != null) {
                    if (moveOutcome.loser() == state.nextToAct()) {
                        // non-viable move, leads to self-mate
                        continue;
                    }

                    if (moveOutcome.winner() == state.nextToAct()) {
                        outcome = moveOutcome;
                        break rollout;
                    }
                }

                double probability = (estimate.moveProbabilities[i] + moveUncertainty) / estimateUncertaintyDenominator;
                double probabilityValue = Math.pow(probability, rolloutProbabilityPower);
                double score = probabilityValue * Math.random();
                if (score > bestMoveScore) {
                    bestMoveScore = score;
                    bestMoveIndex = i;
                }
            }

            Move.apply(moves[bestMoveIndex], state);

            // generate opponent moves
            nextCount = state.legalMoves(nextMoves, pseudoMoves);
            Outcome moveOutcome = state.knownOutcomeOrNull();
            if (moveOutcome != null) {
                outcome = moveOutcome;
                break;
            }

            if (state.pieceCount() <= DeepOracle.instancePieceCount) {
                DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(state);
                outcome = deepOutcome.outcome();
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves = moves;
                moves = tempMoves;
                nMoves = nextCount;
            }
        }

        //noinspection ConstantConditions
        if (state.isDrawnBy50MovesRule() || rolloutValueWeight > 0) {
            double povEstimate = rolloutValueSum / rolloutLength;
            return (outcome.valueFor(fromPov) + rolloutValueWeight * povEstimate) / rolloutValueDenominator;
        }

        return outcome.valueFor(fromPov);
    }


    private static double rolloutOneRandom(State state) {
        Colour fromPov = state.nextToAct();

        int[] pseudoMoves = new int[Move.MAX_PER_PLY];

        int[] moves = new int[Move.MAX_PER_PLY];
        int nMoves = state.legalMoves(moves, pseudoMoves);
        int nextCount;
        int[] nextMoves = new int[Move.MAX_PER_PLY];
        Outcome outcome;

        if (nMoves < 1) {
            return Double.NaN;
        }

        rollout:
        while (true) {
            int bestMoveIndex = 0;
            double bestMoveScore = Double.NEGATIVE_INFINITY;

            for (int i = 0; i < nMoves; i++) {
                byte reversibleMoves = state.reversibleMoves();
                byte castles = state.castles();
                long castlePath = state.castlePath();

                int move = Move.apply(moves[ i ], state);
                int opponentMoveCount = state.legalMoves(nextMoves, pseudoMoves);
                Outcome moveOutcome = state.knownOutcomeOrNull(opponentMoveCount);
                Move.unApply(move, state);

                state.restore(reversibleMoves, castles, castlePath);

                if (moveOutcome != null) {
                    if (moveOutcome.loser() == state.nextToAct()) {
                        // non-viable move, leads to self-mate
                        continue;
                    }

                    if (moveOutcome.winner() == state.nextToAct()) {
                        outcome = moveOutcome;
                        break rollout;
                    }
                }

                double probability = 1.0;
                double score = probability * probability * Math.random();
                if (score > bestMoveScore) {
                    bestMoveScore = score;
                    bestMoveIndex = i;
                }
            }

            Move.apply(moves[bestMoveIndex], state);

            // generate opponent moves
            nextCount = state.legalMoves(nextMoves, pseudoMoves);
            Outcome moveOutcome = state.knownOutcomeOrNull();
            if (moveOutcome != null) {
                outcome = moveOutcome;
                break;
            }

            if (state.pieceCount() <= DeepOracle.instancePieceCount) {
                DeepOutcome deepOutcome = DeepOracle.INSTANCE.see(state);
                outcome = deepOutcome.outcome();
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves = moves;
                moves = tempMoves;
                nMoves = nextCount;
            }
        }

        return outcome.valueFor(fromPov);
    }
}
