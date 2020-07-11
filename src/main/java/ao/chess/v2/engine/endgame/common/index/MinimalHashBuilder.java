package ao.chess.v2.engine.endgame.common.index;

import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;
import ao.util.pass.Traverser;
import it.unimi.dsi.bits.HuTuckerTransformationStrategy;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.sux4j.mph.GOVMinimalPerfectHashFunction;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.IOException;
import java.util.Arrays;
import java.util.Iterator;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 12:53:37 AM
 */
public class MinimalHashBuilder
    implements Traverser<State>,
               Iterable<String>
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        MinimalHashBuilder minHash = new MinimalHashBuilder();

        new PositionTraverser().traverse(
                Arrays.asList(
                        Piece.WHITE_KING,
                        Piece.BLACK_KING),
                minHash);

        minHash.hash();
        minHash.displayReport();
    }


    //--------------------------------------------------------------------
    private final LongSet states =
            new LongOpenHashSet();

    private long count = 0;


    //--------------------------------------------------------------------
    @Override public void traverse(State state) {
        states.add( state.staticHashCode() );
        count++;
    }


    //--------------------------------------------------------------------
    public GOVMinimalPerfectHashFunction<String> hash()
    {
        try {
            return new GOVMinimalPerfectHashFunction.Builder<String>()
                    .keys(this)
                    .transform(new HuTuckerTransformationStrategy(this, true))
                    .build();
        } catch (IOException e) {
            throw new Error( e );
        }
    }


    //--------------------------------------------------------------------
    @Override public Iterator<String> iterator() {
        final LongIterator itr = states.iterator();
        return new Iterator<>() {
            @Override
            public boolean hasNext() {
                return itr.hasNext();
            }

            @Override
            public String next() {
                return encode( itr.nextLong() );
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }

    public static String encode(long staticHash)
    {
//        return new String(new char[]{
//                (char) (staticHash >> 48),
//                (char) (staticHash >> 32),
//                (char) (staticHash >> 16),
//                (char)  staticHash});
        return String.valueOf( staticHash );
    }


    //--------------------------------------------------------------------
    private void displayReport() {
        System.out.println("count\t" + count);
    }
}
