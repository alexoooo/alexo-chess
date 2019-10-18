package ao.chess.v2.engine.mcts.rollout;

import ao.chess.v1.util.Io;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.math.rand.Rand;

/**
 * User: alex
 * Date: 27-Sep-2009
 * Time: 11:56:58 PM
 */
public class MctsRolloutImpl
    implements MctsRollout
{
    //--------------------------------------------------------------------
//    public static class Factory implements MctsRollout.Factory {
//        @Override public MctsRollout newRollout() {
//            return new MctsRolloutImpl();
//        }
//    }

    //--------------------------------------------------------------------
    private final int     nSims;
    private final boolean opt;

    private int[] nextMoves = new int[ Move.MAX_PER_PLY ];
    private int[] moves = new int[ Move.MAX_PER_PLY ];


    //--------------------------------------------------------------------
    public MctsRolloutImpl(boolean optimize)
    {
        this(1, optimize);
    }

    public MctsRolloutImpl(int accuracy, boolean optimize)
    {
        nSims = accuracy;
        opt   = optimize;
    }


    //--------------------------------------------------------------------
    @Override
    public MctsRollout prototype() {
        return new MctsRolloutImpl(nSims, opt);
    }


    //--------------------------------------------------------------------
    @Override
    public double monteCarloPlayout(
            State fromState, MctsHeuristic heuristic)
    {
        double sum = 0;
        for (int i = 0; i < nSims; i++) {
            State curState =
                    (i == (nSims - 1))
                    ? fromState
                    : fromState.prototype();

            sum += computeMonteCarloPlayout(curState, heuristic);
        }
        double average = sum / nSims;
        return Math.abs(average - 0.5) < 0.001
                ? 0.5
                : average > 0.5
                ? 0.9
                : 0.1;
    }


    private double computeMonteCarloPlayout(
            State fromState, MctsHeuristic heuristic) {
        Colour  pov       = fromState.nextToAct();
        State   simState  = fromState;
        int     nextCount = 0;
        int     nMoves    = simState.moves(moves);
        Outcome outcome   = null;

        boolean wasDrawnBy50MovesRule = false;
        do
        {
//            if (opt && simState.pieceCount() <= DeepOracle.instancePieceCount) {
//                return oracleValue(simState, pov, moves, nMoves);
//            }

//            if (! Representation.unpackStream(
//                    Representation.packStream(
//                            simState)).equals( simState )) {
//                System.out.println("PACKING ERROR!!!");
//                System.out.println(simState);
//            }

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
                outcome = simState.isInCheck(simState.nextToAct())
                          ? Outcome.loses(simState.nextToAct())
                          : Outcome.DRAW;
                break;
            }

            {
                int[] tempMoves = nextMoves;
                nextMoves       = moves;
                moves           = tempMoves;
                nMoves          = nextCount;
            }
        }
        while (! (wasDrawnBy50MovesRule =
                    simState.isDrawnBy50MovesRule()));
        if (wasDrawnBy50MovesRule) {
            if (opt) {
                return MaterialEvaluation.evaluate(simState, pov);
            }
            else {
                outcome = Outcome.DRAW;
            }
        }

//        if (opt) {
//            pov = simState.nextToAct();
//        }
        if (outcome == null) {
            return Double.NaN;
        }

        return outcome.valueFor( pov );
    }


    private double oracleValue(
            State from,
            Colour pov,
            int[] moves,
            int nMoves
    ) {
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
}
