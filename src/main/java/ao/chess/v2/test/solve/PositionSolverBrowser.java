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
//                "1. c4";
//                "1. d4";
//                "1. e4";
//                "1.c4 e5 2.g3 Nc6";
//                "1.c4 c5 2.Nf3 Nf6 3.Nc3 e5";
                "1.c4 e5 2.Nc3 Nf6 3.Nf3 Nc6 4.g3 Bb4 5.Nd5 e4 6.Nh4 O-O 7.Bg2 Re8 8.O-O d6 9.b3 g5 10.Bb2 Nd5 " +
                    "11.cd5 Ne5 12.f4 gh4 13.fe5 de5 14.e3 Qg5 15.gh4 Qh4 16.Qe1";
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
