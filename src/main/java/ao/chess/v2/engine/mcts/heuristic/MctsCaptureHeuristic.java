package ao.chess.v2.engine.mcts.heuristic;

import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:54:01 PM
 */
public class MctsCaptureHeuristic
    implements MctsHeuristic
{
    //--------------------------------------------------------------------
    @Override public double firstPlayUrgency(int move) {
        return (! Move.isMobility(move) || Move.isPromotion(move) ? 1500 : 1000) +
                Math.random();
    }


    //--------------------------------------------------------------------
    @Override
    public int[] orderMoves(
            State fromState, int[] moves, int nMoves) {
        return MovePicker.pickRandom(nMoves);
    }
}