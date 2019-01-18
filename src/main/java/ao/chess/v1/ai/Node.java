package ao.chess.v1.ai;

import ao.chess.v1.model.Board;
import ao.chess.v1.old.Evaluation;
import ao.chess.v1.util.Io;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 *
 */
public class Node
{
    //--------------------------------------------------------------------
    private static final int EVAL_RANGE = 550;


    //--------------------------------------------------------------------
    private int    visits;
    private Reward rewardSum;

    private Position position;

    private Node kids[];
    private int  acts[];


    //--------------------------------------------------------------------
    public Node(Position positionKey)
    {
        position  = positionKey;
        visits    = 0;
        rewardSum = new Reward();
    }
    public Node(Board state)
    {
        this(state.transpositionKey());
    }


    //--------------------------------------------------------------------
    public int size()
    {
        if (kids == null) return 1;

        int size = 0;
        for (Node nextChild : kids)
        {
            size += nextChild.size();
        }
        return size + 1;
    }

    public int depth()
    {
        if (kids == null) return 1;

        int depth = 0;
        for (Node kid : kids)
        {
            depth = Math.max(depth, kid.depth());
        }
        return depth + 1;
    }

    public int visits()
    {
        return visits;
    }


    //--------------------------------------------------------------------
    public Node childMatching(Board board)
    {
        if (kids == null) return null;

        Position position = board.transpositionKey();
        for (Node kid : kids)
        {
            if (position.equals( kid.position ))
            {
                return kid;
            }
        }
        return null;
    }


    //--------------------------------------------------------------------
    public void addTo(Map<Position, Node> transposition)
    {
        transposition.put(position, this);
    }

    public void addLineageTo(Map<Position, Node> transposition)
    {
        addTo( transposition );

        if (kids == null) return;
        for (Node kid : kids)
        {
            kid.addLineageTo( transposition );
        }
    }


    //--------------------------------------------------------------------
    public Action optimize()
    {
        if (kids == null) return null;

        Node   optimal       = this;
        int    optimalAct    = 0;
        double optimalReward = Long.MIN_VALUE;
        for (int i = 0; i < kids.length; i++)
        {
            Node   nextChild = kids[ i ];
            int    act       = acts[ i ];
            double reward    = nextChild.averageReward();

            if (reward > optimalReward)
            {
                optimal       = nextChild;
                optimalAct    = act;
                optimalReward = reward;
            }
        }

        Io.display("best move is " + optimal.averageReward());
        return new Action(optimalAct, optimal);
    }


    //--------------------------------------------------------------------
    private double averageReward()
    {
        return rewardSum.averagedOver( visits + 1 );
    }

    private boolean unvisited()
    {
        return visits == 0;
    }


    //--------------------------------------------------------------------
    public void strategize(
            Board               state,
            Map<Position, Node> transposition,
            boolean             optimize)
    {
        List<Action> path = new ArrayList<Action>();
        path.add(new Action(0, this));

        while (! path.get( path.size()-1 ).NODE.unvisited())
        {
            Node node = path.get( path.size()-1 ).NODE;
            Action selectedChild =
                    node.descendByUCB1(state, transposition);
            if (selectedChild == null) break;

            path.add( selectedChild );
            state.makeMove( selectedChild.ACT );
        }

        Node leaf = path.get( path.size()-1 ).NODE;
        propagateValue(
                path,
                optimize
                    ? leaf.evalValue(state, transposition)
                    : leaf.monteCarloValue(state, transposition),
                state);
    }

    private void propagateValue(
            List<Action> path, Reward reward, Board state)
    {
        Reward maxiMax = reward.compliment();
//        Reward maxiMax = reward;
        for (int i = path.size() - 1; i >= 0; i--)
        {
            Action step = path.get(i);
            if (step.ACT != 0)
            {
                state.unmakeMove( step.ACT );
            }

            step.NODE.rewardSum = step.NODE.rewardSum.plus(maxiMax);
            step.NODE.visits++;

            maxiMax = maxiMax.compliment();
        }
    }


