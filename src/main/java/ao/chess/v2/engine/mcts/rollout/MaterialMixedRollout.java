package ao.chess.v2.engine.mcts.rollout;


import ao.chess.v2.data.MovePicker;
import ao.chess.v2.engine.heuristic.material.MaterialEvaluation;
import ao.chess.v2.engine.mcts.MctsHeuristic;
import ao.chess.v2.engine.mcts.MctsRollout;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;


public class MaterialMixedRollout implements MctsRollout {

    //--------------------------------------------------------------------
    private final int[] moves = new int[Move.MAX_PER_PLY];

    private final boolean opt;


    //--------------------------------------------------------------------
    public MaterialMixedRollout() {
        this(false);
    }

    public MaterialMixedRollout(boolean opt) {
        this.opt = opt;
    }


    //--------------------------------------------------------------------
    @Override public double monteCarloPlayout(
            State position, MctsHeuristic heuristic)
    {
        Colour fromPov = position.nextToAct();
        State state   = position;
        while (! state.isDrawnBy50MovesRule())
        {
            int move = bestMove(state);

            if (move == -1) {
                return state.knownOutcome()
                        .valueFor( fromPov );
            }
            Move.apply(move, state);
        }
        return 0.5;
    }


    //--------------------------------------------------------------------
    private int bestMove(State position)
    {
        int nMoves = position.legalMoves(moves);
        if (nMoves == 0) {
            return -1;
        }

        State state = position.prototype();

        double maxScore = Double.NEGATIVE_INFINITY;
        int maxScoreIndex = -1;

        if (opt) {
            for (int m = 0; m < nMoves; m++) {
                int move = Move.apply(moves[m], state);
                double score = MaterialEvaluation.evaluate(state) * Math.random();
                Move.unApply(move, state);

                if (score > maxScore) {
                    maxScore = score;
                    maxScoreIndex = m;
                }
            }
        }
        else {
            int[] moveOrder = MovePicker.pickRandom(nMoves);
            for (int m : moveOrder) {
                int move = Move.apply(moves[m], state);
                double score = MaterialEvaluation.evaluate(state);
                Move.unApply(move, state);

                if (score > maxScore) {
                    maxScore = score;
                    maxScoreIndex = m;
                }
            }
        }

        return moves[maxScoreIndex];
    }
}
