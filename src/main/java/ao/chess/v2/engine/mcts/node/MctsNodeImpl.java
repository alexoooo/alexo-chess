package ao.chess.v2.engine.mcts.node;

import ao.chess.v2.engine.mcts.*;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import it.unimi.dsi.fastutil.longs.LongCollection;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 5:19:10 PM
 */
public class MctsNodeImpl<V extends MctsValue<V>>
        implements MctsNode<V>
{
    //--------------------------------------------------------------------
    public static class Factory<V extends MctsValue<V>>
            implements MctsNode.Factory<V> {
        @Override
        public MctsNodeImpl<V> newNode(
                State                state,
                MctsValue.Factory<V> valueFactory)
        {
            return new MctsNodeImpl<V>(/*state,*/ valueFactory);
        }
    }


    //--------------------------------------------------------------------
    private V                 value; // TODO: inline to save memory
//    private long              stateHash;
    private int[]             acts; // TODO: externalize to save memory
    private MctsNodeImpl<V>[] kids;


    //--------------------------------------------------------------------
    public MctsNodeImpl(/*State state,*/ MctsValue.Factory<V> valueFactory) {
        value     = valueFactory.newValue();
//        stateHash = state.longHashCode();
        acts      = null;
        kids      = null;
    }


    //--------------------------------------------------------------------
    public boolean isUnvisited() {
//        return acts == null || value.visits() < 1024;
        return acts == null;
    }


    //--------------------------------------------------------------------
    @Override
    public void runTrajectory(
            State                 fromProtoState,
            MctsValue.Factory<V>  values,
            MctsRollout           mcRollout,
            MctsHeuristic         heuristic)
    {
        State cursor = fromProtoState.prototype();
        List<MctsNodeImpl<V>> path = new ArrayList<>();
        path.add(this);

        while (! path.get( path.size() - 1 ).isUnvisited())
        {
            MctsNodeImpl<V> node = path.get( path.size() - 1 );

            MctsNodeImpl<V> selectedChild =
                    node.descendByBandit(cursor, heuristic, values);
            if (selectedChild == null) {
                break;
            }

            path.add( selectedChild );
        }

        MctsNodeImpl<V> leaf = path.get( path.size() - 1 );
        if (leaf.kids == null) {
            leaf.initiateKids(cursor);
        }

        double leafValue = mcRollout.monteCarloPlayout(cursor, heuristic);
        backupMcValue(path, leafValue);
    }


    //--------------------------------------------------------------------
    private MctsNodeImpl<V> descendByBandit(
            State                 cursor,
            MctsHeuristic         heuristic,
            MctsValue.Factory<V>  values)
    {
        if (kids.length == 0) {
            return null;
        }

        double greatestValue      = Double.NEGATIVE_INFINITY;
//        double greatestValue      = Double.POSITIVE_INFINITY;
        int    greatestValueIndex = -1;
        for (int i = 0; i < kids.length; i++) {
            MctsNodeImpl<V> kid = kids[ i ];

            double banditValue;
            if (kid == null || kid.isUnvisited()) {
                banditValue = heuristic.firstPlayUrgency(acts[i]);
//                banditValue = -heuristic.firstPlayUrgency(acts[i]);
            } else {
                banditValue = kid.value.confidenceBound(
                        kids.length, value);
            }

            if (banditValue > greatestValue) {
//            if (banditValue < greatestValue) {
                greatestValue      = banditValue;
                greatestValueIndex = i;
            }
        }
        if (greatestValueIndex == -1) {
            return null;
        }

        Move.apply(acts[greatestValueIndex], cursor);
        if (kids[ greatestValueIndex ] == null) {
            kids[ greatestValueIndex ] = new MctsNodeImpl<>(values);
        }
        return kids[ greatestValueIndex ];
    }

    @SuppressWarnings("unchecked")
    private void initiateKids(State fromState) {
        acts = fromState.legalMoves();
        kids = (acts == null)
               ? null : new MctsNodeImpl[ acts.length ];
    }


    //--------------------------------------------------------------------
    private void backupMcValue(
            List<MctsNodeImpl<V>> path,
            double                leafPlayout)
    {
//        double reward = 1.0 - leafPlayout;
        double reward = leafPlayout;

        for (int i = path.size() - 1; i >= 0; i--)
        {
            reward = 1.0 - reward;
            path.get(i).value.update(reward);
        }
    }


    //--------------------------------------------------------------------
    @Override
    public MctsAction<V> bestMove(MctsSelector<V> selector) {
        if (kids == null || kids.length == 0) {
            return null;
        }

        int bestAct = -1;
        MctsNodeImpl<V> bestKid = null;
        for (int i = 0, kidsLength = kids.length; i < kidsLength; i++) {
            MctsNodeImpl<V> kid = kids[i];
            if (kid != null && (bestKid == null ||
                    selector.compare(
                            bestKid.value, kid.value) < 0)) {
                bestKid = kid;
                bestAct = acts[i];
            }
        }

        if (bestKid == null) {
            return null;
        }

        return new MctsAction<>(bestAct, bestKid);
    }


    @Override
    public double moveScore(int action, MctsSelector<V> selector) {
        if (kids == null || kids.length == 0) {
            return Double.NaN;
        }

        for (int i = 0; i < acts.length; i++) {
            if (acts[i] == action) {
                return kids[i] == null
                        ? 0
                        : selector.asDouble(kids[i].value);
            }
        }
        return Double.NaN;
    }


    @Override
    public int[] rankMoves(MctsSelector<V> selector) {
        if (kids == null || kids.length == 0) {
            return new int[0];
        }

        List<Integer> kidIndexes = new ArrayList<>();
        for (int i = 0; i < kids.length; i++) {
            kidIndexes.add(i);
        }
        kidIndexes.sort(Comparator.comparingDouble(index ->
                kids[index] == null
                ? Double.MAX_VALUE
                : -selector.asDouble(kids[index].value)));

        int[] ranked = new int[kids.length];
        for (int i = 0; i < kids.length; i++) {
            ranked[i] = acts[kidIndexes.get(i)];
        }

        return ranked;
    }


    //--------------------------------------------------------------------
    @Override
    public MctsNode childMatching(int action) {
        if (acts == null) return null;

        for (int i = 0, actsLength = acts.length; i < actsLength; i++) {
            int act = acts[i];
            if (act == action) {
                return kids[i];
            }
        }
        
        return null;
    }


    //--------------------------------------------------------------------
    @Override
    public void addStates(LongCollection to) {
//        to.add(stateHash);
//
//        if (kids == null) return;
//        for (MctsNodeImpl kid : kids) {
//            if (kid == null) continue;
//            kid.addStates(to);
//        }
    }


    //--------------------------------------------------------------------
    @Override
    public int maxDepth() {
        if (kids == null || kids.length == 0) {
            return 0;
        }

        int depth = 0;
        for (MctsNodeImpl kid : kids) {
            if (kid == null) continue;
            depth = Math.max(depth, kid.maxDepth());
        }
        return depth + 1;
    }


    @Override
    public int minDepth() {
        if (kids == null || kids.length == 0) {
            return 0;
        }

        int minDepth = Integer.MAX_VALUE;
        for (MctsNodeImpl kid : kids) {
            if (kid == null) {
                return 1;
            }
            minDepth = Math.min(minDepth, kid.minDepth());
        }
        return minDepth + 1;
    }


    @Override
    public int leafCount() {
        if (kids == null) {
            return 1;
        }

        int leafCount = 0;
        for (MctsNodeImpl kid : kids) {
            if (kid == null) continue;
            leafCount += kid.leafCount();
        }
        return leafCount;
    }


    @Override
    public int nodeCount() {
        if (kids == null) return 1;

        int size = 1;
        for (MctsNodeImpl kid : kids) {
            if (kid == null) continue;
            size += kid.nodeCount();
        }
        return size;
    }


    //--------------------------------------------------------------------
    private int uniqueSize() {
        LongSet states = new LongOpenHashSet();
        addStates(states);
        return states.size();
    }


    //--------------------------------------------------------------------
    @Override
    public String toString() {
        return //nodeCount()       + " | " +
               maxDepth()   + " | " +
               value.toString();
    }
}
