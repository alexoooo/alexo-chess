package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.learn.MoveHistory;
import ao.chess.v2.engine.mcts.player.ScoredPlayer;
import ao.chess.v2.engine.neuro.NeuralNetworkPlayer;
import ao.chess.v2.engine.neuro.puct.PuctMixedModel;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * User: aostrovsky
 * Date: 1-Oct-2009
 * Time: 10:49:42 AM
 */
public class Tournament
{
    //--------------------------------------------------------------------
//    private static final int TIME_PER_MOVE = 1_000;
//    private static final int TIME_PER_MOVE = 15_000;
    private static final int TIME_PER_MOVE = 60_000;

    private static final boolean recordThinking = true;
    private static PrintWriter thinkingOut = null;
    private static MoveHistory.Buffer moveExampleBuffer = new MoveHistory.Buffer();


    private static int totalLength;
    private static int gameCount;


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
//        Player a = new MctsPlayer(
//                new MctsNodeImpl.Factory<>(),
//                new Ucb1Value2.Factory(),
//                new MaterialFallbackRollout(new MaterialMixedRollout()),
//                new Ucb1Value2.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<>(),
//                new MctsSchedulerImpl.Factory());

//        MctsPlayer a = new MctsPlayer(
//                new MctsNodeImpl.Factory<>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsRolloutImpl(false),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<>(),
//                new MctsSchedulerImpl.Factory()
//        );

//        Player a = new RandomPlayer();
//        Player b = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsTablebaseRollout(),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<Ucb1TunedValue>(),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player b = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsRolloutImpl(true),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<Ucb1TunedValue>(),
//                new MctsSchedulerImpl.Factory()
//        );

//        Player b = new MctsPlayer(
//                new MctsNodeImpl.Factory<>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsRolloutImpl(false),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsCaptureHeuristic(),
//                new NullTransTable<>(),
//                new MctsSchedulerImpl.Factory()
//        );


//        ParallelMctsPlayer a = new ParallelMctsPlayer(
//                "par",
//                1,
//                0.3,
//                1,
//                false
//        );
//        ParallelMctsPlayer a = new ParallelMctsPlayer(
//                "par",
//                24,
//                0.3,
//                3,
//                false
//        );
//        ParallelMctsPlayer b = a.prototype();

//        Player a = NeuralNetworkPlayer.load(
//                new PuctSingleModel(
////                        Paths.get("lookup/nn/res_10_20191224.zip"),
////                        Paths.get("lookup/nn/res_5a_head.zip")
////                        Paths.get("lookup/nn/res_14b_head.zip")
//                        Paths.get("lookup/nn/res_14b_n811.zip")
////                        Paths.get("lookup/nn/res_20.zip")
//                ),
//                true
//        );

