package ao.chess.v2.engine.heuristic.impl.classification;

import ao.ai.ml.model.common.AiModelUtils;
import ao.ai.ml.model.input.RealList;
import ao.chess.v2.data.BoardLocation;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;

/**
 * User: AO
 * Date: 10/31/10
 * Time: 2:27 PM
 */
public class ChessClassUtils
{
    //------------------------------------------------------------------------
    private ChessClassUtils() {}


    //------------------------------------------------------------------------
    public static RealList encodeByMaterial(State state)
    {
        double[] coding = new double[
                2 * state.pieceCount() + 1 + 1 ];

        // bias
        coding[ coding.length - 1 ] = 1;

        // next-to-act
        coding[ coding.length - 2 ] = AiModelUtils.sgn(
                state.nextToAct() == Colour.WHITE);

        int nextIndex = 0;
        for (BoardLocation orderedLocation : state.material().keySet())
        {
            coding[ nextIndex++ ] = orderedLocation.rank();
            coding[ nextIndex++ ] = orderedLocation.file();
        }

        return new RealList( coding );
    }


    //------------------------------------------------------------------------
    public static RealList encode(State state)
    {
        double[] coding = new double[ 8 * 8 + 1 + 1 ];

        // bias
        coding[ coding.length - 1 ] = 1;

        // next-to-act
        coding[ coding.length - 2 ] = AiModelUtils.sgn(
                state.nextToAct() == Colour.WHITE);

        int boardIndex = 0;
        for (int rank = 0; rank < 8; rank++)
        {
            for (int file = 0; file < 8; file++)
            {
                Piece piece = state.pieceAt(rank, file);
                coding[ boardIndex++ ] = encode( piece );
            }
        }

        return new RealList( coding );
    }

    private static double encode( Piece piece )
    {
        // empty
        if (piece == null) {
            return 0;
        }

        switch (piece)
        {
            case WHITE_PAWN: return  1.00;
            case BLACK_PAWN: return -1.00;

            case WHITE_KNIGHT: return  3.00;
            case BLACK_KNIGHT: return -3.00;

            case WHITE_BISHOP: return  3.25;
            case BLACK_BISHOP: return -3.25;

            case WHITE_ROOK: return  5.00;
            case BLACK_ROOK: return -5.00;

            case WHITE_QUEEN: return  9.00;
            case BLACK_QUEEN: return -9.00;

            case WHITE_KING: return  25.00;
            case BLACK_KING: return -25.00;
        }

        throw new IllegalStateException(
                "Can't recognize piece: " + piece);
    }
}
