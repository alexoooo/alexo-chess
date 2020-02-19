package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.data.Location;
import ao.chess.v2.engine.neuro.NeuralCodec;
import ao.chess.v2.engine.neuro.meta.MetaEstimate;
import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import com.google.common.base.Preconditions;
import org.deeplearning4j.nn.graph.ComputationGraph;
import org.nd4j.linalg.api.ndarray.INDArray;
import org.nd4j.linalg.dataset.api.MultiDataSet;
import org.nd4j.linalg.factory.Nd4j;

import java.util.ArrayList;
import java.util.List;


public class MoveTrainerEncoder
{
    //-----------------------------------------------------------------------------------------------------------------
    private final int batchSize;

    private final int[] propAttacks;
    private final int[] oppAttacks;
    private final double[] fromScores;
    private final double[] toScores;

    private final INDArray features;
    private final INDArray labelFrom;
    private final INDArray labelTo;
    private final INDArray labelOutcome;
    private final INDArray labelError;
    private final MultiDataSet outputBatch;
    private final MultiDataSet outputBatchMeta;

    private final INDArray featuresSingle;

    private final int[] previousLabelFrom;
    private final int[] previousLabelTo;
    private final int[] previousLabelOutcome;


    //-----------------------------------------------------------------------------------------------------------------
    public MoveTrainerEncoder(int batchSize)
    {
        this.batchSize = batchSize;

        propAttacks = new int[Location.COUNT];
        oppAttacks = new int[Location.COUNT];
        fromScores = new double[Location.COUNT];
        toScores = new double[Location.COUNT];

        features = Nd4j.zeros(batchSize, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
        labelFrom = Nd4j.zeros(batchSize, Location.COUNT);
        labelTo = Nd4j.zeros(batchSize, Location.COUNT);
        labelOutcome = Nd4j.zeros(batchSize, Outcome.values.length);
        labelError = Nd4j.zeros(batchSize, 1);

        previousLabelFrom = new int[batchSize];
        previousLabelTo = new int[batchSize];
        previousLabelOutcome = new int[batchSize];

        outputBatch = new org.nd4j.linalg.dataset.MultiDataSet(
                new INDArray[] {
                        features
                },
                new INDArray[] {
                        labelFrom,
                        labelTo,
                        labelOutcome
                }
        );

        outputBatchMeta = new org.nd4j.linalg.dataset.MultiDataSet(
                new INDArray[] {
                        features
                },
                new INDArray[] {
                        labelFrom,
                        labelTo,
                        labelOutcome,
                        labelError
                }
        );

        featuresSingle = Nd4j.zeros(1, Figure.VALUES.length + 2, Location.RANKS, Location.FILES);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public MultiDataSet encodeAllInPlace(List<MoveHistory> examples)
    {
        Preconditions.checkArgument(examples.size() == batchSize);

        for (int i = 0; i < examples.size(); i++) {
            encodeOneInPlace(examples.get(i), i);
        }

        return outputBatch;
    }


    private void encodeOneInPlace(MoveHistory example, int batchIndex)
    {
        labelFrom.putScalar(batchIndex, previousLabelFrom[batchIndex], 0);
        labelTo.putScalar(batchIndex, previousLabelTo[batchIndex], 0);
        labelOutcome.putScalar(batchIndex, previousLabelOutcome[batchIndex], 0);

        NeuralCodec.INSTANCE.encodeMultiState(
                example.state(), features, propAttacks, oppAttacks, batchIndex);

        boolean flip = example.state().nextToAct() == Colour.BLACK;

        double bestMoveScore = 0;
        int bestMoveIndex = 0;
        for (int i = 0; i < example.moveScores().length; i++) {
            double moveScore = example.moveScores()[i];
            if (moveScore > bestMoveScore) {
                bestMoveScore = moveScore;
                bestMoveIndex = i;
            }
        }
        int bestMove = example.legalMoves()[bestMoveIndex];

        int fromSquareIndex = Move.fromSquareIndex(bestMove);
        int adjustedFrom = NeuralCodec.flipIndexIfRequired(fromSquareIndex, flip);
        labelFrom.putScalar(batchIndex, adjustedFrom, 1.0);
        previousLabelFrom[batchIndex] = adjustedFrom;

        int toSquareIndex = Move.toSquareIndex(bestMove);
        int adjustedTo = NeuralCodec.flipIndexIfRequired(toSquareIndex, flip);
        labelTo.putScalar(batchIndex, adjustedTo, 1.0);
        previousLabelTo[batchIndex] = adjustedTo;

        Outcome outcome = example.outcome();
        int outcomeIndex =
                outcome.winner() == example.state().nextToAct()
                ? 0
                : outcome.loser() == example.state().nextToAct()
                ? 1
                : 2;

        labelOutcome.putScalar(batchIndex, outcomeIndex, 1.0);
        previousLabelOutcome[batchIndex] = outcomeIndex;
    }


    public PuctEstimate estimate(MoveHistory example, ComputationGraph nn)
    {
        NeuralCodec.INSTANCE.encodeMultiState(
                example.state(), featuresSingle, propAttacks, oppAttacks);

        INDArray[] outputs = nn.output(featuresSingle);

        double[] moveProbabilities = NeuralCodec.INSTANCE
                .decodeMoveMultiProbabilities(
                        outputs[0],
                        outputs[1],
                        example.state(),
                        example.legalMoves(),
                        fromScores,
                        toScores);

        double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2]);

        return new PuctEstimate(moveProbabilities, winProbability);
    }