        Player a = NeuralNetworkPlayer.load(
//                new PuctMixedModel(ImmutableRangeMap.<Integer, Path>builder()
//                        .put(Range.closed(2, 8),
//                                Paths.get("lookup/nn/res_7_p_2_12_n634.zip"))
//                        .put(Range.closed(9, 20),
//                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
//                        .put(Range.closed(21, 28),
//                                Paths.get("lookup/nn/res_14_p_16_28_n1209.zip"))
//                        .put(Range.closed(29, 32),
//                                Paths.get("lookup/nn/res_14_p_23_32_n956.zip"))
//                        .build()),
                new PuctMixedModel(ImmutableRangeMap.<Integer, Path>builder()
                        .put(Range.closed(2, 20),
                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
                        .put(Range.closed(21, 32),
                                Paths.get("lookup/nn/res_20_n1307.zip"))
                        .build()),
//                0.01
                0.025
//                1.0
        );

        Player b = NeuralNetworkPlayer.load(
                new PuctMixedModel(ImmutableRangeMap.<Integer, Path>builder()
//                        .put(Range.closed(2, 8),
//                                Paths.get("lookup/nn/res_7_p_2_12_n634.zip"))
//                        .put(Range.closed(9, 20),
                        .put(Range.closed(2, 20),
                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
                        .put(Range.closed(21, 28),
                                Paths.get("lookup/nn/res_14_p_16_28_n1209.zip"))
                        .put(Range.closed(29, 32),
                                Paths.get("lookup/nn/res_14_p_23_32_n956.zip"))
                        .build()),
                0.025
//                0.05,
//                0.1
        );

//        Player b = NeuralNetworkPlayer.load(
//                new PuctEnsembleModel(ImmutableList.of(
////                        Paths.get("lookup/nn/res_14b_n811.zip"),
//                        Paths.get("lookup/nn/res_20_n1307.zip")
//                )),
//                0.01
//        );

//        Player b = NeuralNetworkPlayer.load(
//                new PuctSingleModel(
////                        Paths.get("lookup/nn/res_10_20191224.zip"),
////                        Paths.get("lookup/nn/res_5a_head.zip")
////                        Paths.get("lookup/nn/res_14b_head.zip")
//                        Paths.get("lookup/nn/res_20_n1307.zip")
//                ),
////                false
//                true
//        );

//        Player b = NeuralNetworkPlayer.load(
//                new PuctMultiModel(
//                        ImmutableRangeMap.<Integer, Path>builder()
//                                .put(Range.closed(2, 12),
//                                        Paths.get("lookup/nn/res_5_p_2_12_head.zip"))
//                                .put(Range.closed(13, 22),
//                                        Paths.get("lookup/nn/res_5_p_13_22_head.zip"))
//                                .put(Range.closed(23, 32),
//                                        Paths.get("lookup/nn/res_7_p_23_32_head.zip"))
//                                .build()
//                ),
//                false
//        );
//
//        Player a = new PuctPlayer.Builder(
//                new PuctSingleModel(
////                        Paths.get("lookup/nn/res_5a_head.zip")
//                        Paths.get("lookup/nn/res_14b_head.zip")
//                )).build();
//        Player b = new PuctPlayer.Builder(
//                new PuctSingleModel(
//                        Paths.get("lookup/nn/res_10_20191224.zip"),
//                        true
//                )).build();

//        Player a = new TopLeftPlayer();
//        Player a = new RandomPlayer();
//        Player b = new RandomPlayer();


//        Player a = new PuctPlayer.Builder(
//                new PuctSingleModel(
//                        Paths.get("lookup/nn/res_5a_head.zip")
////                        Paths.get("lookup/nn/res_5_p_2_12_head.zip")
////                        Paths.get("lookup/nn/res_5_p_13_22_head.zip")
////                        Paths.get("lookup/nn/res_7_p_23_32_head.zip")
//                ))
////                .threads(1)
//                .threads(48)
//                .stochastic(true)
//                .build();
//
//        Player b = new PuctPlayer.Builder(
//                new PuctMultiModel(
//                        ImmutableRangeMap.<Integer, Path>builder()
//                                .put(Range.closed(2, 12),
//                                        Paths.get("lookup/nn/res_5_p_2_12_head.zip"))
//                                .put(Range.closed(13, 22),
//                                        Paths.get("lookup/nn/res_5_p_13_22_head.zip"))
//                                .put(Range.closed(23, 32),
//                                        Paths.get("lookup/nn/res_7_p_23_32_head.zip"))
//                                .build()
//                ))
//                .threads(52)
//                .stochastic(true)
//                .build();


        int aWinsWhite = 0;
        int aWinsBlack = 0;
        int bWinsWhite = 0;
        int bWinsBlack = 0;
        int draws = 0;

        for (int i = 0; i < 1000; i++) {
            if (i % 2 == 0) {
                Outcome outcome = round(a, b);
                if (outcome == Outcome.WHITE_WINS) {
                    aWinsWhite++;
                }
                else if (outcome == Outcome.BLACK_WINS) {
                    bWinsBlack++;
                }
                else {
                    draws++;
                }
            }
            else {
                Outcome outcome = round(b, a);
                if (outcome == Outcome.WHITE_WINS) {
                    bWinsWhite++;
                }
                else if (outcome == Outcome.BLACK_WINS) {
                    aWinsBlack++;
                }
                else {
                    draws++;
                }
            }

            System.out.println("====================================================================================");
            System.out.println("aWins\t" + aWinsWhite + "\t" + aWinsBlack);
            System.out.println("bWins\t" + bWinsWhite + "\t" + bWinsBlack);
            System.out.println("draws\t" + draws);
        }

        a.close();
        b.close();
        closeThinkingIfRequired();
    }


    //--------------------------------------------------------------------
    private static Outcome round(Player white, Player black)
    {
        State state =
                State.initial();
//                State.fromFen(
////                        "8/8/8/6K1/8/8/1k6/2R5 w  - 70 49"
////                        "8/8/2p1b1k1/r6n/1K6/8/8/8 b  - 100 2"
////                        "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"
////                        "K7/8/1p3k2/8/7p/8/8/8 b - - 0 1"
////                        "8/4n1k1/8/8/5K1p/8/8/8 b - - 0 1"
////                        "4b3/5k2/8/4p3/8/8/2K5/8 b - - 0 1"
////                        "8/8/2p1b1k1/r6n/1K6/8/8/8 b - - 0 1"
////                        "8/8/8/5R1P/4k1B1/6P1/8/6K1 w - - 0 1"
//                        "3r4/6R1/1p6/pNb4k/P3KP1p/8/6B1/8 b - - 3 51"
//                );

        Outcome outcome = Outcome.DRAW;
        int length = 0;

        while (! state.isDrawnBy50MovesRule())
        {
//            System.out.println("---------------------------------------");
//            System.out.println(state);
            length++;

            Player nextToAct =
                    (state.nextToAct() == Colour.WHITE)
                    ? white : black;

            boolean moveMade = false;
            int move = nextToAct.move(state.prototype(),
                    TIME_PER_MOVE, TIME_PER_MOVE, TIME_PER_MOVE);
            int undoable = -1;
            if (move != -1) {
                State stateProto = state.prototype();

                undoable = Move.apply(move, state);
                if (undoable != -1) {
                    moveMade = true;

                    recordThinkingIfRequired(nextToAct, stateProto);
                }
            }

            if (! moveMade) {
                if (state.isInCheck(Colour.WHITE)) {
                    outcome = Outcome.BLACK_WINS;
                }
                else if (state.isInCheck(Colour.BLACK)) {
                    outcome = Outcome.WHITE_WINS;
                }
                break;
            }
        }

        totalLength += length;
        gameCount++;
        System.out.println("Game length: " + length + ", average: " + ((double) totalLength / gameCount));

        flushThinkingIfRequired(outcome);
        return outcome;
    }


    private static void recordThinkingIfRequired(
            Player nextToAct,
            State state
    ) {
        if (! recordThinking || ! (nextToAct instanceof ScoredPlayer)) {
            return;
        }

        ScoredPlayer player = (ScoredPlayer) nextToAct;

        int[] legalMoves = state.legalMoves();

        double[] moveScores = new double[legalMoves.length];
        for (int i = 0; i < legalMoves.length; i++) {
            moveScores[i] = player.moveScoreInternal(legalMoves[i]);
        }

        moveExampleBuffer.add(state, legalMoves, moveScores, player.expectedValue());
    }


    private static void flushThinkingIfRequired(Outcome outcome) {
        if (moveExampleBuffer.isEmpty()) {
            return;
        }

        if (thinkingOut == null) {
            var filenamePattern = DateTimeFormatter.ofPattern("yyyyMMdd'_'HHmmss'_'SSS");
            var timestamp = filenamePattern.format(LocalDateTime.now());

            try {
                thinkingOut = new PrintWriter(new File(
                        "lookup/think_" + TIME_PER_MOVE + "_" + timestamp + ".csv"));
            }
            catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        }

        List<MoveHistory> examples = moveExampleBuffer.build(outcome);
        moveExampleBuffer.clear();

        for (MoveHistory example : examples) {
            thinkingOut.println(example.asString());
        }

        thinkingOut.flush();
    }


    private static void closeThinkingIfRequired() {
        if (thinkingOut != null) {
            thinkingOut.close();
        }
    }
}
