package ao.chess.v2.engine.heuristic.learn;

import ao.ai.ml.model.input.RealList;
import ao.ai.ml.model.output.MultiClass;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;


public class MoveExample {
    private final State state;
    private final int[] legalMoves;
    private final double[] moveScores;


    public MoveExample(String line) {
        String[] parts = line.split(Pattern.quote("|"));

        state = State.fromFen(parts[0]);
        legalMoves = state.legalMoves();

        if (legalMoves.length != parts.length - 1) {
            throw new IllegalArgumentException();
        }

        moveScores = new double[legalMoves.length];

        int totalCount = 0;
        for (int i = 1; i < parts.length; i++) {
            String part = parts[i];
            int move = legalMoves[i - 1];

            int commaIndex = part.indexOf(',');
            String moveInput = part.substring(0, commaIndex);
            int count = Integer.parseInt(part.substring(commaIndex + 1));

            if (! moveInput.equals(Move.toInputNotation(move))) {
                throw new IllegalArgumentException();
            }

            moveScores[i - 1] = count;
            totalCount += count;
        }

        for (int i = 0; i < moveScores.length; i++) {
            moveScores[i] /= totalCount;
        }
    }


    public RealList stateInputVector() {
        double[] vector = new double[64 * Figure.VALUES.length];

        int cell = 0;
        for (int rank = 0; rank < 8; rank++) {
            for (int file = 0; file < 8; file++) {
                cell++;

                Piece piece = state.pieceAt(rank, file);
                if (piece == null) {
                    continue;
                }

                double value = (piece.colour() == state.nextToAct() ? 1 : -1);
                vector[cell * (piece.figure().ordinal() + 1) - 1] = value;
            }
        }

        return new RealList(vector);
    }


    public double[] locationScores() {
        double[] scores = new double[64];

        for (int i = 0; i < legalMoves.length; i++) {
            int move = legalMoves[i];
            int from = Move.fromSquareIndex(move);

            scores[from] += moveScores[i];
        }

        return scores;
    }


    public List<MultiClass> movePositionSample() {
        double[] locationScores = locationScores();

        int fromCount = 0;
        for (double locationScore : locationScores) {
            if (locationScore > 0) {
                fromCount++;
            }
        }

        double averageScore = 1.0 / fromCount;

        List<MultiClass> locations = new ArrayList<>();

        for (int i = 0; i < locationScores.length; i++) {
            if (locationScores[i] >= averageScore) {
                locations.add(MultiClass.create(i));
            }
        }

        return locations;
    }
}
