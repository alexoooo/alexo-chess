package ao.chess.v2.engine.neuro;


import ao.chess.v2.data.Location;
import ao.chess.v2.engine.heuristic.learn.MoveHistory;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.DataSet;
import org.nd4j.linalg.factory.Nd4j;


public enum NeuralCodec {
    INSTANCE;

//    private static final boolean winOnly = true;
    private static final boolean winOnly = false;


    public INDArray encodeMultiState(State state) {
        return encodeState(state);
    }


    public INDArray encodeStateNormalized(State state) {
        return encodeState(state, true);
    }


    public INDArray encodeState(
            State state
    ) {
        return encodeState(state, false);
    }

    public INDArray encodeState(
            State state,
            boolean normalized
    ) {
        INDArray features = Nd4j.zeros(Figure.VALUES.length + 2, Location.RANKS, Location.FILES);

        boolean flip = state.nextToAct() == Colour.BLACK;

        int[][] propAttacks = new int[Location.RANKS][Location.FILES];
        int[][] oppAttacks = new int[Location.RANKS][Location.FILES];

        state.attackCount(propAttacks, state.nextToAct());
        state.attackCount(oppAttacks, state.nextToAct().invert());

        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                int adjustedRank = (flip ? Location.RANKS - rank - 1 : rank);
                int adjustedFile = (flip ? Location.FILES - file - 1 : file);

                if (normalized) {
                    features.put(new int[] {Figure.VALUES.length, adjustedRank, adjustedFile},
                            Nd4j.scalar((double) propAttacks[rank][file] / 15));

                    features.put(new int[] {Figure.VALUES.length + 1, adjustedRank, adjustedFile},
                            Nd4j.scalar((double) oppAttacks[rank][file] / 15));
                }
                else {
                    features.put(new int[] {Figure.VALUES.length, adjustedRank, adjustedFile},
                            Nd4j.scalar(propAttacks[rank][file]));

                    features.put(new int[] {Figure.VALUES.length + 1, adjustedRank, adjustedFile},
                            Nd4j.scalar(oppAttacks[rank][file]));
                }

                Piece piece = state.pieceAt(rank, file);
                if (piece == null) {
                    continue;
                }

                boolean isNextToAct = piece.colour() == state.nextToAct();
                double value = (isNextToAct ? 1 : -1);

                Figure figure = piece.figure();

                features.put(new int[] {figure.ordinal(), adjustedRank, adjustedFile}, Nd4j.scalar(value));
            }
        }

        long[] featureShape = features.shape();
        return features.reshape(1, featureShape[0], featureShape[1], featureShape[2]);
    }


    public double decodeOutcomeValueOnly(
            INDArray output
    ) {
        double value = output.getDouble(0, 0);
        return Math.max(0, Math.min(1, value));

//        double winProbability = output.getDouble(0, 0);
////        double lossProbability = output.getDouble(0, 1);
//        double drawProbability = output.getDouble(0, 2);
//
//        return winProbability +
//                0.5 * drawProbability;
    }


    public static DataSet convertToDataSetValueOnly(
            MoveHistory example
    ) {
        INDArray reshapedFeatures = NeuralCodec.INSTANCE.encodeStateNormalized(
                example.state());

        INDArray labels = Nd4j.zeros(1);
        labels.put(0, Nd4j.scalar(example.outcomeValue()));
        INDArray reshapedLabels = labels.reshape(1, 1);

//        INDArray labels = Nd4j.zeros(3);
//        Outcome outcome = example.outcome();
//        if (outcome.winner() == example.state().nextToAct()) {
//            labels.put(0, Nd4j.scalar(1));
//        }
//        else if (outcome.loser() == example.state().nextToAct()) {
//            labels.put(1, Nd4j.scalar(1));
//        }
//        else {
//            labels.put(2, Nd4j.scalar(1));
//        }
//        INDArray reshapedLabels = labels.reshape(1, 3);

        return new DataSet(reshapedFeatures, reshapedLabels);
    }


    public double decodeOutcome(
            INDArray output
    ) {
        double value = output.getDouble(0, Location.COUNT * 2);

//        double ex = Math.exp(value * Math.E);
//        return ex / (ex + 1);

        double clipped = Math.max(-1, Math.min(1, value));
        return (clipped + 1) / 2;
    }


    public double decodeMultiOutcome(
            INDArray output
    ) {
//        return output.getDouble(0, 0);
        double winProbability = output.getDouble(0, 0);
//        double lossProbability = output.getDouble(0, 1);
        double drawProbability = output.getDouble(0, 2);

        return winOnly
                ? winProbability
                : winProbability + 0.5 * drawProbability;
    }


    public double decodeMultiOutcomeMax(
            INDArray output
    ) {
        double winProbability = output.getDouble(0, 0);
        double lossProbability = output.getDouble(0, 1);
        double drawProbability = output.getDouble(0, 2);

        return winProbability > lossProbability && winProbability > drawProbability
                ? 1.0
                : lossProbability > winProbability && lossProbability > drawProbability
                ? 0
                : 0.5;
    }


    public double[] decodeMoveProbabilities(
            INDArray output,
            State state,
            int[] legalMoves
    ) {
        int legalMoveCount = legalMoves.length;
        boolean flip = state.nextToAct() == Colour.BLACK;

        double[] fromScores = new double[Location.COUNT];
        double[] toScores = new double[Location.COUNT];

        double fromTotal = 0;
        double toTotal = 0;

        for (int i = 0; i < Location.COUNT; i++) {
            int fromAdjustedIndex = flipIndexIfRequired(i, flip);
            double fromValue = Math.max(0, output.getDouble(0, fromAdjustedIndex));
            fromScores[i] = fromValue;
            fromTotal += fromValue;

            int toAdjustedIndex = fromAdjustedIndex + Location.COUNT;
            double toValue = Math.max(0, output.getDouble(0, toAdjustedIndex));
            toScores[i] = toValue;
            toTotal += toValue;
        }

        double moveTotal = 0;
        double[] moveScores = new double[legalMoveCount];

        for (int i = 0; i < legalMoveCount; i++) {
            int move = legalMoves[i];

            if (Move.isPromotion(move) && Figure.VALUES[Move.promotion(move)] != Figure.QUEEN) {
                // NB: under-promotions are not considered
                continue;
            }

            int fromIndex = Move.fromSquareIndex(move);
            double fromPrediction = fromScores[fromIndex] / fromTotal;

            int toIndex = Move.toSquareIndex(move);
            double toPrediction = toScores[toIndex] / toTotal;

            double moveScore = fromPrediction * toPrediction;
            moveScores[i] = moveScore;
            moveTotal += moveScore;
        }

        if (moveTotal != 0) {
            for (int i = 0; i < legalMoveCount; i++) {
                moveScores[i] /= moveTotal;
            }
        }

        return moveScores;
    }


    public double[] decodeMoveMultiProbabilities(
            INDArray outputFrom,
            INDArray outputTo,
            State state,
            int[] legalMoves
    ) {
        int legalMoveCount = legalMoves.length;
        boolean flip = state.nextToAct() == Colour.BLACK;

        double[] fromScores = new double[Location.COUNT];
        double fromTotal = 0;

        double[] toScores = new double[Location.COUNT];
        double toTotal = 0;

        for (int move : legalMoves) {
            int fromIndex = Move.fromSquareIndex(move);
            int fromAdjustedIndex = flipIndexIfRequired(fromIndex, flip);
            if (fromScores[fromIndex] == 0) {
                double prediction = outputFrom.getDouble(0, fromAdjustedIndex);
                fromScores[fromIndex] = prediction;
                fromTotal += prediction;
            }

            int toIndex = Move.toSquareIndex(move);
            int toAdjustedIndex = flipIndexIfRequired(toIndex, flip);
            if (toScores[toIndex] == 0) {
                double prediction = outputTo.getDouble(0, toAdjustedIndex);
                toScores[toIndex] = prediction;
                toTotal += prediction;
            }
        }

        double moveTotal = 0;
        double[] moveScores = new double[legalMoveCount];

        for (int i = 0; i < legalMoveCount; i++) {
            int move = legalMoves[i];

            if (Move.isPromotion(move) && Figure.VALUES[Move.promotion(move)] != Figure.QUEEN) {
                // NB: under-promotions are not considered
                continue;
            }

            int fromIndex = Move.fromSquareIndex(move);
            double fromProbability = fromScores[fromIndex] / fromTotal;

            int toIndex = Move.toSquareIndex(move);
            double toProbability = toScores[toIndex] / toTotal;

            double moveScore = fromProbability * toProbability;
            moveScores[i] = moveScore;
            moveTotal += moveScore;
        }

        if (moveTotal != 0) {
            for (int i = 0; i < legalMoveCount; i++) {
                moveScores[i] /= moveTotal;
            }
        }

        return moveScores;
    }


    private static int flipIndexIfRequired(
            int locationIndex,
            boolean flip
    ) {
        int rank = Location.rankIndex(locationIndex);
        int file = Location.fileIndex(locationIndex);
        int adjustedRank = (flip ? Location.RANKS - rank - 1 : rank);
        int adjustedFile = (flip ? Location.FILES - file - 1 : file);
        return Location.squareIndex(adjustedRank, adjustedFile);
    }
}
