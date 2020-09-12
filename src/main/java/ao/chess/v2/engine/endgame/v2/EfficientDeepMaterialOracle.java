package ao.chess.v2.engine.endgame.v2;


import ao.chess.v2.engine.endgame.common.TablebaseUtils;
import ao.chess.v2.engine.endgame.tablebase.DeepMaterialOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import com.google.common.base.Stopwatch;
import com.google.common.collect.ImmutableList;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import it.unimi.dsi.fastutil.ints.IntSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class EfficientDeepMaterialOracle implements DeepMaterialOracle
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(EfficientDeepMaterialOracle.class);


    //-----------------------------------------------------------------------------------------------------------------
    public static EfficientDeepMaterialOracle computeIfRequired(
            Piece[] nonKings, EfficientDeepOracle oracle)
    {
        Builder builder = new Builder(nonKings);
        builder.compute(oracle);
        return new EfficientDeepMaterialOracle(builder.minHash, builder.outcomeStore);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static class Builder
    {
        //------------------------------------------------------
        private final int tally;
        private final List<Piece> withKings;
        private final EfficientMinPerfectHash minHash;
        private final EfficientOutcomeStore outcomeStore;

        private final int[] legalMoves = new int[Move.MAX_PER_PLY];
        private final int[] movesBuffer = new int[Move.MAX_PER_PLY];

        private long stateCount = 0;
        private int mateCount = 0;
        private Stopwatch mateProgressStopwatch;


        //------------------------------------------------------
        public Builder(Piece[] nonKings)
        {
            withKings = TablebaseUtils.addKings(nonKings);
            tally = MaterialTally.tally(withKings);
            minHash = new EfficientMinPerfectHash(ImmutableList.copyOf(nonKings), hashPath());
            outcomeStore = new EfficientOutcomeStore(outcomePath());
        }


        //------------------------------------------------------
        public void compute(EfficientDeepOracle oracle)
        {
            boolean minHashStored = minHash.isStored();
            boolean outcomesStored = outcomeStore.isStored();
            if (minHashStored && outcomesStored) {
                return;
            }

            logger.info("=================================================================");
            logger.info("Computing " + withKings + " - " + tally);

            if (minHashStored) {
                minHash.loadIfRequired();
            }
            else {
                minHash.computeAndStore(withKings);
            }

            Path tempStatesIndexPath = statesIndexPath();
            Path tempStatesBodyPath = statesBodyPath();
            EfficientStateMap states = EfficientStateMap.loadOrCompute(
                    minHash, withKings, tempStatesIndexPath, tempStatesBodyPath);

            Path tempRetrogradeIndexPath = retrogradeIndexPath();
            Path tempRetrogradeBodyPath = retrogradeBodyPath();
            EfficientMaterialRetrograde retro = EfficientMaterialRetrograde.loadOrCompute(
                    tally, minHash, states, tempRetrogradeIndexPath, tempRetrogradeBodyPath);

            outcomeStore.startCompute(minHash.size());

            findOutcomes(oracle, states, retro);

            outcomeStore.save();

            try {
                retro.close();
                states.close();
                Files.delete(tempStatesIndexPath);
                Files.delete(tempStatesBodyPath);
                Files.delete(tempRetrogradeIndexPath);
                Files.delete(tempRetrogradeBodyPath);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }


        //-------------------------------------------------------------------------------------------------------------
        private void findOutcomes(
                EfficientDeepOracle oracle,
                EfficientStateMap states,
                EfficientMaterialRetrograde retro
        ) {
            IntSet prevWins = new IntOpenHashSet();

            Stopwatch stopwatchInitial = Stopwatch.createStarted();
            logger.info("Looking for initial mates");

            mateProgressStopwatch = Stopwatch.createStarted();
            addImmediateMates(
                    states.states(), oracle, prevWins);

            logger.info("initial mates found, took: {}", stopwatchInitial);

            int ply = 0;

            while (! prevWins.isEmpty())
            {
                IntSet nextWins = new IntOpenHashSet();

                Stopwatch stopwatch = Stopwatch.createStarted();

                addNextImmediates(
                        states, retro,
                        prevWins, oracle, nextWins);

                logger.info("Finished ply {}, took {}", ply, stopwatch);
                ply++;

                prevWins = nextWins;
            }

            logger.info("Done, got {} | {}",
                    outcomeStore.blackWinCount(),
                    outcomeStore.whiteWinCount());
        }


        private void addNextImmediates(
                EfficientStateMap allStates,
                EfficientMaterialRetrograde retro,
                IntSet prevWins,
                EfficientDeepOracle oracle,
                IntSet nextWins)
        {
            stateCount = 0;
            mateCount = 0;

            logger.info("Tracking from {} previous wins", prevWins.size());

            mateProgressStopwatch = Stopwatch.createStarted();
            for (int prevWin : prevWins) {
                addImmediateMates(
                        allStates.states(retro.indexPrecedents( prevWin )),
                        oracle, nextWins);
            }
        }


        //--------------------------------------------------------------------
        private void addImmediateMates(
                Iterable<State> states,
                EfficientDeepOracle oracle,
                IntSet nextWins)
        {
            for (State state : states) {
                stateCount++;

                if (stateCount % 100_000_000 == 0) {
                    logger.info("Mate finding progress: {} - {}, took: {}",
                            stateCount, mateCount, mateProgressStopwatch);
                    mateProgressStopwatch = Stopwatch.createStarted();
                }

                if (isWinKnown(state)) {
                    continue;
                }

                DeepOutcome result = findImminentMate(
                        state, oracle,
                        legalMoves, movesBuffer);

                if (result != null && ! result.isDraw()) {
                    int index = minHash.index(state);
                    nextWins.add(index);

                    if (result.whiteWins()) {
                        outcomeStore.set(index,
                                TablebaseUtils.normalizePly( result.plyDistance() ));
                    }
                    else /*if (result.blackWins())*/ {
                        outcomeStore.set(index,
                                TablebaseUtils.normalizePly( -result.plyDistance() ));
                    }

                    mateCount++;
                }
            }
        }


        private boolean isWinKnown(State state) {
            long staticHash = state.staticHashCode();
            int  index      = minHash.index( staticHash );
            return outcomeStore.get( index ) != 0;
        }


        private DeepOutcome findImminentMate(
                State state, EfficientDeepOracle oracle,
                int[] legalMoves, int[] movesBuffer)
        {
            int legalMoveCount = state.legalMoves(legalMoves, movesBuffer);

            Outcome result = state.knownOutcomeOrNull(legalMoveCount);
            if (result != null && result != Outcome.DRAW) {
                return new DeepOutcome(result, 1);
            }
            else if (result == Outcome.DRAW) {
                return null;
            }

            if (legalMoveCount <= 0) {
                return null;
            }

            int ties       = 0;
            int minWinPly  = Integer.MAX_VALUE;
            int maxLossPly = Integer.MIN_VALUE;
            for (int i = 0; i < legalMoveCount; i++)
            {
                int legalMove = legalMoves[i];

                int undo = Move.apply(legalMove, state);
                long staticHash = state.staticHashCode();
                DeepOutcome imminentResult =
                        tally == state.tallyAllMaterial()
                        ? seeInProgress( staticHash )
                        : oracle.see(state);
                Move.unApply(undo, state);

                if (imminentResult != null && ! imminentResult.isDraw()) {
                    if (state.nextToAct() == imminentResult.winner()) {
                        minWinPly = Math.min(minWinPly,
                                imminentResult.plyDistance() + 1);
                    }
                    else {
                        maxLossPly = Math.max(maxLossPly,
                                imminentResult.plyDistance() + 1);
                    }
                }
                else {
                    ties++;
                }
            }

            if (minWinPly != Integer.MAX_VALUE) {
                return new DeepOutcome(
                        Outcome.wins(state.nextToAct()),
                        minWinPly);
            }
            else if (ties > 0) {
                return null;
            }
            else /*if (maxLossPly != Integer.MIN_VALUE)*/ {
                return new DeepOutcome(
                        Outcome.loses( state.nextToAct() ),
                        maxLossPly);
            }
        }


        private DeepOutcome seeInProgress(long staticHash) {
            int index = minHash.index( staticHash );
            byte outcome = outcomeStore.get(index);
            return outcome > 0
                    ? new DeepOutcome(Outcome.WHITE_WINS, outcome)
                    : outcome < 0
                    ? new DeepOutcome(Outcome.BLACK_WINS, -outcome)
                    : null;
        }


        //------------------------------------------------------
        private Path hashPath() {
            return EfficientDeepOracle.outDir.resolve(tally + "_hash.bin");
        }

        private Path outcomePath() {
            return EfficientDeepOracle.outDir.resolve(tally + "_outcome.bin");
        }

        private Path statesIndexPath() {
            return EfficientDeepOracle.outDir.resolve(tally + "_state_index.bin");
        }

        private Path statesBodyPath() {
            return EfficientDeepOracle.outDir.resolve(tally + "_state_body.bin");
        }

        private Path retrogradeIndexPath() {
            return EfficientDeepOracle.outDir.resolve(tally + "_retro_index.bin");
        }


        private Path retrogradeBodyPath() {
            return EfficientDeepOracle.outDir.resolve(tally + "_retro_body.bin");
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final EfficientMinPerfectHash minHash;
    private final EfficientOutcomeStore outcomeStore;


    //-----------------------------------------------------------------------------------------------------------------
    public EfficientDeepMaterialOracle(
            EfficientMinPerfectHash minHash,
            EfficientOutcomeStore outcomeStore)
    {
        this.minHash = minHash;
        this.outcomeStore = outcomeStore;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public DeepOutcome see(long staticHash) {
        int index = minHash.index( staticHash );
        byte outcome = outcomeStore.get(index);
        return outcome > 0
                ? new DeepOutcome(Outcome.WHITE_WINS, outcome)
                : outcome < 0
                ? new DeepOutcome(Outcome.BLACK_WINS, -outcome)
                : null;
    }


    @Override
    public DeepOutcome see(State state) {
        return see(state.staticHashCode());
    }
}
