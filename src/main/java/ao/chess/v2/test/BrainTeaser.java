package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.player.BanditPlayer;
import ao.chess.v2.engine.mcts.player.par.ParallelMctsPlayer;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;


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
        int time = 7 * 24 * 60 * 60 * 1000;
//        int time = 60 * 1000;
//        int time = 10 * 60 * 1000;
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

//        Player player = new MultiMctsPlayer(List.of(
//                mctsFallbackOptPrototype.prototype(),
//                mctsFallbackDeepOptPrototype.prototype(),
//                mctsFallbackPrototype.prototype(),
//                mctsFallbackDeepPrototype.prototype(),
//                mctsMaterialMixedPrototype.prototype(),
//                mctsMaterialMixedRandomPrototype.prototype(),
//                mctsMaterialMixedDeepPrototype.prototype(),
//                mctsMaterialMixedRandomDeepPrototype.prototype()
//        ));
//        Player player = new MultiMctsPlayer(List.of(
////                MctsPrototypes.mctsFallbackDeep5Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Opt8Prototype.prototype()
////                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Rand32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Rand32Prototype.prototype()
////                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Rand64Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Rand64Prototype.prototype()
//                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep5Opt128Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep2Opt64Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep5Rand64Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep5Rand64Prototype.prototype()
////                MctsPrototypes.mctsFallbackDeep1Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt8Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt8Prototype.prototype()
////                MctsPrototypes.mctsFallbackDeep1Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt32Prototype.prototype()
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

//        Player player = new MultiMctsPlayer(List.of(
////                MctsPrototypes.mctsFallbackDeep1Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep1Opt256Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype()
//                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep2Opt32Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt128Prototype.prototype(),
////                MctsPrototypes.mctsFallbackDeep2Opt192Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep5Rand128Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep5Rand128Prototype.prototype()
////                MctsPrototypes.mctsUcb1Deep1x1Prototype.prototype(),
////                MctsPrototypes.mctsUcb1Deep1x1Prototype.prototype(),
////                MctsPrototypes.mctsUcb1Deep1x1Prototype.prototype(),
////                MctsPrototypes.mctsUcb1Deep1x1Prototype.prototype(),
////                MctsPrototypes.mctsUcb1Deep1x1Prototype.prototype(),
////                MctsPrototypes.mctsUcb1Deep1x1Prototype.prototype()
//        ));

        BanditPlayer protoA = new ParallelMctsPlayer(
                "par",
                3,
                0.5,
                7,
                true
        );
        BanditPlayer protoB = new ParallelMctsPlayer(
                "par",
                3,
                0.4,
                15,
                true
        );
        BanditPlayer protoC = new ParallelMctsPlayer(
                "par",
                3,
                0.4,
                15,
                false
        );
//        Player player = new MultiMctsPlayer(List.of(
//                protoC.prototype(),
//                protoC.prototype(),
//                protoC.prototype()
//        ));

        Player player = new ParallelMctsPlayer(
                "par",
                9,
                0.4,
                15,
                false
        );


        State state = State.fromFen(
                // Trivial puzzles
//                "6rk/6pp/3N4/8/8/8/7P/7K w - - 0 1" // N from d6 to f7
//                "R6R/1r3pp1/4p1kp/3pP3/1r2qPP1/7P/1P1Q3K/8 w - - 1 0" // P from f4 to f5
//                "4r1k1/5bpp/2p5/3pr3/8/1B3pPq/PPR2P2/2R2QK1 b - - 0 1" // r from e5 to e1
//                "2r5/2p2k1p/pqp1RB2/2r5/PbQ2N2/1P3PP1/2P3P1/4R2K w - - 1 0"
//                "6k1/ppp2ppp/8/2n2K1P/2P2P1P/2Bpr3/PP4r1/4RR2 b - - 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/5P2/PPPP2PP/RNBQKBNR b KQkq - 0 1"

                // Travis game
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/4P3/8/PPPP1PPP/RNBQKBNR b KQkq e3 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/8/PPPP1PPP/RNBQKBNR w KQkq c6 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/4P3/5P2/PPPP2PP/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp1p1ppp/4p3/2p5/4P3/5P2/PPPP2PP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pp1p1ppp/4p3/2p5/4P3/2P2P2/PP1P2PP/RNBQKBNR b KQkq - 0 1"

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
                "rn1qk2r/ppp1p1bp/3pb3/5p2/5PnP/3B1N2/PPPP2P1/RNBQK2R w KQkq - 0 1"

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
