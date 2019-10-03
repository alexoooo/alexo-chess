package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.heuristic.MctsCaptureHeuristic;
import ao.chess.v2.engine.mcts.heuristic.MctsHeuristicImpl;
import ao.chess.v2.engine.mcts.node.MctsNodeImpl;
import ao.chess.v2.engine.mcts.player.MctsPlayer;
import ao.chess.v2.engine.mcts.player.MultiMctsPlayer;
import ao.chess.v2.engine.mcts.rollout.MaterialFallbackRollout;
import ao.chess.v2.engine.mcts.rollout.MaterialMixedRollout;
import ao.chess.v2.engine.mcts.rollout.MaterialPureRollout;
import ao.chess.v2.engine.mcts.rollout.MctsRolloutImpl;
import ao.chess.v2.engine.mcts.scheduler.MctsSchedulerImpl;
import ao.chess.v2.engine.mcts.transposition.NullTransTable;
import ao.chess.v2.engine.mcts.value.Ucb1Value;
import ao.chess.v2.engine.mcts.value.Ucb1Value2;
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
    private static final int TIME_PER_MOVE = 3 * 1000;


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
        MctsPlayer mctsPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value.Factory(),
                new MctsRolloutImpl(false),
                new Ucb1Value.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory()
        );

        MctsPlayer mctsFallbackPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value.Factory(),
                new MaterialFallbackRollout(new MctsRolloutImpl(false)),
                new Ucb1Value.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory()
        );

        MctsPlayer mctsFallbackDeepPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value2.Factory(),
                new MaterialFallbackRollout(new MctsRolloutImpl(false)),
                new Ucb1Value2.VisitSelector(),
                new MctsCaptureHeuristic(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory()
        );

        MctsPlayer mctsMaterialPurePrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value.Factory(),
                new MaterialPureRollout(),
                new Ucb1Value.VisitSelector(),
                new MctsCaptureHeuristic(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory());

        MctsPlayer mctsMaterialPureDeepPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value2.Factory(),
                new MaterialPureRollout(),
                new Ucb1Value2.VisitSelector(),
                new MctsCaptureHeuristic(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory());

        MctsPlayer mctsMaterialMixedPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value.Factory(),
                new MaterialFallbackRollout(new MaterialMixedRollout()),
                new Ucb1Value.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory());

        MctsPlayer mctsMaterialMixedRandomPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value.Factory(),
                new MaterialFallbackRollout(new MaterialMixedRollout(true)),
                new Ucb1Value.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory());

        MctsPlayer mctsMaterialMixedDeepPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value2.Factory(),
                new MaterialFallbackRollout(new MaterialMixedRollout()),
                new Ucb1Value2.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory());

        MctsPlayer mctsMaterialMixedRandomDeepPrototype = new MctsPlayer(
                new MctsNodeImpl.Factory<>(),
                new Ucb1Value2.Factory(),
                new MaterialFallbackRollout(new MaterialMixedRollout(true)),
                new Ucb1Value2.VisitSelector(),
                new MctsHeuristicImpl(),
                new NullTransTable<>(),
                new MctsSchedulerImpl.Factory());

        Player a = new MultiMctsPlayer(List.of(
                mctsPrototype.prototype(),
                mctsPrototype.prototype(),
                mctsPrototype.prototype(),
                mctsPrototype.prototype(),
                mctsPrototype.prototype(),
                mctsPrototype.prototype()));
//        Player a = new MctsPlayer(
//                new MctsNodeImpl.Factory<>(),
//                new Ucb1Value2.Factory(),
//                new MaterialFallbackRollout(new MaterialMixedRollout()),
//                new Ucb1Value2.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<>(),
//                new MctsSchedulerImpl.Factory());

//        MctsPlayer a = new MctsPlayer(
//                new MctsNodeImpl.Factory<>(),
//                new Ucb1TunedValue.Factory(),
//                new MctsRolloutImpl(false),
//                new Ucb1TunedValue.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<>(),
//                new MctsSchedulerImpl.Factory()
//        );

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

//        Player b = new MultiMctsPlayer(List.of(
//                mctsProto.prototype(),
//                mctsProto.prototype(),
//                mctsProto.prototype(),
//                mctsProto.prototype(),
//                mctsProto.prototype(),
//                mctsProto.prototype(),
//                mctsProto.prototype(),
//                mctsProto.prototype(),
//                mctsProto.prototype()));

        Player b = new MultiMctsPlayer(List.of(
                mctsFallbackPrototype.prototype(),
                mctsFallbackDeepPrototype.prototype(),
                mctsMaterialMixedPrototype.prototype(),
                mctsMaterialMixedRandomPrototype.prototype(),
                mctsMaterialMixedDeepPrototype.prototype(),
                mctsMaterialMixedRandomDeepPrototype.prototype()
        ));
//        MctsPlayer b = new MctsPlayer(
//                new MctsNodeImpl.Factory<>(),
//                new Ucb1Value2.Factory(),
//                new MaterialPureRollout(),
//                new Ucb1Value2.VisitSelector(),
//                new MctsHeuristicImpl(),
//                new NullTransTable<>(),
//                new MctsSchedulerImpl.Factory()
//        );

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
