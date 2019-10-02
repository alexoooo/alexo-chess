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
    private static final int       picsPerN = 16;

    private static final long[]    lastPick =
            new long[ Move.MAX_PER_PLY ];
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
    public static int[] pickRandom(int nMoves)
    {
        if (nMoves == -1) {
            System.out.println("buh");
        }

        return allPicks[ nMoves                               ]
                       [ (int)(lastPick[nMoves]++ % picsPerN) ];
    }


    //--------------------------------------------------------------------
    private static void shuffle(int[] vals)
    {
        List<Integer> l = new ArrayList<Integer>();
        for (int v : vals) l.add( v );
        Collections.shuffle(l);
        for (int i = 0; i < vals.length; i++) {
            vals[ i ] = l.get( i );
        }
    }
}
