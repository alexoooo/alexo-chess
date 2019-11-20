package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.player.neuro.PuctPlayer;
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
 *
 */
public class BrainTeaser {
    //--------------------------------------------------------------------
    public static void main(String[] args) {
//        int time = 7 * 24 * 60 * 60 * 1000;
//        int time = 60 * 1000;
        int time = 10 * 60 * 1000;
//        int time = 60 * 60 * 1000;
//        int time = 150 * 60 * 1000;

//        Player player = new MultiMctsPlayer(List.of(
//                mctsCapturePrototype.prototype(),
////                mctsCapturePrototype.prototype(),
////                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype()
//        ));

//        Player player = MctsPrototypes.mctsFallbackDeep2LargeOpt8Prototype.prototype();
//        Player player = MctsPrototypes.mctsFallbackDeep5Opt8Prototype.prototype();
//        Player player = MctsPrototypes.mctsFallbackDeep5Opt32Prototype.prototype();
//        Player player = MctsPrototypes.mctsFallbackDeep5Rand8Prototype.prototype();
//        Player player = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();
//        Player player = MctsPrototypes.mctsFallbackDeep1LargeOpt192Prototype.prototype();

//        Player player = MctsPrototypes.mctsUcb5DeepPrototype.prototype();
//        Player player = MctsPrototypes.mctsUcb5Deep2x2Prototype.prototype();
//        Player player = MctsPrototypes.mctsUcb1Deep1x1Prototype.prototype();
//        Player player = MctsPrototypes.mctsUcb1Deep2x2Prototype.prototype();

//        BanditPlayer protoA = new ParallelMctsPlayer(
//                "par",
//                3,
//                0.5,
//                7,
//                true
//        );
//        BanditPlayer protoB = new ParallelMctsPlayer(
//                "par",
//                3,
//                0.4,
//                15,
//                true
//        );
//        BanditPlayer protoC = new ParallelMctsPlayer(
//                "par",
//                3,
//                0.4,
//                15,
//                false
//        );
//        Player player = new MultiMctsPlayer(List.of(
//                protoC.prototype(),
//                protoC.prototype(),
//                protoC.prototype()
//        ));

//        Player player = new ParallelMctsPlayer(
//                "par",
//                9,
//                0.3,
//                3,
////                1,
//                false
//        );

//        Player player = new PuctPlayer(
//                Paths.get("lookup/gen/2/nn.zip"),
//                1,
//                1.5);
        Player player = new PuctPlayer(
//                Paths.get("lookup/gen/8/nn.zip"),
//                Paths.get("lookup/history/carlsen-nn.zip"),
//                Paths.get("lookup/history/mix/champions_2019-11-12.zip"),
                Paths.get("lookup/history/mix/all_mid_batch_20191120-travis.zip"),
//                Paths.get("lookup/history/mix/all_deep_20191119.zip"),
//                1,
                2,
                1.5,
                true,
                7,
                true,
                1.5,
                0);

        State state = State.fromFen(
                // puzzles
//                "6rk/6pp/3N4/8/8/8/7P/7K w - - 0 1" // N from d6 to f7
//                "5Qk1/5p2/1p5p/p4Np1/5q2/7P/PPr5/3R3K b - - 2 1"
//                "5k2/5p2/1p5p/p4Np1/5q2/7P/PPr5/3R3K w - - 0 2"

//                "6k1/5p2/1p5p/p4Np1/5q2/Q6P/PPr5/3R3K w - - 1 0" // Q form a3 to f8
//                "8/2k2p2/2b3p1/P1p1Np2/1p3b2/1P1K4/5r2/R3R3 b - - 0 1" // b from c6 to b5
//                "r1b2k1r/ppppq3/5N1p/4P2Q/4PP2/1B6/PP5P/n2K2R1 w - - 1 0" // Q from h5 to h6 (!!)

//                "R6R/1r3pp1/4p1kp/3pP3/1r2qPP1/7P/1P1Q3K/8 w - - 1 0" // P from f4 to f5
//                "4r1k1/5bpp/2p5/3pr3/8/1B3pPq/PPR2P2/2R2QK1 b - - 0 1" // r from e5 to e1
//                "7R/r1p1q1pp/3k4/1p1n1Q2/3N4/8/1PP2PPP/2B3K1 w - - 1 0" // R from h8 to d8 (!!)

                // trivial
//                "4R3/4R3/8/2k5/8/2p2K2/2P5/8 w"
//                "8/8/2p1b1k1/r6n/1K6/8/8/8 b - - 0 1"
//                "8/2p5/n7/8/8/2p5/4K3/6k1 b"
//                "2k5/8/8/3K4/1B4Nb/1P5N/3R4/8 w"
//                "8/P7/1bk4p/8/3BP3/RR6/3K4/8 w"
//                "8/8/8/8/6R1/5B2/5K1k/8 w  - 98"
//                "4kr2/pp3p1p/8/8/1KnRP2B/8/P1q1N3/8 b"


                // Travis game (black)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq c3 0 1"
                "rnbqkbnr/pppp1ppp/8/4p3/2P5/8/PP1PPPPP/RNBQKBNR w KQkq e6 0 1"
//                "rnbqkbnr/ppp1pppp/8/3P4/8/8/PP1PPPPP/RNBQKBNR b KQkq - 0 1"

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

                // ao game (black)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/ppp1pppp/8/3p4/4P3/8/PPPP1PPP/RNBQKBNR w KQkq d6 0 1"
//                "rnbqkbnr/ppp1pppp/8/3P4/8/8/PPPP1PPP/RNBQKBNR b KQkq - 0 1"
//                "rnb1kbnr/ppp1pppp/8/3q4/8/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1"
//                "rnb1kbnr/ppp1pppp/8/3q4/2P5/8/PP1P1PPP/RNBQKBNR b KQkq c3 0 1"
//                "rnb1kbnr/ppp1pppp/8/4q3/2P5/8/PP1P1PPP/RNBQKBNR w KQkq - 0 1"
//                "rnb1kbnr/ppp1pppp/8/4q3/2P5/8/PP1PNPPP/RNBQKB1R b KQkq - 0 1"
//                "rnb1kbnr/pp2pppp/8/2p1q3/2P5/8/PP1PNPPP/RNBQKB1R w KQkq c6 0 1"
//                "rnb1kbnr/pp2pppp/8/2p1q3/2P5/2N5/PP1PNPPP/R1BQKB1R b KQkq - 0 1"
//                "r1b1kbnr/pp2pppp/2n5/2p1q3/2P5/2N5/PP1PNPPP/R1BQKB1R w KQkq - 0 1"
//                "r1b1kbnr/pp2pppp/2n5/2p1q3/2PP4/2N5/PP2NPPP/R1BQKB1R b KQkq d3 0 1"
//                "r1b1kbnr/pp2pppp/8/2p1q3/2Pn4/2N5/PP2NPPP/R1BQKB1R w KQkq - 0 1"
//                "r1b1kbnr/pp2pppp/8/2pNq3/2Pn4/8/PP2NPPP/R1BQKB1R b KQkq - 0 1"
//                "r3kbnr/pp2pppp/8/2pNq3/2Pn2b1/8/PP2NPPP/R1BQKB1R w KQkq - 0 1"
//                "r3kbnr/pp2pppp/8/2pNq3/2Pn1Bb1/8/PP2NPPP/R2QKB1R b KQkq - 0 1"
//                "r3kbnr/pp2pppp/8/2pNq3/2Pn1B2/8/PP2bPPP/R2QKB1R w KQkq - 0 1"
//                "r3kbnr/pp2pppp/8/2pNB3/2Pn4/8/PP2bPPP/R2QKB1R b KQkq - 0 1"
//                "r3kbnr/pp2pppp/8/2pNB3/2Pn4/8/PP3PPP/R2bKB1R w KQkq - 0 1"
//                "r3kbnr/ppN1pppp/8/2p1B3/2Pn4/8/PP3PPP/R2bKB1R b KQkq - 0 1"
//                "r4bnr/ppNkpppp/8/2p1B3/2Pn4/8/PP3PPP/R2bKB1R w KQ - 0 1"
//                "r4bnr/ppNkpppp/8/2p1B3/2Pn4/8/PP3PPP/3RKB1R b K - 0 1"
//                "3r1bnr/ppNkpppp/8/2p1B3/2Pn4/8/PP3PPP/3RKB1R w K - 0 1"
//                "3r1bnr/pp1kpppp/8/1Np1B3/2Pn4/8/PP3PPP/3RKB1R b K - 0 1"
//                "3r1bnr/pp2pppp/4k3/1Np1B3/2Pn4/8/PP3PPP/3RKB1R w K - 0 1"
//                "3r1bnr/pp2pppp/4k3/2p1B3/2PN4/8/PP3PPP/3RKB1R b K - 0 1"
//                "3r1bnr/pp2pppp/8/2p1k3/2PN4/8/PP3PPP/3RKB1R w K - 0 1"
//                "3r1bnr/pp2pppp/8/2p1k3/2P5/5N2/PP3PPP/3RKB1R b K - 0 1"
//                "3r1bnr/pp2pppp/8/2p5/2P1k3/5N2/PP3PPP/3RKB1R w K - 0 1"
//                "3R1bnr/pp2pppp/8/2p5/2P1k3/5N2/PP3PPP/4KB1R b K - 0 1"
//                "3R1b1r/pp2pppp/5n2/2p5/2P1k3/5N2/PP3PPP/4KB1R w K - 0 1"
//                "3R1b1r/pp2pppp/5n2/2p5/2P1k3/8/PP1N1PPP/4KB1R b K - 0 1"
//                "3R1b1r/pp2pppp/5n2/2p5/2P2k2/8/PP1N1PPP/4KB1R w K - 0 1"
//                "3R1b1r/pp2pppp/5n2/2p5/2P2k2/6P1/PP1N1P1P/4KB1R b K - 0 1"
//                "3R1b1r/pp2pppp/5n2/2p3k1/2P5/6P1/PP1N1P1P/4KB1R w K - 0 1"
//                "1R3b1r/pp2pppp/5n2/2p3k1/2P5/6P1/PP1N1P1P/4KB1R b K - 0 1"
//                "1R3b1r/p3pppp/1p3n2/2p3k1/2P5/6P1/PP1N1P1P/4KB1R w K - 0 1"
//                "11R3b1r/p3pppp/1p3n2/2p3k1/2P5/6P1/PP1N1PBP/4K2R b K - 0 1"
//                "1R3b1r/p2npppp/1p6/2p3k1/2P5/6P1/PP1N1PBP/4K2R w K - 0 1"
//                "5b1r/pR1npppp/1p6/2p3k1/2P5/6P1/PP1N1PBP/4K2R b K - 0 1"
//                "5b1r/pR2pppp/1p6/2p1n1k1/2P5/6P1/PP1N1PBP/4K2R w K - 0 1"
//                "5b1r/R3pppp/1p6/2p1n1k1/2P5/6P1/PP1N1PBP/4K2R b K - 0 1"
//                "5b1r/R4ppp/1p2p3/2p1n1k1/2P5/6P1/PP1N1PBP/4K2R w K - 0 1"
//                "5b1r/R4ppp/1p2p3/2p1n1k1/2P5/6P1/PP1N1PBP/5RK1 b - - 0 1"
//                "7r/R4ppp/1p1bp3/2p1n1k1/2P5/6P1/PP1N1PBP/5RK1 w - - 0 1"
//                "7r/R4ppp/1p1bp3/2p1n1k1/2P1N3/6P1/PP3PBP/5RK1 b - - 0 1"
//                "7r/R4ppp/1p1bpk2/2p1n3/2P1N3/6P1/PP3PBP/5RK1 w - - 0 1"
//                "7r/R4ppp/1p1bp3/2p1n3/2P1N1k1/6P1/PP3PBP/5RK1 w - - 0 1"
//                "7r/R4ppp/1p1Np3/2p1n3/2P3k1/6P1/PP3PBP/5RK1 b - - 0 1"
//                "3r4/R4ppp/1p1Np3/2p1n3/2P3k1/6P1/PP3PBP/5RK1 w - - 0 1"
//                "3r4/R4Npp/1p2p3/2p1n3/2P3k1/6P1/PP3PBP/5RK1 b - - 0 1"
//                "3r4/R4npp/1p2p3/2p5/2P3k1/6P1/PP3PBP/5RK1 w - - 0 1"
//                "3r4/5Rpp/1p2p3/2p5/2P3k1/6P1/PP3PBP/5RK1 b - - 0 1"
//                "8/5Rpp/1p2p3/2p5/2Pr2k1/6P1/PP3PBP/5RK1 w - - 0 1"
//                "8/6Rp/1p2p3/2p5/2Pr2k1/6P1/PP3PBP/5RK1 b - - 0 1"
//                "8/6Rp/1p2p3/2p2k2/2Pr4/6P1/PP3PBP/5RK1 w - - 0 1"
//                "8/7R/1p2p3/2p2k2/2Pr4/6P1/PP3PBP/5RK1 b - - 0 1"
//                "8/7R/1p2p3/2p2k2/2r5/6P1/PP3PBP/5RK1 w - - 0 1"
//                "7R/8/1p2p3/2p2k2/2r5/6P1/PP3PBP/5RK1 b - - 0 1"
//                "7R/8/1p2p3/2p2k2/1r6/6P1/PP3PBP/5RK1 w - - 0 1"
//                "7R/8/1p2p3/2p2k2/1r6/1P4P1/P4PBP/5RK1 b - - 0 1"
//                "7R/8/1p2p3/2p2k2/3r4/1P4P1/P4PBP/5RK1 w - - 0 1"

//                "7R/8/1p2p3/2p2k2/3r4/1P4P1/P4PBP/4R1K1 b - - 0 1" // alt
//                "7R/8/1p6/2p1pk2/3r4/1P4P1/P4PBP/4R1K1 w - - 0 1"
//                "8/8/1p5R/2p1pk2/3r4/1P4P1/P4PBP/4R1K1 b - - 0 1"
//                "8/8/7R/1pp1pk2/3r4/1P4P1/P4PBP/4R1K1 w - - 0 1"
//                "8/8/2R5/1pp1pk2/3r4/1P4P1/P4PBP/4R1K1 b - - 0 1"
//                "8/8/2R5/1p2pk2/2pr4/1P4P1/P4PBP/4R1K1 w - - 0 1"
//                "8/8/2R5/1p2pk2/1Ppr4/6P1/P4PBP/4R1K1 b - - 0 1"
//                "8/8/2R5/1p2pk2/1Pp5/6P1/P2r1PBP/4R1K1 w - - 0 1"
//                "8/8/2R5/1p2pk2/1Pp5/P5P1/3r1PBP/4R1K1 b - - 0 1"
//                "8/8/2R5/1p2pk2/1Pp5/P5P1/r4PBP/4R1K1 w - - 0 1"
//                "8/8/8/1pR1pk2/1Pp5/P5P1/r4PBP/4R1K1 b - - 0 1"
//                "8/8/8/1pR1pk2/1Pp5/r5P1/5PBP/4R1K1 w - - 0 1"
//                "8/8/8/1pR1Rk2/1Pp5/r5P1/5PBP/6K1 b - - 0 1"
//                "8/8/5k2/1pR1R3/1Pp5/r5P1/5PBP/6K1 w - - 0 1"
//                "8/8/5k2/1pRR4/1Pp5/r5P1/5PBP/6K1 b - - 0 1"
//                "8/8/5k2/1pRR4/1P6/r1p3P1/5PBP/6K1 w - - 0 1"
//                "8/8/5k2/1R1R4/1P6/r1p3P1/5PBP/6K1 b - - 0 1"
//                "8/8/5k2/1R1R4/1P6/r5P1/2p2PBP/6K1 w - - 0 1"
//                "8/8/5k2/1RR5/1P6/r5P1/2p2PBP/6K1 b - - 0 1"
//                "8/8/5k2/1RR5/1P6/6P1/r1p2PBP/6K1 w - - 0 1"
//                "8/8/2R2k2/1R6/1P6/6P1/r1p2PBP/6K1 b - - 0 1"
//                "8/4k3/2R5/1R6/1P6/6P1/r1p2PBP/6K1 w - - 0 1"
//                "8/4k3/8/1RR5/1P6/6P1/r1p2PBP/6K1 b - - 0 1"
//                "8/8/3k4/1RR5/1P6/6P1/r1p2PBP/6K1 w - - 0 1"
//                "8/8/3k4/1R1R4/1P6/6P1/r1p2PBP/6K1 b - - 0 1"
//                "8/2k5/8/1R1R4/1P6/6P1/r1p2PBP/6K1 w - - 0 1"
//                "8/2k5/8/1RR5/1P6/6P1/r1p2PBP/6K1 b - - 0 1"
//                "8/8/3k4/1RR5/1P6/6P1/r1p2PBP/6K1 w - - 0 1"
//                "8/8/3k4/1R6/1P6/6P1/r1R2PBP/6K1 b - - 0 1" // !!
//                "8/8/3k4/1R6/1P6/6P1/2r2PBP/6K1 w - - 0 1"
//                "8/8/3k4/1R6/1P6/6PB/2r2P1P/6K1 b - - 0 1"
//                "8/8/2k5/1R6/1P6/6PB/2r2P1P/6K1 w - - 0 1"
//                "8/8/2k5/6R1/1P6/6PB/2r2P1P/6K1 b - - 0 1"
//                "8/8/2k5/6R1/1P6/6PB/1r3P1P/6K1 w - - 0 1"
//                "8/8/2k5/8/1P4R1/6PB/1r3P1P/6K1 b - - 0 1"
//                "8/8/8/1k6/1P4R1/6PB/1r3P1P/6K1 w - - 0 1"
//                "8/8/8/1k4R1/1P6/6PB/1r3P1P/6K1 b - - 0 1" // ???
//                "8/8/8/6R1/1k6/6PB/1r3P1P/6K1 w - - 0 1"
//                "8/8/8/5BR1/1k6/6P1/1r3P1P/6K1 b - - 0 1"
//                "8/8/8/5BR1/8/2k3P1/1r3P1P/6K1 w - - 0 1"
//                "8/8/8/6R1/4B3/2k3P1/1r3P1P/6K1 b - - 0 1"
//                "8/8/8/6R1/4B3/6P1/1r1k1P1P/6K1 w - - 0 1"
//                "8/8/8/6R1/8/6P1/1r1k1PBP/6K1 b - - 0 1"
//                "8/8/8/6R1/8/6P1/1r3PBP/4k1K1 w - - 0 1"
//                "8/8/8/8/6R1/6P1/1r3PBP/4k1K1 b - - 0 1"
//                "8/8/8/8/6R1/6P1/5rBP/4k1K1 w - - 0 1"
//                "8/8/8/8/6RP/6P1/5rB1/4k1K1 b - h3 0 1"
//                "8/8/8/8/6RP/6P1/3r2B1/4k1K1 w - - 0 1"
//                "8/8/8/8/6RP/6PB/3r4/4k1K1 b - - 0 1"
//                "8/8/8/8/6RP/6PB/3rk3/6K1 w - - 0 1"
//                "8/8/8/6R1/7P/6PB/3rk3/6K1 b - - 0 1"
//                "8/8/8/6R1/7P/5kPB/3r4/6K1 w - - 0 1"
//                "8/8/8/6RP/8/5kPB/3r4/6K1 b - - 0 1"
//                "8/8/8/6RP/8/5kPB/8/3r2K1 w - - 0 1"
//                "8/8/8/6RP/8/5kPB/7K/3r4 b - - 0 1"
//                "8/8/8/6RP/8/6PB/5k1K/3r4 w - - 0 1"
//                "8/8/8/6RP/6B1/6P1/5k1K/3r4 b - - 0 1"
//                "8/8/8/6RP/6B1/6P1/5k1K/6r1 w - - 0 1"
//                "8/8/8/5R1P/6B1/6P1/5k1K/6r1 b - - 0 1"
//                "8/8/8/5R1P/6B1/4k1P1/7K/6r1 w - - 0 1"
//                "8/8/8/5R1P/4k1B1/6P1/8/6K1 w - - 0 1"

//                "7R/8/1p2p3/2p2k2/3r4/1P4P1/P4PBP/3R2K1 b - - 0 1" // (???)
//                "7R/8/1p2p3/2p2k2/8/1P4P1/P4PBP/3r2K1 w - - 0 1"
//                "7R/8/1p2p3/2p1k3/8/1P4P1/P4P1P/3r1BK1 w - - 0 1"
//                "1R6/8/1p2p3/2p1k3/8/1P4P1/P4P1P/3r1BK1 b - - 0 1"
//                "1R6/8/1p1rp3/2p1k3/8/1P4P1/P4P1P/5BK1 w - - 0 1"
//                "1R6/8/1p1rp3/2p1k3/8/1P4P1/P4PBP/6K1 b - - 0 1"
//                "1R6/8/1p1rp3/2p5/3k4/1P4P1/P4PBP/6K1 w - - 0 1"
//                "8/1R6/1p1rp3/2p5/3k4/1P4P1/P4PBP/6K1 b - - 0 1"
//                "8/1R6/1p1rp3/2p5/8/1Pk3P1/P4PBP/6K1 w - - 0 1"
//                "8/1R6/1p1rp3/2p5/8/1Pk3PB/P4P1P/6K1 b - - 0 1"
//                "8/1R6/1p1rp3/2p5/8/1P4PB/Pk3P1P/6K1 w - - 0 1"
//                "8/1R6/1p1rp3/2p5/8/1P4P1/Pk3PBP/6K1 b - - 0 1"
//                "8/1R6/1p1rp3/2p5/8/1P4P1/k4PBP/6K1 w - - 0 1"
//                "8/1R6/1p1rp3/2p5/4B3/1P4P1/k4P1P/6K1 b - - 0 1"
//                "8/1R6/1p1rp3/2p5/4B3/1k4P1/5P1P/6K1 w - - 0 1"
//                "8/1R6/1p1rp3/2p5/8/1k3BP1/5P1P/6K1 b - - 0 1"
//                "8/1R6/1p1rp3/2p5/2k5/5BP1/5P1P/6K1 w - - 0 1"
//                "8/1R6/1p1rp3/2p5/2k5/6P1/5PBP/6K1 b - - 0 1"
//                "8/1R6/3rp3/1pp5/2k5/6P1/5PBP/6K1 w - - 0 1"
//                "8/1R6/3rp3/1pp5/2k5/6P1/5P1P/5BK1 b - - 0 1"
//                "8/1R6/3rp3/1pp5/3k4/6P1/5P1P/5BK1 w - - 0 1"
//                "8/1R6/3rp3/1Bp5/3k4/6P1/5P1P/6K1 b - - 0 1"
//                "8/1R6/3r4/1Bp1p3/3k4/6P1/5P1P/6K1 w - - 0 1"
//                "8/1R6/3r4/2p1p3/B2k4/6P1/5P1P/6K1 b - - 0 1"
//                "8/1R6/3r4/4p3/B1pk4/6P1/5P1P/6K1 w - - 0 1"
//                "8/1R6/3r4/4p3/B1pk4/6P1/5PKP/8 b - - 0 1"
//                "8/1R6/3r4/4p3/B2k4/2p3P1/5PKP/8 w - - 0 1"
//                "8/1R6/3r4/4p3/3k4/1Bp3P1/5PKP/8 b - - 0 1"
//                "8/1R6/3r4/4p3/8/1Bpk2P1/5PKP/8 w - - 0 1"
//                "1R6/8/3r4/4p3/8/1Bpk2P1/5PKP/8 b - - 0 1"
//                "1R6/8/5r2/4p3/8/1Bpk2P1/5PKP/8 w - - 0 1"
//                "8/1R6/5r2/4p3/8/1Bpk2P1/5PKP/8 b - - 0 1"
//                "8/1R6/5r2/4p3/8/1Bp3P1/4kPKP/8 w - - 0 1"
//                "8/2R5/5r2/4p3/8/1Bp3P1/4kPKP/8 b - - 0 1"
//                "8/2R5/8/4p3/8/1Bp3P1/4krKP/8 w - - 0 1"
//                "8/2R5/8/4p3/8/1Bp3P1/4kr1P/6K1 b - - 0 1"
//                "8/2R5/8/4p3/8/1Bp3P1/4k2P/5rK1 w - - 0 1"
//                "8/2R5/8/4p3/8/1Bp3P1/4k1KP/5r2 b - - 0 1"
//                "8/2R5/8/4p3/8/1Bp3P1/4k1KP/2r5 w - - 0 1"
//                "8/2R2B2/8/4p3/8/2p3P1/4k1KP/2r5 b - - 0 1"
//                "8/2R2B2/8/4p3/8/6P1/2p1k1KP/2r5 w - - 0 1"
//                "8/5B2/8/2R1p3/8/6P1/2pk2KP/2r5 w - - 0 1"
//                "8/5B2/8/3Rp3/8/6P1/2pk2KP/2r5 b - - 0 1"
//                "8/5B2/8/3Rp3/8/2k3P1/2p3KP/2r5 w - - 0 1"
//                "8/5B2/8/2R1p3/8/2k3P1/2p3KP/2r5 b - - 0 1"
//                "8/5B2/8/2R1p3/3k4/6P1/2p3KP/2r5 w - - 0 1"
//                "8/2R2B2/8/4p3/3k4/6P1/2p3KP/2r5 b - - 0 1"
//                "8/2R2B2/8/8/3kp3/6P1/2p3KP/2r5 w - - 0 1"
//                "8/2R2B2/8/8/3k2P1/4p3/2p3KP/2r5 w - - 0 1"
//                "8/3R1B2/8/8/3k2P1/4p3/2p3KP/2r5 b - - 0 1"
//                "8/3R1B2/8/8/4k1P1/4p3/2p3KP/2r5 w - - 0 1"
//                "8/3R4/6B1/8/4k1P1/4p3/2p3KP/2r5 b - - 0 1"
//                "8/3R4/6B1/8/5kP1/4p3/2p3KP/2r5 w - - 0 1"
//                "8/5R2/6B1/8/5kP1/4p3/2p3KP/2r5 b - - 0 1"
//                "8/5R2/6B1/8/6k1/4p3/2p3KP/2r5 w - - 0 1"
//                "8/5R2/8/5B2/6k1/4p3/2p3KP/2r5 b - - 0 1"
//                "8/5R2/8/5Bk1/8/4p3/2p3KP/2r5 w - - 0 1"
//                "8/5R2/8/5Bk1/7P/4p3/2p3K1/2r5 b - h3 0 1"
//                "8/5R2/8/5B2/7k/4p3/2p3K1/2r5 w - - 0 1"
//                "8/5R2/8/8/7k/3Bp3/2p3K1/2r5 b - - 0 1"
//                "8/5R2/8/8/7k/3Bp3/2p3K1/6r1 w - - 0 1"
//                "8/5R2/8/8/7k/3Bp3/2p5/6K1 b - - 0 1"
//                "8/5R2/8/8/7k/3Bp3/8/2q3K1 w - - 0 1"
//                "8/5R2/8/8/7k/3Bp3/7K/2q5 b - - 0 1"
//                "8/5R2/8/8/7k/3Bp3/3q3K/8 w - - 0 1"
//                "8/5R2/8/8/7k/3qp3/8/6K1 w - - 0 1"
//                "8/5R2/8/8/7k/3qp3/6K1/8 b - - 0 1"
//                "8/5R2/8/8/7k/3q4/4p1K1/8 w - - 0 1"
//                "8/4R3/8/8/7k/6q1/4p1K1/8 w - - 0 1"
//                "8/4R3/8/8/7k/6q1/8/4q2K w - - 0 1"
//                "8/8/8/8/7k/6q1/8/4R2K b - - 0 1"
//                "8/8/8/8/7k/8/8/4q2K w - - 0 1"
//                "8/8/8/8/7k/8/6K1/4q3 b - - 0 1"
//                "8/8/8/8/7k/4q3/6K1/8 w - - 0 1"
//                "8/8/8/8/7k/4q3/8/5K2 b - - 0 1"
//                "8/8/8/8/7k/8/3q4/5K2 w - - 0 1"
//                "8/8/8/8/8/6k1/3q4/6K1 w - - 0 1"
//                "8/8/8/8/8/6k1/5q2/5K2 w - - 0 1"


                // Travis game
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/5P2/PPPP2PP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp1p1ppp/4p3/2p5/4P3/5P2/PPPP2PP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pp1p1ppp/4p3/2p5/4P3/2P2P2/PP1P2PP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp3ppp/4p3/2pp4/4P3/2P2P2/PP1P2PP/RNBQKBNR w KQkq d6 0 1"
//                "rnbqkbnr/pp3ppp/4p3/2pP4/8/2P2P2/PP1P2PP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp3ppp/8/2pp4/8/2P2P2/PP1P2PP/RNBQKBNR w KQkq - 0 1"
//                "rnbqk1nr/pp3ppp/3b4/2pp4/8/1PP2P2/P2P2PP/RNBQKBNR w KQkq - 0 1"
//                "rnbqk1nr/pp3ppp/3b4/2pp4/8/1PPP1P2/P5PP/RNBQKBNR b KQkq - 0 1"
//                "rnb1k1nr/pp3ppp/3b4/2pp4/7q/1PPP1P2/P5PP/RNBQKBNR w KQkq - 0 1"
//                "rnb1k1nr/pp3ppp/3b4/2pp4/7q/1PPP1PP1/P6P/RNBQKBNR b KQkq - 0 1"
//                "rnb1k1nr/pp3ppp/8/2pp4/7q/1PPP1Pb1/P6P/RNBQKBNR w KQkq - 0 1"
//                "rnb1k1nr/pp3ppp/8/2pp4/7q/1PPP1Pb1/P2K3P/RNBQ1BNR b kq - 0 1"
//                "rnb1k2r/pp2nppp/8/2pp4/7q/1PPP1Pb1/P2K3P/RNBQ1BNR w kq - 0 1"
//                "rnb1k2r/pp2nppp/8/2pp4/7q/1PPP1Pb1/P2KN2P/RNBQ1B1R b kq - 0 1"
//                "rnb1k2r/pp2nppp/3b4/2pp4/7q/1PPP1P2/P2KN2P/RNBQ1B1R w kq - 0 1"
//                "rnb2rk1/pp2nppp/3b4/2pp4/7q/BPPP1P2/P2KN2P/RN1Q1B1R w - - 0 1"
//                "rnb2rk1/pp2nppp/3b4/2Bp4/7q/1PPP1P2/P2KN2P/RN1Q1B1R b - - 0 1"
//                "rnb2rk1/pp2nppp/8/2bp4/7q/1PPP1P2/P2KN2P/RN1Q1B1R w - - 0 1"
//                "rnb2rk1/pp2nppp/8/2bp4/P6q/1PPP1P2/3KN2P/RN1Q1B1R b - a3 0 1"

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

                // Mable game 2 (black)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/pppppp1p/6p1/8/4P3/8/PPPP1PPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppp1p/6p1/8/4P3/5N2/PPPP1PPP/RNBQKB1R b KQkq - 0 1"
//                "rnbqkbnr/ppppp2p/6p1/5p2/4P3/5N2/PPPP1PPP/RNBQKB1R w KQkq f6 0 1"
//                "rnbqkbnr/ppppp2p/6p1/5P2/8/5N2/PPPP1PPP/RNBQKB1R b KQkq - 0 1"
//                "rnbqkbnr/ppppp2p/8/5p2/8/5N2/PPPP1PPP/RNBQKB1R w KQkq - 0 1"
//                "rnbqkbnr/ppppp2p/8/5p2/8/3B1N2/PPPP1PPP/RNBQK2R b KQkq - 0 1"
//                "rnbqkb1r/ppppp2p/7n/5p2/8/3B1N2/PPPP1PPP/RNBQK2R w KQkq - 0 1"
//                "rnbqkb1r/ppppp2p/7n/5B2/8/5N2/PPPP1PPP/RNBQK2R b KQkq - 0 1" // bad
//                "rnbqkb1r/ppppp2p/7n/4Np2/8/3B4/PPPP1PPP/RNBQK2R b KQkq - 0 1" // good
//                "rnbqk2r/ppppp1bp/7n/4Np2/8/3B4/PPPP1PPP/RNBQK2R w KQkq - 0 1"
//                "rnbqk2r/ppppp1bp/7n/4Np2/5P2/3B4/PPPP2PP/RNBQK2R b KQkq f3 0 1"
//                "rnbqk2r/ppp1p1bp/3p3n/4Np2/5P2/3B4/PPPP2PP/RNBQK2R w KQkq - 0 1"
//                "rnbqk2r/ppp1p1bp/3p3n/5p2/5P2/3B1N2/PPPP2PP/RNBQK2R b KQkq - 0 1"
//                "rn1qk2r/ppp1p1bp/3pb2n/5p2/5P2/3B1N2/PPPP2PP/RNBQK2R w KQkq - 0 1"
//                "rn1qk2r/ppp1p1bp/3pb2n/5p2/5P1P/3B1N2/PPPP2P1/RNBQK2R b KQkq h3 0 1"
//                "rn1qk2r/ppp1p1bp/3pb3/5p2/5PnP/3B1N2/PPPP2P1/RNBQK2R w KQkq - 0 1"
//                "rn1qk2r/ppp1p1bp/3pb3/5p1P/5Pn1/3B1N2/PPPP2P1/RNBQK2R b KQkq - 0 1"
//                "rn1qk2r/ppp1p1bp/4b3/3p1p1P/5Pn1/3B1N2/PPPP2P1/RNBQK2R w KQkq - 0 1"
//                "rn1qk2r/ppp1p1bp/4b3/3p1p1P/5PnN/3B4/PPPP2P1/RNBQK2R b KQkq - 0 1"
//                "rn2k2r/pppqp1bp/4b3/3p1p1P/5PnN/3B4/PPPP2P1/RNBQK2R w KQkq - 0 1"
//                "rn2k2r/pppqp1bp/4b3/3p1p1P/5PnN/2NB4/PPPP2P1/R1BQK2R b KQkq - 0 1"
//                "r3k2r/pppqp1bp/n3b3/3p1p1P/5PnN/2NB4/PPPP2P1/R1BQK2R w KQkq - 0 1"
//                "r3k2r/pppqp1bp/n3b3/3N1p1P/5PnN/3B4/PPPP2P1/R1BQK2R b KQkq - 0 1"
//                "r3k2r/pppqp1bp/n7/3b1p1P/5PnN/3B4/PPPP2P1/R1BQK2R w KQkq - 0 1"
//                "r3k2r/pppqp1bp/n7/3b1B1P/5PnN/8/PPPP2P1/R1BQK2R b KQkq - 0 1"
//                "r3k2r/pppq2bp/n3p3/3b1B1P/5PnN/8/PPPP2P1/R1BQK2R w KQkq - 0 1"
//                "r3k2r/pppq2bp/n3p3/3b3P/5PBN/8/PPPP2P1/R1BQK2R b KQkq - 0 1"
//                "r3k2r/pppq2bp/4p3/2nb3P/5PBN/8/PPPP2P1/R1BQK2R w KQkq - 0 1"
//                "r3k2r/pppq2bp/4p3/2nb3P/P4PBN/8/1PPP2P1/R1BQK2R b KQkq a3 0 1"
//                "r3k2r/pppq2bp/4p3/3b3P/n4PBN/8/1PPP2P1/R1BQK2R w KQkq - 0 1"
//                "r3k2r/pppq2bp/4p3/3b3P/n4PBN/8/1PPP2P1/R1BQK1R1 b Qkq - 0 1"
//                "r3k2r/pppq2bp/4p3/3b3P/5PBN/8/1nPP2P1/R1BQK1R1 w Qkq - 0 1"
//                "r3k2r/pppq2bp/4p3/3b3P/5PBN/8/1BPP2P1/R2QK1R1 b Qkq - 0 1"
//                "r3k2r/pppq3p/4p3/3b3P/5PBN/8/1bPP2P1/R2QK1R1 w Qkq - 0 1"
//                "r3k2r/pppq3p/4p3/3b3P/5PBN/8/1bPP2P1/1R1QK1R1 b kq - 0 1"
//                "r3k2r/ppp4p/4p3/1q1b3P/5PBN/8/1bPP2P1/1R1QK1R1 w kq - 0 1"
//                "r3k2r/ppp4p/4p1N1/1q1b3P/5PB1/8/1bPP2P1/1R1QK1R1 b kq - 0 1" // ??
//                "r3k2r/ppp5/4p1p1/1q1b3P/5PB1/8/1bPP2P1/1R1QK1R1 w kq - 0 1"
//                "r3k2r/ppp5/4p1P1/1q1b4/5PB1/8/1bPP2P1/1R1QK1R1 b kq - 0 1"
//                "r4rk1/ppp5/4p1P1/1q1b4/5PB1/8/1bPP2P1/1R1QK1R1 w - - 0 1"
//                "r4rk1/ppp5/4p1P1/1q1b4/5P2/8/1bPPB1P1/1R1QK1R1 b - - 0 1"
//                "r4rk1/ppp5/1q2p1P1/3b4/5P2/8/1bPPB1P1/1R1QK1R1 w - - 0 1"
//                "r4rk1/ppp5/1q2p1P1/3b4/5P2/8/1bPPB1P1/1R1QKR2 b - - 0 1"
//                "r4rk1/ppp5/1q2p1P1/8/5P2/8/1bPPB1b1/1R1QKR2 w - - 0 1"
//                "r4rk1/ppp5/1q2p1P1/8/5P2/8/1bPPBRb1/1R1QK3 b - - 0 1"
//                "r4rk1/ppp5/1q2p1P1/8/4bP2/8/1bPPBR2/1R1QK3 w - - 0 1"
//                "r4rk1/ppp5/1q2p1P1/8/4bP2/3B4/1bPP1R2/1R1QK3 b - - 0 1"
//                "r5k1/ppp5/1q2p1P1/8/4br2/3B4/1bPP1R2/1R1QK3 w - - 0 1"
//                "r5k1/ppp5/1q2p1P1/8/4bR2/3B4/1bPP4/1R1QK3 b - - 0 1"
//                "r5k1/ppp5/4p1P1/8/4bR2/3B4/1bPP4/1R1QK1q1 w - - 0 1"
//                "r5k1/ppp5/4p1P1/8/4b3/3B4/1bPP4/1R1QKRq1 b - - 0 1"
//                "r5k1/ppp5/4p1q1/8/4b3/3B4/1bPP4/1R1QKR2 w - - 0 1"
//                "r5k1/ppp5/4p1q1/8/4b3/3B4/1RPP4/3QKR2 b - - 0 1"
//                "r5k1/ppp5/4p1q1/8/8/3b4/1RPP4/3QKR2 w - - 0 1"
//                "r5k1/ppp5/4p1q1/8/8/3P4/1R1P4/3QKR2 b - - 0 1"
//                "r5k1/ppp5/4p3/8/8/3q4/1R1P4/3QKR2 w - - 0 1"
//                "r5k1/ppp5/4p3/8/6Q1/3q4/1R1P4/4KR2 b - - 0 1"
//                "r6k/ppp5/4p3/8/6Q1/3q4/1R1P4/4KR2 w - - 0 1"
//                "r6k/ppp5/4p3/8/6Q1/3q4/1R1P4/4K1R1 b - - 0 1"
//                "r6k/ppp4q/4p3/8/6Q1/8/1R1P4/4K1R1 w - - 0 1"
//                "r6k/ppp4q/4p3/8/3Q4/8/1R1P4/4K1R1 b - - 0 1"
//                "r6k/ppp3q1/4p3/8/3Q4/8/1R1P4/4K1R1 w - - 0 1"
//                "r6k/ppp3Q1/4p3/8/8/8/1R1P4/4K1R1 b - - 0 1"

                // Mable game (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"
//                "rnbqkbnr/1ppppppp/p7/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/1ppppppp/p7/8/3P4/2P5/PP2PPPP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/1pppp1pp/p7/5p2/3P4/2P5/PP2PPPP/RNBQKBNR w KQkq f6 0 1"
//                "rnbqkbnr/1pppp1pp/p7/5p2/3P4/2P5/PP1NPPPP/R1BQKBNR b KQkq - 0 1"
//                "rnbqkbnr/2ppp1pp/p7/1p3p2/3P4/2P2P2/PP1NP1PP/R1BQKBNR b KQkq - 0 1"
//                "rnbqkbnr/2ppp1pp/p7/1p6/3P1p2/2P2P2/PP1NP1PP/R1BQKBNR w KQkq - 0 1"
//                "rnbqkbnr/2ppp1pp/p7/1p6/3PNp2/2P2P2/PP2P1PP/R1BQKBNR b KQkq - 0 1"
//                "rnbqkbnr/2ppp1p1/p6p/1p6/3PNp2/2P2P2/PP2P1PP/R1BQKBNR w KQkq - 0 1"
//                "rnbqkbnr/2ppp1p1/p6p/1p6/3PNB2/2P2P2/PP2P1PP/R2QKBNR b KQkq - 0 1"
//                "rn1qkbnr/1bppp1p1/p6p/1p6/3PNB2/2P2P2/PP2P1PP/R2QKBNR w KQkq - 0 1"
//                "rn1qkbnr/1bppp1p1/p6p/1p6/3PNB2/2P2P2/PP2P1PP/RQ2KBNR b KQkq - 0 1"
//                "rn1qkbnr/2ppp1p1/p6p/1p6/3PbB2/2P2P2/PP2P1PP/RQ2KBNR w KQkq - 0 1"
//                "rn1qkbnr/2ppp1p1/p6p/1p6/3PQB2/2P2P2/PP2P1PP/R3KBNR b KQkq - 0 1"
//                "rn1qkbnr/2p1p1p1/p6p/1p1p4/3PQB2/2P2P2/PP2P1PP/R3KBNR w KQkq d6 0 1"
//                "rn1qkbnr/2p1p1p1/p5Qp/1p1p4/3P1B2/2P2P2/PP2P1PP/R3KBNR b KQkq - 0 1"
//                "rn1q1bnr/2pkp1p1/p6p/1p1p1Q2/3P1B2/2P2P2/PP2P1PP/R3KBNR b KQ - 2 2"
//                "rn1q1bnr/2pk2p1/p3p2p/1p1p1Q2/3P1B2/2P2P2/PP2P1PP/R3KBNR w KQ - 2 2"
//                "rn1q1bnr/2pk1Qp1/p3p2p/1p1p4/3P1B2/2P2P2/PP2P1PP/R3KBNR b KQ - 2 2"
//                "rn3bnr/2pkqQp1/p3p2p/1p1p4/3P1B2/2P2P2/PP2P1PP/R3KBNR w KQ - 2 2"
//                "rn3bnr/2pkq1p1/p3p1Qp/1p1p4/3P1B2/2P2P2/PP2P1PP/R3KBNR b KQ - 2 2"
//                "rn3bnr/2pk2p1/p3p1Qp/1p1p4/3P1B1q/2P2P2/PP2P1PP/R3KBNR w KQ - 2 2"
//                "rn3bnr/2pk2p1/p3p1Qp/1p1p4/3P1B1q/2P2PP1/PP2P2P/R3KBNR b KQ - 2 2"
//                "rn3bnr/2pk2p1/p3p1Qp/1p1p4/3P1q2/2P2PP1/PP2P2P/R3KBNR w KQ - 2 2"
//                "rn3bnr/2pk2p1/p3p1Qp/1p1p4/3P1P2/2P2P2/PP2P2P/R3KBNR b KQ - 2 2"
//                "rn3b1r/2pkn1p1/p3p1Qp/1p1p4/3P1P2/2P2P2/PP2P2P/R3KBNR w KQ - 2 2"
//                "rn3b1r/2pknQp1/p3p2p/1p1p4/3P1P2/2P2P2/PP2P2P/R3KBNR b KQ - 2 2"
//                "rn3b1r/2p1nQp1/p2kp2p/1p1p4/3P1P2/2P2P2/PP2P2P/R3KBNR w KQ - 2 2"
//                "rn3b1r/2p1nQp1/p2kp2p/1p1p4/3P1P2/2P2P1B/PP2P2P/R3K1NR b KQ - 2 2"
//                "rn3b1r/2p1nQp1/p1k1p2p/1p1p4/3P1P2/2P2P1B/PP2P2P/R3K1NR w KQ - 2 2"
//                "rn3b1r/2p1nQp1/p1k1B2p/1p1p4/3P1P2/2P2P2/PP2P2P/R3K1NR b KQ - 2 2"
//                "rn3b1r/2p1nQp1/pk2B2p/1p1p4/3P1P2/2P2P2/PP2P2P/R3K1NR w KQ - 2 2"
//                "rn3b1r/2p1nQp1/pk2B2p/1p1p4/3P1P2/2P2P2/PP2P2P/2KR2NR b - - 3 2"
//                "rn3b1r/4nQp1/pkp1B2p/1p1p4/3P1P2/2P2P2/PP2P2P/2KR2NR w - - 3 2"
//                "rn3b1r/4nQp1/pkp1B2p/1p1p1P2/3P4/2P2P2/PP2P2P/2KR2NR b - - 3 2"
//                "rn3b1r/4nQ2/pkp1B2p/1p1p1Pp1/3P4/2P2P2/PP2P2P/2KR2NR w - g6 3 2"
//                "rn3b1r/4nQ2/pkp1BP1p/1p1p2p1/3P4/2P2P2/PP2P2P/2KR2NR b - - 3 2"
//                "rn3b1r/4nQ2/p1p1BP1p/kp1p2p1/3P4/2P2P2/PP2P2P/2KR2NR w - - 3 2"
//                "rn3b1r/4PQ2/p1p1B2p/kp1p2p1/3P4/2P2P2/PP2P2P/2KR2NR b - - 3 2"
//                "rn5r/4bQ2/p1p1B2p/kp1p2p1/3P4/2P2P2/PP2P2P/2KR2NR w - - 3 2"
//                "rn5r/4Q3/p1p1B2p/kp1p2p1/3P4/2P2P2/PP2P2P/2KR2NR b - - 3 2"
//                "rn5r/4Q3/p3B2p/kppp2p1/3P4/2P2P2/PP2P2P/2KR2NR w - - 3 2"
//                "rn5r/4Q3/p6p/kppB2p1/3P4/2P2P2/PP2P2P/2KR2NR b - - 3 2"
//                "rn5r/4Q3/p6p/1ppB2p1/k2P4/2P2P2/PP2P2P/2KR2NR w - - 3 2"
//                "rn5r/4Q3/p6p/1ppB2p1/k2P4/1PP2P2/P3P2P/2KR2NR b - - 3 2"
//                "rn5r/4Q3/p6p/1ppB2p1/3P4/kPP2P2/P3P2P/2KR2NR w - - 3 2"
//                "Bn5r/4Q3/p6p/1pp3p1/3P4/kPP2P2/P3P2P/2KR2NR b - - 3 2"
//                "Bn5r/4Q3/p6p/1pp3p1/3P4/1PP2P2/k3P2P/2KR2NR w - - 3 2"
//                "Bn5r/Q7/p6p/1pp3p1/3P4/1PP2P2/k3P2P/2KR2NR b - - 3 2"
//                "Bn5r/Q7/p6p/1p4p1/3p4/1PP2P2/k3P2P/2KR2NR w - - 3 2"
//                "n6r/8/p6p/1p4p1/3Q4/1PP2P2/k3P2P/2KR2NR b - - 4 3"
//                "n3r3/8/p6p/1p4p1/3Q4/1PP2P2/k3P2P/2KR2NR w - - 4 3"
//                "n3r3/8/p6p/1p4p1/1P1Q4/2P2P2/k3P2P/2KR2NR b - - 4 3"
//                "n3r3/8/p6p/1p4p1/1P1Q4/k1P2P2/4P2P/2KR2NR w - - 4 3"
//                "n3r3/8/p6p/1p4p1/1P1QP3/k1P2P2/7P/2KR2NR b - e3 4 3"
//                "4r3/8/pn5p/1p4p1/1P1QP3/k1P2P2/7P/2KR2NR w - - 4 3"
//                "4r3/8/pQ5p/1p4p1/1P2P3/k1P2P2/7P/2KR2NR b - - 4 3"
//                "4r3/8/1Q5p/pp4p1/1P2P3/k1P2P2/7P/2KR2NR w - - 4 3"
//                "4r3/8/1Q5p/pp4p1/1P2P3/k1P2P2/4N2P/2KR3R b - - 4 3"
//                "r3/8/1Q5p/1p4p1/pP2P3/k1P2P2/4N2P/2KR3R w - - 4 3"
//                "r7/8/7p/1Q4p1/pP2P3/k1P2P2/4N2P/2KR3R b - - 0 3"
//                "r7/8/7p/1Q6/pP2P1p1/k1P2P2/4N2P/2KR3R w - - 0 3"
//                "r7/8/7p/8/pP2P1p1/k1PQ1P2/4N2P/2KR3R b - - 0 3"
//                "r7/8/7p/8/pP2P3/k1PQ1p2/4N2P/2KR3R w - - 0 3"
//                "r7/8/7p/8/pP2P3/k1P2Q2/4N2P/2KR3R b - - 0 3"
//                "5r2/8/7p/8/pP2P3/k1P2Q2/4N2P/2KR3R w - - 0 3"
//                "5Q2/8/7p/8/pP2P3/k1P5/4N2P/2KR3R b - - 0 3"
//                "5Q2/8/7p/8/pP2P3/1kP5/4N2P/2KR3R w - - 0 3"
//                "1Q6/8/7p/8/pP2P3/1kP5/4N2P/2KR3R b - - 0 3"
//                "1Q6/8/8/7p/pP2P3/1kP5/4N2P/2KR3R w - - 0 3"
//                "1Q6/8/8/7p/pP1NP3/1kP5/7P/2KR3R b - - 0 3"
//                "1Q6/8/8/7p/pP1NP3/2k5/7P/2KR3R w - - 0 3"
//                "1Q6/8/8/7p/pP2P3/2k5/4N2P/2KR3R b - - 0 3"
//                "1Q6/8/8/7p/pP2P3/1k6/4N2P/2KR3R w - - 0 3"
//                "1Q6/8/8/7p/pP2P2P/1k6/4N3/2KR3R b - h3 0 3"
//                "1Q6/8/8/7p/1P2P2P/pk6/4N3/2KR3R w - - 0 3"
//                "1Q6/8/8/7p/1P1NP2P/pk6/8/2KR3R b - - 0 3"
//                "1Q6/8/8/7p/1P1NP2P/p7/k7/2KR3R w - - 0 3"
//                "Q7/8/8/7p/1P1NP2P/p7/k7/2KR3R b - - 0 3"
//                "Q7/8/8/7p/1P1NP2P/p7/8/k1KR3R w - - 0 3"
//                "8/8/8/7p/1P1NP2P/Q7/8/k1KR3R b - - 0 3"

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

                // mate in 9 (19)
//                "1Nr1n3/p3p1q1/P2p1prk/4p3/1pB1n1P1/1P1R4/3b2KN/8 w"

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
