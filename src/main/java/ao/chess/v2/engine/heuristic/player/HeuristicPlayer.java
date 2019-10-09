package ao.chess.v2.engine.heuristic.player;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.state.State;
import ao.util.math.rand.Rand;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 12:41:49 PM
 */
public class HeuristicPlayer implements Player
{
    //--------------------------------------------------------------------
    private final MoveHeuristic HEURISTIC;
    private final boolean opt;


    //--------------------------------------------------------------------
    public HeuristicPlayer(MoveHeuristic heuristic) {
        this(heuristic, false);
    }

    public HeuristicPlayer(MoveHeuristic heuristic, boolean opt) {
        HEURISTIC = heuristic;
        this.opt = opt;
    }


    //--------------------------------------------------------------------
    @Override public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
//        double   total      = 0;
        int[] legalMoves = position.legalMoves();
        if (legalMoves == null || legalMoves.length == 0) return -1;

        double[] moveValues = new double[ legalMoves.length ];
        for (int i = 0; i < legalMoves.length; i++) {
            double value = HEURISTIC.evaluate(position, legalMoves[i]);
            moveValues[i] = value;
//            total += value;
        }

        int    maxMove      = -1;
        double maxMoveValue = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < legalMoves.length; i++) {
//            double probability = moveValues[ i ] / total;
//            double value       = Rand.nextDouble( probability );
//            double value       = moveValues[ i ];

            double value;
            if (opt) {
                value = moveValues[ i ];
            }
            else {
                value = moveValues[ i ] * Rand.nextDouble();
            }

            if (maxMoveValue < value ||
                    maxMoveValue == value && Rand.nextBoolean()) {
                maxMoveValue = value;
                maxMove      = legalMoves[ i ];
            }
        }

        return maxMove;
    }


    //--------------------------------------------------------------------
    @Override
    public String toString()
    {
        return HEURISTIC.toString();
    }
}
