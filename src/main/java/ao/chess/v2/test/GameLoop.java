package ao.chess.v2.test;


import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.learn.MoveHistory;
import ao.chess.v2.engine.mcts.player.ScoredPlayer;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.util.List;


public class GameLoop
{
    //-----------------------------------------------------------------------------------------------------------------
    private final MoveHistory.Buffer moveExampleBuffer = new MoveHistory.Buffer();


    //-----------------------------------------------------------------------------------------------------------------
    public Outcome play(
            Player white,
            Player black,
            int millisecondsPerMove
    ) {
        State state = State.initial();

        Outcome outcome = Outcome.DRAW;

        while (! state.isDrawnBy50MovesRule())
        {
            Player nextToAct =
                    (state.nextToAct() == Colour.WHITE)
                    ? white : black;

            boolean moveMade = false;
            int move = nextToAct.move(state.prototype(),
                    millisecondsPerMove, millisecondsPerMove, millisecondsPerMove);
            int undoable = -1;
            if (move != -1) {
                State stateProto = state.prototype();

                undoable = Move.apply(move, state);
                if (undoable != -1) {
                    moveMade = true;
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

        return outcome;
    }


    public List<MoveHistory> playWithHistory(
            ScoredPlayer white,
            ScoredPlayer black,
            int millisecondsPerMove
    ) {
        State state = State.initial();

        Outcome outcome = Outcome.DRAW;

        while (! state.isDrawnBy50MovesRule())
        {
//            System.out.println("---------------------------------------");
//            System.out.println(state);

            ScoredPlayer nextToAct =
                    (state.nextToAct() == Colour.WHITE)
                    ? white : black;

            boolean moveMade = false;
            int move = nextToAct.move(state.prototype(),
                    millisecondsPerMove, millisecondsPerMove, millisecondsPerMove);
            int undoable = -1;
            if (move != -1) {
                State stateProto = state.prototype();

                undoable = Move.apply(move, state);
                if (undoable != -1) {
                    moveMade = true;

                    recordThinking(nextToAct, stateProto);
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

        List<MoveHistory> history = moveExampleBuffer.build(outcome);
        moveExampleBuffer.clear();

        return history;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void recordThinking(
            ScoredPlayer nextToAct,
            State state
    ) {
        int[] legalMoves = state.legalMoves();

        double[] moveScores = new double[legalMoves.length];
        for (int i = 0; i < legalMoves.length; i++) {
            moveScores[i] = nextToAct.moveScoreInternal(legalMoves[i]);
        }

        moveExampleBuffer.add(state, legalMoves, moveScores);
    }
}
