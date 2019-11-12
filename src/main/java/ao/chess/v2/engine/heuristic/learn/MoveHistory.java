package ao.chess.v2.engine.heuristic.learn;

import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;


public class MoveHistory {
    //-----------------------------------------------------------------------------------------------------------------
    public static class Buffer
    {
        private final List<Fragment> fragments = new ArrayList<>();


        public void add(
                State state,
                int[] legalMoves,
                double[] moveScores)
        {
            add(state, legalMoves, moveScores, Double.NaN);
        }


        public void add(
                State state,
                int[] legalMoves,
                double[] moveScores,
                double expectedValue)
        {
            fragments.add(new Fragment(
                    state.prototype(), legalMoves, moveScores, expectedValue));
        }


        public boolean isEmpty()
        {
            return fragments.isEmpty();
        }


        public void clear()
        {
            fragments.clear();
        }


        public List<MoveHistory> build(Outcome outcome)
        {
            return fragments
                    .stream()
                    .map(fragment -> new MoveHistory(
                            fragment.state,
                            fragment.legalMoves,
                            fragment.moveScores,
                            Double.isNaN(fragment.expectedValue)
                                ? outcome.valueFor(fragment.state.nextToAct())
                                : fragment.expectedValue,
                            outcome))
                    .collect(Collectors.toList());
        }
    }


    private static class Fragment
    {
        private final State state;
        private final int[] legalMoves;
        private final double[] moveScores;
        private final double expectedValue;


        public Fragment(
                State state, int[] legalMoves, double[] moveScores, double expectedValue)
        {
            this.state = state;
            this.legalMoves = legalMoves;
            this.moveScores = moveScores;
            this.expectedValue = expectedValue;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final State state;
    private final int[] legalMoves;
    private final double[] moveScores;
    private final double expectedValue;
    private final Outcome outcome;


    //-----------------------------------------------------------------------------------------------------------------
    public MoveHistory(
            State state, int[] legalMoves, double[] moveScores, double expectedValue, Outcome outcome) {
        this.state = state;
        this.legalMoves = legalMoves;
        this.moveScores = moveScores;
        this.expectedValue = expectedValue;
        this.outcome = outcome;
    }


    public MoveHistory(String line) {
        String[] parts = line.split(Pattern.quote("|"));

        String[] firstPartSegments = parts[0].split(Pattern.quote(","));
        String fen = firstPartSegments[0];
        int outcomeOrdinal = Integer.parseInt(firstPartSegments[1]);

        state = State.fromFen(fen);
        outcome = Outcome.values[outcomeOrdinal];

        expectedValue =
                firstPartSegments.length == 2
                ? Double.parseDouble(firstPartSegments[1])
                : outcome.valueFor(state.nextToAct());

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


    //-----------------------------------------------------------------------------------------------------------------
    public String asString()
    {
        String fenAndOutcome =
                state.toFen() + "," +
                outcome.ordinal() + "," +
                expectedValue;

        StringBuilder str = new StringBuilder();

        str.append(fenAndOutcome);

        for (int i = 0; i < legalMoves.length; i++) {
            str.append('|')
                    .append(Move.toInputNotation(legalMoves[i]))
                    .append(',')
                    .append((int) moveScores[i]);
        }

        return str.toString();
    }


    public State state() {
        return state;
    }


    public int[] legalMoves() {
        return legalMoves;
    }


    public double[] moveScores() {
        return moveScores;
    }


    public double expectedValue() {
        return expectedValue;
    }


    public Outcome outcome() {
        return outcome;
    }


    public double outcomeScore() {
        if (outcome == Outcome.DRAW) {
            return 0;
        }

        return outcome.winner() == state.nextToAct()
                ? 1 : -1;
    }


    //-----------------------------------------------------------------------------------------------------------------
//    public RealList stateInputVector() {
//        double[] vector = new double[64 * Figure.VALUES.length];
//
//        int cell = 0;
//        for (int rank = 0; rank < 8; rank++) {
//            for (int file = 0; file < 8; file++) {
//                cell++;
//
//                Piece piece = state.pieceAt(rank, file);
//                if (piece == null) {
//                    continue;
//                }
//
//                double value = (piece.colour() == state.nextToAct() ? 1 : -1);
//                vector[cell * (piece.figure().ordinal() + 1) - 1] = value;
//            }
//        }
//
//        return new RealList(vector);
//    }


    public double[] fromLocationScores() {
        double[] scores = new double[64];

        for (int i = 0; i < legalMoves.length; i++) {
            int move = legalMoves[i];
            int from = Move.fromSquareIndex(move);

            scores[from] += moveScores[i];
        }

        return scores;
    }


    public double[] toLocationScores() {
        double[] scores = new double[64];

        for (int i = 0; i < legalMoves.length; i++) {
            int move = legalMoves[i];
            int from = Move.toSquareIndex(move);

            scores[from] += moveScores[i];
        }

        return scores;
    }


//    public List<MultiClass> movePositionSample() {
//        double[] locationScores = fromLocationScores();
//
//        int fromCount = 0;
//        for (double locationScore : locationScores) {
//            if (locationScore > 0) {
//                fromCount++;
//            }
//        }
//
//        double averageScore = 1.0 / fromCount;
//
//        List<MultiClass> locations = new ArrayList<>();
//
//        for (int i = 0; i < locationScores.length; i++) {
//            if (locationScores[i] >= averageScore) {
//                locations.add(MultiClass.create(i));
//            }
//        }
//
//        return locations;
//    }
}
