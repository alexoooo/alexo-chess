package ao.chess.v2.engine.endgame.tablebase;

import ao.chess.v2.engine.endgame.common.MaterialRetrograde;
import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.engine.endgame.common.StateMap;
import ao.chess.v2.engine.endgame.common.index.MinPerfectHash;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.time.Stopwatch;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;

import java.util.List;

/**
 * User: aostrovsky
 * Date: 13-Oct-2009
 * Time: 11:15:38 PM
 */
public class SimpleDeepMaterialOracle implements DeepMaterialOracle
{
    //--------------------------------------------------------------------
    // TODO: is this necessary, or can just use Long2ByteMap?
    private final MinPerfectHash indexer;
    private final byte[]         outcomes;


    //--------------------------------------------------------------------
    public SimpleDeepMaterialOracle(
            final DeepOracle oracle,
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
        outcomes = new byte[ states.size() ];

        IntSet prevWins = new IntOpenHashSet();

        addImmediateMates(
                states.states(), oracle, materialTally, prevWins);

        int ply = 0;
        System.out.println("initial mates found, took " + timer);
        timer = new Stopwatch();

        while (! prevWins.isEmpty())
        {
            IntSet nextWins = new IntOpenHashSet();

            addNextImmediates(states, materialTally, retro,
                                prevWins, oracle, nextWins);

            System.out.println(
                    "finished ply " + (ply++) + ", took " + timer);
            timer = new Stopwatch();

            prevWins = nextWins;
        }

        // TODO: does this work?
        // compact(material);

        System.out.println("done, got " +
                blackWinCount() + " | " +
                whiteWinCount() + " | " +
                states.size());
    }

    private byte normalizePly(int relativePlyDistance) {
        if (relativePlyDistance > 0) {
            return (byte) Math.min(relativePlyDistance, Byte.MAX_VALUE);
        }
        else if (relativePlyDistance < 0) {
            return (byte) Math.max(relativePlyDistance, Byte.MIN_VALUE);
        }
        return 0;
    }


    //--------------------------------------------------------------------
    public void compact(List<Piece> material) {
        int       round = 0;
        Stopwatch timer = new Stopwatch();
        while (compactRound(material)) {
            System.out.println("Compaction round " + (++round) +
                    " done, took " + timer);
            timer = new Stopwatch();
        }
    }

    private boolean compactRound(List<Piece> material) {
        final boolean[] wasChanged = {false};
        new PositionTraverser().traverse(material, state -> {
            int index = indexer.index(state);
            int ply   = outcomes[ index ];

            if (ply == 0) {
                return;
            }
            int[] legalMoves = state.legalMoves();
            if (legalMoves == null) {
                return;
            }

            int minWin  = Byte.MAX_VALUE;
            int maxLoss = Byte.MIN_VALUE;
            for (int legalMove : legalMoves) {
                Move.apply(legalMove, state);
                int childIndex = indexer.index(state);
                Move.unApply(legalMove, state);

                byte childOutcome = outcomes[ childIndex ];
                if (ply > 0 && childOutcome > 0) {
                    minWin  = (byte) Math.min(
                                minWin, childOutcome);
                }
                else if (ply < 0 && childOutcome < 0) {
                    maxLoss = (byte) Math.max(
                                maxLoss, -childOutcome);
                }
            }

            if (ply > 0 && minWin < ply) {
                outcomes[index] = normalizePly(minWin);
                wasChanged[0]   = true;
            } else if (ply < 0 && maxLoss > -ply) {
                outcomes[index] = normalizePly(-maxLoss);
                wasChanged[0]   = true;
            }
        });
        return wasChanged[0];
    }


    //--------------------------------------------------------------------
    private void addNextImmediates(
            StateMap           allStates,
            int                materialTally,
            MaterialRetrograde retro,
            IntSet             prevWins,
            DeepOracle         oracle,
            IntSet             nextWins)
    {
        for (int prevWin : prevWins) {
            addImmediateMates(
                    allStates.states(retro.indexPrecedents( prevWin )),
                    oracle, materialTally, nextWins);
        }
    }


    //--------------------------------------------------------------------
    private void addImmediateMates(
            Iterable<State> states,
            DeepOracle      oracle,
            int             materialTally,
            IntSet          nextWins)
    {
        for (State state : states) {
            if (isWinKnown(state)) continue;

            DeepOutcome result = findImminentMate(
                    state, materialTally, oracle);
            if (result != null && ! result.isDraw()) {
                int index = indexer.index(state);
                nextWins.add(index);

                if (result.whiteWins()) {
                   outcomes[ index ] =
                            normalizePly( result.plyDistance() );
                } else /*if (result.blackWins())*/ {
                    outcomes[ index ] =
                            normalizePly( -result.plyDistance() );
                }
            }
        }
    }

    private boolean isWinKnown(State state) {
        long staticHash = state.staticHashCode();
        int  index      = indexer.index( staticHash );
        return outcomes[ index ] != 0;
    }


    //--------------------------------------------------------------------
    private DeepOutcome findImminentMate(
            State state, int materialTally, DeepOracle oracle)
    {
        Outcome result = state.knownOutcomeOrNull();
        if (result != null && result != Outcome.DRAW) {
            return new DeepOutcome(result, 1);
        } else if (result == Outcome.DRAW) {
            return null;
        }

        int legalMoves[] = state.legalMoves();
        if (legalMoves == null) return null;

        int ties       = 0;
        int minWinPly  = Integer.MAX_VALUE;
        int maxLossPly = Integer.MIN_VALUE;
        for (int legalMove : legalMoves)
        {
            Move.apply(legalMove, state);
            long        staticHash     = state.staticHashCode();
            DeepOutcome imminentResult =
                    materialTally == state.tallyAllMaterial()
                    ? see( staticHash )
                    : oracle.see(state);
            Move.unApply(legalMove, state);

            if (imminentResult != null &&
                    ! imminentResult.isDraw()) {
                if (state.nextToAct() ==
                        imminentResult.winner()) {
                    minWinPly = Math.min(minWinPly,
                            imminentResult.plyDistance() + 1);
                } else {
                    maxLossPly = Math.max(maxLossPly,
                            imminentResult.plyDistance() + 1);
                }
            } else {
                ties++;
            }
        }

        if (minWinPly != Integer.MAX_VALUE) {
            return new DeepOutcome(
                    Outcome.wins(state.nextToAct()),
                    minWinPly);
        } else if (ties > 0) {
            return null;
        } else /*if (maxLossPly != Integer.MIN_VALUE)*/ {
            return new DeepOutcome(
                    Outcome.loses( state.nextToAct() ),
                    maxLossPly);
        }
    }


    //--------------------------------------------------------------------
    private int whiteWinCount() {
        int count = 0;
        for (byte outcome : outcomes) {
            if (outcome > 0) {
                count++;
            }
        }
        return count;
    }

    private int blackWinCount() {
        int count = 0;
        for (byte outcome : outcomes) {
            if (outcome < 0) {
                count++;
            }
        }
        return count;
    }


    //--------------------------------------------------------------------
    public DeepOutcome see(long staticHash) {
        int index   = indexer.index( staticHash );
        int outcome = outcomes[index];
        return outcome > 0
               ? new DeepOutcome(Outcome.WHITE_WINS, outcome)
               : outcome < 0
                 ? new DeepOutcome(Outcome.BLACK_WINS, -outcome)
                 : null;
    }

    @Override public DeepOutcome see(State state) {
        return see( state.staticHashCode() );
    }
}