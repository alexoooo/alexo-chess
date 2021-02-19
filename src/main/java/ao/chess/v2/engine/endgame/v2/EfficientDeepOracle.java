package ao.chess.v2.engine.endgame.v2;


import ao.chess.v2.engine.endgame.common.TablebaseUtils;
import ao.chess.v2.engine.endgame.tablebase.DeepMaterialOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.endgame.tablebase.NilDeepMaterialOracle;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.io.Dirs;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;


// see:
//   - https://github.com/AndyGrant/Pyrrhic/
//   - https://github.com/ljgw/syzygy-bridge
public class EfficientDeepOracle {
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(EfficientDeepOracle.class);


    //-----------------------------------------------------------------------------------------------------------------
    static final Path outDir = Dirs.get(
//            Config.workingDirectory() + "table/tablebase-v2").toPath();
//            Config.workingDirectory() + "table/tablebase-v3").toPath();
//            Config.workingDirectory() + "table/tablebase-v4").toPath();
            Config.workingDirectory() + "table/tablebase-v5").toPath();

//    public static final int pieceCount = 2;
//    public static final int pieceCount = 3;
    public static final int pieceCount = 4;
//    public static final int pieceCount = 5;


    private static final EfficientDeepOracle instance = new EfficientDeepOracle();


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        State state = State.fromFen(
//                "5k2/8/6KP/8/8/8/8/8 w - - 0 1"
//                "8/2k5/8/8/8/7K/7P/8 w - - 0 1"
//                "8/1k6/8/8/8/7K/7P/8 w - - 0 1"
//                "8/8/8/p7/k7/4K3/8/8 w - - 0 1"
//                "8/8/8/p7/k7/4K3/8/8 b - - 0 1"
//                "7k/5K2/6P1/8/8/8/8/8 b - - 0 1"
//                "7k/5K2/6P1/8/8/8/8/8 w - - 0 1"
//                "6k1/8/6K1/6P1/8/8/8/8 w - - 0 1"
//                "6k1/8/6K1/6P1/8/8/8/8 b - - 0 1"
//                "5k2/8/8/8/8/2P5/8/3K4 b - - 0 1"
//                "5k2/8/8/8/8/2P5/8/3K4 w - - 0 1"
//                "8/8/4k3/8/2PK4/8/8/8 b - - 0 1"
//                "8/8/4k3/8/2PK4/8/8/8 w - - 0 1"
//                "3k4/8/2KP4/8/8/8/8/8 b - - 0 1"
//                "3k4/8/2KP4/8/8/8/8/8 w - - 0 1"
//                "3k4/8/3KP3/8/8/8/8/8 w - - 0 1"
//                "3k4/8/3KP3/8/8/8/8/8 b - - 0 1"
//                "7k/8/6K1/6P1/8/8/8/8 w - - 0 1"
//                "7k/8/6K1/6P1/8/8/8/8 b - - 0 1"
//                "8/8/4k3/8/4K3/4P3/8/8 w - - 0 1"
//                "8/8/4k3/8/4K3/4P3/8/8 b - - 0 1"
//                "8/8/8/6p1/7k/8/6K1/8 w - - 0 1"
//                "8/8/8/6p1/7k/8/6K1/8 b - - 0 1"
//                "2k5/8/2K5/2P5/8/8/8/8 w - - 0 1"
//                "2k5/8/2K5/2P5/8/8/8/8 b - - 0 1"
//                "k7/8/K7/P7/8/8/8/8 w - - 0 1"
//                "k7/8/K7/P7/8/8/8/8 b - - 0 1"
//                "8/5k1K/7P/8/8/8/8/8 w - - 0 1"
//                "8/5k1K/7P/8/8/8/8/8 b - - 0 1"
//                "8/8/8/1p6/1k6/8/8/1K6 w - - 0 1"
//                "8/8/8/1p6/1k6/8/8/1K6 b - - 0 1"
//                "8/8/8/8/1pk5/8/8/1K6 w - - 0 1"
//                "8/8/8/8/1pk5/8/8/1K6 b - - 0 1"
//                "8/8/8/8/3pk3/8/4K3/8 w - - 0 1"
//                "8/8/8/8/3pk3/8/4K3/8 b - - 0 1"
//                "8/8/8/8/8/3pk3/8/4K3 w - - 0 1"
//                "8/8/8/8/8/3pk3/8/4K3 b - - 0 1"
//                "2k5/8/8/8/1PK5/8/8/8 w - - 0 1"
//                "2k5/8/8/8/1PK5/8/8/8 b - - 0 1"
//                "4k3/8/4KP2/8/8/8/8/8 w - - 0 1"
//                "4k3/8/4KP2/8/8/8/8/8 b - - 0 1"
//                "8/8/8/2k5/8/2K5/2P5/8 w - - 0 1"
//                "8/8/8/2k5/8/2K5/2P5/8 b - - 0 1"
//                "5k2/8/2K1P3/8/8/8/8/8 w - - 0 1"
//                "5k2/8/2K1P3/8/8/8/8/8 b - - 0 1"

//                "8/8/1k6/5K2/8/8/8/8 w - - 0 1"
//                "8/8/1R6/k4K2/8/8/8/8 b - - 0 1"

//                "8/8/8/6K1/8/8/k7/3R4 w"
//                "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"
//                "8/8/8/8/8/3k3K/7P/8 w - - 0 1"

//                "K7/8/1p3k2/8/7p/8/8/8 b - - 0 1"
//                "8/8/8/8/2q5/8/1B5K/1k6 b - - 0 1"
//                "6K1/8/8/B7/8/8/4k3/1r6 b"

                // https://syzygy-tables.info/?fen=8/8/8/1k6/8/8/8/RK6_w_-_-_0_1
//                "8/8/8/1k6/8/8/8/RK6 w - - 0 1"

                // TODO: should be draw, but shows up as win?
                "8/4k3/8/5K1P/7P/8/8/8 b - h3 0 1"

                // 4k3/8/8/5K1P/7P/8/8/8 w - - 0 1
//                "8/5k2/8/5K1P/7P/8/8/8 w - - 0 1"

//                "6K1/8/8/B7/8/1p6/4k3/1r6 w - - 0 1"
        );

