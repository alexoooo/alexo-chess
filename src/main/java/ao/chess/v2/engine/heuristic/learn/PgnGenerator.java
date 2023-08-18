package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.CastleType;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;


public class PgnGenerator {
    public static void main(String[] args) throws IOException
    {
        Path fenPath =
//                Paths.get("C:/~/tmp/travis-game-1.txt");
                Paths.get("C:/~/tmp/josh-game-1.txt");

        List<String> lines = Files.readAllLines(fenPath);

        State previous = State.fromFen(lines.get(0));

        for (int i = 1; i < lines.size(); i++)
        {
//            System.out.println(i + " =============================");
//            System.out.println(previous);

            State next = State.fromFen(lines.get(i));

            int move = findMove(previous, next);

            if (previous.nextToAct() == Colour.WHITE) {
                System.out.print((i / 2 + 1) +". ");
            }

            System.out.print(toPgn(move, next) + " ");

            previous = next;
        }
    }


    public static int findMove(State from, State to)
    {
        State cursor = from.prototype();

        for (int move : from.legalMoves())
        {
            int undo = Move.apply(move, cursor);

            if (cursor.equalsIgnoringReversibleMovesAndCastlePath(to)) {
                return move;
            }

            Move.unApply(undo, cursor);
        }

        System.out.println("!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!!");
        System.out.println("Can't find move to:");
        System.out.println(to);
        throw new IllegalStateException();
    }


    public static String toPgn(int move, State to)
    {
        if (Move.isCastle(move)) {
            return Move.castleType(move) == CastleType.KING_SIDE
                    ? "O-O" : "O-O-O";
        }

        Figure figure = Figure.VALUES[ Move.figure(move) ];
        String figurePrefix = figure == Figure.PAWN ? "" : figure.symbol();

        boolean capture = Move.isCapture(move);
        String captureInfix = capture ? "x" : "";

        boolean isCheck = to.isInCheck(to.nextToAct());
        String checkSuffix = isCheck ? "+" : "";

        return figurePrefix +
                Move.toInputNotationSource(move) +
                captureInfix +
                Move.toPgnNotationDestination(move) +
                checkSuffix;
    }
}
