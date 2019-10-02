package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;

/**
 * User: aostrovsky
 * Date: 30-Sep-2009
 * Time: 5:14:28 PM
 */
public class Ucbt2Value implements MctsValue<Ucbt2Value>
{
    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<Ucbt2Value> {
        @Override public Ucbt2Value newValue() {
            return new Ucbt2Value();
        }
    }


    //--------------------------------------------------------------------
    private double sum;
    private double sumSquares;
    private int    visits;


    //--------------------------------------------------------------------
    public Ucbt2Value()
    {
        sum        = 0;
        sumSquares = 0;
        visits     = 0;
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
            Ucbt2Value transpositionValue,
            Ucbt2Value withRespectToParent)
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
            implements MctsSelector<Ucbt2Value> {
        @Override public int compare(
                Ucbt2Value a, Ucbt2Value b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(Ucbt2Value value) {
            return value.visits;
        }
    }

    public static class MeanSelector
            implements MctsSelector<Ucbt2Value> {
        @Override public int compare(
                Ucbt2Value a, Ucbt2Value b) {
            return Double.compare(a.mean(), b.mean());
        }

        @Override
        public double asDouble(Ucbt2Value value) {
            return value.visits;
        }
    }
}