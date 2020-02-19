package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.neuro.puct.PuctPlayer;
import ao.chess.v2.engine.neuro.puct.PuctSingleModel;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.nio.file.Paths;


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
    public static void main(String[] args) {
//        int time = 7 * 24 * 60 * 60 * 1000;
//        int time = 10 * 1000;
//        int time = 15 * 1000;
        int time = 45 * 1000;
//        int time = 60 * 1000;
//        int time = 10 * 60 * 1000;
//        int time = 60 * 60 * 1000;
//        int time = 24 * 60 * 60 * 1000;

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

        Player player = new PuctPlayer.Builder(
                new PuctMultiModel(
                        ImmutableRangeMap.<Integer, Path>builder()
                                .put(Range.closed(2, 12),
                                        Paths.get("lookup/nn/res_5_p_2_12_head.zip"))
                                .put(Range.closed(13, 22),
                                        Paths.get("lookup/nn/res_5_p_13_22_head.zip"))
                                .put(Range.closed(23, 32),
                                        Paths.get("lookup/nn/res_7_p_23_32_n828.zip"))
                                .build()
                ))
//                .threads(1)
//                .threads(48)
//                .threads(52)
//                .threads(64)
//                .threads(96)
//                .threads(128)
//                .threads(160)
//                .threads(192)
//                .threads(224)
                .threads(256)
                .stochastic(true)
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
                // puzzles
//                "6rk/6pp/3N4/8/8/8/7P/7K w - - 0 1" // N from d6 to f7
//                "5Qk1/5p2/1p5p/p4Np1/5q2/7P/PPr5/3R3K b - - 2 1"
//                "5k2/5p2/1p5p/p4Np1/5q2/7P/PPr5/3R3K w - - 0 2"

//                "6k1/5p2/1p5p/p4Np1/5q2/Q6P/PPr5/3R3K w - - 10" // Q form a3 to f8
//                "8/2k2p2/2b3p1/P1p1Np2/1p3b2/1P1K4/5r2/R3R3 b - - 0 1" // b from c6 to b5
//                "r1b2k1r/ppppq3/5N1p/4P2Q/4PP2/1B6/PP5P/n2K2R1 w - - 1 0" // Q from h5 to h6 (!!)

//                "R6R/1r3pp1/4p1kp/3pP3/1r2qPP1/7P/1P1Q3K/8 w - - 1 0" // P from f4 to f5
//                "4r1k1/5bpp/2p5/3pr3/8/1B3pPq/PPR2P2/2R2QK1 b - - 0 1" // r from e5 to e1 (!!)
//                "7R/r1p1q1pp/3k4/1p1n1Q2/3N4/8/1PP2PPP/2B3K1 w - - 1 0" // R from h8 to d8 (!!)
//                "rn3rk1/pbppq1pp/1p2pb2/4N2Q/3PN3/3B4/PPP2PPP/R3K2R w KQ - 7 11" // Q from h5 to h7 (!!!)
//                "r1bqkb1r/pp1n1pp1/2p1pn1p/6N1/3P4/3B1N2/PPP2PPP/R1BQK2R w KQkq - 0 8" // g5 e6 (deep blue)

                // mate in 9 (17) - mid net finds it
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
//                "rn1qk2r/1b2bppp/p2ppn2/1pp5/4PB2/2NP1N2/PPPQBPPP/3R1RK1 b kq - 0 1"
                "rn1q1rk1/1b2bppp/p2ppn2/1pp5/4PB2/2NP1N2/PPPQBPPP/3R1RK1 w - - 0 1"

                // Josh (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/3PP3/8/PPP2PPP/RNBQKBNR b KQkq d3 0 1"
//                "rnbqkbnr/pp1ppppp/8/8/3pP3/8/PPP2PPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pp1ppppp/8/8/3QP3/8/PPP2PPP/RNB1KBNR b KQkq - 0 1"
//                "rnbqkbnr/1p1ppppp/p7/8/3QP3/8/PPP2PPP/RNB1KBNR w KQkq - 0 1"

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

                // Josh (black)
                // 1. d2d4 d7d5 2. c2c4 Ng8f6 3. c4xd5 Qd8xd5 4. Nb1c3 Qd5a5 5. Ng1f3 Bc8g4 6. Nf3e5 c7c5
                // 7. Ne5xg4 c5xd4 8. Qd1xd4 Nb8c6 9. Ng4xf6+ g7xf6 10. Qd4d5 e7e6 11. Qd5xa5 Nc6xa5
                // 12. g2g3 O-O-O 13. Bf1g2 Bf8b4 14. O-O Na5c6 15. Bg2xc6 b7xc6 16. Nc3a4 e6e5 17. Bc1e3 Kc8b7
                // 18. a2a3 Bb4d6 19. Ra1c1 f6f5 20. Be3g5 Rd8f8 21. Bg5h4 f7f6 22. b2b4 Rh8g8 23. Rf1d1 Kb7c7
                // 24. Rc1c2 f5f4 25. Kg1f1 f4xg3 26. h2xg3 f6f5 27. b4b5 f5f4 28. Rc2xc6+ Kc7d7 29. Rd1xd6+

                // Gus (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p4Q/4P3/8/PPPP1PPP/RNB1KBNR b KQkq - 0 1"
//                "rnbqkbnr/pp2pppp/3p4/2p4Q/4P3/8/PPPP1PPP/RNB1KBNR w KQkq - 0 1"
//                "rnbqkbnr/pp2pppp/3p4/2p4Q/2B1P3/8/PPPP1PPP/RNB1K1NR b KQkq - 0 1"
//                "rnbqkbnr/pp3ppp/3pp3/2p4Q/2B1P3/8/PPPP1PPP/RNB1K1NR w KQkq - 0 1"
//                "rnbqkbnr/pp3ppp/3pp3/2p4Q/2B1P3/7N/PPPP1PPP/RNB1K2R b KQkq - 0 1"
//                "r1bqkbnr/pp3ppp/2npp3/2p4Q/2B1P3/7N/PPPP1PPP/RNB1K2R w KQkq - 0 1"
//                "r1bqkbnr/pp3ppp/2npp3/2p4Q/2B1P3/P6N/1PPP1PPP/RNB1K2R b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2nppn2/2p4Q/2B1P3/P6N/1PPP1PPP/RNB1K2R w KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2nppn2/2p5/2B1P3/P4Q1N/1PPP1PPP/RNB1K2R b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n1pn2/2pp4/2B1P3/P4Q1N/1PPP1PPP/RNB1K2R w KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n1pn2/1Bpp4/4P3/P4Q1N/1PPP1PPP/RNB1K2R b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n1pn2/1Bp5/4p3/P4Q1N/1PPP1PPP/RNB1K2R w KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n1pn2/1Bp5/4pQ2/P6N/1PPP1PPP/RNB1K2R b KQkq - 0 1"
//                "r2qkb1r/pp1b1ppp/2n1pn2/1Bp5/4pQ2/P6N/1PPP1PPP/RNB1K2R w KQkq - 0 1"
//                "r2qkb1r/pp1b1ppp/2n1pn2/1Bp5/4pQ2/P1N4N/1PPP1PPP/R1B1K2R b KQkq - 0 1"
//                "r2qkb1r/pp1b1ppp/4pn2/1Bp5/3npQ2/P1N4N/1PPP1PPP/R1B1K2R w KQkq - 0 1"
//                "r2qkb1r/pp1b1ppp/4pn2/1Bp5/3npQ2/P1N4N/RPPP1PPP/2B1K2R b kq - 0 1"
//                "r2qkb1r/pp1b1ppp/4pn2/1np5/4pQ2/P1N4N/RPPP1PPP/2B1K2R w kq - 0 1"
//                "r2qkb1r/pp1b1ppp/4pn2/1np5/P3pQ2/2N4N/RPPP1PPP/2B1K2R b kq - 0 1"
//                "r2qkb1r/pp1b1ppp/4pn2/2p5/P3pQ2/2n4N/RPPP1PPP/2B1K2R w kq - 0 1"
//                "r2qkb1r/pp1b1ppp/4pn2/2p5/P3pQ2/2P4N/R1PP1PPP/2B1K2R b kq - 0 1"
//                "r2qkb1r/pp3ppp/2b1pn2/2p5/P3pQ2/2P4N/R1PP1PPP/2B1K2R w kq - 0 1"
//                "r2qkb1r/pp3ppp/2b1pn2/2p3Q1/P3p3/2P4N/R1PP1PPP/2B1K2R b kq - 0 1"
//                "r2qkb1r/pp3pp1/2b1pn1p/2p3Q1/P3p3/2P4N/R1PP1PPP/2B1K2R w kq - 0 1"
//                "r2qkb1r/pp3pp1/2b1pn1p/2Q5/P3p3/2P4N/R1PP1PPP/2B1K2R b kq - 0 1"
//                "r2qk2r/pp3pp1/2b1pn1p/2b5/P3p3/2P4N/R1PP1PPP/2B1K2R w kq - 0 1"
//                "r2qk2r/pp3pp1/2b1pn1p/2b5/P3p3/B1P4N/R1PP1PPP/4K2R b kq - 0 1"
//                "r1q1k2r/pp3pp1/2b1pn1p/2b5/P3p3/B1P4N/R1PP1PPP/4K2R w kq - 0 1"
//                "r1q1k2r/pp3pp1/2b1pn1p/2B5/P3p3/2P4N/R1PP1PPP/4K2R b kq - 0 1"
//                "r1q1k2r/p4pp1/1pb1pn1p/2B5/P3p3/2P4N/R1PP1PPP/4K2R w kq - 0 1"
//                "r1q1k2r/p4pp1/1pb1pn1p/8/P2Bp3/2P4N/R1PP1PPP/4K2R b kq - 0 1"
//                "r1q2rk1/p4pp1/1pb1pn1p/8/P2Bp3/2P4N/R1PP1PPP/4K2R w - - 0 1"

                // Pira (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/8/4P3/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/8/4P3/PPPP1PPP/RNBQKBNR w KQkq c6 0 1"
//                "rnbqkbnr/pp1ppppp/8/1Bp5/8/4P3/PPPP1PPP/RNBQK1NR b KQkq - 0 1"
//                "rnbqkb1r/pp1ppppp/5n2/1Bp5/8/2P1P3/PP1P1PPP/RNBQK1NR b KQkq - 0 1"
//                "rnbqkb1r/1p1ppppp/p4n2/1Bp5/8/2P1P3/PP1P1PPP/RNBQK1NR w KQkq - 0 1"
//                "rnbqkb1r/1p1ppppp/p4n2/2p5/B7/2P1P3/PP1P1PPP/RNBQK1NR b KQkq - 0 1"
//                "rnbqkb1r/3ppppp/p4n2/1pp5/B7/2P1P3/PP1P1PPP/RNBQK1NR w KQkq b6 0 1"
//                "rnbqkb1r/3ppppp/p4n2/1pp5/B7/2P1PQ2/PP1P1PPP/RNB1K1NR b KQkq - 0 1"
//                "rnbqkb1r/4pppp/p4n2/1ppp4/B7/2P1PQ2/PP1P1PPP/RNB1K1NR w KQkq d6 0 1"
//                "rnbqkb1r/4pppp/p4n2/1ppp4/8/2P1PQ2/PPBP1PPP/RNB1K1NR b KQkq - 0 1"
//                "rnbqkb1r/5ppp/p4n2/1pppp3/8/2P1PQ2/PPBP1PPP/RNB1K1NR w KQkq e6 0 1"
//                "rnbqkb1r/5ppp/p4n2/1pppp3/P7/2P1PQ2/1PBP1PPP/RNB1K1NR b KQkq a3 0 1"
//                "rnbqkb1r/5ppp/p4n2/2ppp3/Pp6/2P1PQ2/1PBP1PPP/RNB1K1NR w KQkq - 0 1"
//                "rnbqkb1r/5ppp/p4n2/2ppp3/PpP5/4PQ2/1PBP1PPP/RNB1K1NR b KQkq - 0 1"
//                "rnbqkb1r/5ppp/p4n2/2pp4/PpP1p3/4PQ2/1PBP1PPP/RNB1K1NR w KQkq - 0 1"
//                "rnbqkb1r/5ppp/p4n2/2pp4/PpP1pQ2/4P3/1PBP1PPP/RNB1K1NR b KQkq - 0 1"
//                "rnbqk2r/5ppp/p2b1n2/2pp4/PpP1pQ2/4P3/1PBP1PPP/RNB1K1NR w KQkq - 0 1"
//                "rnbqk2r/5ppp/p2b1n2/2pp4/PpP1p2Q/4P3/1PBP1PPP/RNB1K1NR b KQkq - 0 1"
//                "rn1qk2r/5ppp/p2bbn2/2pp4/PpP1p2Q/4P3/1PBP1PPP/RNB1K1NR w KQkq - 0 1"
//                "rn1qk2r/5ppp/p2bbn2/2pP4/Pp2p2Q/4P3/1PBP1PPP/RNB1K1NR b KQkq - 0 1"
//                "rn1qk2r/5ppp/p2b1n2/2pb4/Pp2p2Q/4P3/1PBP1PPP/RNB1K1NR w KQkq - 0 1"
//                "rn1qk2r/5ppp/p2b1n2/2pb2Q1/Pp2p3/4P3/1PBP1PPP/RNB1K1NR b KQkq - 0 1"
//                "rn1q1rk1/5ppp/p2b1n2/2pb2Q1/Pp2p3/4P3/1PBP1PPP/RNB1K1NR w KQ - 0 1"
//                "rn1q1rk1/5ppp/p2b1n2/2pb2Q1/Pp2p3/4P2N/1PBP1PPP/RNB1K2R b KQ - 0 1"
//                "rn1q1rk1/5pp1/p2b1n1p/2pb2Q1/Pp2p3/4P2N/1PBP1PPP/RNB1K2R w KQ - 0 1"
//                "rn1q1rk1/5pp1/p2b1n1p/2pb4/Pp2p2Q/4P2N/1PBP1PPP/RNB1K2R b KQ - 0 1"
//                "r2q1rk1/5pp1/p1nb1n1p/2pb4/Pp2p2Q/4P2N/1PBP1PPP/RNB1K2R w KQ - 0 1"
//                "r2q1rk1/5pp1/p1nb1n1p/2pb4/Pp2p2Q/3PP2N/1PB2PPP/RNB1K2R b KQ - 0 1"
//                "r2q1rk1/5pp1/p1nb1n1p/2pb4/Pp5Q/3pP2N/1PB2PPP/RNB1K2R w KQ - 0 1"
//                "r2q1rk1/5pp1/p1nb1n1p/2pb4/Pp5Q/3BP2N/1P3PPP/RNB1K2R b KQ - 0 1"
//                "r2q1rk1/5pp1/p2b1n1p/2pbn3/Pp5Q/3BP2N/1P3PPP/RNB1K2R w KQ - 0 1"
//                "r2q1rk1/5pp1/p2b1n1p/2pbn3/Pp3N1Q/3BP3/1P3PPP/RNB1K2R b KQ - 0 1" // ??
//                "r2q1rk1/5pp1/p2b1n1p/2pb4/Pp3N1Q/3nP3/1P3PPP/RNB1K2R w KQ - 0 1"
//                "r2q1rk1/5pp1/p2b1n1p/2pb4/Pp5Q/3NP3/1P3PPP/RNB1K2R b KQ - 0 1"
//                "r4rk1/2q2pp1/p2b1n1p/2pb4/Pp5Q/3NP3/1P3PPP/RNB1K2R w KQ - 0 1"
//                "r4rk1/2q2pp1/p2b1n1p/2pb4/Pp5Q/3NPP2/1P4PP/RNB1K2R b KQ - 0 1"
//                "r4rk1/2q2pp1/p2b1n1p/3b4/Ppp4Q/3NPP2/1P4PP/RNB1K2R w KQ - 0 1"
//                "r4rk1/2q2pp1/p2b1n1p/3b4/Ppp2N1Q/4PP2/1P4PP/RNB1K2R b KQ - 0 1"
//                "r4rk1/1bq2pp1/p2b1n1p/8/Ppp2N1Q/4PP2/1P4PP/RNB1K2R w KQ - 0 1"
//                "r4rk1/1bq2pp1/p2b1n1p/8/Ppp2N1Q/4PP2/1P1N2PP/R1B1K2R b KQ - 0 1"
//                "r3r1k1/1bq2pp1/p2b1n1p/8/Ppp2N1Q/4PP2/1P1N2PP/R1B1K2R w KQ - 0 1"
//                "r3r1k1/1bq2pp1/p2b1n1p/8/Ppp2N1Q/4PP2/1P1NK1PP/R1B4R b - - 0 1"
//                "r3r1k1/1bq2pp1/p4n1p/8/Ppp2b1Q/4PP2/1P1NK1PP/R1B4R w - - 0 1"
//                "r3r1k1/1bq2pp1/p4n1p/8/Ppp2Q2/4PP2/1P1NK1PP/R1B4R b - - 0 1"
//                "r3r1k1/1b3pp1/p4n1p/8/Ppp2q2/4PP2/1P1NK1PP/R1B4R w - - 0 1"
//                "r3r1k1/1b3pp1/p4n1p/8/Ppp2q2/4PP2/1P1NK1PP/R1B2R2 b - - 0 1"
//                "r3r1k1/1b3pp1/p4n1p/8/Ppp5/4qP2/1P1NK1PP/R1B2R2 w - - 0 1"
//                "r3r1k1/1b3pp1/p4n1p/8/Ppp5/4qP2/1P1N2PP/R1BK1R2 b - - 0 1"
//                "3rr1k1/1b3pp1/p4n1p/8/Ppp5/4qP2/1P1N2PP/R1BK1R2 w - - 0 1"
//                "3rr1k1/1b3pp1/p4n1p/P7/1pp5/4qP2/1P1N2PP/R1BK1R2 b - - 0 1"
//                "3rr1k1/1b3pp1/p4n1p/P7/1pp5/1q3P2/1P1N2PP/R1BK1R2 w - - 0 1"

                // Travis (black)
                //  1. c2c4 e7e5 2. Ng1f3 Nb8c6 3. Nb1c3 d7d6 4. d2d4 e5xd4 5. Nf3xd4 Bc8d7 6. g2g3 Ng8f6
                //  7. Bf1g2 Bf8e7 8. O-O O-O 9. Nd4xc6 Bd7xc6 10. Bg2xc6 b7xc6 11. Qd1c2 Qd8d7 12. Bc1e3 Rf8e8
                //  13. Ra1d1 h7h5 14. Be3d4 h5h4 15. e2e4 Qd7g4 16. f2f3 Qg4g6 17. Qc2g2 h4xg3 18. h2xg3 Nf6h5
                //  19. g3g4 Nh5f4 20. Qg2h2 Be7g5 21. Nc3e2 Nf4xe2+ 22. Qh2xe2 Bg5f4 23. Bd4c3 f7f6 24. c4c5 Kg8f7
                //  25. c5xd6 c7xd6 26. Qe2c4+ Re8e6 27. Qc4xc6 Ra8h8 28. Rd1d5 Rh8h3 29. Rd5f5 Qg6h6
                //  30. Qc6c7+ Re6e7 31. Qc7c4+ Re7e6 32. Qc4a6 Bf4e3+
//                "r2q1rk1/ppp1bppp/2bp1n2/8/2P5/2N3P1/PP2PPBP/R1BQ1RK1 w - - 0 1" // g2c6??
//                "r3r1k1/p1p2pp1/2pp2q1/6b1/2PBPnP1/2N2P2/PP5Q/3R1RK1 w - - 0 1" // c3e2?
//                "r3r1k1/p1p2pp1/2pp2q1/8/2PBPbP1/5P2/PP2Q3/3R1RK1 w - - 0 1" // d4c3?
//                "8/p4kp1/2Qprpq1/3R4/4PbP1/2B2P1r/PP6/5RK1 w - - 0 1" // ??

                // Phi (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/8/7P/PPPPPPP1/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/8/5N1P/PPPPPPP1/RNBQKB1R b KQkq - 0 1"
//                "r1bqkbnr/pp1ppppp/2n5/2p5/8/5N1P/PPPPPPP1/RNBQKB1R w KQkq - 0 1"
//                "r1bqkbnr/pp1ppppp/2n5/2p5/8/2N2N1P/PPPPPPP1/R1BQKB1R b KQkq - 0 1"
//                "r1bqkbnr/pp1ppp1p/2n3p1/2p5/8/2N2N1P/PPPPPPP1/R1BQKB1R w KQkq - 0 1"
//                "r1bqkbnr/pp1ppp1p/2n3p1/2p5/4N3/5N1P/PPPPPPP1/R1BQKB1R b KQkq - 0 1" // ?? f8g7 - give up pawn
//                "r1bqk1nr/pp1pppbp/2n3p1/2p5/4N3/5N1P/PPPPPPP1/R1BQKB1R w KQkq - 0 1"
//                "r1bqk1nr/pp1pppbp/2n3p1/2N5/8/5N1P/PPPPPPP1/R1BQKB1R b KQkq - 0 1"
//                "r1bqk1nr/pp2ppbp/2np2p1/2N5/8/5N1P/PPPPPPP1/R1BQKB1R w KQkq - 0 1"
//                "r1bqk1nr/pp2ppbp/2np2p1/8/8/1N3N1P/PPPPPPP1/R1BQKB1R b KQkq - 0 1"
//                "r1bqk2r/pp2ppbp/2np1np1/8/8/1N3N1P/PPPPPPP1/R1BQKB1R w KQkq - 0 1"
//                "r1bqk2r/pp2ppbp/2np1np1/8/3P4/1N3N1P/PPP1PPP1/R1BQKB1R b KQkq d3 0 1"
//                "r1bq1rk1/pp2ppbp/2np1np1/8/3P4/1N3N1P/PPP1PPP1/R1BQKB1R w KQ - 0 1"
//                "r1bq1rk1/pp2ppbp/2np1np1/8/3P4/1N3N1P/PPPBPPP1/R2QKB1R b KQ - 0 1"
//                "r1bq1rk1/pp3pbp/2np1np1/4p3/3P4/1N3N1P/PPPBPPP1/R2QKB1R w KQ e6 0 1"
//                "r1bq1rk1/pp3pbp/2np1np1/4p3/3P2P1/1N3N1P/PPPBPP2/R2QKB1R b KQ g3 0 1"
//                "r1bq1rk1/pp3pbp/2np2p1/4p3/3Pn1P1/1N3N1P/PPPBPP2/R2QKB1R w KQ - 0 1"
//                "r1bq1rk1/pp3pbp/2np2p1/4P3/4n1P1/1N3N1P/PPPBPP2/R2QKB1R b KQ - 0 1"
//                "r2q1rk1/pp3pbp/2npb1p1/4P3/4n1P1/1N3N1P/PPPBPP2/R2QKB1R w KQ - 0 1"
//                "r2q1rk1/pp3pbp/2nPb1p1/8/4n1P1/1N3N1P/PPPBPP2/R2QKB1R b KQ - 0 1"
//                "r4rk1/pp3pbp/2nqb1p1/8/4n1P1/1N3N1P/PPPBPP2/R2QKB1R w KQ - 0 1"
//                "r4rk1/pp3pbp/2nqb1p1/N7/4n1P1/5N1P/PPPBPP2/R2QKB1R b KQ - 0 1"
//                "r4rk1/pp3pbp/3qb1p1/n7/4n1P1/5N1P/PPPBPP2/R2QKB1R w KQ - 0 1"
//                "r4rk1/pp3pbp/3qb1p1/B7/4n1P1/5N1P/PPP1PP2/R2QKB1R b KQ - 0 1"
//                "r4rk1/pp3pbp/4b1p1/B1q5/4n1P1/5N1P/PPP1PP2/R2QKB1R w KQ - 0 1"

                // chess.com (black)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq c3 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/2P5/8/PP1PPPPP/RNBQKBNR w KQkq e6 0 2"
//                "rnbqkbnr/pppp1ppp/8/4p3/2P5/2N5/PP1PPPPP/R1BQKBNR b KQkq - 1 2"
//                "rnbqkb1r/pppp1ppp/5n2/4p3/2P5/2N5/PP1PPPPP/R1BQKBNR w KQkq - 2 3"
//                "rnbqkb1r/pppp1ppp/5n2/4p3/2P5/2N3P1/PP1PPP1P/R1BQKBNR b KQkq - 0 3"
//                "rnbqk2r/pppp1ppp/5n2/4p3/1bP5/2N3P1/PP1PPP1P/R1BQKBNR w KQkq - 1 4"
//                "rnbqk2r/pppp1ppp/5n2/4p3/1bP5/2N2NP1/PP1PPP1P/R1BQKB1R b KQkq - 2 4"
//                "rnbqk2r/pppp1ppp/5n2/8/1bP1p3/2N2NP1/PP1PPP1P/R1BQKB1R w KQkq - 0 5"
//                "rnbqk2r/pppp1ppp/5n2/6N1/1bP1p3/2N3P1/PP1PPP1P/R1BQKB1R b KQkq - 1 5"
//                "rnbqk2r/pppp1ppp/5n2/6N1/2P1p3/2b3P1/PP1PPP1P/R1BQKB1R w KQkq - 0 6"
//                "rnbqk2r/pppp1ppp/5n2/6N1/2P1p3/2P3P1/PP2PP1P/R1BQKB1R b KQkq - 0 6"
//                "rnbq1rk1/pppp1ppp/5n2/6N1/2P1p3/2P3P1/PP2PP1P/R1BQKB1R w KQ - 1 7"
//                "rnbq1rk1/pppp1ppp/5n2/6N1/2P1p3/2P3P1/PP2PPBP/R1BQK2R b KQ - 2 7"
//                "rnbqr1k1/pppp1ppp/5n2/6N1/2P1p3/2P3P1/PP2PPBP/R1BQK2R w KQ - 3 8"
//                "rnbqr1k1/pppp1ppp/5n2/6N1/2P1p3/2P3P1/PP2PPBP/R1BQ1RK1 b - - 4 8"
//                "rnbqr1k1/pppp1pp1/5n1p/6N1/2P1p3/2P3P1/PP2PPBP/R1BQ1RK1 w - - 0 9" // N g5 e4 ????!!
//                "rnbqr1k1/pppp1pp1/5n1p/8/2P1N3/2P3P1/PP2PPBP/R1BQ1RK1 b - - 0 9"
//                "rnbqr1k1/pppp1pp1/7p/8/2P1n3/2P3P1/PP2PPBP/R1BQ1RK1 w - - 0 10"
//                "rnbqr1k1/pppp1pp1/7p/8/2P1n3/2P3P1/PPQ1PPBP/R1B2RK1 b - - 1 10"
//                "rnb1r1k1/ppppqpp1/7p/8/2P1n3/2P3P1/PPQ1PPBP/R1B2RK1 w - - 2 11"
//                "rnb1r1k1/ppppqpp1/7p/8/2P1n3/1QP3P1/PP2PPBP/R1B2RK1 b - - 3 11"
//                "rnb1r1k1/ppp1qpp1/3p3p/8/2P1n3/1QP3P1/PP2PPBP/R1B2RK1 w - - 0 12"
//                "rnb1r1k1/ppp1qpp1/3p3p/8/2P1nB2/1QP3P1/PP2PPBP/R4RK1 b - - 1 12"
//                "rnb1r1k1/ppp1qpp1/3p3p/2n5/2P2B2/1QP3P1/PP2PPBP/R4RK1 w - - 2 13"

                // duane (white)
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/2P5/PP1P1PPP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp2pppp/8/2pP4/8/2P5/PP1P1PPP/RNBQKBNR b KQkq - 0 1"
//                "rnb1kbnr/pp2pppp/8/2pq4/2P5/8/PP1P1PPP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp2pppp/8/2p5/2P5/2N5/PP1P1PPP/R1BQKBNR b KQkq - 0 1"
//                "r1bqkbnr/pp2pppp/2n5/2p5/2P5/2NP4/PP3PPP/R1BQKBNR b KQkq - 0 1"
//                "r1bqkb1r/pp2pppp/2n2n2/2p5/2P5/2NP4/PP2BPPP/R1BQK1NR b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/2n2n2/2p1p3/2P2P2/2NP4/PP2B1PP/R1BQK1NR b KQkq f3 0 1"
//                "r1bqkb1r/pp3ppp/2n2n2/2p5/2P2B2/2NP4/PP2B1PP/R2QK1NR b KQkq - 0 1"
//                "r1bqkb1r/pp3ppp/5n2/2p5/2Pn1B2/2NP2P1/PP2B2P/R2QK1NR b KQkq - 0 1"
//                "r1bqk2r/pp3ppp/3b1n2/2p5/2Pn1B2/2NP1NP1/PP2B2P/R2QK2R b KQkq - 0 1"
//                "r1bqk2r/pp3ppp/5n2/2p5/2Pn1P2/2NP1N2/PP2B2P/R2QK2R b KQkq - 0 1"
//                "r1bq1rk1/pp3ppp/5n2/2p5/N1Pn1P2/3P1N2/PP2B2P/R2QK2R b KQ - 0 1"
//                "r1bqr1k1/pp3ppp/5n2/2p5/N1PN1P2/3P4/PP2B2P/R2QK2R b KQ - 0 1"
//                "r1b1r1k1/pp3ppp/5n2/2p5/N1Pq1P2/3P4/PP2B2P/R2QKR2 b Q - 0 1"
//                "r3r1k1/pp3ppp/5n2/2p5/N1Pq1Pb1/3P4/PP2B2P/1R1QKR2 b - - 0 1"
//                "r3r1k1/pp3ppp/5n2/2p5/N1Pq1P2/1Q1P4/PP2b2P/1R2KR2 b - - 0 1"
//                "r3r1k1/pp3ppp/5n2/2p5/N1Pq1P2/1Q1b4/PP1K3P/1R3R2 b - - 0 1"
//                "r3r1k1/pp3ppp/5n2/2p5/N1bq1P2/1Q6/PPK4P/1R3R2 b - - 0 1"

                // Mable game 4 (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/6P1/8/PPPPPP1P/RNBQKBNR b KQkq g3 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/6P1/8/PPPPPP1P/RNBQKBNR w KQkq e6 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/4P1P1/8/PPPP1P1P/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/ppp2ppp/8/3pp3/4P1P1/8/PPPP1P1P/RNBQKBNR w KQkq d6 0 1"
//                "rnbqkbnr/ppp2ppp/8/1B1pp3/4P1P1/8/PPPP1P1P/RNBQK1NR b KQkq - 0 1"
//                "r1bqkbnr/ppp2ppp/2n5/1B1pp3/4P1P1/8/PPPP1P1P/RNBQK1NR w KQkq - 0 1"
//                "r1bqkbnr/ppp2ppp/2n5/1B1pp3/4P1P1/5N2/PPPP1P1P/RNBQK2R b KQkq - 0 1"
//                "r2qkbnr/ppp2ppp/2n5/1B1pp3/4P1b1/5N2/PPPP1P1P/RNBQK2R w KQkq - 0 1"
//                "r2qkbnr/ppp2ppp/2n5/1B1pp3/4P1b1/2N2N2/PPPP1P1P/R1BQK2R b KQkq - 0 1"
//                "r2qkbnr/ppp2ppp/2n5/1B2p3/4p1b1/2N2N2/PPPP1P1P/R1BQK2R w KQkq - 0 1"
//                "r2qkbnr/ppp2ppp/2n5/1B2p3/4N1b1/5N2/PPPP1P1P/R1BQK2R b KQkq - 0 1"
//                "r3kbnr/ppp2ppp/2n5/1B1qp3/4N1b1/5N2/PPPP1P1P/R1BQK2R w KQkq - 0 1"
//                "r3kbnr/ppp2ppp/2n5/1B1qp3/4N1b1/5N2/PPPPQP1P/R1B1K2R b KQkq - 0 1"
//                "r3kbnr/ppp2ppp/2n5/1B1qp1N1/6b1/5N2/PPPPQP1P/R1B1K2R b KQkq - 0 1"
//                "2kr1bnr/ppp2ppp/2n5/1B1qp1N1/6b1/5N2/PPPPQP1P/R1B1K2R w KQ - 0 1"
//                "2kr1bnr/ppp2ppp/2n5/1B1qp1N1/2P3b1/5N2/PP1PQP1P/R1B1K2R b KQ c3 0 1" // g4f3??
//                "2kr1bnr/ppp2ppp/2n5/1B1qp1N1/2P5/5b2/PP1PQP1P/R1B1K2R w KQ - 0 1"
//                "2kr1bnr/ppp2ppp/2n5/1B1qp3/2P5/5N2/PP1PQP1P/R1B1K2R b KQ - 0 1"
//                "2kr1bnr/ppp2ppp/2n1q3/1B2p3/2P5/5N2/PP1PQP1P/R1B1K2R w KQ - 0 1"
//                "2kr1bnr/ppp2ppp/2n1q3/1B2p3/2P5/3P1N2/PP2QP1P/R1B1K2R b KQ - 0 1"
//                "2kr2nr/ppp2ppp/2n1q3/1B2p3/1bP5/3P1N2/PP2QP1P/R1B1K2R w KQ - 0 1"
//                "2kr2nr/ppp2ppp/2n1q3/1B2p3/1bP5/3P1N2/PP1BQP1P/R3K2R b KQ - 0 1"
//                "2kr2nr/ppp2ppp/2n1q3/1B2p3/2P5/3P1N2/PP1bQP1P/R3K2R w KQ - 0 1"
//                "2kr2nr/ppp2ppp/2n1q3/1B2p3/2P5/3P1N2/PP1KQP1P/R6R b - - 0 1"
//                "2kr3r/ppp1nppp/2n1q3/1B2p3/2P5/3P1N2/PP1KQP1P/R6R w - - 0 1"
//                "2kr3r/ppp1nppp/2n1q3/1B2p3/2P5/3P1N2/PP1KQP1P/6RR b - - 0 1"
//                "2kr2r1/ppp1nppp/2n1q3/1B2p3/2P5/3P1N2/PP1KQP1P/6RR w - - 0 1"
//                "2kr2r1/ppp1nppp/2n1q3/1B2p1R1/2P5/3P1N2/PP1KQP1P/7R b - - 0 1"
//                "2kr2r1/ppp1nppp/2n1q3/1B4R1/2P1p3/3P1N2/PP1KQP1P/7R w - - 0 1"


                // endgame test
//                "8/8/8/k7/8/8/3R4/7K w "
//                "8/8/4k3/8/8/4K3/6P1/8 w - - 0 1" // bm e4, f4

                // my
//                "r2kq2r/pb2ppbp/1p4p1/2ppP3/1n3P2/2NPQN2/PPP3PP/1KR2B1R w kq"
//                "r1r2k2/4p1bp/1p3pp1/1p1pP3/1P1PbP2/1P3NP1/NK5P/2R2R2 w"

                // mate in one 1
//                "rb6/k1p4R/P1P5/PpK5/8/8/8/5B2 w - b" // bm axb6 (ep)

                // mate in two 5
//                "N5Q1/nkPP4/8/8/4K3/8/8/8 w"
//                "5B2/1K6/8/pkp5/5Q2/8/2P5/8 w" // Q from f4 to e3
//                "3k4/R6R/3n4/8/8/3K4/8/8 w"

                // mate in three (7)
//                "K7/8/2k1P3/8/3Q4/8/8/8 w"
//                "4rQK1/6P1/8/8/8/8/5R2/6k1 w"

                // mate in four (7)
//                "kq4n1/4p2Q/1P2P4/1K6/8/8/p7/8 w" //

                // mate in five (11)
                // http://www.chess-poster.com/chess_problems/mate_in_5.htm
//                "n1rb4/1p3p1p/1p6/1R5K/8/p3p1PN/1PP1R3/N6k w"

                // easy
//                "1rbq1rk1/p1b1nppp/1p2p3/8/1B1pN3/P2B4/1P3PPP/2RQ1R1K w" // bm Nf6+ (325,000)
//                "7k/5K2/5P1p/3p4/6P1/3p4/8/8 w" // bm g5 (+100,000 -2250000)
//                "8/6B1/p5p1/Pp4kp/1P5r/5P1Q/4q1PK/8 w" // bm Qxh4 (1,825,000)

                // mid
//                "1q1k4/2Rr4/8/2Q3K1/8/8/8/8 w" // bm Kh6 (+6,275,000 -7,725,000)

                // hard
//                "8/8/p1p5/1p5p/1P5p/8/PPP2K1p/4R1rk w" // bm Rf1


//                "8/8/1p1r1k2/p1pPN1p1/P3KnP1/1P6/8/3R4 b" // bm Nxd5


//                "3r2k1/p2r1p1p/1p2p1p1/q4n2/3P4/PQ5P/1P1RNPP1/3R2K1 b" // bm Nxd4

//                "1k1r4/pp1b1R2/3q2pp/4p3/2B5/4Q3/PPP2B2/2K5 b - -" // bm Qd1+
//                "3r1k2/4npp1/1ppr3p/p6P/P2PPPP1/1NR5/5K2/2R5 w - -" // bm d5
//                "2q1rr1k/3bbnnp/p2p1pp1/2pPp3/PpP1P1P1/1P2BNNP/2BQ1PRK/7R b - -" // bm f5
//                "rnbqkb1r/p3pppp/1p6/2ppP3/3N4/2P5/PPP1QPPP/R1B1KB1R w KQkq" // bm e6
//                "r1b2rk1/2q1b1pp/p2ppn2/1p6/3QP3/1BN1B3/PPP3PP/R4RK1 w - -" // bm Nd5 a4
//                "2r3k1/pppR1pp1/4p3/4P1P1/5P2/1P4K1/P1P5/8 w - -" // bm g6
//                "1nk1r1r1/pp2n1pp/4p3/q2pPp1N/b1pP1P2/B1P2R2/2P1B1PP/R2Q2K1 w - -" // bm Nf6
        );

//        int move = player.move(state, time, time, 0);
//        Move.apply(move, state);
//        player.move(state, time, time, 0);

        System.out.println(state);
        System.out.println(Move.toString(
                player.move(state, time, time, 0)
        ));

        System.exit(0);
    }
}
