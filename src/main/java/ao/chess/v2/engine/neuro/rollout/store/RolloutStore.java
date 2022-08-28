package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.engine.neuro.rollout.RolloutNode;
import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;

import java.util.List;


public interface RolloutStore extends AutoCloseable {
    //-----------------------------------------------------------------------------------------------------------------
    long rootIndex = 0;


    //-----------------------------------------------------------------------------------------------------------------
    /**
     * @param moveCount number of legal moves at root state
     * @return true if the root state was initialized, otherwise false if it already existed
     */
    boolean initRootIfRequired(int moveCount);


    void incrementVisitCount(long nodeIndex);


    void decrementVisitCount(long nodeIndex);


    void addValue(long nodeIndex, double value);


    void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome);


    long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount);


    long nextIndex();


    //-----------------------------------------------------------------------------------------------------------------
    long getVisitCount(long nodeIndex);


    KnownOutcome getKnownOutcome(long nodeIndex);


    long getChildIndex(long nodeIndex, int moveIndex);


    double getValueSum(long nodeIndex);


    double getValueSquareSum(long nodeIndex);


    double getAverageValue(long nodeIndex, double defaultValue);


    //-----------------------------------------------------------------------------------------------------------------
    TranspositionInfo getTranspositionOrNull(long hashHigh, long hashLow);


    void setTransposition(long hashHigh, long hashLow, long nodeIndex, double valueSum, long visitCount);


    //-----------------------------------------------------------------------------------------------------------------
    /**
     * @return estimated number of nodes flushed (optional value)
     */
    default long flush() {
        return 0;
    }


    //-----------------------------------------------------------------------------------------------------------------
    // NB: used to avoid repeated synchronization

    default void backupValue(
            List<RolloutNode> path,
            double leafValue)
    {
        double inverseValue = 1.0 - leafValue;

        // NB: negating all rewards so that we're always maximizing
        boolean reverse = true;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            double negaMaxValue = reverse ? inverseValue : leafValue;

            RolloutNode node = path.get(i);

            addValue(node.index, negaMaxValue);

            reverse = ! reverse;
        }
    }


    default int populateChildInfoAndSelectSolution(
            long index,
            int moveCount,
            double[] moveValueSums,
            double[] moveValueSquareSums,
            long[] moveVisitCounts,
            long[] moveChildIndexes)
    {
        int nonWinKnownCount = 0;
        for (int i = 0; i < moveCount; i++) {
            long childIndex = getChildIndex(index, i);
//            RolloutNode child = childOrNull(i, store);

            long childVisitCount;
            double childValueSum;
            double childValueSquareSum;

            if (childIndex == -1) {
//            if (child == null) {
                childValueSum = 0;
                childValueSquareSum = 0;
                childVisitCount = 0;
            }
            else {
                childValueSum = getValueSum(childIndex);
//                childValueSum = child.valueSum(store);

                childValueSquareSum = getValueSquareSum(childIndex);
//                childValueSquareSum = child.valueSquareSum(store);

                childVisitCount = getVisitCount(childIndex);
//                childVisitCount = child.visitCount(store);

                double known = getKnownOutcome(childIndex).toValue();
//                double known = child.knownValue(store);

                if (! Double.isNaN(known)) {
                    if (known == 0.0) {
//                        setKnownOutcome(KnownOutcome.Win, store);
                        setKnownOutcome(index, KnownOutcome.Win);
                        return i;
                    }

                    nonWinKnownCount++;
                    childValueSum = (1.0 - known) * childVisitCount;
                }
            }

            moveValueSums[i] = childValueSum;
            moveValueSquareSums[i] = childValueSquareSum;
            moveVisitCounts[i] = childVisitCount;
            moveChildIndexes[i] = childIndex;
        }

        if (nonWinKnownCount == moveCount) {
            KnownOutcome bestChildOutcome = KnownOutcome.Win;
            int moveIndex = 0;
            for (int i = 0; i < moveCount; i++) {
                long childIndex = getChildIndex(index, i);
//                RolloutNode child = childOrNull(i, store);

                KnownOutcome value = getKnownOutcome(childIndex);
//                KnownOutcome value = child.knownOutcome(store);

                if (value == KnownOutcome.Draw) {
                    bestChildOutcome = value;
                    moveIndex = i;
                }
            }

            setKnownOutcome(index, bestChildOutcome.reverse());
//            setKnownOutcome(bestChildOutcome.reverse(), store);

            return moveIndex;
        }

        return -1;
    }
}
