package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.neuro.puct.PuctMultiModel;
import ao.chess.v2.engine.neuro.rollout.RolloutPlayer;
import ao.chess.v2.engine.neuro.rollout.store.SynchronizedRolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.TieredRolloutStore;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableRangeMap;
import com.google.common.collect.Range;

import java.io.FileNotFoundException;
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
    private static final int flushFrequencyMillis = 60_000;


    //--------------------------------------------------------------------
    public static void main(String[] args) throws FileNotFoundException {
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

//        Player player = new PuctPlayer(
//                new PuctSingleModel(
//                        Paths.get("lookup/nn/all_mid_batch_20191120-travis.zip"),
//                        false
//                ),
//                2,
//                1.25,
//                true,
//                0,
//                true,
//                1.25,
//                0);

//        Player player = new PuctPlayer(
//                new PuctSingleModel(
//                        Paths.get("lookup/nn/multi_6d_20191208.zip"),
//                        true
//                ),
//                4,
//                2.5,
//                false,
//                0,
//                true,
//                0.75,
//                0);

//        Player player = new PuctPlayer(
//                new PuctSingleModel(
//                        Paths.get("lookup/nn/res_4h_20191215.zip"),
//                        true
//                ),
////                6,
//                12,
////                1.0,
//                1.25,
////                3.0,
//                32768,
////                65536,
////                false,
//                true,
//                true,
//                0);

//        Player player = new PuctPlayer.Builder(
//                new PuctSingleModel(
////                        Paths.get("lookup/nn/res_5a_head.zip")
////                        Paths.get("lookup/nn/res_5_p_2_12_head.zip")
////                        Paths.get("lookup/nn/res_5_p_13_22_head.zip")
//                        Paths.get("lookup/nn/res_7_p_23_32_head.zip")
//                ))
////                .threads(1)
//                .threads(48)
//                .stochastic(true)
//                .build();

//        Player player = new PuctPlayer.Builder(
//                new PuctMultiModel(
//                        ImmutableRangeMap.<Integer, Path>builder()
////                                .put(Range.closed(2, 10),
////                                        Paths.get("lookup/nn/res_7_p_2_12_n634.zip"))
////                                .put(Range.closed(11, 22),
//                                .put(Range.closed(2, 22),
//                                        Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
////                                .put(Range.closed(23, 28),
////                                        Paths.get("lookup/nn/res_14_p_16_28_n1209.zip"))
////                                .put(Range.closed(29, 32),
////                                        Paths.get("lookup/nn/res_14_p_23_32_n956.zip"))
//                                .put(Range.closed(23, 32),
////                                        Paths.get("lookup/nn/res_14b_n811.zip"))
//                                        Paths.get("lookup/nn/res_20_n1307.zip"))
////                                        Paths.get("lookup/nn/res_20_b_n578.zip"))
//                                .build()
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


        Player player = new RolloutPlayer.Builder(
                new PuctMultiModel(
                        ImmutableRangeMap.<Integer, Path>builder()
                                .put(Range.closed(2, 22),
                                        Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
                                .put(Range.closed(23, 32),
//                                        Paths.get("lookup/nn/res_14b_n811.zip"))
                                        Paths.get("lookup/nn/res_20_n1307.zip"))
//                                        Paths.get("lookup/nn/res_20_b_n578.zip"))
                                .build()
                ))
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
                .threads(256)
//                .stochastic(true)
//                .store(new SynchronizedRolloutStore(new FileRolloutStore(Path.of(
//                        new FileRolloutStore(Path.of(
//                                "lookup/tree/test.bin"))))
                .store(new SynchronizedRolloutStore(
                        new TieredRolloutStore(Path.of(
                                "lookup/tree/test.bin"))))
                .build();

//        Player player = new MetaPlayer.Builder(
//                new MetaSingleModel(
//                        Paths.get("lookup/nn/res_5m_head.zip")
//                ))
////                .threads(1)
//                .threads(48)
//                .stochastic(true)
//                .build();

        State state = State.fromFen(
                // https://www.youtube.com/watch?v=TdcPgTzSTnM
//                "2r4q/p4pk1/3pb1p1/N7/1PPp2n1/P3b1PB/2R1P1K1/3NQR2 b - - 0 1"

                // https://www.youtube.com/watch?v=weihJSohY28
//                "6r1/1p2n2p/1np2pp1/3p1k2/2P2p2/PP1P2PK/NN1BPb1P/R7 w - - 0 1"

//                "2bQr2k/Brp3bp/2p2np1/5p2/2B1P3/P1N2P2/1q4PP/3R1RK1 w - - 4 20"

                // mate in 9 (17) - mid and big nets find it
//                "1Nr1n3/p3p1q1/P2p1prk/4p3/1pB1n1P1/1P1R4/3b2KN/8 w"

                // Paul (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 0 1"
//                "rnbqkbnr/pp2pppp/3p4/2p5/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 1"
//                "rnbqkbnr/pp2pppp/3p4/2p5/4P3/2N2N2/PPPP1PPP/R1BQKB1R b KQkq - 0 1"
//                "rnbqkbnr/1p2pppp/p2p4/2p5/4P3/2N2N2/PPPP1PPP/R1BQKB1R w KQkq - 0 1"
//                "rnbqkbnr/1p2pppp/p2p4/2p5/4P3/2NP1N2/PPP2PPP/R1BQKB1R b KQkq - 0 1"
//                "rnbqkbnr/1p2pppp/p2p4/2p5/4P3/2NP1N2/PPP2PPP/R1BQKB1R b KQkq - 0 1"
//                "rnbqkbnr/4pppp/p2p4/1pp5/4P3/2NP1N2/PPP2PPP/R1BQKB1R w KQkq b6 0 1"
//                "rnbqkbnr/4pppp/p2p4/1pp5/4P3/2NP1N2/PPP1BPPP/R1BQK2R b KQkq - 0 1"
//                "rn1qkbnr/1b2pppp/p2p4/1pp5/4P3/2NP1N2/PPP1BPPP/R1BQK2R w KQkq - 0 1"
//                "rn1qkbnr/1b2pppp/p2p4/1pp5/4PB2/2NP1N2/PPP1BPPP/R2QK2R b KQkq - 0 1"
//                "rn1qkb1r/1b2pppp/p2p1n2/1pp5/4PB2/2NP1N2/PPP1BPPP/R2QK2R w KQkq - 0 1"
//                "rn1qkb1r/1b2pppp/p2p1n2/1pp5/4PB2/2NP1N2/PPPQBPPP/R3K2R b KQkq - 0 1"
//                "rn1qkb1r/1b3ppp/p2ppn2/1pp5/4PB2/2NP1N2/PPPQBPPP/R3K2R w KQkq - 0 1"
//                "rn1qkb1r/1b3ppp/p2ppn2/1pp5/4PB2/2NP1N2/PPPQBPPP/R4RK1 b kq - 0 1"
//                "rn1qk2r/1b2bppp/p2ppn2/1pp5/4PB2/2NP1N2/PPPQBPPP/R4RK1 w kq - 0 1"
//                "rn1qk2r/1b2bppp/p2ppn2/1pp5/4PB2/2NP1N2/PPPQBPPP/3R1RK1 b kq - 1 1"
//                "rn1q1rk1/1b2bppp/p2ppn2/1pp5/4PB2/2NP1N2/PPPQBPPP/3R1RK1 w - - 2 2"
//                "rn1q1rk1/1b2bppp/p2ppn2/1pp3B1/4P3/2NP1N2/PPPQBPPP/3R1RK1 b - - 2 2"
//                "r2q1rk1/1b1nbppp/p2ppn2/1pp3B1/4P3/2NP1N2/PPPQBPPP/3R1RK1 w - - 2 2"
//                "r2q1rk1/1b1nbppp/p2ppn2/1pp3B1/4P3/2NP1N1P/PPPQBPP1/3R1RK1 b - - 2 2"
//                "r4rk1/1bqnbppp/p2ppn2/1pp3B1/4P3/2NP1N1P/PPPQBPP1/3R1RK1 w - - 3 3"
//                "r4rk1/1bqnbppp/p2ppn2/1pp3B1/4P3/2NPQN1P/PPP1BPP1/3R1RK1 b - - 4 3"
//                "r3r1k1/1bqnbppp/p2ppn2/1pp3B1/4P3/2NPQN1P/PPP1BPP1/3R1RK1 w - - 4 3"


                // Travis 2 (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"
//                "rnbqkb1r/pppppppp/5n2/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkb1r/pppppppp/5n2/8/2PP4/8/PP2PPPP/RNBQKBNR b KQkq c3 0 1"
//                "rnbqkb1r/pppp1ppp/4pn2/8/2PP4/5N2/PP2PPPP/RNBQKB1R b KQkq - 0 1"
//                "rnbqkb1r/ppp2ppp/4pn2/3p4/2PP4/5N2/PP2PPPP/RNBQKB1R w KQkq d6 0 1"
//                "rnbqkb1r/ppp2ppp/4pn2/3p4/2PP4/2N2N2/PP2PPPP/R1BQKB1R b KQkq - 0 1"
//                "rnbqkb1r/pp3ppp/4pn2/2pp4/2PP4/2N2N2/PP2PPPP/R1BQKB1R w KQkq c6 0 1"
//                "rnbqkb1r/pp3ppp/4pn2/2pp4/2PP4/2N1PN2/PP3PPP/R1BQKB1R b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n1pn2/2pp4/2PP4/2N1PN2/PP3PPP/R1BQKB1R w KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n1pn2/2pP4/3P4/2N1PN2/PP3PPP/R1BQKB1R b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n2n2/2pp4/3P4/2N1PN2/PP3PPP/R1BQKB1R w KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n2n2/2pp4/3P4/2N1PN2/PP2BPPP/R1BQK2R b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n2n2/3p4/3p4/2N1PN2/PP2BPPP/R1BQK2R w KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n2n2/3p4/3N4/2N1P3/PP2BPPP/R1BQK2R b KQkq - 0 1"
//                "r1bqkb1r/1p3ppp/p1n2n2/3p4/3N4/2N1P3/PP2BPPP/R1BQK2R w KQkq - 0 1"
//                "r1bqkb1r/1p3ppp/p1n2n2/3p4/3N4/2N1P3/PP2BPPP/R1BQ1RK1 b kq - 0 1"
//                "r1bqk2r/1p3ppp/p1nb1n2/3p4/3N4/2N1P3/PP2BPPP/R1BQ1RK1 w kq - 0 1"
//                "r1bqk2r/1p3ppp/p1nb1n2/3p4/8/2N1PN2/PP2BPPP/R1BQ1RK1 b kq - 0 1"
//                "r1bq1rk1/1p3ppp/p1nb1n2/3p4/8/2N1PN2/PP2BPPP/R1BQ1RK1 w - - 0 1"
//                "r1bq1rk1/1p3ppp/p1nb1n2/3p4/8/P1N1PN2/1P2BPPP/R1BQ1RK1 b - - 0 1"
//                "r2q1rk1/1p3ppp/p1nbbn2/3p4/8/P1N1PN2/1P2BPPP/R1BQ1RK1 w - - 0 1"
//                "r2q1rk1/1p3ppp/p1nbbn2/3p4/1P6/P1N1PN2/4BPPP/R1BQ1RK1 b - b3 0 1"
//                "r4rk1/1p2qppp/p1nbbn2/3p4/1P6/P1N1PN2/4BPPP/R1BQ1RK1 w - - 0 1"
//                "r4rk1/1p2qppp/p1nbbn2/3p4/1P6/P1N1PN2/1B2BPPP/R2Q1RK1 b - - 0 1"
//                "3r1rk1/1p2qppp/p1nbbn2/3p4/1P6/P1N1PN2/1B2BPPP/R2Q1RK1 w - - 0 1"
//                "3r1rk1/1p2qppp/p1nbbn2/3p4/1P1N4/P1N1P3/1B2BPPP/R2Q1RK1 b - - 0 1"
//                "3r1rk1/1p2qppp/p2bbn2/3pn3/1P1N4/P1N1P3/1B2BPPP/R2Q1RK1 w - - 0 1"
//                "3r1rk1/1p2qppp/p2bbn2/3pn3/1P1N4/P1N1P3/1BQ1BPPP/R4RK1 b - - 0 1"
//                "2r2rk1/1p2qppp/p2bbn2/3pn3/1P1N4/P1N1P3/1BQ1BPPP/R4RK1 w - - 0 1"
//                "2r2rk1/1p2qppp/p2bbn2/3pn3/1P1N4/P1N1P3/1BQ1BPPP/2R2RK1 b - - 0 1"
//                "2r2rk1/1p2qppp/p2bbn2/3p4/1PnN4/P1N1P3/1BQ1BPPP/2R2RK1 w - - 0 1"
//                "2r2rk1/1p2qppp/p2bbn2/3p4/1PnN4/P1N1P2P/1BQ1BPP1/2R2RK1 b - - 0 1"
//                "2r2rk1/4qppp/p2bbn2/1p1p4/1PnN4/P1N1P2P/1BQ1BPP1/2R2RK1 w - b6 0 1"
//                "2r2rk1/4qppp/p2bbn2/1p1p4/1PnN4/P1N1P2P/1BQ1BPP1/2R1R1K1 b - - 0 1"
//                "2r2rk1/4qpp1/p2bbn1p/1p1p4/1PnN4/P1N1P2P/1BQ1BPP1/2R1R1K1 w - - 0 1"
//                "2r2rk1/4qpp1/p2bbn1p/1p1p4/1PnN4/P1N1PB1P/1BQ2PP1/2R1R1K1 b - - 0 1"
//                "2r1r1k1/4qpp1/p2bbn1p/1p1p4/1PnN4/P1N1PB1P/1BQ2PP1/2R1R1K1 w - - 0 1"
//                "2r1r1k1/4qpp1/p2bbn1p/1p1p4/1PnN4/P1N1PB1P/1B2QPP1/2R1R1K1 b - - 0 1"
//                "1br1r1k1/4qpp1/p3bn1p/1p1p4/1PnN4/P1N1PB1P/1B2QPP1/2R1R1K1 w - - 0 1"
//                "1br1r1k1/4qpp1/p3Nn1p/1p1p4/1Pn5/P1N1PB1P/1B2QPP1/2R1R1K1 b - - 0 1"
//                "1br1r1k1/4q1p1/p3pn1p/1p1p4/1Pn5/P1N1PB1P/1B2QPP1/2R1R1K1 w - - 0 1"
//                "1br1r1k1/4q1p1/p3pn1p/1p1p4/1Pn5/P1N1PB1P/1B2QPP1/2RR2K1 b - - 0 1"
//                "1br2rk1/4q1p1/p3pn1p/1p1p4/1Pn5/P1N1PB1P/1B2QPP1/2RR2K1 w - - 0 1"
//                "1br2rk1/4q1p1/p3pn1p/1p1p4/1Pn1P3/P1N2B1P/1B2QPP1/2RR2K1 b - - 0 1"
//                "1br2rk1/6p1/p2qpn1p/1p1p4/1Pn1P3/P1N2B1P/1B2QPP1/2RR2K1 w - - 0 1"
//                "1br2rk1/6p1/p2qpn1p/1p1p4/1Pn1P3/P1N2BPP/1B2QP2/2RR2K1 b - - 0 1"
//                "2r2rk1/b5p1/p2qpn1p/1p1p4/1Pn1P3/P1N2BPP/1B2QP2/2RR2K1 w - - 0 1"
//                "2r2rk1/b5p1/p2qpn1p/1p1p4/1Pn1P3/P1N2BPP/1B2QPK1/2RR4 b - - 0 1"
//                "2r2rk1/b5p1/p2qpn1p/1p6/1PnpP3/P1N2BPP/1B2QPK1/2RR4 w - - 0 1"

//                "r1bqkbnr/pppppppp/8/8/2Pn2P1/8/PP1PPP1P/RNB1KBNR w KQkq - 1 3"
//                "r1bqkbnr/pppppppp/8/8/P1Pn2P1/8/1P1PPP1P/RNB1KBNR b KQkq a3 0 3"
//                "r1bqkbnr/pppppppp/8/8/P1P3P1/5n2/1P1PPP1P/RNB1KBNR w KQkq - 1 4"

                // Travis (queen odds)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/7P/8/PPPPPPP1/RNB1KBNR b KQkq h3 0 1"
//                "rnbqkbnr/ppp1pppp/8/3p4/7P/8/PPPPPPP1/RNB1KBNR w KQkq d6 0 1"
//                "rnbqkbnr/ppp1pppp/8/3p4/P6P/8/1PPPPPP1/RNB1KBNR b KQkq a3 0 1"
//                "rnbqkbnr/ppp2ppp/8/3pp3/P6P/8/1PPPPPP1/RNB1KBNR w KQkq e6 0 1"
//                "rnbqkbnr/ppp2ppp/8/3pp3/P6P/3P4/1PP1PPP1/RNB1KBNR b KQkq - 0 1"
//                "rnbqkb1r/ppp2ppp/5n2/3pp3/P6P/3P4/1PP1PPP1/RNB1KBNR w KQkq - 0 1"
////                "rnbqkb1r/ppp2ppp/5n1B/3pp3/P6P/3P4/1PP1PPP1/RN2KBNR b KQkq - 0 1" // ???
//                "rnbqkb1r/ppp2ppp/5n2/3pp3/P6P/2PP4/1P2PPP1/RNB1KBNR b KQkq - 0 1"
//                "rnbqk2r/ppp2ppp/5n2/2bpp3/P6P/2PP4/1P2PPP1/RNB1KBNR w KQkq - 0 1"
//                "rnbqk2r/ppp2ppp/5n2/2bpp3/P6P/2PPP3/1P3PP1/RNB1KBNR b KQkq - 0 1"
//                "rnbqk2r/ppp2ppp/5n2/2b1p3/P2p3P/2PPP3/1P3PP1/RNB1KBNR w KQkq - 0 1"
//                "rnbqk2r/ppp2ppp/5n2/2b1p3/PP1p3P/2PPP3/5PP1/RNB1KBNR b KQkq b3 0 1"
//                "rnbqk2r/ppp1bppp/5n2/4p3/PP1p3P/2PPP3/5PP1/RNB1KBNR w KQkq - 0 1"
//                "rnbqk2r/ppp1bppp/5n2/4p3/PP1P3P/2PP4/5PP1/RNB1KBNR b KQkq - 0 1"
//                "rnbqk2r/ppp1bppp/5n2/8/PP1p3P/2PP4/5PP1/RNB1KBNR w KQkq - 0 2"
//                "rnbqk2r/ppp1bppp/5n2/1P6/P2p3P/2PP4/5PP1/RNB1KBNR b KQkq - 0 1"


                // Travis (queen odds) attempt 2
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/P7/8/1PPPPPPP/RNB1KBNR b KQkq a3 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/P7/8/1PPPPPPP/RNB1KBNR w KQkq e6 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/P6P/8/1PPPPPP1/RNB1KBNR b KQkq h3 0 1"
//                "rnbqkbnr/ppp2ppp/8/3pp3/P6P/8/1PPPPPP1/RNB1KBNR w KQkq d6 0 1"
//                "rnbqkbnr/ppp2ppp/8/3pp3/P6P/3P4/1PP1PPP1/RNB1KBNR b KQkq - 0 1"

                // Travis (Queeen odds) attempts 3
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNB1KBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/8/5N2/PPPPPPPP/RNB1KB1R b KQkq - 0 1"


//                "2K1k1br/2qp1n1r/2p2pN1/3p1N2/2P4P/8/P2P4/8 w - - 0 14"
//                "4k1br/2Kp1n1r/2p2pN1/3p1N2/2P4P/8/P2P4/8 b - - 0 14"
//                "4k1br/2Kp1n1r/2p2pN1/5N2/2p4P/8/P2P4/8 w - - 0 15" // or here?
//                "2K1k1br/3p1n1r/2p2pN1/5N2/2p4P/8/P2P4/8 b - - 1 15"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/2p4P/8/P2P4/8 w - d6 0 16"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/P1p4P/8/3P4/8 b - a3 0 16" // 14b fail, 14 2-22 works!
                "2K1k1br/5n1r/2p2pN1/3p1N2/P6P/2p5/3P4/8 w - - 0 17"
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

        boolean solved = false;
        int episodeCount = time / flushFrequencyMillis;
        for (int i = 0; i < episodeCount; i++) {
            int move = player.move(state, flushFrequencyMillis, flushFrequencyMillis, 0);
            System.out.println("Episode " + i + ") " + Move.toString(move));

            if (player.isSolved(state)) {
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
        System.exit(0);
    }
}
