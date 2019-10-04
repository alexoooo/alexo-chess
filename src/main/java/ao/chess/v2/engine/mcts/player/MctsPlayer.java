package ao.chess.v2.engine.mcts.player;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.mcts.*;
import ao.chess.v2.engine.mcts.message.MctsAction;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.math.rand.Rand;
import it.unimi.dsi.fastutil.longs.LongLists;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 12:52:02 PM
 */
public class MctsPlayer implements Player
{
    //--------------------------------------------------------------------
    private final MctsNode.Factory      nodes;
    private final MctsValue.Factory     values;
    private final MctsRollout           rollouts;
    private final MctsSelector          sellectors;
    private final MctsHeuristic         heuristics;
    private final MctsScheduler.Factory schedulers;
    private final TranspositionTable    transTable;

    private final String name;

    private final int[] moves  = new int[ Move.MAX_PER_PLY ];

    private State              prevState = null;
    private MctsNode           prevPlay  = null;

    private MctsNode previousRootOrNull = null;


    //--------------------------------------------------------------------
    public <V extends MctsValue<V>> MctsPlayer(
            MctsNode.Factory<V>   nodeFactory,
            MctsValue.Factory<V>  valueFactory,
            MctsRollout           rollOutInstance,
            MctsSelector<V>       selectorInstance,
            MctsHeuristic         heuristicInstance,
            TranspositionTable<V> transpositionTable,
            MctsScheduler.Factory schedulerFactory)
    {
        this(nodeFactory,
                valueFactory,
                rollOutInstance,
                selectorInstance,
                heuristicInstance,
                transpositionTable,
                schedulerFactory,
                "");
    }


