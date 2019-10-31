package ao.chess.v2.test;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.learn.MoveHistory;
import ao.chess.v2.engine.mcts.player.ScoredPlayer;
import ao.chess.v2.engine.mcts.player.neuro.PuctPlayer;
import ao.chess.v2.engine.simple.RandomPlayer;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UncheckedIOException;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


/**
 * User: aostrovsky
 * Date: 1-Oct-2009
 * Time: 10:49:42 AM
 */
public class Tournament
{
    //--------------------------------------------------------------------
    private static final int TIME_PER_MOVE = 1_000;

    private static final boolean recordThinking = true;
    private static PrintWriter thinkingOut = null;
    private static MoveHistory.Buffer moveExampleBuffer = new MoveHistory.Buffer();


    //--------------------------------------------------------------------
    public static void main(String[] args)
    {
//        Player a = MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep2Opt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep4Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep4Opt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep6Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep7Opt192Prototype.prototype();
////        Player b = MctsPrototypes.mctsFallbackDeep8Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep2Opt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep6Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep2Opt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep4Opt192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep6Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep7Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep6Opt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep5Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep5Tune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep10Tune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep5Tune192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep05Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep2Tune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep3Tune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep2Tune192Prototype.prototype();

        // TBD
//        Player a = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep09Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep11Tune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeepLargeOpt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep5LargeOpt192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep5LargeOpt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep1LargeOpt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep2LargeOpt192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep1LargeOpt192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeepOpt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeepLargeOpt192Prototype.prototype();

//        Player a = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeepTune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep2LargeOpt192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep1Tune192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeepTune192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();
//        Player a = MctsPrototypes.mctsFallbackDeep2LargeOpt192Prototype.prototype();
//        Player b = MctsPrototypes.mctsFallbackDeep5Opt192Prototype.prototype();

//        Player a = new MultiMctsPlayer(List.of(
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype(),
//                mctsPrototype.prototype()));
//        Player a = new MultiMctsPlayer(List.of(
//                mctsFallbackPrototype.prototype(),
//                mctsFallbackPrototype.prototype(),
//                mctsFallbackDeepPrototype.prototype(),
//                mctsMaterialMixedRandomPrototype.prototype(),
//                mctsMaterialMixedRandomPrototype.prototype(),
//                mctsMaterialMixedRandomDeepPrototype.prototype()));
//        Player a = new MultiMctsPlayer(List.of(
//                MctsPrototypes.mctsFallbackOptPrototype.prototype(),
//                MctsPrototypes.mctsFallbackDeepOptPrototype.prototype(),
//                MctsPrototypes.mctsFallbackPrototype.prototype(),
//                MctsPrototypes.mctsFallbackDeepPrototype.prototype(),
//                MctsPrototypes.mctsMaterialMixedPrototype.prototype(),
//                MctsPrototypes.mctsMaterialMixedRandomPrototype.prototype(),
//                MctsPrototypes.mctsMaterialMixedDeepPrototype.prototype(),
//                MctsPrototypes.mctsMaterialMixedRandomDeepPrototype.prototype()));
//        Player a = new MultiMctsPlayer(List.of(
//                MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype(),
//                MctsPrototypes.mctsFallbackDeep3Opt192Prototype.prototype()));
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
//                LinearByMaterial.retrieve("test"),
//                true);
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


//        ParallelMctsPlayer a = new ParallelMctsPlayer(
//                "par",
//                9,
//                0.3,
//                1,
//                false
//        );
//        ParallelMctsPlayer b = a.prototype();

//        Player a = NeuralNetworkPlayer.load(
//                Paths.get("lookup/nn_2019-10-25.zip"));
//
//        Player b = NeuralNetworkPlayer.load(
//                Paths.get("lookup/nn_2019-10-25b.zip"));

        Player a = new PuctPlayer(
                Paths.get("lookup/gen/4/nn.zip"),
                1,
                4);
//        Player b = new PuctPlayer(
//                Paths.get("lookup/nn_2019-10-26b.zip"),
//                2,
//                4,
//                0.2);
//        Player a = new TopLeftPlayer();
        Player b = new RandomPlayer();

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
        closeThinkingIfRequired();
    }


    //--------------------------------------------------------------------
    private static Outcome round(Player white, Player black)
    {
        State state = State.initial();

        Outcome outcome = Outcome.DRAW;

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
                State stateProto = state.prototype();

                undoable = Move.apply(move, state);
                if (undoable != -1) {
                    moveMade = true;

                    recordThinkingIfRequired(nextToAct, stateProto);
                }
            }

            if (! moveMade) {
                if (state.isInCheck(Colour.WHITE)) {
                    outcome = Outcome.BLACK_WINS;
                }
                else if (state.isInCheck(Colour.BLACK)) {
                    outcome = Outcome.WHITE_WINS;
                }
                break;
            }
        }

        flushThinkingIfRequired(outcome);
        return outcome;
    }


    private static void recordThinkingIfRequired(
            Player nextToAct,
            State state
    ) {
        if (! recordThinking || ! (nextToAct instanceof ScoredPlayer)) {
            return;
        }

        ScoredPlayer player = (ScoredPlayer) nextToAct;

        int[] legalMoves = state.legalMoves();

        double[] moveScores = new double[legalMoves.length];
        for (int i = 0; i < legalMoves.length; i++) {
            moveScores[i] = player.moveScoreInternal(legalMoves[i]);
        }

        moveExampleBuffer.add(state, legalMoves, moveScores);
    }


    private static void flushThinkingIfRequired(Outcome outcome) {
        if (moveExampleBuffer.isEmpty()) {
            return;
        }

        if (thinkingOut == null) {
            var filenamePattern = DateTimeFormatter.ofPattern("yyyyMMdd'_'HHmmss'_'SSS");
            var timestamp = filenamePattern.format(LocalDateTime.now());

            try {
                thinkingOut = new PrintWriter(new File(
                        "lookup/think_" + TIME_PER_MOVE + "_" + timestamp + ".csv"));
            }
            catch (FileNotFoundException e) {
                throw new UncheckedIOException(e);
            }
        }

        List<MoveHistory> examples = moveExampleBuffer.build(outcome);
        moveExampleBuffer.clear();

        for (MoveHistory example : examples) {
            thinkingOut.println(example.asString());
        }

        thinkingOut.flush();
    }


    private static void closeThinkingIfRequired() {
        if (thinkingOut != null) {
            thinkingOut.close();
        }
    }
}
