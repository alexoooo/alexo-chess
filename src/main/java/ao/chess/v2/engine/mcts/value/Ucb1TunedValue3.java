package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;


public class Ucb1TunedValue3 implements MctsValue<Ucb1TunedValue3>
{
    //--------------------------------------------------------------------
    private static final double exploration = 0.3;


    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucb1TunedValue3> {
        @Override public Ucb1TunedValue3 newValue() {
            return new Ucb1TunedValue3();
        }
    }


    //--------------------------------------------------------------------
    private double sum;
    private double sumSquares;
    private int    visits;


    //--------------------------------------------------------------------
    public Ucb1TunedValue3()
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
    @Override
    public double confidenceBound(
            int parentChoices,
            Ucb1TunedValue3 withRespectToParent)
    {
        int    trials = withRespectToParent.visits;
        double mean   = mean();
        return mean
                + exploration * Math.sqrt(
                    (Math.log(trials) / visits)
                    * Math.min(0.25,
                            varianceBound(mean, trials)));
    }

    private double mean() {
        return sum / visits;
    }


    //--------------------------------------------------------------------
    private double varianceBound(
            double mean, int trials)
    {
        return sumSquares / visits
                - mean * mean
                + Math.sqrt((2 * Math.log(trials)) / visits);
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return visits + " (" + (sum / visits) + ")";
    }


    //--------------------------------------------------------------------
    public static class VisitSelector
            implements MctsSelector<Ucb1TunedValue3> {
        @Override public int compare(
                Ucb1TunedValue3 a, Ucb1TunedValue3 b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucb1TunedValue3 value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucb1TunedValue3> {
        @Override public int compare(
                Ucb1TunedValue3 a, Ucb1TunedValue3 b) {
            return Double.compare(a.mean(), b.mean());
        }

        @Override
        public double asDouble(Ucb1TunedValue3 value) {
            return value.mean();
        }
    }
}
