package ao.chess.v2.engine.trans;

import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.mcts.MctsScheduler;
import ao.chess.v2.engine.mcts.MctsNode;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.engine.mcts.node.MctsNodeImpl;
import ao.chess.v2.engine.mcts.scheduler.MctsSchedulerImpl;
import ao.chess.v2.state.State;
import ao.chess.v2.state.Move;
import ao.chess.v1.util.Io;
import it.unimi.dsi.fastutil.longs.*;

/**
 * User: aostrovsky
 * Date: 12-Oct-2009
 * Time: 10:53:05 AM
 */
public class TransPlayer implements Player
{
    //--------------------------------------------------------------------
    private final Long2ObjectMap<TransNode> transTable =
            new Long2ObjectOpenHashMap<TransNode>();


    //--------------------------------------------------------------------
    private State     prevState = null;
    private TransNode prevPlay  = null;


    //--------------------------------------------------------------------
    @Override
    public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        TransNode root = null;
        if (prevState != null && prevPlay != null) {
            root = prevPlay.childMatching(
                    action(prevState, position));
        }

        if (root == null) {
            root = new TransNode(position);
        } else {
            Io.display("Recycling " + root);
        }
        LongSet states = new LongOpenHashSet();
        root.addStates(states);
        //Io.display("Unique states " + states.size());
        transTable.keySet().retainAll(states);

        MctsScheduler scheduler = new MctsSchedulerImpl.Factory()
                .newScheduler(timeLeft, timePerMove, timeIncrement);

//        int  count  = 0;
//        long lastReport = System.currentTimeMillis();
        while (scheduler.shouldContinue()) {
            root.runTrajectory(
                    position, transTable);

//            if (count++ != 0 && count % 10000 == 0) {
//                long timer  = System.currentTimeMillis() - lastReport;
//                long before = System.currentTimeMillis();
//                Io.display( root );
//                Io.display( root.bestMove().information() );
//                Io.display( "took " + timer + " | " +
//                        (System.currentTimeMillis() - before) );
//                lastReport = System.currentTimeMillis();
//            }
        }

        TransAction act = root.bestMove();
        if (act == null) return -1; // game is done 

        prevPlay  = act.node();
        prevState = position.prototype();
        Move.apply(act.action(), prevState);

        return act.action();
    }


    //--------------------------------------------------------------------
    private int action(State from, State to)
    {
        int[] moves  = new int[ Move.MAX_PER_PLY ];
        int   nMoves = from.moves( moves );

        for (int i = 0; i < nMoves; i++) {
            int move = Move.apply(moves[i], from);
            if (from.equals( to )) {
                Move.unApply(move, from);
                return move;
            }
            Move.unApply(move, from);
        }
        return -1;
    }
}
