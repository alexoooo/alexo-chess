package ao.chess.v2.engine.endgame.test;

import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.engine.endgame.tablebase.DeepOracle;
import ao.chess.v2.engine.endgame.tablebase.DeepOutcome;
import ao.chess.v2.piece.MaterialTally;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.data.Arrs;
import ao.util.math.stats.Exhauster;
import ao.util.pass.Traverser;

import java.util.*;

/**
 * User: aostrovsky
 * Date: 19-Oct-2009
 * Time: 8:52:43 PM
 */
public class FullVerifier
{
    //--------------------------------------------------------------------
    public static void main(String[] args) {
        verifyFully(3);
//        verifyMaterial(Arrays.asList(
//                Piece.WHITE_KING, Piece.BLACK_KING, Piece.WHITE_PAWN
//        ));

        System.out.println("DONE");
    }


    //--------------------------------------------------------------------
    private static void verifyFully(int nPieces) {
        Set<Integer> seen = new HashSet<Integer>();
        for (Piece[] pick :
                new Exhauster<Piece>(Piece.VALUES, nPieces - 2)) {
            if (Arrs.indexOf(pick, Piece.WHITE_KING) != -1 ||
                    Arrs.indexOf(pick, Piece.BLACK_KING) != -1) {
                continue;
            }

            List<Piece> material = new ArrayList<Piece>();
            material.addAll(Arrays.asList(pick));
            material.add(Piece.WHITE_KING);
            material.add(Piece.BLACK_KING);

            int tally = MaterialTally.tally(material);
            if (seen.contains( tally )) continue;
            seen.add( tally );

            verifyMaterial(material);
        }
    }


    //--------------------------------------------------------------------
    private static void verifyMaterial(List<Piece> material) {
        System.out.println("Verifying " + material);

        new PositionTraverser().traverse(material,
                new Traverser<State>() {
                    @Override public void traverse(State state) {
                        verifyPath(state);
                    }
                });
    }


    //--------------------------------------------------------------------
    private static void verifyPath(State from) {
        DeepOutcome outcome = DeepOracle.INSTANCE.see(from);
//        if (outcome == null || outcome.isDraw()) return;
        boolean isDraw = (outcome == null || outcome.isDraw());

        Outcome realOutcome = from.knownOutcome();
        if (realOutcome != null) {
            if (isDraw && realOutcome != Outcome.DRAW ||
                    outcome == null ||
                    realOutcome != outcome.outcome()) {
                System.out.println(
                        "KNOWN OUTCOME INCONSISTENCY!!!! in\n" + from +
                        "\n" + outcome + " vs " + realOutcome);
            }
            return;
        }

        boolean canDraw     = false;
        int     shortestWin = Integer.MAX_VALUE;
        int     longestLoss = Integer.MIN_VALUE;

        for (int legalMove : from.legalMoves()) {
            Move.apply(legalMove, from);
            DeepOutcome subOutcome = DeepOracle.INSTANCE.see(from);
            Move.unApply(legalMove, from);
            if (subOutcome == null || subOutcome.isDraw()) {
                canDraw = true;
                continue;
            }

            if (subOutcome.winner() == from.nextToAct()) {
                shortestWin = Math.min(
                        shortestWin, subOutcome.plyDistance() + 1);
            } else {
                longestLoss = Math.max(
                        longestLoss, subOutcome.plyDistance() + 1);
            }
        }

        if (shortestWin != Integer.MAX_VALUE) {
            if (outcome == null ||
                    outcome.winner() != from.nextToAct() ||
                    outcome.plyDistance() < shortestWin) {
                System.out.println(
                        "WIN INCONSISTENCY!!!! in\n" + from +
                        "\n" + outcome + " vs " +
                        shortestWin + " | " + longestLoss);
//                verifyPath(from);
            }
        } else if (canDraw) {
            if (! isDraw) {
                System.out.println(
                        "TIE INCONSISTENCY!!!! in\n" + from +
                        "\n" + outcome + " vs " +
                        shortestWin + " | " + longestLoss);
            }
        } else if (longestLoss != Integer.MIN_VALUE) {
            if (isDraw || outcome.winner() == from.nextToAct() ||
                    outcome.plyDistance() > longestLoss) {
                System.out.println(
                        "LOSS INCONSISTENCY!!!! in\n" + from +
                        "\n" + outcome + " vs " +
                        shortestWin + " | " + longestLoss);
//                verifyPath(from);
            }
        } else {
            if (! isDraw) {
                System.out.println(
                        "??? INCONSISTENCY!!!! in\n" + from +
                        "\n" + outcome + " vs " +
                        shortestWin + " | " + longestLoss);
            }
        }
    }
}
