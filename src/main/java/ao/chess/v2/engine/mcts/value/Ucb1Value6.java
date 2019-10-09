package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 10:54:23 PM
 */
public class Ucb1Value6 implements MctsValue<Ucb1Value6>
{
    //--------------------------------------------------------------------
    private static final double exploration = 0.6;


    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucb1Value6> {
        @Override public Ucb1Value6 newValue() {
            return new Ucb1Value6();
        }
    }


    //--------------------------------------------------------------------
    private int    visits;
    private double sum;


    //--------------------------------------------------------------------
    public Ucb1Value6()
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
            Ucb1Value6 transpositionValue,
            Ucb1Value6 withRespectToParent) {
        return averageReward() +
                (visits == 0
                ? 1000 + Math.random()
                : exploration * Math.sqrt(2 * Math.log(withRespectToParent.visits) / visits));
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return visits + " (" + averageReward() + ")";
    }


    //--------------------------------------------------------------------
    public static class VisitSelector
            implements MctsSelector<Ucb1Value6> {
        @Override public int compare(Ucb1Value6 a, Ucb1Value6 b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucb1Value6 value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucb1Value6> {
        @Override public int compare(Ucb1Value6 a, Ucb1Value6 b) {
            return Double.compare(a.averageReward(), b.averageReward());
        }

        @Override
        public double asDouble(Ucb1Value6 value) {
            return value.averageReward();
        }
    }
}