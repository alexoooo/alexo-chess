package ao.chess.v2.engine.endgame.common;

import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.util.data.Arrs;
import ao.util.data.AutovivifiedList;
import ao.util.math.stats.Exhauster;
import ao.util.misc.Factories;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


public enum TablebaseUtils {;
    //--------------------------------------------------------------------
    public static List<Piece> addKings(Piece... pieces) {
        List<Piece> pieceList = new ArrayList<Piece>();
        pieceList.add( Piece.BLACK_KING );
        pieceList.add( Piece.WHITE_KING );
        pieceList.addAll( Arrays.asList(pieces) );
        return pieceList;
    }


    public static boolean hasKing(Piece[] pieces) {
        return Arrs.indexOf(pieces, Piece.WHITE_KING) != -1 ||
                Arrs.indexOf(pieces, Piece.BLACK_KING) != -1;
    }


    public static int pawnCount(Piece[] pieces) {
        int count = 0;
        for (Piece p : pieces) {
            if (p.figure() == Figure.PAWN) {
                count++;
            }
        }
        return count;
    }


    public static int[] deadEndMaterialTallies() {
        return new int[] {
                MaterialTally.tally(),
                MaterialTally.tally(Piece.WHITE_KNIGHT),
                MaterialTally.tally(Piece.WHITE_BISHOP),
                MaterialTally.tally(Piece.BLACK_KNIGHT),
                MaterialTally.tally(Piece.BLACK_BISHOP)
        };
    }


    public static List<Piece[]> materialPermutationsWithoutKingsByPawnsDescending(int nonKingCount) {
        @SuppressWarnings({"MismatchedQueryAndUpdateOfCollection"})
        List<List<Piece[]>> byPawnCount =
                new AutovivifiedList<>(Factories.newArrayList());

        for (Piece[] exhaustiveCombo : new Exhauster<>(Piece.VALUES, nonKingCount)) {
            if (hasKing(exhaustiveCombo)) {
                continue;
            }

            byPawnCount.get(
                    pawnCount(exhaustiveCombo)
            ).add( exhaustiveCombo );
        }

        List<Piece[]> inOrder = new ArrayList<>();
        for (List<Piece[]> pieceLists : byPawnCount) {
            inOrder.addAll(pieceLists);
        }
        return inOrder;
    }


    public static byte normalizePly(int relativePlyDistance) {
        if (relativePlyDistance > 0) {
            return (byte) Math.min(relativePlyDistance, Byte.MAX_VALUE);
        }
        else if (relativePlyDistance < 0) {
            return (byte) Math.max(relativePlyDistance, Byte.MIN_VALUE);
        }
        return 0;
    }
}
