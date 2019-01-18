package ao.chess.v2.engine.heuristic.index;

import ao.chess.v2.data.MovePicker;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.text.AoFormat;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.apache.log4j.Logger;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 3:42:55 PM
 */
public class MaterialMovePopulation
{
    //--------------------------------------------------------------------
    private final static Logger LOG =
            Logger.getLogger(MaterialMovePopulation.class);

    private MaterialMovePopulation() {}


    //--------------------------------------------------------------------
    public static void populate(
            Int2ObjectMap<IntSet> tallyToMoves)
    {
        int consecutiveNoneAdded = 0;
        for (int i = 0; i < Integer.MAX_VALUE; i++) {
            if ((i + 1) % 1000 == 0) {
                LOG.debug("round " + (i + 1) +
                            "/" + consecutiveNoneAdded +
                            " with " + tallyToMoves.size() +
                            "/" + AoFormat.decimal(
                                    deepCount(tallyToMoves)));
            }

            if (randomGame(State.initial(), tallyToMoves)) {
                consecutiveNoneAdded = 0;
            } else {
                if (consecutiveNoneAdded++ > 1000) {
                    return;
                }
            }
        }
    }

    private static int deepCount(
            Int2ObjectMap<IntSet> tallyToMoves) {
        int deepCount = 0;
        for (IntSet v : tallyToMoves.values()) {
            deepCount += v.size();
        }
        return deepCount;
    }



    //--------------------------------------------------------------------
    private static boolean randomGame(
            State fromState, Int2ObjectMap<IntSet> tallyToMoves)
    {
        State   simState  = fromState;
        int     nextCount = 0;
        int[]   nextMoves = new int[ Move.MAX_PER_PLY ];
        int[]   moves     = new int[ Move.MAX_PER_PLY ];
        int     nMoves    = simState.moves(moves);
        boolean moveAdded = false;

        do
        {
            int     move;
            boolean madeMove = false;

            int beforeMove = simState.tallyAllMaterial();
            for (int moveIndex = 0; moveIndex < nMoves; moveIndex++) {
                moveAdded |= autoGet(tallyToMoves, beforeMove)
                                .add(moves[ moveIndex ]);
            }

            int[] moveOrder  = MovePicker.pickRandom(nMoves);
            for (int moveIndex : moveOrder)
            {
                move = Move.apply(moves[ moveIndex ], simState);

                // generate opponent moves
                nextCount = simState.moves(nextMoves);

                if (nextCount < 0) { // if leads to mate
                    Move.unApply(move, simState);
                } else {
                    madeMove = true;
                    break;
                }
            }
            if (! madeMove) {
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        while (! simState.isDrawnBy50MovesRule());

        return moveAdded;
    }

    private static IntSet autoGet(
            Int2ObjectMap<IntSet> tallyToMoves, int materialTally) {
        IntSet moves = tallyToMoves.get( materialTally );
        if (moves == null) {
            moves = new IntOpenHashSet();
            tallyToMoves.put( materialTally, moves );
        }
        return moves;
    }
}