    //--------------------------------------------------------------------
    private Action descendByUCB1(
            Board               state,
            Map<Position, Node> transposition)
    {
        if (kids == null)
        {
            populateKids(state, transposition);
        }

        double greatestUtc   = Long.MIN_VALUE;
        Action greatestChild = null;
        for (int i = 0; i < kids.length; i++)
        {
            Node kid = kids[ i ];
            int  act = acts[ i ];

            double utcValue;
            if (kid.unvisited())
            {
//                utcValue =
//                        Integer.MAX_VALUE
//                            + ((Move.capture(act) != 0)
//                                ? 100000 : 1000)
//                              * Math.random();
                utcValue = 1.0;
            }
            else
            {
                utcValue =
                    kid.averageReward() +
                    Math.sqrt(Math.log(visits) /
                              (5 * kid.visits));
            }

            if (utcValue > greatestUtc)
            {
                greatestUtc   = utcValue;
                greatestChild = new Action(acts[i], kid);
            }
        }

        return greatestChild;
    }


    //--------------------------------------------------------------------
    private Reward monteCarloValue(
            Board               state,
            Map<Position, Node> transposition)
    {
        int legalMoves[]  = new int[256];
        int movePath[]    = new int[1024];
        int movePathIndex = 0;

        boolean reachedPieceCountGoal = false;

        FullOutcome out;
        do
        {
            int moveCount = state.generateMoves(false, legalMoves, 0);
            if (moveCount > 0)
            {
                int move = RandomBot.random(legalMoves, moveCount);
                movePath[ movePathIndex++ ] = move;
                state.makeMove(move);

                int c = state.pieceCount();
//                if (c <= 20) {
//                    reachedPieceCountGoal = true;
//                }
            }
        }
        while ((out = AlexoChess.outcome(state, null, 0))
                   == FullOutcome.UNDECIDED );

//        if (reachedPieceCountGoal) {
//            System.out.println("monte carlo pieces count goal reached");
//        }

        for (int i = movePathIndex - 1; i >= 0; i--)
        {
            state.unmakeMove( movePath[i] );
        }

        return out.isDraw()
                ? new Reward(0.5)
                : out.scores( state.toMove == Board.WHITE_TO_MOVE )
                  ? new Reward(1.0) : new Reward(0);
    }


    //--------------------------------------------------------------------
    private Reward evalValue(
            Board               state,
            Map<Position, Node> transposition)
    {
//        populateKids(state, transposition);

        FullOutcome out =
                AlexoChess.outcome(
                        state, state.history, state.historyIndex);
        if (out != FullOutcome.UNDECIDED)
        {
//            Io.display(out);
            return out.isDraw()
                    ? new Reward(0.5)
                    : out.scores( state.toMove == Board.WHITE_TO_MOVE )
                      ? new Reward(1.0) : new Reward(0);
        }
       
        double value        = Evaluation.evaluate(state, false);
        double nextToActVal =
                (state.toMove == Board.WHITE_TO_MOVE)
                 ? value : value;
        double cappedVal =
                1.0/(1 + Math.exp(-nextToActVal/EVAL_RANGE));
        return new Reward(cappedVal);
    }

    private void populateKids(
            Board               state,
            Map<Position, Node> transposition)
    {
        if (kids != null) return;

        int legalMoves[] = new int[256];
        int moveCount = state.generateMoves(false, legalMoves, 0);

        kids = new Node[ moveCount ];
        acts = new int[  moveCount ];

        for (int i = 0; i < moveCount; i++)
        {
            int move = legalMoves[ i ];
            if (move == 0) continue;

            state.makeMove(move);
            Position moveStateKey = state.transpositionKey();
            Node existing =
                    transposition.get(moveStateKey);
            if (existing == null)
            {
                Node newChild = new Node(moveStateKey);
                kids[ i ] = newChild;
                newChild.addTo(transposition);
            }
            else
            {
                kids[ i ] = existing;
            }
            acts[ i ] = move;
            state.unmakeMove(move);
        }
    }


    //--------------------------------------------------------------------
    public static class Action
    {
        private final int  ACT;
        private final Node NODE;

        public Action(int act, Node node)
        {
            ACT  = act;
            NODE = node;
        }

        public int act()
        {
            return ACT;
        }
        public Node node()
        {
            return NODE;
        }
    }


    //--------------------------------------------------------------------
    public static void randomBench(Board state)
    {
        int[] legalMoves = new int[128];
        do
        {
            int moveCount = state.generateMoves(false, legalMoves, 0);
            if (moveCount > 0)
            {
                int move = RandomBot.random(legalMoves, moveCount);
                state.makeMove(move);
            }
        }
        while (AlexoChess.outcome(state, null, 0)
                 == FullOutcome.UNDECIDED );
    }
}
