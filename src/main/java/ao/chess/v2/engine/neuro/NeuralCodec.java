package ao.chess.v2.engine.neuro;


import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.factory.Nd4j;

import java.util.Arrays;


public enum NeuralCodec {
    INSTANCE;

//    private static final boolean winOnly = true;
    private static final boolean winOnly = false;

    public static final int inputChannels = Figure.VALUES.length + 2;


    public INDArray encodeMultiState(State state) {
        return encodeState(state, true);
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

        int[] propAttacks = new int[Location.COUNT];
        int[] oppAttacks = new int[Location.COUNT];

        state.attackCount(propAttacks, state.nextToAct());
        state.attackCount(oppAttacks, state.nextToAct().invert());

        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                int adjustedRank = (flip ? Location.RANKS - rank - 1 : rank);
                int adjustedFile = (flip ? Location.FILES - file - 1 : file);
                int location = Location.squareIndex(rank, file);

                if (normalized) {
                    features.put(new int[] {Figure.VALUES.length, adjustedRank, adjustedFile},
                            Nd4j.scalar((double) propAttacks[location] / 15));

                    features.put(new int[] {Figure.VALUES.length + 1, adjustedRank, adjustedFile},
                            Nd4j.scalar((double) oppAttacks[location] / 15));
                }
                else {
                    features.put(new int[] {Figure.VALUES.length, adjustedRank, adjustedFile},
                            Nd4j.scalar(propAttacks[location]));

                    features.put(new int[] {Figure.VALUES.length + 1, adjustedRank, adjustedFile},
                            Nd4j.scalar(oppAttacks[location]));
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


    public void encodeMultiState(
            State state,
            INDArray features,
            int[] propAttacks,
            int[] oppAttacks
    ) {
        encodeMultiState(state, features, propAttacks, oppAttacks, 0);
    }

    public void encodeMultiState(
            State state,
            INDArray features,
            int[] propAttacks,
            int[] oppAttacks,
            int batchIndex
    ) {
        Colour nextToAct = state.nextToAct();
        boolean flip = nextToAct == Colour.BLACK;

        state.attackCount(propAttacks, nextToAct);
        state.attackCount(oppAttacks, nextToAct.invert());

        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                int adjustedRank = (flip ? Location.RANKS - rank - 1 : rank);
                int adjustedFile = (flip ? Location.FILES - file - 1 : file);
                int square = Location.squareIndex(rank, file);

                features.putScalar(batchIndex, Figure.VALUES.length, adjustedRank, adjustedFile,
                        (double) propAttacks[square] / 15);

                features.putScalar(batchIndex, Figure.VALUES.length + 1, adjustedRank, adjustedFile,
                        (double) oppAttacks[square] / 15);

                for (int i = 0; i < Figure.VALUES.length; i++) {
                    features.putScalar(batchIndex, i, adjustedRank, adjustedFile,
                            0.0);
                }

                Piece piece = state.pieceAt(rank, file);
                if (piece == null) {
                    continue;
                }

                boolean isNextToAct = piece.colour() == nextToAct;
                double value = (isNextToAct ? 1 : -1);

                Figure figure = piece.figure();

                features.putScalar(batchIndex, figure.ordinal(), adjustedRank, adjustedFile,
                        value);
            }
        }
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


    public double decodeMultiOutcome(
            INDArray output
    ) {
        return decodeMultiOutcome(output, 0);
    }


    public double decodeMultiOutcome(
            INDArray output,
            int batch
    ) {
//        return output.getDouble(batch, 0);
        double winProbability = output.getDouble(batch, 0);
//        double lossProbability = output.getDouble(batch, 1);
        double drawProbability = output.getDouble(batch, 2);

//        return 1.0 - lossProbability;
        return winOnly
                ? winProbability
                : winProbability + 0.5 * drawProbability;
    }


    public double[] decodeMoveMultiProbabilities(
            INDArray outputFrom,
            INDArray outputTo,
            State state,
            int[] legalMoves,
            double[] fromScores,
            double[] toScores
    ) {
        return decodeMoveMultiProbabilities(
                outputFrom, outputTo, state, legalMoves, fromScores, toScores, 0);
    }


    public double[] decodeMoveMultiProbabilities(
            INDArray outputFrom,
            INDArray outputTo,
            State state,
            int[] legalMoves,
            double[] fromScores,
            double[] toScores,
            int batchIndex
    ) {
        Arrays.fill(fromScores, 0.0);
        Arrays.fill(toScores, 0.0);

        int legalMoveCount = legalMoves.length;
        boolean flip = state.nextToAct() == Colour.BLACK;

        double fromTotal = 0;
        double toTotal = 0;

        for (int move : legalMoves) {
            int fromIndex = Move.fromSquareIndex(move);
            int fromAdjustedIndex = flipIndexIfRequired(fromIndex, flip);
            if (fromScores[fromIndex] == 0) {
                double prediction = outputFrom.getDouble(batchIndex, fromAdjustedIndex);
                fromScores[fromIndex] = prediction;
                fromTotal += prediction;
            }

            int toIndex = Move.toSquareIndex(move);
            int toAdjustedIndex = flipIndexIfRequired(toIndex, flip);
            if (toScores[toIndex] == 0) {
                double prediction = outputTo.getDouble(batchIndex, toAdjustedIndex);
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


    public static int flipIndexIfRequired(
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
