package ao.chess.v2.engine.endgame.bitbase;

import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.data.Arrs;
import ao.util.data.AutovivifiedList;
import ao.util.io.Dirs;
import ao.util.math.stats.Exhauster;
import ao.util.misc.Factories;
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
public class BitOracle
{
    //--------------------------------------------------------------------
    private static final File outDir = Dirs.get(
            Config.workingDirectory() + "table/bitbase");


    //--------------------------------------------------------------------
    public static void main(String[] args) {
        BitOracle oracle = new BitOracle(3);

        State state = State.fromFen("8/8/2k5/8/7P/3K4/8/8 w");
        System.out.println(state);
        System.out.println(oracle.see(state));
    }


    //--------------------------------------------------------------------
    private final Int2ObjectMap<BitMaterialOracle> oracles =
            new Int2ObjectOpenHashMap<BitMaterialOracle>();

    private final int                           pieceCount;


    //--------------------------------------------------------------------
    public BitOracle(int nPieces)
    {
        pieceCount = nPieces;

        addDeadEnds();

        int nNonKings = nPieces - 2;
        for (int n = 1; n <= nNonKings; n++) {
            addPermutations(n);
        }
    }

    private void addDeadEnds() {
        for (Piece[] allDraws : new Piece[][]{
                {}, {Piece.WHITE_KNIGHT}, {Piece.WHITE_BISHOP},
                    {Piece.BLACK_KNIGHT}, {Piece.BLACK_BISHOP}}) {
            oracles.put(MaterialTally.tally(allDraws),
                        new NilBitMaterialOracle());
        }
    }


    //--------------------------------------------------------------------
    private void addPermutations(int n) {
        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
        List<List<Piece[]>> byPawnCount =
                new AutovivifiedList<List<Piece[]>>(
                        Factories.<Piece[]>newArrayList());

        for (Piece[] exhaustiveCombo :
                new Exhauster<Piece>(Piece.VALUES, n)) {
            if (hasKing(exhaustiveCombo)) continue;
            byPawnCount.get(
                    pawnCount(exhaustiveCombo)
            ).add( exhaustiveCombo );
        }
        
        for (List<Piece[]> pieceLists : byPawnCount) {
            for (Piece[] pieces : pieceLists) {
                add( pieces );
            }
        }
    }


    //--------------------------------------------------------------------
    private boolean hasKing(Piece[] pieces) {
        return Arrs.indexOf(pieces, Piece.WHITE_KING) != -1 ||
                Arrs.indexOf(pieces, Piece.BLACK_KING) != -1;
    }

    private int pawnCount(Piece[] pieces) {
        int count = 0;
        for (Piece p : pieces) {
            if (p.figure() == Figure.PAWN) {
                count++;
            }
        }
        return count;
    }


    //--------------------------------------------------------------------
    private void add(Piece... pieces) {
        int tally = MaterialTally.tally(pieces);
        if (oracles.containsKey(tally)) return;

        File           cacheFile      = materialOracleFile(tally);
        FastBitMaterialOracle materialOracle =
                PersistentObjects.retrieve( cacheFile );
        if (materialOracle == null) {
            materialOracle =
                    new FastBitMaterialOracle(this, nonKings(pieces));
            PersistentObjects.persist(materialOracle, cacheFile);
            System.out.println("Oracle persisted cache for " +
                                    Arrays.toString(pieces));
        } else {
            System.out.println("Oracle retrieved cache for " +
                                    Arrays.toString(pieces));
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
    public Outcome see(State position)
    {
        if (position.pieceCount() > pieceCount) return null;

        BitMaterialOracle oracle =
                oracles.get( position.tallyNonKings() );
        Outcome outcome = (oracle == null)
                          ? null : oracle.see( position );
        return (outcome == null) ? Outcome.DRAW : outcome;
    }
}
