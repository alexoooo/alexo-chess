package ao.chess.v2.engine.endgame.test;

import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;
import ao.util.pass.Traverser;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 14-Oct-2009
 * Time: 12:25:59 AM
 */
public class HashCollide
        implements Traverser<State>
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {

        HashCollide visitor = new HashCollide();

        feed(visitor);

        for (Figure figure : Figure.VALUES) {
            if (figure == Figure.KING) continue;

            for (Colour colour : Colour.VALUES) {
                feed(visitor, Piece.valueOf(colour, figure));
            }
        }
        
        visitor.displayReport();
    }

    private static void feed(
            Traverser<State> visitor,
            Piece...         nonKingPieces)
    {
        List<Piece> pieces = new ArrayList<Piece>(Arrays.asList(
                Piece.WHITE_KING, Piece.BLACK_KING));

        pieces.addAll(Arrays.asList(nonKingPieces));

        long before = System.currentTimeMillis();
        System.out.println("feeding: " + pieces);
        new PositionTraverser().traverse(pieces, visitor);
        System.out.println("took: " +
                (System.currentTimeMillis() - before));
    }


    //--------------------------------------------------------------------
    private final Long2ObjectMap<State> byHash =
            new Long2ObjectOpenHashMap<State>();

    private int count      = 0;
    private int collisions = 0;

    
    //--------------------------------------------------------------------
    @Override public void traverse(State state)
    {
//        long hash = state.longHashCode();
        long hash = state.staticHashCode();

        State existing = byHash.get( hash );
        if (existing == null) {
            byHash.put( hash, state );
        } else if (! existing.equals( state )) {
            System.out.println("collision found for " + state);
            collisions++;
        }
        count++;
    }


    //--------------------------------------------------------------------
    private void displayReport() {
        System.out.println(collisions + " of " + count);
    }
}
