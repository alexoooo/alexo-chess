package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.heuristic.MctsCaptureHeuristic;
import ao.chess.v2.engine.mcts.node.MctsNodeImpl;
import ao.chess.v2.engine.mcts.player.MctsPlayer;
import ao.chess.v2.engine.mcts.rollout.MctsRolloutImpl;
import ao.chess.v2.engine.mcts.scheduler.MctsSchedulerImpl;
import ao.chess.v2.engine.mcts.transposition.NullTransTable;
import ao.chess.v2.engine.mcts.value.Ucb1TunedValue;
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
        int time = 24 * 60 * 60 * 1000;
//        int time = 10 * 60 * 1000;

//        Player player = new UctPlayer(true);
//        Player player = new UctPlayer(false);
//        Player player = new SimPlayer(true);
//        Player player = new SimPlayer(false);
//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1Value>(),
//                new Ucb1Value.Factory(),
//                new MctsRolloutImpl(),
//                new Ucb1Value.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NativeTransTable<Ucb1Value>(
//                        new Ucb1Value.Factory()),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1Value2>(),
//                new Ucb1Value2.Factory(),
//                new MctsRolloutImpl(),
//                new Ucb1Value2.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NativeTransTable<Ucb1Value2>(
//                        new Ucb1Value2.Factory()),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsDeepRolloutImpl(128),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsRolloutImpl(false),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
////                new NativeTransTable<Ucb1TunedValue>(
////                        new Ucb1TunedValue.Factory()),
//                new NullTransTable<Ucb1TunedValue>(),
//                new MctsSchedulerImpl.Factory()
//        );

//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsTablebaseRollout(),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<Ucb1TunedValue>(),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsTablebaseRollout(),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsCaptureHeuristic(),
//                new NullTransTable<Ucb1TunedValue>(),
//                new MctsSchedulerImpl.Factory()
//        );
        Player player = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1TunedValue.Factory(),
                new MctsRolloutImpl(false),
                new Ucb1TunedValue.VisitSelector(),
                new MctsCaptureHeuristic(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory()
        );

//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<UcbTunedValue>(),
//                new UcbTunedValue.Factory(),
//                new MctsRolloutImpl(),
//                new UcbTunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player player = new MctsPlayer(
//                new MctsNodeImpl.Factory<UcbTuned2Value>(),
//                new UcbTuned2Value.Factory(),
//                new MctsRolloutImpl(),
//                new UcbTuned2Value.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player player = new TransPlayer();

        State  state  = State.fromFen(
                // Travis game
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"

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
