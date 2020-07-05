package ao.chess.v2.engine.mcts.rollout;

import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:56:58 PM
 */
public class MctsDeepRolloutImpl
    implements MctsRollout
{
    //--------------------------------------------------------------------
//    private static final int maxDepth = 2;
    private static final int maxDepth = 999;
//    private static final int minDepth = 10;
    private static final int minDepth = 1;


    //--------------------------------------------------------------------
    private final int iterations;
    private final int repetitions;

    private final int[] moves = new int[Move.MAX_PER_PLY];
    private final int[] pseudoMoves = new int[Move.MAX_PER_PLY];
    private final double[] expectation = new double[Move.MAX_PER_PLY];


    //--------------------------------------------------------------------
    public MctsDeepRolloutImpl()
    {
        this(1, 1);
    }


    public MctsDeepRolloutImpl(int repetitions, int iterations)
    {
        this.repetitions = repetitions;
        this.iterations = iterations;
    }


    @Override
    public MctsRollout prototype() {
        return new MctsDeepRolloutImpl(repetitions, iterations);
    }


    //--------------------------------------------------------------------
    @Override
    public double monteCarloPlayout(
            State fromState, MctsHeuristic heuristic)
    {
        double sum = 0;
        for (int i = 0; i < repetitions; i++) {
            State curState =
                    (i == (repetitions - 1))
                            ? fromState
                            : fromState.prototype();

            sum += monteCarloPlayoutIteration(curState, heuristic);
        }
        return sum / repetitions;
    }


    private double monteCarloPlayoutIteration(
            State position, MctsHeuristic heuristic)
    {
        int depth = 0;

        Colour fromPov = position.nextToAct();
        State  state   = position;
        while (! state.isDrawnBy50MovesRule())
        {
            int move;

            if (depth > minDepth && depth < maxDepth) {
                move = bestMove(state, heuristic);
            }
            else {
                int nMoves = position.legalMoves(moves, pseudoMoves);
                if (nMoves == 0) {
                    move = -1;
                }
                else {
                    move = moves[(int) (Math.random() * nMoves)];
                }
            }

            if (move == -1) {
                return state.knownOutcomeOrNull()
                        .valueFor( fromPov );
            }
            Move.apply(move, state);

            depth++;
        }
        return MaterialEvaluation.evaluate(state, fromPov);
    }


    //--------------------------------------------------------------------
    private int bestMove(State position, MctsHeuristic heuristic)
    {
        int nMoves = position.legalMoves(moves, pseudoMoves);
        if (nMoves == 0) {
            return -1;
        }

        Colour fromPov = position.nextToAct();

        State    state       = position.prototype();
//        int   [] count       = new int   [ nMoves ];
//        double[] expectation = new double[ nMoves ];
        for (int m = 0; m < nMoves; m++) {
            expectation[m] = 0;
            for (int i = 0; i < iterations; i++) {
                int move = Move.apply(moves[m], state);
                expectation[m] += computeMonteCarloPlayout(
                        state, heuristic, fromPov);
                Move.unApply(move, state);
//                count[ m ]++;
            }
        }

        return optimize(nMoves/*, moves, count, expectation*/);
    }


    //--------------------------------------------------------------------
    private int optimize(
            int      nMoves//,
//            int   [] moves,
//            int   [] count,
//            double[] expectation
    )
    {
        double maxEv      = -1;
        int    maxEvIndex = -1;
        for (int m = 0; m < nMoves; m++) {
//            double ev = expectation[ m ] / count[ m ];
            double ev = expectation[ m ]/* / nSims*/;
            if (ev > maxEv) {
                maxEv      = ev + Math.random() / 1000;
                maxEvIndex = m;
            }
        }
        return moves[ maxEvIndex ];
    }


    //--------------------------------------------------------------------
    private double computeMonteCarloPlayout(
            State fromState,
            MctsHeuristic heuristic,
            Colour fromPov
    ) {
        State   simState  = fromState.prototype();
        int     nextCount = 0;
        int[]   nextMoves = new int[ Move.MAX_PER_PLY ];
        int[]   moves     = new int[ Move.MAX_PER_PLY ];
        int     nMoves    = simState.moves(moves);

        do
        {
            int     move;
            boolean madeMove = false;

            int[] moveOrder = heuristic.orderMoves(
                    simState, moves, nMoves);
            for (int moveIndex : moveOrder)
            {
                move = Move.apply(moves[ moveIndex ], simState);

                // generate opponent moves
                nextCount = simState.moves(nextMoves);

                if (nextCount < 0) { // if leads to mate
                    Move.unApply(move, simState);
                }
                else {
                    madeMove = true;
                    break;
                }
            }

            if (! madeMove) {
                Outcome outcome = simState.isInCheck(simState.nextToAct())
                        ? Outcome.loses(simState.nextToAct())
                        : Outcome.DRAW;
                return outcome.valueFor(fromPov);
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        while (! simState.isDrawnBy50MovesRule());

        return MaterialEvaluation.evaluate(simState, fromPov);
    }
}