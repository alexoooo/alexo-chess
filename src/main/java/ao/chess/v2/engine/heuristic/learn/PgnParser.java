package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.data.BoardLocation;
import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.CastleType;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;


public class PgnParser {
    private State state = State.initial();
    private MoveHistory.Buffer buffer = new MoveHistory.Buffer();
    private List<MoveHistory> pending = new ArrayList<>();


    public Optional<List<MoveHistory>> process(String line) {
        System.out.println("> " + line);

        if (! line.isEmpty() && Character.isDigit(line.charAt(0))) {
            String[] tokens = line.split("\\s+");
            for (String token : tokens) {
                onNext(token);
            }
        }

        if (pending.isEmpty()) {
            return Optional.empty();
        }

        List<MoveHistory> ref = pending;
        pending = new ArrayList<>();
        return Optional.of(ref);
    }


    private void onNext(String token) {
        int dotIndex = token.indexOf(".");

        String move =
                dotIndex == -1
                ? token
                : token.substring(dotIndex + 1);

        if (move.equals("*")) {
            buffer.clear();
            state = State.initial();
            return;
        }

        Outcome outcomeOrNull = tryParseOutcome(move);

        if (outcomeOrNull != null) {
            pending = buffer.build(outcomeOrNull);
            buffer.clear();
            state = State.initial();
            return;
        }

        processMove(move);
    }


    private Outcome tryParseOutcome(String token) {
        switch (token) {
            case "1-0":
                return Outcome.WHITE_WINS;

            case "0-1":
                return Outcome.BLACK_WINS;

            case "1/2-1/2":
                return Outcome.DRAW;

            default:
                return null;
        }
    }


    private void processMove(String move) {
//        if (move.equals("b8=Q+")) {
//            System.out.println("!!!!1");
//        }

        String withoutDecorator = move.replaceAll("[x+]", "");

        int promotionIndex = withoutDecorator.indexOf('=');
        String clean =
                promotionIndex == -1
                ? withoutDecorator
                : withoutDecorator.substring(0, promotionIndex);

//        if (clean.length() == 1) {
//            System.out.println("!! " + move);
//        }

        Figure promotionFigureOrNull =
                promotionIndex == -1
                ? null
                : Figure.ofSymbol(withoutDecorator.substring(promotionIndex + 1));

        Figure figure = parseFigure(clean);

        CastleType castleTypeOrNull =
                clean.equals("O-O")
                ? CastleType.KING_SIDE
                : clean.equals("O-O-O")
                ? CastleType.QUEEN_SIDE
                : null;

        BoardLocation toLocationOrNull;
        if (castleTypeOrNull != null) {
            toLocationOrNull = null;
        }
        else {
            String destination = clean.substring(clean.length() - 2);
            toLocationOrNull = BoardLocation.parse(destination);
        }

        int sourceRank = -1;
        int sourceFile = -1;
        if (castleTypeOrNull == null) {
            if (clean.length() == 4) {
                sourceFile = State.FILES.indexOf(clean.substring(1, 2));
                if (sourceFile == -1) {
                    sourceRank = Integer.parseInt(clean.substring(1, 2)) - 1;
                }
            }
            else if (figure == Figure.PAWN && clean.length() == 3) {
                sourceFile = State.FILES.indexOf(clean.substring(0, 1));
                if (sourceFile == -1) {
                    sourceRank = Integer.parseInt(clean.substring(0, 1)) - 1;
                }
            }
        }

        int[] legalMoves = state.legalMoves();

        for (int i = 0; i < legalMoves.length; i++) {
            int legalMove = legalMoves[i];

            if (castleTypeOrNull != null) {
                if (Move.isCastle(legalMove) &&
                        Move.castleType(legalMove) == castleTypeOrNull) {
                    apply(legalMoves, i, move);
                    return;
                }
                continue;
            }

            if (promotionFigureOrNull != null &&
                    (! Move.isPromotion(legalMove) ||
                            Move.promotion(legalMove) != promotionFigureOrNull.ordinal())) {
                continue;
            }

            Figure moveFigure = Figure.VALUES[Move.figure(legalMove)];
            if (moveFigure != figure) {
                continue;
            }

            int moveToIndex = Move.toSquareIndex(legalMove);
            int moveToRank = Location.rankIndex(moveToIndex);
            int moveToFile = Location.fileIndex(moveToIndex);
            if (toLocationOrNull.rank() != moveToRank ||
                    toLocationOrNull.file() != moveToFile) {
                continue;
            }

            int moveFromIndex = Move.fromSquareIndex(legalMove);
            int moveFromRank = Location.rankIndex(moveFromIndex);
            int moveFromFile = Location.fileIndex(moveFromIndex);
            if (sourceRank != -1 && sourceRank != moveFromRank ||
                    sourceFile != -1 && sourceFile != moveFromFile) {
                continue;
            }

            apply(legalMoves, i, move);

            return;
        }

        throw new IllegalStateException("Unable to process: " + move);
    }


    private void apply(int[] legalMoves, int moveIndex, String moveName) {
        int legalMove = legalMoves[moveIndex];

        double[] moveScores = new double[legalMoves.length];

        moveScores[moveIndex] = 1000;
        buffer.add(state, legalMoves, moveScores);
        Move.apply(legalMove, state);

        System.out.println("------------------------------------");
        System.out.println(moveName + " - " + Move.toString(legalMove));
        System.out.println(state);
    }


    private Figure parseFigure(String move) {
        String symbol = move.substring(0, 1);

        for (var figure : Figure.VALUES) {
            if (figure.symbol().equals(symbol)) {
                return figure;
            }
        }

        return Figure.PAWN;
    }
}
