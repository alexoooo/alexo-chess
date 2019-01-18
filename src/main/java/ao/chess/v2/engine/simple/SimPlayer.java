package ao.chess.v2.engine.simple;

import ao.chess.v1.util.Io;
import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.Pool;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.chess.v2.state.Status;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;

/**
 * User: aostrovsky
 * Date: 16-Sep-2009
 * Time: 12:00:56 AM
 */
public class SimPlayer implements Player
{
    //--------------------------------------------------------------------
    private final boolean OPTIMIZE;


    //--------------------------------------------------------------------
    public SimPlayer(boolean optimize)
    {
        OPTIMIZE = optimize;
    }


    //--------------------------------------------------------------------
    public int move(
            State position,
            int   timeLeft,
            int   timePerMove,
            int   timeIncrement)
    {
        int[] moves  = new int[Move.MAX_PER_PLY];
        int   nMoves = position.legalMoves(moves);
        if (nMoves <= 0) return -1;

        int      totalCount  = 0;
        long     start       = System.currentTimeMillis();
        State    state       = position.prototype();
        int   [] count       = new int   [ nMoves ];
        double[] expectation = new double[ nMoves ];
        while ((System.currentTimeMillis() - start) < timePerMove) {
            for (int m = 0; m < nMoves; m++)
            {
                int move = Move.apply(moves[m], state);
                expectation[m] += simulate(
                        state, position.nextToAct());
                Move.unApply(move, state);
                count[ m ]++;

                if (totalCount++ != 0 && totalCount % 25000 == 0) {
                    optimize(nMoves, moves, count, expectation);
                }
            }
        }

        return optimize(nMoves, moves, count, expectation);
    }

    private int optimize(
            int      nMoves,
            int   [] moves,
            int   [] count,
            double[] expectation) {
        double maxEv      = -1;
        int    maxEvIndex = -1;
        for (int m = 0; m < nMoves; m++) {
            double ev = expectation[ m ] / count[ m ];
            if (ev > maxEv) {
                maxEv      = ev;
                maxEvIndex = m;
            }
        }
        int bestMove = moves[ maxEvIndex ];
        Io.display("Best so far is: " +
                        maxEv + " | " +
                        Move.toString(bestMove));
        return moves[ maxEvIndex ];
    }


    //--------------------------------------------------------------------
    private double simulate(final State state, final Colour fromPov)
    {
        if (OPTIMIZE) {
            List<Callable<Double>> tasks =
                    new ArrayList<Callable<Double>>(Pool.CORES);
            for (int i = 0; i < Pool.CORES; i++) {
                tasks.add(new Callable<Double>() {
                    public Double call() throws Exception {
                        return doSimulate(state, fromPov);
                    }
                });
            }

            try {
                double sum = 0;
                for (Future<Double> value : Pool.EXEC.invokeAll(tasks)) {
                    sum += value.get();
                }
                return sum / Pool.CORES;
            } catch (Exception e) {
                e.printStackTrace();
                return Double.NaN;
            }
        } else {
            return doSimulate(state, fromPov);
        }
    }

    private double doSimulate(State protoState, Colour fromPov)
    {
        State   state     = protoState.prototype();
        int     nextCount = 0;
        int[]   nextMoves = new int[ Move.MAX_PER_PLY ];
        int[]   moves     = new int[ Move.MAX_PER_PLY ];
        int     nMoves    = state.moves(moves);
        Outcome outcome   = null;

        if (nMoves < 1) return Double.NaN;

        boolean wasDrawnBy50MovesRule = false;
        do
        {
            int     move;
            boolean madeMove = false;

            int[] moveOrder = MovePicker.pickRandom(nMoves);
            for (int moveIndex : moveOrder)
            {
                move = Move.apply(moves[ moveIndex ], state);

                // generate opponent moves
                nextCount = state.moves(nextMoves);

                if (nextCount < 0) { // if leads to mate
                    Move.unApply(move, state);
                } else {
                    madeMove = true;
                    break;
                }
            }
            if (! madeMove) {
                outcome = state.isInCheck(state.nextToAct())
                          ? Outcome.loses(state.nextToAct())
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
                    state.isDrawnBy50MovesRule()));
        if (wasDrawnBy50MovesRule) {
            outcome = Outcome.DRAW;
        }

        return outcome == null
               ? Double.NaN
               : outcome.valueFor(fromPov);
    }
}
