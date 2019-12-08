package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.player.neuro.PuctPlayer;
import ao.chess.v2.engine.mcts.player.neuro.PuctSingleModel;
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

        Player player = new PuctPlayer(
                new PuctSingleModel(
                        Paths.get("lookup/nn/multi_6d_20191208.zip"),
                        true
                ),
                4,
                2.5,
                false,
                0,
                true,
//                1.0,
                0.75,
//                0.5,
//                0.25,
//                0.0,
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
//                "r1bqkb1r/pp1n1pp1/2p1pn1p/6N1/3P4/3B1N2/PPP2PPP/R1BQK2R w KQkq - 0 8" // g5 e6 (deep blue)

                // trivial
//                "4R3/4R3/8/2k5/8/2p2K2/2P5/8 w"
//                "8/8/2p1b1k1/r6n/1K6/8/8/8 b - - 0 1"
//                "8/2p5/n7/8/8/2p5/4K3/6k1 b"
//                "2k5/8/8/3K4/1B4Nb/1P5N/3R4/8 w"
//                "8/P7/1bk4p/8/3BP3/RR6/3K4/8 w"
//                "8/8/8/8/6R1/5B2/5K1k/8 w  - 98"
//                "4kr2/pp3p1p/8/8/1KnRP2B/8/P1q1N3/8 b"
//                "K7/8/8/8/5r2/6p1/4q1kq/8 b  - 91 n"


                // Travis 2 (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/3P4/8/PPP1PPPP/RNBQKBNR b KQkq d3 0 1"
//                "rnbqkb1r/pppppppp/5n2/8/3P4/8/PPP1PPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkb1r/pppppppp/5n2/8/2PP4/8/PP2PPPP/RNBQKBNR b KQkq c3 0 1"
//                "rnbqkb1r/pppp1ppp/4pn2/8/2PP4/5N2/PP2PPPP/RNBQKB1R b KQkq - 0 1"
//                "rnbqkb1r/ppp2ppp/4pn2/3p4/2PP4/5N2/PP2PPPP/RNBQKB1R w KQkq d6 0 1"

                // Travis (black)
                //  1. c2c4 e7e5 2. Ng1f3 Nb8c6 3. Nb1c3 d7d6 4. d2d4 e5xd4 5. Nf3xd4 Bc8d7 6. g2g3 Ng8f6
                //  7. Bf1g2 Bf8e7 8. O-O O-O 9. Nd4xc6 Bd7xc6 10. Bg2xc6 b7xc6 11. Qd1c2 Qd8d7 12. Bc1e3 Rf8e8
                //  13. Ra1d1 h7h5 14. Be3d4 h5h4 15. e2e4 Qd7g4 16. f2f3 Qg4g6 17. Qc2g2 h4xg3 18. h2xg3 Nf6h5
                //  19. g3g4 Nh5f4 20. Qg2h2 Be7g5 21. Nc3e2 Nf4xe2+ 22. Qh2xe2 Bg5f4 23. Bd4c3 f7f6 24. c4c5 Kg8f7
                //  25. c5xd6 c7xd6 26. Qe2c4+ Re8e6 27. Qc4xc6 Ra8h8 28. Rd1d5 Rh8h3 29. Rd5f5 Qg6h6
                //  30. Qc6c7+ Re6e7 31. Qc7c4+ Re7e6 32. Qc4a6 Bf4e3+
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/2P5/8/PP1PPPPP/RNBQKBNR b KQkq c3 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/2P5/8/PP1PPPPP/RNBQKBNR w KQkq e6 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/2P5/5N2/PP1PPPPP/RNBQKB1R b KQkq - 0 1"
//                "r1bqkbnr/pppp1ppp/2n5/4p3/2P5/5N2/PP1PPPPP/RNBQKB1R w KQkq - 0 1"
//                "rnbqkbnr/pppp1ppp/8/4p3/2P5/2N2N2/PP1PPPPP/R1BQKB1R b KQkq - 0 1"
//                "r1bqkbnr/ppp2ppp/2np4/4p3/2P5/2N2N2/PP1PPPPP/R1BQKB1R w KQkq - 0 1"
//                "r1bqkbnr/ppp2ppp/2np4/4p3/2PP4/2N2N2/PP2PPPP/R1BQKB1R b KQkq d3 0 1"
//                "r1bqkbnr/ppp2ppp/2np4/8/2Pp4/2N2N2/PP2PPPP/R1BQKB1R w KQkq - 0 1"
//                "r1bqkbnr/ppp2ppp/2np4/8/2PN4/2N5/PP2PPPP/R1BQKB1R b KQkq - 0 1"
//                "r2qkbnr/pppb1ppp/2np4/8/2PN4/2N5/PP2PPPP/R1BQKB1R w KQkq - 0 1"
//                "r2qkbnr/pppb1ppp/2np4/8/2PN4/2N3P1/PP2PP1P/R1BQKB1R b KQkq - 0 1"
//                "r2qkb1r/pppb1ppp/2np1n2/8/2PN4/2N3P1/PP2PP1P/R1BQKB1R w KQkq - 0 1"
//                "r2qkb1r/pppb1ppp/2np1n2/8/2PN4/2N3P1/PP2PPBP/R1BQK2R b KQkq - 0 1"
//                "r2qk2r/pppbbppp/2np1n2/8/2PN4/2N3P1/PP2PPBP/R1BQK2R w KQkq - 0 1"
//                "r2qk2r/pppbbppp/2np1n2/8/2PN4/2N3P1/PP2PPBP/R1BQ1RK1 b kq - 0 1"
//                "r2q1rk1/pppbbppp/2np1n2/8/2PN4/2N3P1/PP2PPBP/R1BQ1RK1 w - - 0 1"
//                "r2q1rk1/pppbbppp/2Np1n2/8/2P5/2N3P1/PP2PPBP/R1BQ1RK1 b - - 0 1"
//                "r2q1rk1/ppp1bppp/2bp1n2/8/2P5/2N3P1/PP2PPBP/R1BQ1RK1 w - - 0 1" // ??
//                "r2q1rk1/ppp1bppp/2Bp1n2/8/2P5/2N3P1/PP2PP1P/R1BQ1RK1 b - - 0 1"
//                "r2q1rk1/p1p1bppp/2pp1n2/8/2P5/2N3P1/PP2PP1P/R1BQ1RK1 w - - 0 1"
//                "r2q1rk1/p1p1bppp/2pp1n2/8/2P5/2N3P1/PPQ1PP1P/R1B2RK1 b - - 0 1"
//                "r4rk1/p1pqbppp/2pp1n2/8/2P5/2N3P1/PPQ1PP1P/R1B2RK1 w - - 0 1"
//                "r4rk1/p1pqbppp/2pp1n2/8/2P5/2N1B1P1/PPQ1PP1P/R4RK1 b - - 0 1"
//                "r3r1k1/p1pqbppp/2pp1n2/8/2P5/2N1B1P1/PPQ1PP1P/R4RK1 w - - 0 1"
//                "r3r1k1/p1pqbppp/2pp1n2/8/2P5/2N1B1P1/PPQ1PP1P/3R1RK1 b - - 0 1"
//                "r3r1k1/p1pqbpp1/2pp1n2/7p/2P5/2N1B1P1/PPQ1PP1P/3R1RK1 w - h6 0 1"
//                "r3r1k1/p1pqbpp1/2pp1n2/7p/2PB4/2N3P1/PPQ1PP1P/3R1RK1 b - - 0 1"
//                "r3r1k1/p1pqbpp1/2pp1n2/8/2PB3p/2N3P1/PPQ1PP1P/3R1RK1 w - - 0 1"
//                "r3r1k1/p1pqbpp1/2pp1n2/8/2PBP2p/2N3P1/PPQ2P1P/3R1RK1 b - e3 0 1"
//                "r3r1k1/p1p1bpp1/2pp1n2/8/2PBP1qp/2N3P1/PPQ2P1P/3R1RK1 w - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp1n2/8/2PBP1qp/2N2PP1/PPQ4P/3R1RK1 b - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp1nq1/8/2PBP2p/2N2PP1/PPQ4P/3R1RK1 w - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp1nq1/8/2PBP2p/2N2PP1/PP4QP/3R1RK1 b - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp1nq1/8/2PBP3/2N2Pp1/PP4QP/3R1RK1 w - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp2q1/7n/2PBP3/2N2PP1/PP4Q1/3R1RK1 w - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp2q1/7n/2PBP1P1/2N2P2/PP4Q1/3R1RK1 b - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp2q1/8/2PBPnP1/2N2P2/PP4Q1/3R1RK1 w - - 0 1"
//                "r3r1k1/p1p1bpp1/2pp2q1/8/2PBPnP1/2N2P2/PP5Q/3R1RK1 b - - 0 1"
//                "r3r1k1/p1p2pp1/2pp2q1/6b1/2PBPnP1/2N2P2/PP5Q/3R1RK1 w - - 0 1" // c3e2?
//                "r3r1k1/p1p2pp1/2pp2q1/6b1/2PBPnP1/5P2/PP2N2Q/3R1RK1 b - - 0 1"
//                "r3r1k1/p1p2pp1/2pp2q1/6b1/2PBP1P1/5P2/PP2n2Q/3R1RK1 w - - 0 1"
//                "r3r1k1/p1p2pp1/2pp2q1/6b1/2PBP1P1/5P2/PP2Q3/3R1RK1 b - - 0 1"
//                "r3r1k1/p1p2pp1/2pp2q1/8/2PBPbP1/5P2/PP2Q3/3R1RK1 w - - 0 1" // d4c3?
//                "r3r1k1/p1p2pp1/2pp2q1/8/2P1PbP1/2B2P2/PP2Q3/3R1RK1 b - - 0 1"
//                "r3r1k1/p1p3p1/2pp1pq1/8/2P1PbP1/2B2P2/PP2Q3/3R1RK1 w - - 0 1"
//                "r3r1k1/p1p3p1/2pp1pq1/2P5/4PbP1/2B2P2/PP2Q3/3R1RK1 b - - 0 1"
//                "r3r3/p1p2kp1/2pp1pq1/2P5/4PbP1/2B2P2/PP2Q3/3R1RK1 w - - 0 1"
//                "r3r3/p1p2kp1/2pP1pq1/8/4PbP1/2B2P2/PP2Q3/3R1RK1 b - - 0 1"
//                "r3r3/p4kp1/2pp1pq1/8/4PbP1/2B2P2/PP2Q3/3R1RK1 w - - 0 1"
//                "r3r3/p4kp1/2pp1pq1/8/2Q1PbP1/2B2P2/PP6/3R1RK1 b - - 0 1"
//                "r7/p4kp1/2pprpq1/8/2Q1PbP1/2B2P2/PP6/3R1RK1 w - - 0 1"
//                "r7/p4kp1/2Qprpq1/8/4PbP1/2B2P2/PP6/3R1RK1 b - - 0 1"
//                "7r/p4kp1/2Qprpq1/8/4PbP1/2B2P2/PP6/3R1RK1 w - - 0 1"
//                "7r/p4kp1/2Qprpq1/3R4/4PbP1/2B2P2/PP6/5RK1 b - - 0 1"
//                "8/p4kp1/2Qprpq1/3R4/4PbP1/2B2P1r/PP6/5RK1 w - - 0 1" // ??
//                "8/p4kp1/2Qprp1q/5R2/4PbP1/2B2P1r/PP6/5RK1 w - - 0 1"
//                "8/p1Q2kp1/3prp1q/5R2/4PbP1/2B2P1r/PP6/5RK1 b - - 0 1"
//                "8/p1Q1rkp1/3p1p1q/5R2/4PbP1/2B2P1r/PP6/5RK1 w - - 0 1"
//                "8/p3rkp1/3p1p1q/5R2/2Q1PbP1/2B2P1r/PP6/5RK1 b - - 0 1"
//                "8/p4kp1/3prp1q/5R2/2Q1PbP1/2B2P1r/PP6/5RK1 w - - 0 1"
//                "8/p4kp1/Q2prp1q/5R2/4PbP1/2B2P1r/PP6/5RK1 b - - 0 1"
//                "8/p4kp1/Q2prp1q/5R2/4PbP1/2B2P1r/PP6/5RK1 b - - 0 1"
//                "8/p4kp1/Q2prp1q/5R2/4P1P1/2B1bP1r/PP6/5RK1 w - - 0 1"

                // Phi (white)
//                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                "rnbqkbnr/pppppppp/8/8/8/7P/PPPPPPP1/RNBQKBNR b KQkq - 0 1"
//                "rnbqkbnr/pp1ppppp/8/2p5/8/5N1P/PPPPPPP1/RNBQKB1R b KQkq - 0 1"
//                "r1bqkbnr/pp1ppppp/2n5/2p5/8/5N1P/PPPPPPP1/RNBQKB1R w KQkq - 0 1"
//                "r1bqkbnr/pp1ppppp/2n5/2p5/8/2N2N1P/PPPPPPP1/R1BQKB1R b KQkq - 0 1"
//                "r1bqkbnr/pp1ppp1p/2n3p1/2p5/8/2N2N1P/PPPPPPP1/R1BQKB1R w KQkq - 0 1"
                "r1bqkbnr/pp1ppp1p/2n3p1/2p5/4N3/5N1P/PPPPPPP1/R1BQKB1R b KQkq - 0 1" // ?? f8g7 - give up pawn
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
