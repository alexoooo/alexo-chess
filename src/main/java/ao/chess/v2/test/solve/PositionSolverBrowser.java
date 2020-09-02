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
//                "1.c4 e5 2.Nc3 Nf6";
//                "1.c4 c5 2.Nf3 Nc6 3.Nc3 e5 4.g3 g6 5.a3 Bg7 6.Bg2 Nge7 7.O-O d6 8.Rb1 O-O 9.d3 f5 10.Bg5";
//                "1.c4 c5 2.Nf3 g6 3.e4 Bg7 4.d4 cxd4 5.Nxd4 Nc6 6.Nc2 Qb6 7.Nc3 Bxc3 8.bxc3 Qa5 9.f3 b6 10.Be2 Nf6 " +
//                    "11.O-O O-O";
//                "1.d4 Nf6 2.c4 e6 3.Nf3";
//                "1.d4 g6 2.c4 Bg7 3.Nc3 d6 4.Nf3 Nf6";
                "1.d4 d5 2.c4 dxc4 3.Nf3 a6 4.e3";
//                "1.d4 c6 2.Nf3 Nf6 3.c4 d5 4.e3 Bf5 5.Nc3 a6";
//                "1.d4 c6 2.c4 Nf6 3.Nf3 d5 4.e3 Bf5 5.Nc3 a6 6.Be2 e6 7.O-O h6 8.Bd3 Bxd3 9.Qxd3 Bb4 10.a3 Bxc3 " +
//                    "11.Qxc3 O-O 12.b3 Nbd7";
 //                "1. e4";
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
//                "1.Nf3 Nc6 2.e4 e5";
//                "1.Nf3 Nc6 2.e4 e5 3.d4 exd4 4.Nxd4 Nxd4";

        List<State> moveHistories = PgnParser.parse(history);

        RolloutStore store = new FileRolloutStore(
                Paths.get("lookup/tree/root.bin"));
//                Paths.get("lookup/tree/root-1.bin"));

        System.out.println("history: " + history);


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
