package ao.chess.v2.engine.endgame.common.index;

import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;
import ao.chess.v2.engine.endgame.common.PositionTraverser;
import it.unimi.dsi.sux4j.mph.MinimalPerfectHashFunction;

import java.io.Serializable;
import java.util.List;

/**
 * User: alex
 * Date: 18-Oct-2009
 * Time: 12:04:50 PM
 */
public class MinPerfectHash implements Serializable
{
    //--------------------------------------------------------------------
    private final MinimalPerfectHashFunction<String> hash;


    //--------------------------------------------------------------------
    public MinPerfectHash(List<Piece> material)
    {
        MinimalHashBuilder minHash = new MinimalHashBuilder();

        new PositionTraverser().traverse(
                material, minHash);

        hash = minHash.hash();
    }


    //--------------------------------------------------------------------
    public int index(long staticHash) {
        return (int) hash.getLong(
                MinimalHashBuilder.encode(
                        staticHash));
    }

    public int index(State state) {
        return index( state.staticHashCode() );
    }

    public int size() {
        return hash.size();
    }
}
