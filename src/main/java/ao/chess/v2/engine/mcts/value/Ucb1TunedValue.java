package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;

/**
 * User: aostrovsky
 * Date: 30-Sep-2009
 * Time: 5:14:28 PM
 */
public class Ucb1TunedValue implements MctsValue<Ucb1TunedValue>
{
    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucb1TunedValue> {
        @Override public Ucb1TunedValue newValue() {
            return new Ucb1TunedValue();
        }
    }


    //--------------------------------------------------------------------
    private double sum;
    private double sumSquares;
    private int    visits;


    //--------------------------------------------------------------------
    public Ucb1TunedValue()
    {
        sum        = 0;
        sumSquares = 0;
        visits     = 0;
    }


    //--------------------------------------------------------------------
    @Override
    public int visits() {
        return visits;
    }


    //--------------------------------------------------------------------
    @Override public void update(double winRate)
    {
        sum        += winRate;
        sumSquares += winRate * winRate;

        visits++;
    }


    //--------------------------------------------------------------------
    @Override public double confidenceBound(
            Ucb1TunedValue transpositionValue,
            Ucb1TunedValue withRespectToParent)
    {
        int    trials = withRespectToParent.visits;
        double mean   = mean();
        return mean
                + Math.sqrt(
                    (Math.log(trials) / visits)
                    * Math.min(0.25,
                            varianceBound(mean, trials)));
    }

    private double mean() {
        return sum / visits;
    }


    //--------------------------------------------------------------------
    private double varianceBound(
            double mean, int turn) {

        return sumSquares / visits
                - mean * mean
                + Math.sqrt((2 * Math.log(turn)) / visits);
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return visits + " (" + (sum / visits) + ")";
    }


    //--------------------------------------------------------------------
    public static class VisitSelector
            implements MctsSelector<Ucb1TunedValue> {
        @Override public int compare(
                Ucb1TunedValue a, Ucb1TunedValue b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucb1TunedValue value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucb1TunedValue> {
        @Override public int compare(
                Ucb1TunedValue a, Ucb1TunedValue b) {
            return Double.compare(a.mean(), b.mean());
        }

        @Override
        public double asDouble(Ucb1TunedValue value) {
            return value.mean();
        }
    }
}
