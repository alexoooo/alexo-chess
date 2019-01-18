package ao.chess.v2.engine.endgame.common;

import ao.chess.v2.engine.endgame.common.index.MinPerfectHash;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.data.Arrs;
import ao.util.pass.Traverser;

import java.util.Arrays;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 8:30:02 PM
 */
public class MaterialRetrograde
        implements Traverser<State>
{
    //--------------------------------------------------------------------
    private final int            materialTally;
    private final MinPerfectHash indexer;
    private final int[][]        precedents;


    //--------------------------------------------------------------------
    public MaterialRetrograde(
            int            allMaterialTally,
            MinPerfectHash minHash)
    {
        indexer       = minHash;
        precedents    = new int[ indexer.size() ][];
        materialTally = allMaterialTally;
    }


    //--------------------------------------------------------------------
    @Override public void traverse(State state)
    {
        int[] moves = state.legalMoves();
        if (moves == null || moves.length == 0) return;

        long parentHash = state.staticHashCode();
        for (int legalMove : moves) {
            Move.apply(legalMove, state);
            long childHash = state.staticHashCode();
            boolean materialMatches =
                    (materialTally == state.tallyAllMaterial());
            Move.unApply(legalMove, state);

            if (materialMatches) {
                add(indexer.index(childHash ),
                    indexer.index(parentHash));
            }
        }
    }


    //--------------------------------------------------------------------
    private void add(int index, int parentIndex)
    {
        int[] parents = precedents[ index ];
        if (parents == null)
        {
            precedents[index] = new int[]{ parentIndex };
        }
        else if (Arrs.indexOf(parents, parentIndex) == -1)
        {
            int[] newParents = Arrays.copyOf(
                    parents, parents.length + 1);
            newParents[ parents.length ] = parentIndex;
            precedents[index] = newParents;
        }
    }


    //--------------------------------------------------------------------
    public int[] precedents(State of) {
        return precedents( of.staticHashCode() );
    }

    public int[] precedents(long ofStaticHash) {
        return indexPrecedents( indexer.index(ofStaticHash) );
    }

    public int[] indexPrecedents(int ofIndex) {
        int[] existing = precedents[ ofIndex];
        return existing == null
               ? new int[0]
               : existing;
    }
}
