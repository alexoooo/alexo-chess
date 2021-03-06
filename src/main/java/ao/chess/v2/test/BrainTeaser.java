package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.neuro.puct.PuctMixedModel;
import ao.chess.v2.engine.neuro.puct.PuctModel;
import ao.chess.v2.engine.neuro.rollout.RolloutPlayer;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;

import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;


/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 9:49:17 PM
 * 
 * See
 *   http://en.wikibooks.org/wiki/Chess/Puzzles/Directmates
 */
public class BrainTeaser {
    //--------------------------------------------------------------------
//    private static final int flushFrequencyMillis = 10_000;
//    private static final int flushFrequencyMillis = 60_000;
//    private static final int flushFrequencyMillis = 10 * 60 * 1_000;
    private static final int flushFrequencyMillis = 15 * 60 * 1_000;


    //--------------------------------------------------------------------
    public static void main(String[] args) {
        System.out.println("Start time: " + LocalDateTime.now());

//        int time = 7 * 24 * 60 * 60 * 1000;
//        int time = 5 * 1000;
//        int time = 7_500;
//        int time = 10 * 1000;
//        int time = 15 * 1000;
//        int time = 45 * 1000;
//        int time = 60 * 1000;
//        int time = 10 * 60 * 1000;
//        int time = 15 * 60 * 1000;
//        int time = 60 * 60 * 1000;
//        int time = 3 * 60 * 60 * 1000;
//        int time = (int) (1.51 * 60 * 60 * 1000);
        int time = 7 * 24 * 60 * 60 * 1000;

//        Player player = new ParallelMctsPlayer(
//                "par",
//                9,
//                0.3,
//                3,
////                1,
//                false
//        );


//        PuctModel model = new PuctMultiModel(
//                ImmutableRangeMap.<Integer, Path>builder()
//                        .put(Range.closed(2, 22),
//                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
//                        .put(Range.closed(23, 32),
////                                        Paths.get("lookup/nn/res_14b_n811.zip"))
//                                Paths.get("lookup/nn/res_20_n1307.zip"))
//                        .build()
//        );

//                new PuctMixedModel(ImmutableRangeMap.<Integer, Path>builder()
//                        .put(Range.closed(2, 20),
//                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
////                        .put(Range.closed(21, 32),
////                                Paths.get("lookup/nn/res_20_n1307.zip"))
//                        .put(Range.closed(21, 28),
//                                Paths.get("lookup/nn/res_14_p_16_28_n1209.zip"))
//                        .put(Range.closed(29, 32),
//                                Paths.get("lookup/nn/res_14_p_23_32_n956.zip"))
//                        .build())
//        PuctModel model = new PuctEnsembleModel(ImmutableList.of(
//                Paths.get("lookup/nn/res_20_n1307.zip"),
//                Paths.get("lookup/nn/res_20_b_n1008.zip")));
//
//        Player player = NeuralNetworkPlayer.load(
//                model,
//                0.025
////                0.05,
////                0.1
//        );

//        Player player = new PuctPlayer.Builder(
//                new PuctMultiModel(ImmutableRangeMap.<Integer, Path>builder()
//                        .put(Range.closed(2, 22),
//                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
//                        .put(Range.closed(23, 32),
//                                Paths.get("lookup/nn/res_20_n1307.zip"))
//                        .build()
//                ))
////                .threads(1)
////                .threads(48)
////                .threads(52)
////                .threads(64)
////                .threads(96)
//                .threads(128)
////                .threads(160)
////                .threads(192)
////                .threads(224)
////                .threads(256)
////                .stochastic(true)
//                .build();

        PuctModel model = new PuctMixedModel(ImmutableRangeMap.<Integer, Path>builder()
                .put(Range.closed(2, 20),
                        Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
                .put(Range.closed(21, 28),
                        Paths.get("lookup/nn/res_14_p_16_28_n1209.zip"))
                .put(Range.closed(29, 32),
                        Paths.get("lookup/nn/res_14_p_23_32_n956.zip"))
                .build());
//        PuctModel model = new PuctMultiModel(ImmutableRangeMap.<Integer, Path>builder()
//                .put(Range.closed(2, 22),
//                        Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
//                .put(Range.closed(23, 32),
//                        Paths.get("lookup/nn/res_20_n1307.zip"))
//                .build());
//        PuctModel model = new PuctEnsembleModel(ImmutableList.of(
//                Paths.get("lookup/nn/res_20_n1307.zip"),
//                Paths.get("lookup/nn/res_20_b_n1008.zip")
//        ));
        Player player = new RolloutPlayer.Builder(model)
//                .binerize(false)
                .binerize(true)
//                .rolloutLength(4 * 1024)
//                .threads(1)
//                .threads(2)
//                .threads(48)
//                .threads(52)
//                .threads(64)
//                .threads(96)
//                .threads(128)
//                .threads(160)
//                .threads(192)
//                .threads(224)
//                .threads(256)
//                .threads(256)
//                .threads(384)
                .threads(512)
//                .stochastic(true)
//                .store(new SynchronizedRolloutStore(new MapRolloutStore()))
                .build();


        State state = State.fromFen(
                // https://www.youtube.com/watch?v=TdcPgTzSTnM
//                "2r4q/p4pk1/3pb1p1/N7/1PPp2n1/P3b1PB/2R1P1K1/3NQR2 b - - 0 1"

                // https://www.youtube.com/watch?v=weihJSohY28
//                "6r1/1p2n2p/1np2pp1/3p1k2/2P2p2/PP1P2PK/NN1BPb1P/R7 w - - 0 1"

//                "2bQr2k/Brp3bp/2p2np1/5p2/2B1P3/P1N2P2/1q4PP/3R1RK1 w - - 4 20"

                // mate in 9 (17) - mid and big nets find it
//                "1Nr1n3/p3p1q1/P2p1prk/4p3/1pB1n1P1/1P1R4/3b2KN/8 w"

                // Travis (Queeen odds) attempts 3
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNB1KB1R b KQkq - 0 1"

                // https://github.com/LeelaChessZero/lc0/issues/1403
//                "8/k2q4/2p1n2R/2bpP3/P7/1R1Q4/5rP1/3K4 b - - 4 40"

                // https://en.wikipedia.org/wiki/Philidor_position
//                "8/8/8/8/4pk2/R7/7r/4K3 w - - 0 1" // draw
//                "3k4/5R2/3K4/3B4/8/8/4r3/8 w - - 0 1" // win

                // https://www.chess.com/news/view/stockfish-12-released-130-elo-points-stronger
//                "5N2/8/6p1/4p3/6kp/7q/PP2PP1P/1B4K1 w - - 0 1"

                // https://www.youtube.com/watch?v=j03ZNv13h0w
//                "K7/3N4/kp6/1p3q2/8/1P5R/4b2p/7B w - - 0 1"

                // https://www.youtube.com/watch?v=TaqdpJZsyKM
//                "8/3P3k/n2K3p/2p3n1/1b4N1/2p1p1P1/8/3B4 w - - 0 1"

//                "r5k1/4bp2/5np1/1p5p/8/4BNPP/1P1R1P2/6K1 b - - 1 24"
//                "n1QBq1k1/5p1p/5KP1/p7/8/8/8/8 w - - 0 2"
//                "8/qb6/8/2p5/2Q5/8/8/3BKn1k w - - 0 1"

                // https://chess.stackexchange.com/questions/5945/how-to-get-position-evaluation-with-uci
//                "5rk1/1p2bppp/p4n2/5b2/P7/1B2BN2/1P3PPP/2R3K1 b - - 0 18" // thinks white is winning

                // https://www.youtube.com/watch?v=HUv9P9-Ods0
//                "q4r2/2n1ppbk/6pp/1NpPn3/2B3P1/3Q3P/1PPB1P2/4R1K1 w - - 1 23"
//                "5r2/2b1pp1k/6pp/2pP4/2B3P1/4Q2P/1qPB1PK1/8 w - - 0 27"

                // https://www.youtube.com/watch?v=YlshAt9EjqU
//                "r1br4/1p3pkp/3p1np1/pNpPq3/2P1PQ2/1P1B3P/P5P1/R4RK1 w - a6 0 19"
//                "2b5/1pr2pk1/r4np1/p1pPp3/N1P1P2p/PP1B3P/5RP1/5RK1 w - - 0 28"
//                "r7/1R6/3P1kp1/2P2p2/4p2p/7P/6P1/6K1 w - - 1 44" // doesn't see b7e7

//                "8/8/8/8/8/3k3K/7P/8 w - - 0 1"
//                "8/8/8/8/6K1/3k4/7P/8 b - - 0 1"
//                "8/8/8/8/4k1K1/8/7P/8 w - - 0 1"
//                "8/8/8/4k3/6KP/8/8/8 w - - 0 1"

                // https://www.youtube.com/watch?v=U4ogK0MIzqk
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 2"
//                "rnbqkbnr/pp2pppp/3p4/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 3"
//                "rnbqkbnr/pp2pppp/3p4/8/3pP3/5N2/PPP2PPP/RNBQKB1R w KQkq - 0 4"
//                "rnbqkb1r/pp2pppp/3p1n2/8/3NP3/8/PPP2PPP/RNBQKB1R w KQkq - 1 5"
//                "rnbqkb1r/pp2pp1p/3p1np1/8/3NP3/2N5/PPP2PPP/R1BQKB1R w KQkq - 0 6"
//                "rnbqkb1r/pp3p1p/3p1np1/4p3/3NP3/2N1B3/PPP2PPP/R2QKB1R w KQkq e6 0 7"
//                "r1bqkb1r/pp3p1p/2np1np1/4p3/4P3/2N1BN2/PPP2PPP/R2QKB1R w KQkq - 2 8"
//                "r1bqk2r/pp3pbp/2np1np1/4p3/2B1P3/2N1BN2/PPP2PPP/R2QK2R w KQkq - 4 9"
//                "r1bqk2r/pp3pbp/3p1np1/4p3/1nB1P3/2NQBN2/PPP2PPP/R3K2R w KQkq - 6 10"
//                "r1b1k2r/ppq2pbp/3p1np1/4p3/1nB1P3/2N1BN2/PPPQ1PPP/R3K2R w KQkq - 8 11"
//                "r1b1k2r/pp3pbp/3p1np1/qN2p3/1nB1P3/4BN2/PPPQ1PPP/R3K2R w KQkq - 10 12"
//                "r1b4r/pp2kpbp/3N1np1/q3p3/1nB1P3/4BN2/PPPQ1PPP/R3K2R w KQ - 1 13"
//                "r1b4r/pp2kpbp/3N1np1/q3p3/2B1P3/2P1BN2/PPnQ1PPP/R3K2R w KQ - 1 14"
//                "r1b4r/pp3pbp/3k1np1/q3p3/2B1P3/2P1BN2/PPQ2PPP/R3K2R w KQ - 0 15"
//                "r1b4r/pp2kpbp/5np1/q3p1N1/2B1P3/2P1B3/PPQ2PPP/R3K2R w KQ - 2 16"
//                "r1b4r/pp2kpbp/6p1/q3p1N1/2B1P1n1/1QP1B3/PP3PPP/R3K2R w KQ - 4 17"
//                "r1b4r/pp2kNbp/6p1/q3p3/2B1P3/1QP1n3/PP3PPP/R3K2R w KQ - 0 18"
//                "r1b1r3/pp2kNbp/6p1/q3p3/2B1P3/1QP1P3/PP4PP/R3K2R w KQ - 1 19"
//                "r1b1r3/pp2kNbp/6p1/2q1p3/2B1P3/1QP1P3/PP4PP/R4RK1 w - - 3 20"
//                "r1b1r3/pp2kNbp/6p1/4p3/2B1P3/1QP1q3/PP4PP/3R1RK1 w - - 0 21"
//                "r1b1r3/pp2kNbp/6p1/2q1p3/2B1P3/1QP5/PP4PP/3R1R1K w - - 2 22"
//                "r1b1r3/pp2kNbp/6p1/3qp3/2B1P3/1QP5/PP4PP/5R1K w - - 0 23"
//                "r3r3/pp2kNbp/6p1/3Ppb2/2B5/1QP5/PP4PP/5R1K w - - 1 24"
//                "r3rk2/pQ3Nbp/6p1/3Ppb2/2B5/2P5/PP4PP/5R1K w - - 1 25"
//                "r3r1k1/pQ4bp/3N2p1/3Ppb2/2B5/2P5/PP4PP/5R1K w - - 3 26"
//                "r3r2k/p4Qbp/3N2p1/3Ppb2/2B5/2P5/PP4PP/5R1K w - - 5 27"
//                "r3N2k/p4Q1p/6pb/3Ppb2/2B5/2P5/PP4PP/5R1K w - - 1 28"
//                "r3N1k1/p6p/5Qpb/3Ppb2/2B5/2P5/PP4PP/5R1K w - - 3 29"
                "r3N1k1/p6p/3PbQpb/4p3/2B5/2P5/PP4PP/5R1K w - - 1 30"

                // https://www.youtube.com/watch?v=yyHmAx3d7GI
//                "r5k1/p1p3pp/2p3n1/3pp3/4P2q/1B3r1P/P2PNP2/RNB1QRK1 w - - 0 1"
//                "rnbqkbnr/pp2pppp/3p4/2p5/4P3/3P1N2/PPP2PPP/RNBQKB1R b KQkq - 0 3"
//                "r1bqkbnr/pp2pppp/2np4/2p5/4P3/3P1N2/PPP2PPP/RNBQKB1R w KQkq - 1 4"

                // "simple draw"?
//                "8/1p1b4/8/P7/3BPk2/7p/6pK/8 b - - 0 1"

                // http://www.talkchess.com/forum3/viewtopic.php?t=57603
//                "8/8/8/1k6/8/8/8/RK6 w - -"
//                "r3k2r/8/8/8/8/8/8/R3K2R w KQkq -"
//                "4k3/8/8/PpPpPpPp/PpPpPpPp/8/8/4K3 b - a3"

//                "rnbqkb1r/pppp1ppp/8/4P3/4n3/2P2P2/P5PP/RNBQKBNR b KQkq - 0 1"

//                "2K1k1br/2qp1n1r/2p2pN1/3p1N2/2P4P/8/P2P4/8 w - - 0 14"
//                "4k1br/2Kp1n1r/2p2pN1/3p1N2/2P4P/8/P2P4/8 b - - 0 14"
//                "4k1br/2Kp1n1r/2p2pN1/5N2/2p4P/8/P2P4/8 w - - 0 15" // or here?
//                "2K1k1br/3p1n1r/2p2pN1/5N2/2p4P/8/P2P4/8 b - - 1 15"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/2p4P/8/P2P4/8 w - d6 0 16"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/P1p4P/8/3P4/8 b - a3 0 16" // 14b fail, 14 2-22 works!
//                "2K1k1br/5n1r/2p2pN1/3p1N2/P6P/2p5/3P4/8 w - - 0 17"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/P6P/2P5/8/8 b - - 0 17" // chess.com sees mate here
//                "2K1k1br/5n1r/5pN1/2pp1N2/P6P/2P5/8/8 w - - 0 18"
//                "2K1k1br/5n1r/5pN1/P1pp1N2/7P/2P5/8/8 b - - 0 18"
//                "2K1k1br/5n1r/5pN1/P1p2N2/3p3P/2P5/8/8 w - - 0 19"
//                "2K1k1br/5n1r/P4pN1/2p2N2/3p3P/2P5/8/8 b - - 0 19"
//                "2K1k1br/5n1r/P4pN1/2p2N2/7P/2Pp4/8/8 w - - 0 20"
//                "2K1k1br/P4n1r/5pN1/2p2N2/7P/2Pp4/8/8 b - - 0 20"
//                "2K1k1br/P4n1r/5pN1/5N2/2p4P/2Pp4/8/8 w - - 0 21"
//                "Q1K1k1br/5n1r/5pN1/5N2/2p4P/2Pp4/8/8 b - - 0 21"
//                "Q1K1k1br/5n1r/5pN1/5N2/2p4P/2P5/3p4/8 w - - 0 22"
//                "2K1k1br/5n1r/5pN1/5N2/Q1p4P/2P5/3p4/8 b - - 1 22"
        );

//        int move = player.move(state, time, time, 0);
//        Move.apply(move, state);
//        player.move(state, time, time, 0);

        System.out.println(state);
        Stopwatch stopwatch = Stopwatch.createStarted();

        boolean solved = false;
        int episodeCount = time / flushFrequencyMillis;
        for (int i = 0; i < episodeCount; i++) {
            int move = player.move(state, flushFrequencyMillis, flushFrequencyMillis, 0);
            System.out.println("Episode " + i + ") " + Move.toString(move));

            if (player.isSolved(state) || state.knownOutcomeOrNull() != null) {
                solved = true;
                break;
            }
        }

        if (! solved) {
            int remainingTime = time % flushFrequencyMillis;
            int move = player.move(state, remainingTime, remainingTime, 0);
            System.out.println(Move.toString(move));
        }

        player.close();
        System.out.println("Solved in " + stopwatch);
        System.exit(0);
    }
}
