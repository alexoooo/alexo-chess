package ao.chess.v2.engine.heuristic.impl.simple;

import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.persist.PersistentObjects;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 11:39:11 AM
 */
public class SimpleWinTally implements MoveHeuristic, Serializable
{
    //--------------------------------------------------------------------
    private static final long serialVersionUID = -120528807166915154L;

    private final static Logger LOG =
            Logger.getLogger(SimpleWinTally.class);

    private static final File dir = Config.dir(
            "lookup/heuristic/simple-win-tally");

    
    //--------------------------------------------------------------------
    private final String               id;
    private final Int2ObjectMap<int[]> moveToStats;


    //--------------------------------------------------------------------
    public SimpleWinTally(String id) {
        this.id     = id;
        moveToStats = new Int2ObjectOpenHashMap<int[]>();
    }


    //--------------------------------------------------------------------
    private int[] whiteBlackTie(int move) {
        int[] winLossTie = moveToStats.get( move );
        if (winLossTie == null) {
            winLossTie = new int[3];
            moveToStats.put( move, winLossTie );
        }
        return winLossTie;
    }


    //--------------------------------------------------------------------
    @Override public double evaluate(State state, int move) {
        int[] wbt      = whiteBlackTie(move);
        int   plays    = wbt[0] + wbt[1] + wbt[2];
        double winRate =
               (plays == 0) ? 1.0 :
               (((state.nextToAct() == Colour.WHITE)
                  ? wbt[0] : wbt[1]) + 0.5 * wbt[2]) / plays;
        return Math.exp(winRate);
    }


    //--------------------------------------------------------------------
    @Override public void update(
            State fromState, int move, Outcome outcome) {
        if (outcome == Outcome.DRAW) return;

        whiteBlackTie(move)[
                outcome == Outcome.WHITE_WINS ? 0 :
                outcome == Outcome.BLACK_WINS ? 1 : 2
        ]++;
    }


    //--------------------------------------------------------------------
    public static SimpleWinTally retrieve(String id) {
        SimpleWinTally tally =
                PersistentObjects.retrieve( new File(dir, id) );
        return (tally == null)
               ? new SimpleWinTally(id)
               : tally;
    }
    
    @Override public void persist() {
        LOG.debug("persisting " + id + " with " + moveToStats.size());
        PersistentObjects.persist(this, new File(dir, id));
    }
}
