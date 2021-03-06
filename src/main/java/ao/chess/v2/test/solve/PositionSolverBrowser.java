package ao.chess.v2.test.solve;


import ao.chess.v2.engine.heuristic.learn.PgnGenerator;
import ao.chess.v2.engine.heuristic.learn.PgnParser;
import ao.chess.v2.engine.neuro.rollout.RolloutNode;
import ao.chess.v2.engine.neuro.rollout.store.FileRolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.RolloutStore;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import com.google.common.primitives.Ints;

import java.nio.file.Paths;
import java.util.List;


public class PositionSolverBrowser {
    public static void main(String[] args) throws Exception {
        State nextState = State.initial();

        String history = "" +
//                "1.e4 c5 2.Nf3 d6";
//                "1.c4 e5 2.Nc3 Nf6";
//                "1.c4 c5 2.Nf3 Nc6 3.Nc3 e5 4.g3 g6 5.a3 Bg7 6.Bg2 Nge7 7.O-O d6 8.Rb1 O-O 9.d3 f5 10.Bg5";
//                "1.c4 c5 2.Nf3 g6 3.e4 Bg7 4.d4 cxd4 5.Nxd4 Nc6 6.Nc2 Qb6 7.Nc3 Bxc3 8.bxc3 Qa5 9.f3 b6 10.Be2 Nf6 " +
//                    "11.O-O O-O";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 Bf5 5.Nc3";
//                "1.d4 g6 2.c4 Bg7 3.Nc3 d6 4.Nf3 Nf6";
//                "1.d4 d5 2.Nf3 Nf6 3.c4 dxc4 4.e3 e6 5.Bxc4 c5 6.O-O a6 7.a4 cxd4 8.exd4 Nc6 9.Nc3 Bb4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 a6 5.Bxc4 Nf6 6.O-O Nbd7";
//                "1.d4 c6 2.e4 d5 3.e5 Bf5 4.c4 e6 5.Nc3 Ne7";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5";
//                "1.d4 c6 2.c4 d5 3.Nf3 Nf6 4.Nc3 e6 5.e3 Nbd7 6.Qc2 Bd6 7.Bd3 O-O 8.O-O";
//                "1.d4 c6 2.Nf3 Nf6 3.c4 d5 4.e3 Bf5 5.Nc3 a6 6.Bd2 e6 7.Nh4 Bg4 8.Qb3";
//                "1.d4 c6 2.Nf3 Nf6 3.c4 d5 4.e3 a6 5.Nc3 Bf5 6.Bd3 Bxd3 7.Qxd3 e6 8.O-O Be7 9.e4 O-O 10.Bf4 dxe4 " +
//                    "11.Nxe4";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 Bf5 5.Nc3 a6 6.Bd3 Bg6 7.Bxg6 hxg6 8.O-O";
//                "1.d4 Nf6 2.c4 c6 3.Nf3 d5 4.e3 a6 5.Nbd2 Bf5 6.b4 Nbd7 7.c5 e6 8.a4 Rg8";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 Be7 5.Bg2 O-O"; // x
//                "1.d4 d5 2.c4 dxc4 3.e3 a6 4.Bxc4 Nf6 5.Nf3 e6 6.O-O c5";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Nf3 Nf6 5.Bxc4 c5 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Nf3 Nf6 5.Bxc4 c5 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.Qe2 Nc6";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Bxc4 Nf6 5.Nf3 c5 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Bxc4 c5 5.Nf3 Nf6 6.O-O a6 7.a4 cxd4";
//                "1.d4 d5 2.c4 dxc4 3.e3 a6 4.Bxc4 c5 5.Nf3 e6 6.O-O Nf6 7.a4";
//                "1.d4 d5 2.c4 dxc4 3.e3 a6 4.Bxc4 e6 5.Nf3 c5 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 b5 16.Rfd1 Qe8 17.axb5 Be6 18.Qd3 Bxa2 " +
//                    "19.Rxa2 Qxb5 20.Qxb5 axb5 21.Rxa8 Rxa8 22.g3 h5 23.h3 g6";
//                "1.d4 d5 2.c4 dxc4 3.e3 e6 4.Bxc4 a6 5.Nf3 c5 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 b5 16.Rfd1 Qe8 17.axb5 Be6 18.Qd3 Bxa2 " +
//                    "19.Rxa2 Qxb5 20.Qxb5 axb5 21.Rxa8 Rxa8 22.g3 h5 23.h3";
//                "1.d4 d5 2.c4 dxc4 3.e3 Nf6 4.Bxc4 a6 5.Nf3 e6 6.O-O c5 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5";
//                "1.d4 c6 2.Nf3 Nf6 3.c4 d5 4.e3 Bf5 5.Nc3 a6";
//                "1.d4 c6 2.c4 Nf6 3.Nf3 d5 4.e3 Bf5 5.Nc3 a6 6.Be2 e6 7.O-O h6 8.Bd3 Bxd3 9.Qxd3 Bb4 10.a3 Bxc3 " +
//                    "11.Qxc3 O-O 12.b3 Nbd7";//                "1. e4";
//                "1.c4 e5 2.g3 Nc6";
//                "1.c4 c5 2.Nf3 Nf6 3.Nc3 e5";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Bb4 5.Nd5 e4 6.Nh4 O-O 7.Bg2 Re8 8.O-O d6 9.b3 g5 10.Bb2 Nd5 " +
//                    "11.cd5 Ne5 12.f4 gh4 13.fe5 de5 14.e3 Qg5 15.gh4 Qh4 16.Qe1";
//                "1.c4 e5 2.g3 Nc6 3.Nc3 f5 4.Nf3 Nf6 5.Bg2 g6 6.Rb1 Bg7 7.O-O a5 8.d3 d6 9.a3";
//                "1.c4 e5 2.g3 Nc6 3.Nc3 f5 4.Nf3 Nf6 5.Bg2 g6 6.Rb1 Bg7 7.O-O O-O 8.d3 d6 9.b4 h6 10.b5 Ne7 " +
//                    "11.Qb3 Nd7 12.Ba3 Kh7 13.Bb4 a6 14.Rfc1 a5 15.Ba3 Rb8 16.b6 c5 17.e3 Nc6 18.Nb5 Qe7 " +
//                    "19.Bb2 a4 20.Qxa4 Nxb6 21.Qc2 Nd7 22.a3 Nf6 23.Ba1";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Be7 5.Bg2 O-O 6.O-O Re8";
//                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Be7 5.Bg2 h6 6.e4 O-O";
//                "1.d4 Nf6 2.c4";
//                "1.c4 e5 2.Nc3 Nf6";
//                "1. e3";
//                "1. g4";
//                "1.d4 Nf6 2.c4 e6 3.Nf3 d5 4.g3 dxc4 5.Bg2";
//                "1.d4 Nf6 2.c4 e6 3.Nc3 Bb4 4.Nf3 O-O 5.Bg5";
//                "1.d4 Nf6 2.c4 c6 4.Nf3 d5 5.Nc3 e6 6.e3 Nbd7";
//                "1.Nf3 d5 2.d4 Nf6 3.c4 e6 4.Nc3";
//                "1.Nf3 Nf6 2.c4";
//                "1.Nf3 Nc6 2.e4 e5 3.d4 exd4 4.Nxd4 Nxd4";
//                "1.d4 d5 2.c4 dxc4 3.e3 Nf6 4.Bxc4 c5 5.Nf3 e6";
//                "1.d4 d5 2.Nf3 Nf6 3.c4 dxc4 4.e3 a6 5.Bxc4 b5 6.Bd3 Nbd7 a4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 a6 5.Bxc4 Nf6 6.O-O c5 7.b3";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.b3 Nbd7 8.Bb2 Be7 9.dxc5 Bxc5 10.Be2 b6";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4 Bd6 9.Nd2 O-O 10.b3 e5";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 Nbd7 8.Nc3 b5 9.d5 exd5 10.Nxd5 Bb7 " +
//                    "11.Nxf6 Qxf6 12.a4 b4 13.e4 Be7";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 b6 8.Nc3 cxd4 9.Qxd4 Qxd4 10.Nxd4 Bb7";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 b6 8.Nc3 cxd4 9.Nxd4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4 Be7 9.a3 O-O 10.b4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4 Be7 9.b3 Bd7 10.Nd2 Nc6";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 Nf6 5.Bxc4 c5 6.O-O a6 7.Be2 Nbd7";
//                "1.d4 d5 2.Nf3 Nf6 3.c4 dxc4 4.e3 e6 5.Bxc4 c5 6.O-O a6 7.Be2 cxd4 8.Nxd4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.Be2 Nbd7 8.Nc3 b5 9.d5 exd5 10.Nxd5 Bb7 " +
//                    "11.Nxf6 Qxf6 12.a4 b4 13.e4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Bc4 Qxd1 15.Rfxd1 Bf5 16.Bb3 Rfd8 17.Ng5 Bg6 18.Bb6 Rxd1 " +
//                    "19.Rxd1 Rc8";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.a5 Bb4 16.Bb6 Qe7 17.Rc1 Rac8 18.h4";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 b5 16.Rfd1 Qe8 17.axb5 Be6 18.Qd3 Bxa2 " +
//                    "19.Rxa2 Qxb5 20.Qxb5 axb5 21.Rxa8 Rxa8 22.Kf1 h5 23.Ke2";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.a5 Nf6 14.Ba2 Qxd1 15.Rxd1 h6 16.Bd2 Rd8 17.Re1 Kf8 18.Bc3";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 a6 6.O-O Nf6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Qxd1 15.Rfxd1 Bd7 16.a5 Rac8 17.Bd4 Rfd8 18.Bb6 Re8 " +
//                    "19.Ng5 Rf8 20.h3 Bb5 21.Rd4 Rc2 22.Re1 Rxb2 23.Rxe7 Rxa2 24.Rxb7 h6 25.Nf3 Nh5";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 Nf6 6.O-O a6 7.Be2 Nbd7";
//                "1.d4 d5 2.Nf3 e6 3.c4 dxc4 4.e3 c5 5.Bxc4 Nf6 6.O-O a6 7.a4 cxd4 8.exd4 Be7 9.Nc3 O-O 10.d5 Nxd5 " +
//                    "11.Nxd5 exd5 12.Bxd5 Nd7 13.Be3 Nf6 14.Ba2 Bf5 15.Qb3 Be4 16.Rfd1";

//                "1.d4 d5 2.c4 e6 3.Nc3 Nf6";
//                "1.d4 e6 2.c4 d5 3.Nc3 Nf6";
//                "1.d4 Nf6 2.c4 e6 3.Nf3";
//                "1.d4 g6 2.c4 Bg7 3.Nc3 d6";
                "1.d4 c5 2.d5 g6 3.e4";

        List<State> moveHistories = PgnParser.parse(history);

        RolloutStore store = new FileRolloutStore(
                Paths.get("lookup/tree/root.bin"),
                null);
//                Paths.get("lookup/tree/root-1.bin"));

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
