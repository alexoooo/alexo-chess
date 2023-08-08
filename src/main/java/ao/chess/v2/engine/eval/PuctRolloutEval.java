package ao.chess.v2.engine.eval;


import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.endgame.v2.EfficientDeepOracle;
import ao.chess.v2.engine.neuro.puct.PuctEstimate;
import ao.chess.v2.engine.neuro.puct.PuctModelPool;
import ao.chess.v2.engine.neuro.rollout.RolloutContext;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.util.HashMap;
import java.util.Map;


public class PuctRolloutEval implements PositionEvaluator {
    //-----------------------------------------------------------------------------------------------------------------
    private static final double rolloutValueDiscount = 0.9;
//    private static final double rolloutValueDiscount = 0.95;
//    private static final double rolloutValueDiscount = 0.99;

    private static final double estimateUncertainty = 0.0;
    //    private static final double estimateUncertainty = 0.01;
//    private static final double estimateUncertainty = 0.025;
    private static final double estimateUncertaintyDenominator = 1.0 + estimateUncertainty;


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public double evaluate(int topLevelMoveCount, State state, RolloutContext context) {
        Colour fromPov = state.nextToAct();

        int[] moves = context.movesA;
        int nMoves = topLevelMoveCount;
        int nextCount;
        int[] nextMoves = context.movesB;
        Outcome outcome = null;

        if (nMoves < 1) {
            return Double.NaN;
        }

        double discountedValueSum = 0;
        double nextDiscount = 1.0;
        double discountSum = 0;

        Map<PuctModelPool.CacheKey, PuctEstimate> localCache = new HashMap<>();

        rollout:
        for (int rolloutLength = 0; rolloutLength <= context.rolloutLength; rolloutLength++) {
//            PuctEstimate estimate = context.pool.estimateBlocking(state, moves, nMoves);
            PuctEstimate estimate = context.pool.estimateBlockingLocalCached(state, moves, nMoves, localCache);

            double stateValue =
                    state.nextToAct() == fromPov
                    ? estimate.expectedValue()
                    : 1.0 - estimate.expectedValue();

            discountedValueSum += stateValue * nextDiscount;
            discountSum += nextDiscount;
            nextDiscount *= rolloutValueDiscount;

            int bestMoveIndex = 0;
            double bestMoveScore = Double.NEGATIVE_INFINITY;
            double moveUncertainty = estimateUncertainty / nMoves;

            for (int i = 0; i < nMoves; i++) {
                byte reversibleMoves = state.reversibleMoves();
                byte castles = state.castles();
                long castlePath = state.castlePath();

                int move = Move.apply(moves[ i ], state);
                int opponentMoveCount = state.legalMoves(nextMoves, context.movesC);
                Outcome moveOutcome = state.knownOutcomeOrNull(opponentMoveCount);
                Move.unApply(move, state);

                state.restore(reversibleMoves, castles, castlePath);

                if (moveOutcome != null) {
                    if (moveOutcome.loser() == state.nextToAct()) {
                        // non-viable move, in check and doesn't prevent checkmate
                        continue;
                    }

                    if (moveOutcome.winner() == state.nextToAct()) {
                        // mate in one
                        outcome = moveOutcome;
                        context.terminalRolloutHits.increment();
                        break rollout;
                    }
                }

                double probability =
                        (estimate.moveProbabilities[i] + moveUncertainty) /
                                estimateUncertaintyDenominator;

                double probabilityScore = Math.pow(probability, context.probabilityPower);
                double score = probabilityScore * context.random.nextDouble();
                if (score > bestMoveScore) {
                    bestMoveScore = score;
                    bestMoveIndex = i;
                }
            }

            Move.apply(moves[bestMoveIndex], state);

            // generate opponent moves
            nextCount = state.legalMoves(nextMoves, context.movesC);
            Outcome moveOutcome = state.knownOutcomeOrNull();
            if (moveOutcome != null) {
                outcome = moveOutcome;
                context.terminalRolloutHits.increment();
                break;
            }

            if (state.pieceCount() <= EfficientDeepOracle.pieceCount) {
                DeepOutcome deepOutcome = EfficientDeepOracle.getOrNull(state);
                outcome = deepOutcome.outcome();
                context.tablebaseRolloutHits.increment();
                break;
            }

            if (estimate.certainty() >= context.certaintyLimit) {
                return stateValue;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves = moves;
                moves = tempMoves;
                nMoves = nextCount;
            }
        }

        if (outcome != null) {
            return outcome.valueFor(fromPov);
        }

        double expectedValue = discountedValueSum / discountSum;

        return context.binerize
                ? (expectedValue > context.random.nextDouble() ? 1.0 : 0.0)
                : expectedValue;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() {}
}
