package ao.chess.v2.engine.heuristic.material;


import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;


// https://en.wikipedia.org/wiki/Chess_piece_relative_value
public enum MaterialEvaluation {
    ;

    private static final double maxMaterial = 100;
    private static final double maxDenominator = maxMaterial * 2;

    private static final double[] material = new double[Figure.VALUES.length];

    static {
        material[Figure.PAWN.ordinal()] = 1;
        material[Figure.KNIGHT.ordinal()] = 3;
        material[Figure.BISHOP.ordinal()] = 3;
        material[Figure.ROOK.ordinal()] = 5;
        material[Figure.QUEEN.ordinal()] = 9;
    }


    public static double evaluate(
            State state
    ) {
        double whiteSum = 0;
        double blackSum = 0;

        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                Piece pieceOrNull = state.pieceAt(rank, file);
                if (pieceOrNull == null) {
                    continue;
                }

                Figure figure = pieceOrNull.figure();

                double value = material[figure.ordinal()];

                if (pieceOrNull.isWhite()) {
                    whiteSum += value;
                }
                else {
                    blackSum += value;
                }
            }
        }

        double materialSum =
                state.nextToAct() == Colour.WHITE
                ? whiteSum - blackSum
                : blackSum - whiteSum;

        return (materialSum + maxMaterial) / maxDenominator;
    }
}
