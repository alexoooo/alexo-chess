package ao.chess.v2.engine.endgame.bitbase;

import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.engine.endgame.common.index.MinPerfectHash;
import ao.chess.v2.engine.endgame.common.StateMap;
import ao.chess.v2.engine.endgame.common.MaterialRetrograde;
import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.util.time.Stopwatch;
import it.unimi.dsi.bits.BitVector;
import it.unimi.dsi.bits.LongArrayBitVector;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;

/**
 * User: aostrovsky
 * Date: 13-Oct-2009
 * Time: 11:15:38 PM
 */
public class FastBitMaterialOracle implements BitMaterialOracle
{
    //--------------------------------------------------------------------
    private final MinPerfectHash indexer;
    private final LongArrayBitVector blackWins;
    private final LongArrayBitVector whiteWins;


    //--------------------------------------------------------------------
    public FastBitMaterialOracle(
            final BitOracle oracle,
            final List<Piece> material)
    {
        System.out.println("Computing MaterialOracle for " + material);
        Stopwatch timer = new Stopwatch();

        int materialTally = MaterialTally.tally(material);
        indexer           = new MinPerfectHash(material);
        
        System.out.println("computed perfect hash, took " + timer);
        timer = new Stopwatch();

        StateMap states = new StateMap(indexer);
        new PositionTraverser().traverse(
                material, states);

        System.out.println("filled state map " +
                states.size() + ", took " + timer);
        timer = new Stopwatch();

        MaterialRetrograde retro =
                new MaterialRetrograde(materialTally, indexer);
        states.traverse(retro);

        System.out.println("retrograde analysis done, took " + timer);
        timer = new Stopwatch();

        // initial mates
        blackWins = LongArrayBitVector.ofLength( states.size() );
        whiteWins = LongArrayBitVector.ofLength( states.size() );

        IntSet prevWhiteWins = new IntOpenHashSet();
        IntSet prevBlackWins = new IntOpenHashSet();

        addImmediateMates(
                states.states(), oracle, materialTally,
                prevWhiteWins, prevBlackWins);

        int ply = 1;
        System.out.println("initial mates found, took " + timer);
        timer = new Stopwatch();

        while (! prevWhiteWins.isEmpty() ||
               ! prevBlackWins.isEmpty())
        {
            addAll( whiteWins, prevWhiteWins );
            addAll( blackWins, prevBlackWins );

            IntSet nextWhiteWins = new IntOpenHashSet();
            IntSet nextBlackWins = new IntOpenHashSet();

            addNextImmediates(states, materialTally, retro,
                    prevWhiteWins, oracle, nextWhiteWins, nextBlackWins);
            addNextImmediates(states, materialTally, retro,
                    prevBlackWins, oracle, nextWhiteWins, nextBlackWins);

            System.out.println(
                    "finished ply " + (ply++) + ", took " + timer);
            timer = new Stopwatch();

            prevWhiteWins = nextWhiteWins;
            prevBlackWins = nextBlackWins;
        }

        System.out.println("finalizing & trimming");

        blackWins.trim();
        whiteWins.trim();

        System.out.println("done, got " +
                blackWins.count() + " | " +
                whiteWins.count() + " | " +
                states.size());
    }

    private void addAll(BitVector to, IntSet indexes) {
        for (int index : indexes) {
            to.set( index );
        }
    }


    //--------------------------------------------------------------------
    private void addNextImmediates(
            StateMap allStates,
            int                materialTally,
            MaterialRetrograde retro,
            IntSet             prevWins,
            BitOracle oracle,
            IntSet             nextWhiteWins,
            IntSet             nextBlackWins)
    {
        for (int prevWin : prevWins) {
            addImmediateMates(
                    allStates.states(retro.indexPrecedents( prevWin )),
                    oracle, materialTally, nextWhiteWins, nextBlackWins);
        }
    }


    //--------------------------------------------------------------------
    private void addImmediateMates(
            Iterable<State> states,
            BitOracle oracle,
            int             materialTally,
            IntSet          nextWhiteWins,
            IntSet          nextBlackWins)
    {
        for (State state : states) {
            if (! isUnknown(state)) continue;

            Outcome result = findImminentMate(
                    state, materialTally, oracle);
            if (result == Outcome.WHITE_WINS) {
                nextWhiteWins.add(
                        indexer.index(state.staticHashCode()) );
            } else if (result == Outcome.BLACK_WINS) {
                nextBlackWins.add(
                        indexer.index(state.staticHashCode()) );
            }
        }
    }

    private boolean isUnknown(State state) {
        long staticHash = state.staticHashCode();
        int  index      = indexer.index( staticHash );
        return ! (whiteWins.get(index) ||
                  blackWins.get(index));
    }


    //--------------------------------------------------------------------
    private Outcome findImminentMate(
            State state, int materialTally, BitOracle oracle)
    {
        Outcome result = state.knownOutcome();
        if (result != null && result != Outcome.DRAW) {
            return result;
        }

        int legalMoves[] = state.legalMoves();
        if (legalMoves == null) return null;

        int ties = 0, losses = 0;
        for (int legalMove : legalMoves)
        {
            Move.apply(legalMove, state);

            long    staticHash     = state.staticHashCode();
            Outcome imminentResult =
                    //allStates.containsState( staticHash )
                    materialTally == state.tallyAllMaterial()
                    ? see( staticHash )
                    : oracle.see(state);
            Move.unApply(legalMove, state);

            if (imminentResult != null &&
                    imminentResult != Outcome.DRAW) {
                if (state.nextToAct() == imminentResult.winner()) {
                    return imminentResult;
                } else {
                    losses++;
                }
            } else {
                ties++;
            }
        }

        return   ties   > 0 ? null
               : losses > 0 ? Outcome.loses( state.nextToAct() )
                            : null;
    }


    //--------------------------------------------------------------------
    public Outcome see(long staticHash) {
        int index = indexer.index( staticHash );
        return whiteWins.get(index)
               ? Outcome.WHITE_WINS
               : blackWins.get(index)
                 ? Outcome.BLACK_WINS
                 : null;
    }

    @Override public Outcome see(State state) {
        return see( state.staticHashCode() );
    }
}