    public List<PuctEstimate> estimateAll(
            List<MoveHistory> examples, ComputationGraph nn)
    {
        Preconditions.checkArgument(examples.size() == batchSize);

        for (int batchIndex = 0; batchIndex < batchSize; batchIndex++)
        {
            NeuralCodec.INSTANCE.encodeMultiState(
                    examples.get(batchIndex).state(),
                    features, propAttacks, oppAttacks,
                    batchIndex);
        }

        INDArray[] outputs = nn.output(features);

        List<PuctEstimate> estimates = new ArrayList<>();

        for (int batchIndex = 0; batchIndex < batchSize; batchIndex++)
        {
            double[] moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveMultiProbabilities(
                            outputs[0],
                            outputs[1],
                            examples.get(batchIndex).state(),
                            examples.get(batchIndex).legalMoves(),
                            fromScores,
                            toScores,
                            batchIndex);

            double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2], batchIndex);

            estimates.add(new PuctEstimate(moveProbabilities, winProbability));
        }

        return estimates;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public MultiDataSet encodeAllInPlaceMeta(List<MoveHistory> examples, List<MetaEstimate> estimates)
    {
        Preconditions.checkArgument(examples.size() == batchSize);

        for (int i = 0; i < examples.size(); i++) {
            encodeOneInPlaceMeta(examples.get(i), estimates.get(i), i);
        }

        return outputBatchMeta;
    }


    private void encodeOneInPlaceMeta(
            MoveHistory example, MetaEstimate estimate, int batchIndex)
    {
        labelFrom.putScalar(batchIndex, previousLabelFrom[batchIndex], 0);
        labelTo.putScalar(batchIndex, previousLabelTo[batchIndex], 0);
        labelOutcome.putScalar(batchIndex, previousLabelOutcome[batchIndex], 0);
//        labelError.putScalar(batchIndex, 0, 0);

        NeuralCodec.INSTANCE.encodeMultiState(
                example.state(), features, propAttacks, oppAttacks, batchIndex);

        boolean flip = example.state().nextToAct() == Colour.BLACK;

        double bestMoveScore = 0;
        int bestMoveIndex = 0;
        for (int i = 0; i < example.moveScores().length; i++) {
            double moveScore = example.moveScores()[i];
            if (moveScore > bestMoveScore) {
                bestMoveScore = moveScore;
                bestMoveIndex = i;
            }
        }
        int bestMove = example.legalMoves()[bestMoveIndex];

        int fromSquareIndex = Move.fromSquareIndex(bestMove);
        int adjustedFrom = NeuralCodec.flipIndexIfRequired(fromSquareIndex, flip);
        labelFrom.putScalar(batchIndex, adjustedFrom, 1.0);
        previousLabelFrom[batchIndex] = adjustedFrom;

        int toSquareIndex = Move.toSquareIndex(bestMove);
        int adjustedTo = NeuralCodec.flipIndexIfRequired(toSquareIndex, flip);
        labelTo.putScalar(batchIndex, adjustedTo, 1.0);
        previousLabelTo[batchIndex] = adjustedTo;

        Outcome outcome = example.outcome();
        int outcomeIndex =
                outcome.winner() == example.state().nextToAct()
                ? 0
                : outcome.loser() == example.state().nextToAct()
                ? 1
                : 2;

        labelOutcome.putScalar(batchIndex, outcomeIndex, 1.0);
        previousLabelOutcome[batchIndex] = outcomeIndex;

        double error = Math.abs(example.outcomeValue() - estimate.winProbability);
        labelError.putScalar(batchIndex, 0, error);
    }


    public MetaEstimate estimateMeta(MoveHistory example, ComputationGraph nn)
    {
        NeuralCodec.INSTANCE.encodeMultiState(
                example.state(), featuresSingle, propAttacks, oppAttacks);

        INDArray[] outputs = nn.output(featuresSingle);

        double[] moveProbabilities = NeuralCodec.INSTANCE
                .decodeMoveMultiProbabilities(
                        outputs[0],
                        outputs[1],
                        example.state(),
                        example.legalMoves(),
                        fromScores,
                        toScores);

        double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2]);
        double winError = outputs[3].getDouble(0, 0);

        return new MetaEstimate(moveProbabilities, winProbability, winError);
    }


    public List<MetaEstimate> estimateAllMeta(
            List<MoveHistory> examples, ComputationGraph nn)
    {
        Preconditions.checkArgument(examples.size() == batchSize);

        for (int batchIndex = 0; batchIndex < batchSize; batchIndex++)
        {
            NeuralCodec.INSTANCE.encodeMultiState(
                    examples.get(batchIndex).state(),
                    features, propAttacks, oppAttacks,
                    batchIndex);
        }

        INDArray[] outputs = nn.output(features);

        List<MetaEstimate> estimates = new ArrayList<>();

        for (int batchIndex = 0; batchIndex < batchSize; batchIndex++)
        {
            double[] moveProbabilities = NeuralCodec.INSTANCE
                    .decodeMoveMultiProbabilities(
                            outputs[0],
                            outputs[1],
                            examples.get(batchIndex).state(),
                            examples.get(batchIndex).legalMoves(),
                            fromScores,
                            toScores,
                            batchIndex);

            double winProbability = NeuralCodec.INSTANCE.decodeMultiOutcome(outputs[2], batchIndex);
            double winError = outputs[3].getDouble(batchIndex, 0);

            estimates.add(new MetaEstimate(moveProbabilities, winProbability, winError));
        }

        return estimates;
    }
}