    public <V extends MctsValue<V>> MctsPlayer(
            MctsNode.Factory<V>   nodeFactory,
            MctsValue.Factory<V>  valueFactory,
            MctsRollout           rollOutInstance,
            MctsSelector<V>       selectorInstance,
            MctsHeuristic         heuristicInstance,
            TranspositionTable<V> transpositionTable,
            MctsScheduler.Factory schedulerFactory,
            String name)
    {
        nodes       = nodeFactory;
        values      = valueFactory;
        rollouts    = rollOutInstance;
        sellectors  = selectorInstance;
        heuristics  = heuristicInstance;
        transTable  = transpositionTable;
        schedulers  = schedulerFactory;

        this.name = name;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public MctsPlayer prototype() {
        return new MctsPlayer(
                nodes,
                values,
                rollouts.prototype(),
                sellectors,
                heuristics,
                transTable,
                schedulers,
                name);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @SuppressWarnings("unchecked")
    @Override
    public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        int oracleAction = oracleAction(position);
        if (oracleAction != -1) {
//            Sched.sleep(2500);
            return oracleAction;
        }

        MctsNode root = null;
        if (prevState != null && prevPlay != null) {
            root = prevPlay.childMatching(
                    action(prevState, position));
        }

        if (root == null) {
            root = nodes.newNode(position, values);
            transTable.retain( LongLists.EMPTY_LIST );
        } else {
            Io.display("Recycling " + root);

//            LongSet states = new LongOpenHashSet();
//            root.addStates(states);
//            Io.display("Unique states " + states.size());
//            transTable.retain(states);
        }

        MctsScheduler scheduler = schedulers.newScheduler(
                timeLeft, timePerMove, timeIncrement);

        int  count  = 0;
//        long lastReport = System.currentTimeMillis();
        while (scheduler.shouldContinue()) {
            for (int i = 0; i < 100; i++) {
                root.runTrajectory(
                        position, values, rollouts, transTable, heuristics);
                count++;
            }

            if (count != 0 && count % 10000 == 0) {
//            if (count != 0 && count % 10 == 0) {
//                long timer  = System.currentTimeMillis() - lastReport;
//                long before = System.currentTimeMillis();
//                Io.display( root );
                MctsAction bestMove = root.bestMove(sellectors);
                if (bestMove == null) {
                    break;
                }
                else {
                    Io.display( root.bestMove(sellectors).information() );
                }

//                Io.display( "took " + timer + " | " +
//                        (System.currentTimeMillis() - before) );
//                lastReport = System.currentTimeMillis();
            }
        }

        MctsAction act = root.bestMove(sellectors);
        if (act == null) {
            prevPlay = null;
            prevState = null;
            return -1; // game is done
        }

        prevPlay = act.node();
        prevState = position.prototype();
        Move.apply(act.action(), prevState);

        return act.action();
    }


    //-----------------------------------------------------------------------------------------------------------------
    MctsNode moveInternal(
            State position,
            MctsScheduler scheduler)
    {
        MctsNode root = null;

        if (prevState != null) {
            if (prevState.equals(position)) {
                root = previousRootOrNull;
            }
            else if (prevPlay != null) {
                root = prevPlay.childMatching(
                        action(prevState, position));
            }
        }

        if (root == null) {
            root = nodes.newNode(position, values);
            transTable.retain( LongLists.EMPTY_LIST );
        }
        else {
            MctsAction act = root.bestMove(sellectors);
            Io.display( "Recycling " + root + " | " + (act == null ? "" : act.information()) + " | " + name);
        }

        int count = 0;
        while (scheduler.shouldContinue()) {
            for (int i = 0; i < 100; i++) {
                root.runTrajectory(
                        position, values, rollouts, transTable, heuristics);
                count++;
            }

            if (count != 0 && count % 100_000 == 0) {
                MctsAction bestMove = root.bestMove(sellectors);
                if (bestMove == null) {
//                    Io.display( "!! null move?" );
                    prevState = null;
                    prevPlay = null;
                    break;
                }
                else {
                    Io.display( root.bestMove(sellectors).information() );
                }
            }
        }

//        MctsAction act = root.bestMove(sellectors);
//        if (act == null) return -1; // game is done
//
//        prevPlay = act.node();
//        prevState = position.prototype();
//        Move.apply(act.action(), prevState);
//        prevPlay = root;
        prevState = position.prototype();

        previousRootOrNull = root;
        return root;
    }


    void notifyMoveInternal(State position, int action) {
        if (previousRootOrNull == null) {
            return;
        }

        prevPlay = previousRootOrNull.childMatching(action);
        prevState = position.prototype();
        Move.apply(action, prevState);
    }


    void clearInternal() {
        previousRootOrNull = null;
        prevPlay = null;
        prevState = null;
    }


    double moveScoreInternal(
            MctsNode node,
            int move
    ) {
        return node.moveScore(move, sellectors);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private int oracleAction(State from) {
        if (from.pieceCount() > 5) return -1;

        boolean canDraw     = false;
        int     bestOutcome = 0;
        int     bestMove    = -1;
        for (int legalMove : from.legalMoves()) {
            Move.apply(legalMove, from);
            DeepOutcome outcome = DeepOracle.INSTANCE.see(from);
            Move.unApply(legalMove, from);
            if (outcome == null || outcome.isDraw()) {
                canDraw = true;
                continue;
            }

            if (outcome.outcome().winner() == from.nextToAct()) {
                if (bestOutcome <= 0 ||
                        bestOutcome > outcome.plyDistance() ||
                        (bestOutcome == outcome.plyDistance() &&
                            Rand.nextBoolean())) {
                    Io.display(outcome.outcome() + " in " +
                                outcome.plyDistance() + " with " +
                                Move.toString(legalMove));
                    bestOutcome = outcome.plyDistance();
                    bestMove    = legalMove;
                }
            } else if (! canDraw && bestOutcome <= 0
                            && bestOutcome > -outcome.plyDistance()) {
                Io.display(outcome.outcome() + " in " +
                                outcome.plyDistance() + " with " +
                                Move.toString(legalMove));
                bestOutcome = -outcome.plyDistance();
                bestMove    = legalMove;
            }
        }

        return (bestOutcome <= 0 && canDraw)
                ? -1 : bestMove;
    }


    //--------------------------------------------------------------------
    private int action(State from, State to)
    {
        int nMoves = from.moves( moves );

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
