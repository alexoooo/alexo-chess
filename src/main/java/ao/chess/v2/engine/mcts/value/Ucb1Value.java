package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 10:54:23 PM
 */
public class Ucb1Value implements MctsValue<Ucb1Value>
{
    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucb1Value> {
        @Override public Ucb1Value newValue() {
            return new Ucb1Value();
        }
    }


    //--------------------------------------------------------------------
    private int    visits;
    private double sum;


    //--------------------------------------------------------------------
    public Ucb1Value()
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
            Ucb1Value withRespectToParent) {
        return averageReward() +
               (visits == 0
               ? 1000 + Math.random()
               : Math.sqrt((2 * Math.log(
                       withRespectToParent.visits)) /visits));
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return visits + " (" + averageReward() + ")";
    }


    //--------------------------------------------------------------------
    public static class VisitSelector
            implements MctsSelector<Ucb1Value> {
        @Override public int compare(Ucb1Value a, Ucb1Value b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucb1Value value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucb1Value> {
        @Override public int compare(Ucb1Value a, Ucb1Value b) {
            return Double.compare(a.averageReward(), b.averageReward());
        }

        @Override
        public double asDouble(Ucb1Value value) {
            return value.averageReward();
        }
    }
}
