package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;


public class Ucb1TunedValue10 implements MctsValue<Ucb1TunedValue10>
{
    //--------------------------------------------------------------------
    private static final double exploration = 1.0;


    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucb1TunedValue10> {
        @Override public Ucb1TunedValue10 newValue() {
            return new Ucb1TunedValue10();
        }
    }


    //--------------------------------------------------------------------
    private double sum;
    private double sumSquares;
    private int    visits;


    //--------------------------------------------------------------------
    public Ucb1TunedValue10()
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
            Ucb1TunedValue10 transpositionValue,
            Ucb1TunedValue10 withRespectToParent)
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
            implements MctsSelector<Ucb1TunedValue10> {
        @Override public int compare(
                Ucb1TunedValue10 a, Ucb1TunedValue10 b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucb1TunedValue10 value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucb1TunedValue10> {
        @Override public int compare(
                Ucb1TunedValue10 a, Ucb1TunedValue10 b) {
            return Double.compare(a.mean(), b.mean());
        }

        @Override
        public double asDouble(Ucb1TunedValue10 value) {
            return value.mean();
        }
    }
}