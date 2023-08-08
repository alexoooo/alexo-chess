package ao.chess.v2.test.solve;


import ao.chess.v2.engine.heuristic.learn.PgnGenerator;
import ao.chess.v2.engine.heuristic.learn.PgnParser;
import ao.chess.v2.engine.neuro.rollout.RolloutNode;
import ao.chess.v2.engine.neuro.rollout.store.FileRolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStore;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import com.google.common.primitives.Ints;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class PositionSolverBrowser {
    public static void main(String[] args) throws Throwable {
        State nextState = State.initial();

        String history = "" +
//                "1.c4 Nf6 2.g3 e6 3.Bg2 d5 4.Nf3 Be7 5.O-O O-O 6.d4 Nbd7"; // x
                "1.c4 Nf6 2.g3 e6 3.Nf3 d5 4.Bg2 Be7 5.O-O O-O 6.d4 Nbd7 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6";

//                "1.e4 e5 2.Nf3 Nc6 3.Bb5 Nf6 4.O-O Nxe4 5.d4 Nd6 6.Bxc6 dxc6 7.dxe5 Nf5 8.Qxd8 Kxd8 9.h3 Bd7 " +
//                    "10.Nc3 h6 11.b3 Kc8 12.Bb2 a5 13.Rad1 b6 14.a4 c5"; // x

//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Nxe5 23.Nxe6 fxe6 24.Bxe5 Rf5 25.Qe3 Rxe5 26.Qxe5 Bf6 " +
//                    "27.Qe2 Bxa1 28.Rxa1 dxc4 29.Nxc4 Bxg2 30.Kxg2 axb4 31.axb4 Qd5 32.Kh2 Rf8 33.Kg1 Rd8 " +
//                    "34.Re1 Qb5 35.Rb1 Rd4 36.Rc1 Qxb4 37.Ne3 Qd6 38.Qc2"; // x
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Nxe5 23.Nxe6 fxe6 24.Bxe5 Rf5 25.Qe3 Rxe5 26.Qxe5 Bf6 " +
//                    "27.Qe2 Bxa1 28.Rxa1 dxc4 29.Nxc4 Bxg2 30.Kxg2 axb4 31.axb4 Qd5 32.Kh2 Rf8 33.Kg1 Rd8 " +
//                    "34.Re1 Qb5 35.Rb1 Rd4 36.Rc1 Rd6";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.bxa5 Na6 22.Rab1 Nac5 23.Qe3 bxa5 34.Nd4 Nb6 35.Bc3 Nba4 36.Ba1 Nb6 " +
//                    "37.Nb5 Ba6 38.Bd4 dxc4 39.Nd6 Bxd6 40.exd6 Nbd7 41.Ba1 e5 42.Bd5 Nd3 43.Qa7 c3 44.Bxc3 Rc5 " +
//                    "45.Ne4 Rxd5 46.Qxa6 Qc8 47.Qxc8 Rxc8 48.Rb7 f5";

//                "1.Nf3 Nf6 2.g3 e6 3.Bg2 Be7 4.c4 d5 5.d4 O-O 6.O-O Nbd7 7.Qc2 c6 8.b3 b6 9.Rd1 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 14.Bb2 h6"; // x
//                "1.Nf3 Nf6 2.g3 e6 3.Bg2 Be7 4.d4 d5 5.O-O O-O 6.c4 Nbd7 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7";

//                "1.Nf3 Nf6 2.c4 c5 3.g3 Nc6 4.Bg2 d5";
//                "1.Nf3 c5 2.c4 g6 3.Nc3 Bg7";
//                "1.Nf3 Nf6 2.c4 c5 3.e3 g6 4.Nc3 Bg7";
//                "1.Nf3 Nf6 2.c4 c5 3.Nc3 Nc6 4.e3 e6 5.d4 d5";
//                "1.Nf3 Nf6 2.c4 c5 3.Nc3 g6 4.e3 Bg7 5.d4";
//                "1.Nf3 Nf6 2.c4 c5 3.Nc3 g6 4.g3 Nc6 5.Bg2 Bg7";
//                "1.Nf3 d5 2.d4 c5 3.c4 cxd4 4.cxd5 Nf6";
//                "1.Nf3 d5 2.d4 Nf6 3.c4 e6 4.Nc3";
//                "1.Nf3 Nf6 2.c4 e6 3.g3 d5 4.d4 Be7 5.Bg2 Nbd7 6.O-O O-O";
//                "1.Nf3 Nf6 2.c4 e6 3.g3 d5 4.Bg2 Be7 5.d4 O-O 6.O-O Nbd7 7.Qc2 c6 8.Rd1 b6 9.b3";
//                "1.Nf3 Nf6 2.g3 g6 3.Bg2 Bg7 4.O-O O-O 5.c4";
//                "1.Nf3 Nf6 2.g3 e6 3.Bg2 Be7 4.O-O O-O 5.d4 d5 6.c4 Nbd7 7.Qc2";
//                "1.Nf3 Nf6 2.g3 e6 3.Bg2 Be7 4.c4 d5 5.d4 O-O 6.O-O Nbd7 7.Qc2 c6 8.b3 b6 9.Rd1 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 14.Bb2 h6"; // x
//                "1.Nf3 Nf6 2.g3 e6 3.Bg2 Be7 4.c4 d5 5.d4 O-O 6.O-O Nbd7 7.Qc2 c6 8.b3 b6 9.Rd1 Ba6 10.Nbd2 c5 " +
//                    "11.e4 dxc4";
//                "1.Nf3 Nf6 2.g3 e6 3.Bg2 Be7 4.c4 d5 5.O-O O-O 6.d4 Nbd7 7.Qc2 c6 8.b3 b6 9.Rd1 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7";
//                "1.Nf3 Nf6 2.g3 e6 3.Bg2 Be7 4.c4 d5 5.O-O O-O 6.d4 Nbd7 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7";
//                "1.Nf3 Nc6 2.e4 e5 3.d4 exd4 4.Nxd4 Nxd4";
//                "1.Nf3 Nf6 2.c4 e6 3. g3 d5 4. Bg2 Be7 5.d4 O-O 6.O-O Nbd7 7.Qc2 c6 8.b3 b6 9.Rd1 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1";

//                "1.e4 e5 2.Nf3 Nc6 3.Bb5 Nf6 4.O-O Nxe4 5.d4 Nd6 6.Bxc6 dxc6 7.dxe5 Nf5 8.Qxd8 Kxd8 9.h3 h6 " +
//                    "10.Rd1 Ke8 11.Nc3 a5 12.Bf4";
//                "1.e4 e5 2.Nf3 Nc6 3.Bb5 Nf6 4.O-O Nxe4 5.d4 Nd6 6.Bxc6 dxc6 7.dxe5 Nf5 8.Qxd8 Kxd8 9.Nc3 Bd7 10.h3";
//                "1.e4 c5 2.Nf3 d6 3.d4 cxd4 4.Nxd4 Nc6 5.Nc3 Nf6 6.Bg5";

//                "1.Nf3 Nf6 2.c4 e6 3.g3 Be7 4.Bg2 O-O 5.O-O d5 7.d4 dxc4 8.Qc2";
//                "1.Nf3 Nf6 2.c4 e6 3.g3 d5 4.Bg2 Be7 5.O-O O-O 6.d4 Nbd7 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2"; // x
//                "1.Nf3 Nf6 2.c4 e6 3.g3 d5 4.d4 Be7 5.Bg2 Nbd7 6.Qc2 h6";
//                "1.d4 Nf6 2.c4 e6 3.g3 d5 4.Nf3 Be7 5.Bg2 O-O 6.Nc3";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 d5 4.cxd5";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.a3";
//                "1.c4 e5 2.g3 Nf6 3.Bg2 c6 4.Nf3 e4 5.Nd4";
//                "1.c4 e5 2.g3 Nf6 3.Bg2 Nc6 4.Nc3 h6";
//                "1.c4 Nf6 2.Nf3 e6 3.g3 d5 4.Bg2 Be7 5.O-O O-O 6.d4 Nbd7 7.Qc2 c6"; // x
//                "1.c4 Nf6 2.d e6 3.e3 Be7 4.d5";
//                "1.c4 Nf6 2.Nc3 e5 3.Nf3 Nc6 4.g3 Bb4 5.Nd5 e4 6.Nh4 O-O 7.Bg2 d6 8.Nxb4 Nxb4 9.a3 Na6 10.d3 exd3 " +
//                    "11.Qxd3 Nxc5 12.Qxc2 a5 13.a4 Nfd7 14.O-O Nb8 15.b3 Nc6";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.Rac1 Ba8 20.h4 a5 21.Nd4 Qd7 22.Qb1 b5 23.b4 axb4 24.axb4 N5a6 25.cxb5 Nxb5 26.Nxb5 Qxb5 " +
//                    "27.Bf1 Qb7 28.b5 d4 29.Qe4 Qb8 30.Rxc8 Rxc8 31.Qxd4 Rd8 32.Qc4 Nb4 33.Bd4 Nc2 34.Qxc2 Rxd4 " +
//                    "35.Ra1 Qb7 36.Qc6 Qxc6 37.bxc6 Bxc6 38.Nc4 Bf8 39.Rc1";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 Nbd7 6.O-O O-O 7.Qc2 c6 8.Rd1 b6 9.b3 Ba6 10.Nbd2 Rc8 " +
//                    "11.e4 c5 12.e5 Ne8 13.dxc5 Bxc5 14.a3 Be7 15.Qb1 Bb7 16.Bb2 h6 17.Qd3 Nc5 18.Qc2 Nc7 " +
//                    "19.b4 Nd7 20.Qb3 a5 21.Nd4 Ba8 22.h3 Nxe5 23.Nxe6 fxe6 24.Bxe5 Rf5 25.f4 Qf8 26.Kh2 dxc4 " +
//                    "27.Nxc4 Bxg2 28.Kxg2 Nd5 29.Rxd5";

//                "1.d4 Nf6 2.c4 e6 3.e3 Be7 4.Nc3 d5";
//                "1.e4 c5 2.Nf3 d6 3.Nc3 Nf6 4.d4 cxd4 5.Nxd4 a6 6.h3 b5 7.g4";
//                "1.e4 c5 2.Nf3 d6 3.Nc3 e5 4.Bc4 Be7 5.d3 Nf6 6.O-O O-O";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.cxd5 exd5 5.Nc3 c6 6.Qc2 g6 7.Bg5";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 O-O 6.O-O dxc4 7.a4 c5 8.dxc5";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 d5 4.cxd5 exd5 5.Bg5 c6 6.Qc2 Be7 7.e3 Nbd7 8.h3 O-O 9.Bd3 Re8";
//                "1.d4 Nf6 2.c4 e6 3.a3 c5 4.e3 d5 5.Nf3";
//                "1.d4 d5 2.c4 c6 3.Nf3 Nf6 4.e3 Bf5 5.Nc3 e6 6.Nh4 Bg6 7.Nxg6 hxg6";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 Nbd7 5.Nbd2 e6 6.b3 Be7 7.Bb2 O-O"; // x
//                "1.d4 d5 2.c4 c6 3.Nf3 Nf6 4.Nc3 e6 5.e3 Nbd7 6.Bd3 dxc4 7.Bxc4 b5 8.Bd3";
//                "1.d4 d5 2.c4 c6 3.Nf3 Nf6 4.Nc3 e6 5.e3 Nbd7";
//                "1.d4 d5 2.c4 dxc4 3.e4 Nf6 4.e5 Nfd7 5.Bxc4 Nb6";
//                "1.d4 d5 2.c4 dxc4 3.Nf3 a6 4.e3 Nf6 5.Bc4 e6 6.O-O";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.Nc3 e6 5.e3 Nbd7 6.Bd3 dxc4 6.Bxc4 b5";
//                "1.e4 c5 2.Nf3 d6 3.d4 cxd4 4.Nxd4 Nf6 5.Nc3 a6 6.Bg5";
//                "1.e4 e5 2.Nf3 Nc6 3.Bb5 a6 4.Ba4 Nf6 5.O-O Be7 6.Re1 b5 7.Bb3 d6 8.c3 O-O 9.h3"; // x
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Bb4 5.Bg2 O-O 6.O-O e4";

        List<State> moveHistories = PgnParser.parse(history);

        RolloutStore store = new FileRolloutStore(
                PositionSolver.treeDir.resolve("root.bin"),
                null);
//                Paths.get("lookup/tree/root-1.bin"));

        System.out.println("As of: " + LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
        System.out.println("PGN: " + history);

        long nextIndex = RolloutStore.rootIndex;
        printDetails(nextIndex, nextState, store);

        for (int i = 1; i < moveHistories.size(); i++) {
            State moveState = moveHistories.get(i);
            int move = PgnGenerator.findMove(nextState, moveState);
            int moveIndex = Ints.indexOf(nextState.legalMoves(), move);

            System.out.println(i + "\t" + Move.toInputNotation(move) + "\t" + Move.toString(move));

            nextIndex = store.getChildIndex(nextIndex, moveIndex);
            nextState = moveState;

            printDetails(nextIndex, nextState, store);
        }

        store.close();
    }


    private static void printDetails(long index, State state, RolloutStore store) {
        String detail = new RolloutNode(index).toString(state, store);
        System.out.println(detail);
    }
}
