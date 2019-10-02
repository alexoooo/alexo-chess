package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.impl.classification.LinearBinarySingular;
import ao.chess.v2.engine.heuristic.player.HeuristicPlayer;
import ao.chess.v2.engine.mcts.heuristic.MctsCaptureHeuristic;
import ao.chess.v2.engine.mcts.heuristic.MctsHeuristicImpl;
import ao.chess.v2.engine.mcts.node.MctsNodeImpl;
import ao.chess.v2.engine.mcts.player.MctsPlayer;
import ao.chess.v2.engine.mcts.player.MultiMctsPlayer;
import ao.chess.v2.engine.mcts.rollout.MctsRolloutImpl;
import ao.chess.v2.engine.mcts.scheduler.MctsSchedulerImpl;
import ao.chess.v2.engine.mcts.transposition.NullTransTable;
import ao.chess.v2.engine.mcts.value.Ucb1TunedValue;
import ao.chess.v2.engine.simple.RandomPlayer;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.util.List;

/**
 * User: aostrovsky
 * Date: 1-Oct-2009
 * Time: 10:49:42 AM
 */
public class Tournament
{
    //--------------------------------------------------------------------
    private static final int TIME_PER_MOVE = 1000;


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        MctsPlayer a = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1TunedValue.Factory(),
                new MctsRolloutImpl(false),
                new Ucb1TunedValue.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory()
        );
//        Player a = new RandomPlayer();
//        Player a = new SimPlayer(false);
//        Player a = new HeuristicPlayer(
////                new SimpleWinTally("test"));
//                new LinearBinarySingular("test"));

//        Player b = new SimPlayer(false);
//        Player b = new HeuristicPlayer(
////                        new SimpleWinTally("test"));
//                new LinearBinarySingular("test"));
//        Player b = new RandomPlayer();
//        Player b = new TransPlayer();
//        Player b = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsTablebaseRollout(),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<Ucb1TunedValue>(),
//                new MctsSchedulerImpl.Factory()
//        );
//        Player b = new MctsPlayer(
//                new MctsNodeImpl.Factory<Ucb1TunedValue>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsRolloutImpl(true),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<Ucb1TunedValue>(),
//                new MctsSchedulerImpl.Factory()
//        );

//        Player b = new MctsPlayer(
//                new MctsNodeImpl.Factory<>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsRolloutImpl(false),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsCaptureHeuristic(),
//                new NullTransTable<>(),
//                new MctsSchedulerImpl.Factory()
//        );

        MctsPlayer bProto = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1TunedValue.Factory(),
                new MctsRolloutImpl(false),
                new Ucb1TunedValue.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory());
        Player b = new MultiMctsPlayer(List.of(
                bProto.prototype(),
                bProto.prototype()));

        int aWins = 0;
        int bWins = 0;
        int draws = 0;

        for (int i = 0; i < 10000; i++) {
            if (i % 2 == 0) {
                Outcome outcome = round(a, b);
                if (outcome == Outcome.WHITE_WINS) {
                    aWins++;
                } else if (outcome == Outcome.BLACK_WINS) {
                    bWins++;
                } else {
                    draws++;
                }
            } else {
                Outcome outcome = round(b, a);
                if (outcome == Outcome.WHITE_WINS) {
                    bWins++;
                } else if (outcome == Outcome.BLACK_WINS) {
                    aWins++;
                } else {
                    draws++;
                }
            }

            System.out.println("====================================================================================");
            System.out.println("aWins\t" + aWins);
            System.out.println("bWins\t" + bWins);
            System.out.println("draws\t" + draws);
        }

        a.close();
        b.close();
    }


    //--------------------------------------------------------------------
    private static Outcome round(Player white, Player black)
    {
        State state = State.initial();

        while (! state.isDrawnBy50MovesRule())
        {
//            System.out.println("---------------------------------------");
//            System.out.println(state);

            Player nextToAct =
                    (state.nextToAct() == Colour.WHITE)
                    ? white : black;

            boolean moveMade = false;
            int move = nextToAct.move(state.prototype(),
                    TIME_PER_MOVE, TIME_PER_MOVE, TIME_PER_MOVE);
            int undoable = -1;
            if (move != -1) {
                undoable = Move.apply(move, state);
                if (undoable != -1) {
                    moveMade = true;
                }
            }

            if (! moveMade) {
                if (state.isInCheck(Colour.WHITE)) {
                    return Outcome.BLACK_WINS;
                } else if (state.isInCheck(Colour.BLACK)) {
                    return Outcome.WHITE_WINS;
                }
                return Outcome.DRAW;
            }

//            System.out.println(Move.toString(undoable));
        }
        return Outcome.DRAW;
    }
}
