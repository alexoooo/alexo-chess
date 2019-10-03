package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;

/**
 * User: aostrovsky
 * Date: 3-Oct-2009
 * Time: 7:20:53 PM
 */
public class UcbTunedValue implements MctsValue<UcbTunedValue>
{
    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<UcbTunedValue> {
        @Override public UcbTunedValue newValue() {
            return new UcbTunedValue();
        }
    }


    //--------------------------------------------------------------------
    private double sum;
    private int    visits;


    //--------------------------------------------------------------------
    public UcbTunedValue()
    {
        sum    = 0;
        visits = 0;
    }


    //--------------------------------------------------------------------
    @Override
    public int visits() {
        return visits;
    }


    @Override
    public void update(double winRate)
    {
        sum += winRate;
        visits++;
    }


    @Override
    public double confidenceBound(
            UcbTunedValue transpositionValue,
            UcbTunedValue withRespectToParent)
    {
        double mean   = sum / visits;
        double v      = Math.max(0.001, mean * (1.0 - mean));
        int    parent = withRespectToParent.visits;

        return mean + Math.sqrt(v * Math.log(parent) / visits);
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return visits + " (" + (sum / visits) + ")";
    }


    //--------------------------------------------------------------------
    public static class VisitSelector
            implements MctsSelector<UcbTunedValue> {
        @Override public int compare(
                UcbTunedValue a, UcbTunedValue b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(UcbTunedValue value) {
            return value.visits;
        }
    }
}
