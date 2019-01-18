package ao.chess.v2.engine.heuristic.impl.simple;

import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.persist.PersistentObjects;
import ao.util.time.Stopwatch;
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
public class DoubleWinTally implements MoveHeuristic, Serializable
{
    //--------------------------------------------------------------------
    private static final long serialVersionUID = -120528807166915154L;

    private final static Logger LOG =
            Logger.getLogger(DoubleWinTally.class);

    private static final File dir = Config.dir(
            "lookup/heuristic/double-win-tally");


    //--------------------------------------------------------------------
    private final String                              id;
    private final Int2ObjectMap<Int2ObjectMap<int[]>> stateToMoveToStats;


    //--------------------------------------------------------------------
    public DoubleWinTally(String id) {
        this.id            = id;
        stateToMoveToStats =
                new Int2ObjectOpenHashMap<Int2ObjectMap<int[]>>();
    }


    //--------------------------------------------------------------------
    private int[] whiteBlackTie(State state, int move) {
        return lookup(lookup(state), move);
    }

    private Int2ObjectMap<int[]> lookup(State state) {
        int                  tally  = state.tallyAllMaterial();
        Int2ObjectMap<int[]> subMap = stateToMoveToStats.get(tally);
        if (subMap == null) {
            subMap = new Int2ObjectOpenHashMap<int[]>();
            stateToMoveToStats.put(tally, subMap);
        }
        return subMap;
    }

    private int[] lookup(Int2ObjectMap<int[]> in, int move) {
        int[] winLossTie = in.get( move );
        if (winLossTie == null) {
            winLossTie = new int[3];
            in.put( move, winLossTie );
        }
        return winLossTie;
    }


    //--------------------------------------------------------------------
    @Override public double evaluate(State state, int move) {
        int[] wbt      = whiteBlackTie(state, move);
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
//        int[] wbt = whiteBlackTie(fromState, move);

        whiteBlackTie(fromState, move)[
                outcome == Outcome.WHITE_WINS ? 0 :
                outcome == Outcome.BLACK_WINS ? 1 : 2
        ]++;
    }


    //--------------------------------------------------------------------
    public static DoubleWinTally retrieve(String id) {
        DoubleWinTally tally =
                PersistentObjects.retrieve( new File(dir, id) );
        return (tally == null)
               ? new DoubleWinTally(id)
               : tally;
    }

    @Override public void persist() {
        int totalCount = 0;
        for (Int2ObjectMap<int[]> v : stateToMoveToStats.values()) {
            totalCount += v.size();
        }

        Stopwatch timer = new Stopwatch();
        PersistentObjects.persist(this, new File(dir, id));
        LOG.debug("persisted " + id +
                    " with " + stateToMoveToStats.size() +
                    " totalling " + totalCount +
                    " took " + timer);
    }


    //--------------------------------------------------------------------
    @Override
    public String toString()
    {
        int vCount = 0;
        for (Int2ObjectMap<int[]> v : stateToMoveToStats.values())
        {
            vCount += v.size();
        }

        return "DoubleWinTally " + stateToMoveToStats.size() +
                    " -> " + vCount;
    }
}