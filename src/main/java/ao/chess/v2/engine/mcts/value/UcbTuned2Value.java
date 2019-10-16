package ao.chess.v2.engine.mcts.value;

import ao.chess.v2.engine.mcts.MctsSelector;
import ao.chess.v2.engine.mcts.MctsValue;

/**
 * User: aostrovsky
 * Date: 3-Oct-2009
 * Time: 7:20:53 PM
 */
public class UcbTuned2Value implements MctsValue<UcbTuned2Value>
{
    //--------------------------------------------------------------------
    public static class Factory
            implements MctsValue.Factory<UcbTuned2Value> {
        @Override public UcbTuned2Value newValue() {
            return new UcbTuned2Value();
        }
    }


    //--------------------------------------------------------------------
    private double sum;
    private int    visits;


    //--------------------------------------------------------------------
    public UcbTuned2Value()
    {
        sum    = 0;
        visits = 0;
    }



    //--------------------------------------------------------------------
    @Override
    public int visits() {
        return visits;
    }


    //--------------------------------------------------------------------
    @Override
    public void update(double winRate)
    {
        sum += winRate;
        visits++;
    }


    //--------------------------------------------------------------------
    @Override public double confidenceBound(
            int parentChoices,
            UcbTuned2Value withRespectToParent)
    {
        double mean   = sum / visits;
        double v      = Math.max(0.001, mean * (1.0 - mean));
        int    parent = withRespectToParent.visits;

        return mean + Math.sqrt(v * Math.log(parent) / visits) +
                Math.log(parent) / visits;
    }


    //--------------------------------------------------------------------
    @Override public String toString() {
        return visits + " (" + (sum / visits) + ")";
    }


    //--------------------------------------------------------------------
    public static class VisitSelector
            implements MctsSelector<UcbTuned2Value> {
        @Override public int compare(
                UcbTuned2Value a, UcbTuned2Value b) {
            return a.visits - b.visits;
        }

        @Override
        public double asDouble(UcbTuned2Value value) {
            return value.visits;
        }
    }
}