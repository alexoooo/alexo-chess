package ao.chess.v2.engine.endgame.tablebase;

import ao.chess.v2.engine.endgame.common.TablebaseUtils;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;
import ao.util.io.Dirs;
import ao.util.persist.PersistentObjects;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 10:01:26 PM
 */
public class DeepOracle
{
    //--------------------------------------------------------------------
    private static final File outDir = Dirs.get(
            Config.workingDirectory() + "table/tablebase");


    //--------------------------------------------------------------------
    public static void main(String[] args) {
        DeepOracle oracle = INSTANCE;

        State state = State.fromFen(
//                "4k3/8/8/8/8/8/4P3/4K3 w - - 0 1"

                "K7/8/1p3k2/8/7p/8/8/8 b - - 0 1"
//                "8/8/8/6K1/8/8/k7/3R4 w"
//                "8/8/8/8/8/3k3K/7P/8 w - - 0 1"
//                "8/8/8/8/2q5/8/1B5K/1k6 b - - 0 1"
//                "6K1/8/8/B7/8/8/4k3/1r6 b"
        );
        System.out.println(state);
        System.out.println(oracle.see(state));

        System.exit(0);
    }


    //--------------------------------------------------------------------
//    public static final int instancePieceCount = 3;
    public static final int instancePieceCount = 4;
//    public static final int instancePieceCount = 5;

    public static final DeepOracle INSTANCE =
            create(instancePieceCount);


    //--------------------------------------------------------------------
    public static void init() {
        // trigger static block
    }


    //--------------------------------------------------------------------
    public static DeepOracle create(int nPieces)
    {
        DeepOracle instance = new DeepOracle(nPieces);

        instance.addDeadEnds();

        int nNonKings = nPieces - 2;
        for (int n = 1; n <= nNonKings; n++) {
            instance.addPermutations(n);
        }

        return instance;
    }


    //--------------------------------------------------------------------
    private final Int2ObjectMap<DeepMaterialOracle> oracles =
            new Int2ObjectOpenHashMap<DeepMaterialOracle>();

    private final int pieceCount;


    //--------------------------------------------------------------------
    private DeepOracle(int nPieces)
    {
        pieceCount = nPieces;
    }


    private void addDeadEnds() {
        for (int deadEndTally : TablebaseUtils.deadEndMaterialTallies()) {
            oracles.put(deadEndTally, new NilDeepMaterialOracle());
        }
    }


    //--------------------------------------------------------------------
    private void addPermutations(int n) {
        for (Piece[] pieces : TablebaseUtils.materialPermutationsWithoutKingsByPawnsDescending(n)) {
            add( pieces );
        }
    }


    //--------------------------------------------------------------------
    private void add(Piece... pieces) {
        int tally = MaterialTally.tally(pieces);
        if (oracles.containsKey(tally)) return;

        File cacheFile = materialOracleFile(tally);
//        Io.display("DeepOracle: adding " + cacheFile);
        System.out.println("DeepOracle: adding " + cacheFile);

        SimpleDeepMaterialOracle materialOracle =
                PersistentObjects.retrieve( cacheFile );
        if (materialOracle == null) {
            materialOracle = new SimpleDeepMaterialOracle(
                                        this, nonKings(pieces));
            PersistentObjects.persist(materialOracle, cacheFile);
            System.out.println("Oracle persisted cache for " +
                                    Arrays.toString(pieces));
        } else {
            System.out.println("Oracle retrieved cache for " +
                                    Arrays.toString(pieces));
            
//            materialOracle.compact(nonKings(pieces));
//            PersistentObjects.persist(materialOracle, cacheFile);
        }
        oracles.put(tally, materialOracle);
    }

    private File materialOracleFile(int tally) {
        return new File(outDir, tally + ".bin");
    }

    private List<Piece> nonKings(Piece... pieces) {
        List<Piece> pieceList = new ArrayList<Piece>();
        pieceList.add( Piece.BLACK_KING );
        pieceList.add( Piece.WHITE_KING );
        pieceList.addAll( Arrays.asList(pieces) );
        return pieceList;
    }


    //--------------------------------------------------------------------
    public DeepOutcome see(State position)
    {
        if (position.pieceCount() > pieceCount) {
            return null;
        }

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