        System.out.println(state);

        DeepOutcome deepOutcomeOrNull = getOrNull(state);
        if (deepOutcomeOrNull == null) {
            System.out.println("Not in tablebase");
        }
        else {
            System.out.println(deepOutcomeOrNull);

            Colour pov = state.nextToAct();
            if (deepOutcomeOrNull.winner() == pov) {
                for (int move : state.legalMoves()) {
                    int undo = Move.apply(move, state);
                    DeepOutcome deepOutcome = getOrNull(state);
                    Move.unApply(undo, state);

                    if (deepOutcome != null && deepOutcome.winner() == pov) {
                        System.out.println(Move.toString(move) + " - " + deepOutcome);
                    }
                }
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static void init()
    {
        instance.initIfRequired();
    }


    public static DeepOutcome getOrNull(State position)
    {
        if (position.pieceCount() > pieceCount) {
            return null;
        }

        return instance.see(position);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final Int2ObjectMap<DeepMaterialOracle> oracles =
            new Int2ObjectOpenHashMap<>();


    //-----------------------------------------------------------------------------------------------------------------
    private synchronized void initIfRequired()
    {
        if (! oracles.isEmpty()) {
            return;
        }

        for (int deadEndTally : TablebaseUtils.deadEndMaterialTallies()) {
            oracles.put(deadEndTally, new NilDeepMaterialOracle());
        }

        for (int count = 2; count <= pieceCount; count++) {
            int nNonKings = count - 2;
            List<Piece[]> materialPermutationsWithoutKingsByPawnsDescending =
                    TablebaseUtils.materialPermutationsWithoutKingsByPawnsDescending(nNonKings);

            logger.info("Providing {} pieces, total of {} material combinations",
                    pieceCount, materialPermutationsWithoutKingsByPawnsDescending.size());

            Stopwatch stopwatch = Stopwatch.createStarted();

            for (Piece[] nonKings : materialPermutationsWithoutKingsByPawnsDescending) {
                add( nonKings );
            }

            logger.info("Done loading, took: {}", stopwatch);
        }
    }


    private void add(Piece[] nonKings) {
        int tally = MaterialTally.tally(nonKings);

//        List<Piece> asList = Arrays.asList(nonKings);
//        if (asList.contains(Piece.BLACK_PAWN) || asList.contains(Piece.WHITE_PAWN)) {
//            List<Piece> withKings = TablebaseUtils.addKings(nonKings);
//            int tallyWithKinds = MaterialTally.tally(withKings);
//            System.out.println(asList + " - " + tallyWithKinds);
//        }

        if (oracles.containsKey(tally)) {
            return;
        }

        EfficientDeepMaterialOracle materialOracle =
                EfficientDeepMaterialOracle.computeIfRequired(nonKings, this);

        oracles.put(tally, materialOracle);
    }


    //-----------------------------------------------------------------------------------------------------------------
    synchronized DeepOutcome see(State position)
    {
        initIfRequired();

        DeepMaterialOracle oracle =
                oracles.get( position.tallyNonKings() );

        DeepOutcome outcome =
                (oracle == null)
                ? null
                : oracle.see( position );

        return (outcome == null)
                ? DeepOutcome.DRAW
                : outcome;
    }
}
