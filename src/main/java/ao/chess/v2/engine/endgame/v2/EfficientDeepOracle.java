package ao.chess.v2.engine.endgame.v2;


import ao.chess.v2.engine.endgame.common.TablebaseUtils;
import ao.chess.v2.engine.endgame.tablebase.DeepMaterialOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.endgame.tablebase.NilDeepMaterialOracle;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;
import ao.util.io.Dirs;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.List;


public class EfficientDeepOracle {
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(EfficientDeepOracle.class);


    //-----------------------------------------------------------------------------------------------------------------
    static final Path outDir = Dirs.get(
            Config.workingDirectory() + "table/tablebase-v2").toPath();

//    public static final int pieceCount = 3;
//    public static final int pieceCount = 4;
    public static final int pieceCount = 5;


    private static final EfficientDeepOracle instance = new EfficientDeepOracle();


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        State state = State.fromFen(
//                "8/8/8/6K1/8/8/k7/3R4 w"
//                "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"

//                "K7/8/1p3k2/8/7p/8/8/8 b - - 0 1"
//                "8/8/8/8/8/3k3K/7P/8 w - - 0 1"
//                "8/8/8/8/2q5/8/1B5K/1k6 b - - 0 1"
//                "6K1/8/8/B7/8/8/4k3/1r6 b"

                "6K1/8/8/B7/8/1p6/4k3/1r6 w - - 0 1"
        );

        System.out.println(state);
        System.out.println(getOrNull(state));
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

        int nNonKings = pieceCount - 2;
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


    private void add(Piece[] nonKings) {
        int tally = MaterialTally.tally(nonKings);
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
