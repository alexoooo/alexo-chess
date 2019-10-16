package ao.chess.v2.data;

import ao.chess.v2.state.Move;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * User: alexo
 * Date: Feb 26, 2009
 * Time: 1:00:52 AM
 */
public class MovePicker
{
    //--------------------------------------------------------------------
    private MovePicker() {}


    //--------------------------------------------------------------------
    private static final int picsPerN = 256;

//    private static final long[] lastPick =
//            new long[ Move.MAX_PER_PLY ];
//    private static AtomicInteger nextIndex = new AtomicInteger();
    private static volatile long nextIndex = 0;

    private static final int[][][] allPicks =
            new int [ Move.MAX_PER_PLY ][][];

    static
    {
        for (int nMoves = 0; nMoves < Move.MAX_PER_PLY; nMoves++) {
            int[][] availPicks = new int[ picsPerN ][ nMoves ];

            for (int i = 0; i < picsPerN; i++) {
                int[] picks = availPicks[ i ];
                for (int j = 0; j < picks.length; j++) picks[j] = j;
                shuffle(picks);
            }
            allPicks[ nMoves ] = availPicks;
        }
    }


    //--------------------------------------------------------------------
    public static void init() {
        // trigger static block
    }


    //--------------------------------------------------------------------
    @SuppressWarnings("NonAtomicOperationOnVolatileField")
    public static int[] pickRandom(int nMoves)
    {
        return allPicks[ nMoves                 ]
                       [ (int) (nextIndex++ % picsPerN) ];
    }


    //--------------------------------------------------------------------
    private static void shuffle(int[] vals)
    {
        List<Integer> l = new ArrayList<>();
        for (int v : vals) {
            l.add( v );
        }
        Collections.shuffle(l);
        for (int i = 0; i < vals.length; i++) {
            vals[ i ] = l.get( i );
        }
    }
}
