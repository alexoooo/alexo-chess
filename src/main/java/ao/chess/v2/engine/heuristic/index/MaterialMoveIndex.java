package ao.chess.v2.engine.heuristic.index;

import ao.chess.v2.engine.run.Config;
import ao.chess.v2.state.State;
import ao.util.persist.PersistentObjects;
import it.unimi.dsi.bits.HuTuckerTransformationStrategy;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import it.unimi.dsi.fastutil.ints.IntSet;
import it.unimi.dsi.fastutil.objects.ObjectIterator;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 2:18:00 PM
 */
public class MaterialMoveIndex
{
    //--------------------------------------------------------------------
    private static final File DIR = Config.dir(
            "lookup/heuristic");

    private static final MinimalPerfectHashFunction<CharSequence>
            HASH = retrieveOrComputeIndex();

    private MaterialMoveIndex() {}


    //--------------------------------------------------------------------
    public static void main(String[] args) {
        indexOf(State.initial().tallyAllMaterial(),
                State.initial().legalMoves()[0]);
    }


    //--------------------------------------------------------------------
    private static MinimalPerfectHashFunction<CharSequence>
            retrieveOrComputeIndex() {
        File storeFile = new File(DIR, "index");
        MinimalPerfectHashFunction<CharSequence> hash =
                        PersistentObjects.retrieve( storeFile );
        if (hash != null) return hash;

        Iterable<CharSequence> combos = new Iterable<CharSequence>() {
            @Override public Iterator<CharSequence> iterator() {
                return iterateMaterialMoves();
            }
        };

        try {
            hash = new MinimalPerfectHashFunction<CharSequence>(
                        combos, new HuTuckerTransformationStrategy(
                                        combos, true));
            PersistentObjects.persist(hash, storeFile);
            return hash;
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }


    //--------------------------------------------------------------------
    private static Iterator<CharSequence> iterateMaterialMoves()
    {
        Int2ObjectMap<IntSet> tallyToMoves =
                new Int2ObjectOpenHashMap<IntSet>();

        MaterialMovePopulation.populate( tallyToMoves );

        final ObjectIterator<Int2ObjectMap.Entry<IntSet>>
                itr = tallyToMoves.int2ObjectEntrySet().iterator();
        return new Iterator<CharSequence>() {
            private IntIterator subItr;
            private int         nextTally;

            private void advanceTally() {
                Int2ObjectMap.Entry<IntSet> e = itr.next();
                nextTally = e.getIntKey();
                subItr    = e.getValue().iterator();
            }

            @Override public boolean hasNext() {
                return itr.hasNext() || subItr.hasNext();
            }

            @Override public CharSequence next() {
                if (subItr.hasNext()) {
                    return toChars(nextTally, subItr.nextInt());
                } else {
                    advanceTally();
                    return next();
                }
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    //--------------------------------------------------------------------
    private static CharSequence toChars(int materialTally, int move)
    {
        return new ArrayCharSequence(new char[]{
                (char)  (materialTally         & 0xFF),
                (char) ((materialTally >>> 16) & 0xFF),

                (char)  (move         & 0xFF),
                (char) ((move >>> 16) & 0xFF)
        });
    }


    //--------------------------------------------------------------------
    public static int indexOf(int materialTally, int move)
    {
        return (int) HASH.getLong(toChars(materialTally, move));
    }
}
