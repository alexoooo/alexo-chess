package ao.chess.v2.engine.endgame.test;

import ao.chess.v2.data.Location;
import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.pass.Traverser;

import java.util.Arrays;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 12-Oct-2009
 * Time: 3:18:38 PM
 */
public class TerminalTraverser
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        long before = System.currentTimeMillis();

        final long wWins[] = {0};
        final long bWins[] = {0};
        final long draws[] = {0};

        new TerminalTraverser().terminals(
                Arrays.asList(
                        Piece.WHITE_KING,
                        Piece.BLACK_KING,
                        Piece.WHITE_ROOK),
                new TerminalVisitor() {
                    @Override
                    public void visit(State state, Outcome outcome) {
                        System.out.println();
                        System.out.println(outcome);
                        System.out.println(state);

                        switch (outcome) {
                            case WHITE_WINS: wWins[0]++; break;
                            case BLACK_WINS: bWins[0]++; break;
                            case DRAW:       draws[0]++; break;
                        }
                    }
                }
        );

        System.out.println("found: " +
                wWins[0] + " white wins, " +
                bWins[0] + " black wins, " +
                draws[0] + " draws");
        System.out.println(
                "took " + (System.currentTimeMillis() - before));
    }


    //--------------------------------------------------------------------
    private final Piece[][] BOARD;


    //--------------------------------------------------------------------
    public TerminalTraverser() {
        BOARD = new Piece[ Location.RANKS ]
                         [ Location.FILES ];
    }


    //--------------------------------------------------------------------
    public void terminals(
            final List<Piece>     pieces,
            final TerminalVisitor visitor)
    {
        new PositionTraverser().traverse(pieces,
                new Traverser<State>() {
                    @Override public void traverse(State state) {
                        checkTerminal(state, visitor);
                    }
                });
    }


    //--------------------------------------------------------------------
    private void checkTerminal(
            State           state,
            TerminalVisitor terminalVisitor)
    {
        int moves[] = state.legalMoves();
        if (moves == null) {
            if (state.isInCheck( state.nextToAct() )) {
                terminalVisitor.visit(
                        state, Outcome.loses(state.nextToAct()));
            }
        } else if (moves.length == 0) {
            if (state.isInCheck( state.nextToAct() )) {
                terminalVisitor.visit(
                        state, Outcome.loses(state.nextToAct()));
            } else {
                terminalVisitor.visit(
                        state, Outcome.DRAW);
            }
        }
    }


    //--------------------------------------------------------------------
    public static interface TerminalVisitor
    {
        public void visit(State state, Outcome outcome);
    }
}
