package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;


// https://arxiv.org/pdf/1909.02229.pdf
public class Ucb1LargeValue implements MctsValue<Ucb1LargeValue>
{
    //--------------------------------------------------------------------
    private static final double exploration = 1.0;


    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucb1LargeValue> {
        @Override public Ucb1LargeValue newValue() {
            return new Ucb1LargeValue();
        }
    }


    //--------------------------------------------------------------------
    private int    visits;
    private double sum;


    //--------------------------------------------------------------------
    public Ucb1LargeValue()
    {
        visits = 0;
        sum    = 0;
    }


    //--------------------------------------------------------------------
    @Override
    public int visits() {
        return visits;
    }


    private double averageReward() {
        return sum / visits;
    }


    //--------------------------------------------------------------------
    @Override
    public void update(double winRate) {
        visits++;
        sum += winRate;
    }


    //--------------------------------------------------------------------
    @Override
    public double confidenceBound(
            int parentChoices,
            Ucb1LargeValue withRespectToParent) {
        return averageReward() +
                (visits == 0
                ? 1000 + Math.random()
                : exploration * Math.sqrt(
                        2 * Math.log((double) withRespectToParent.visits / parentChoices) / visits));
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return visits + " (" + averageReward() + ")";
    }


    //--------------------------------------------------------------------
    public static class VisitSelector
            implements MctsSelector<Ucb1LargeValue> {
        @Override public int compare(Ucb1LargeValue a, Ucb1LargeValue b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucb1LargeValue value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucb1LargeValue> {
        @Override public int compare(Ucb1LargeValue a, Ucb1LargeValue b) {
            return Double.compare(a.averageReward(), b.averageReward());
        }

        @Override
        public double asDouble(Ucb1LargeValue value) {
            return value.averageReward();
        }
    }
}