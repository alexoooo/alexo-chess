package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;


// https://arxiv.org/pdf/1909.02229.pdf
public class Ucb1LargeValue2 implements MctsValue<Ucb1LargeValue2>
{
    //--------------------------------------------------------------------
    private static final double exploration = 0.2;


    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucb1LargeValue2> {
        @Override public Ucb1LargeValue2 newValue() {
            return new Ucb1LargeValue2();
        }
    }


    //--------------------------------------------------------------------
    private int    visits;
    private double sum;


    //--------------------------------------------------------------------
    public Ucb1LargeValue2()
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
            Ucb1LargeValue2 withRespectToParent) {
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
            implements MctsSelector<Ucb1LargeValue2> {
        @Override public int compare(Ucb1LargeValue2 a, Ucb1LargeValue2 b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucb1LargeValue2 value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucb1LargeValue2> {
        @Override public int compare(Ucb1LargeValue2 a, Ucb1LargeValue2 b) {
            return Double.compare(a.averageReward(), b.averageReward());
        }

        @Override
        public double asDouble(Ucb1LargeValue2 value) {
            return value.averageReward();
        }
    }
}