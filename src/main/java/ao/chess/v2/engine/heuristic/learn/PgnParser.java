package ao.chess.v2.engine.heuristic.learn;


import ao.chess.v2.data.BoardLocation;
import ao.chess.v2.data.Location;
import ao.chess.v2.piece.Figure;
import ao.chess.v2.state.CastleType;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;


public class PgnParser {
    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
        String game =
                "[...]\n" +
                "1. e4 c6 2. d3 g6 3. Nf3 Bg7 4. Be2 d5 5. e5 e6 6. Bf4 Ne7 7. d4 Qb6 8. b3 f6\n" +
                "9. a4 fxe5 10. Bxe5 Bxe5 11. Nxe5 Nd7 12. Nf3 Nf6 13. a5 Qc7 14. O-O O-O 15. c4\n" +
                "dxc4 16. bxc4 Rd8 17. Nc3 e5 18. Nxe5 Nf5 19. Nf3 Nxd4 20. Nxd4 Qf4 21. Qb3\n" +
                "Rxd4 22. c5+ Nd5 23. Bf3 Qf7 24. Nxd5 Rxd5 25. Bxd5 Qxd5 26. Rab1 Qxb3 27. Rxb3\n" +
                "Kg7 28. Rd1 Kh6 29. Rd8 b5 30. Re3 b4 31. f4 Kh5 32. h3 a6 33. Rh8 g5 34. Re5\n" +
                "Kh6 35. Rxg5 Bb7 36. Rxa8 Bxa8 37. Rg8 Bb7 38. g4 b3 39. Rb8 Ba8 40. Rxb3 Kg6\n" +
                "41. Rb8 Kf7 42. Rxa8 Ke6 43. Rxa6 Kd5 44. f5 Kxc5 45. Ra7 Kd4 46. Rd7+ Ke5 47.\n" +
                "Re7+ Kd4 48. Rxh7 c5 49. a6 c4 50. a7 c3 51. Rd7+ Kc4 52. Rc7+ Kb4 53. a8=Q Kb3\n" +
                "54. Qc8 Kb2 55. h4 Kb1 56. g5 c2 57. h5 c1=Q+ 58. Rxc1+ Ka2 59. f6 Ka3 60. f7\n" +
                "Kb3 61. g6 Ka3 62. g7 Kb3 63. g8=Q Ka4 64. h6 Ka5 65. h7 Kb6 66. h8=Q Ka5 67.\n" +
                "f8=Q Kb5 68. Qch3 Kb6 69. Qh8h7 Ka5 70. Qhf1 Kb6 71. Qc5# 1-0\n" +
                "";

        PgnParser parser = new PgnParser();
        for (var line : game.split("\n")) {
            parser.process(line);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static List<State> parse(String pgn) {
        PgnParser parser = new PgnParser();
        for (var line : pgn.split("\n")) {
            Optional<List<MoveHistory>> parsed = parser.process(line);
            if (parsed.isPresent()) {
                return parsed.get().stream().map(MoveHistory::state).collect(Collectors.toList());
            }
        }

        List<State> incomplete = parser.buffer.build(Outcome.DRAW)
                .stream().map(MoveHistory::state).collect(Collectors.toList());

        return ImmutableList.<State>builder().addAll(incomplete).add(parser.state).build();
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final MoveHistory.Buffer buffer = new MoveHistory.Buffer();
    private State state = State.initial();
    private List<MoveHistory> pending = new ArrayList<>();
    private boolean skipUntilNext = false;


    //-----------------------------------------------------------------------------------------------------------------
    public Optional<List<MoveHistory>> process(String line) {
//        System.out.println("> " + line);

        if (skipUntilNext) {
            if (line.startsWith("1.")) {
                skipUntilNext = false;
            }
            else {
                return Optional.empty();
            }
        }

        if (! line.isEmpty() && ! line.startsWith("[")) {
            String[] tokens = line
                    .replaceAll("[{][^}]+[}]", "")
                    .replaceAll("\\d+\\.\\s?", "")
                    .split("\\s+");
            for (String token : tokens) {
                onNext(token);
                if (skipUntilNext) {
                    return Optional.empty();
                }
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

        if (move.equals("*") ||
                move.equals("--") ||
                move.contains(".")) {
            buffer.clear();
            state = State.initial();
            skipUntilNext = true;
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
//        if (move.equals("Qh8h7")) {
//            System.out.println("!!!!1");
//        }

        String withoutDecorator = move.replaceAll("[x+#]", "");

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
            String padded =
                    figure == Figure.PAWN
                    ? "P" + clean
                    : clean;

            if (padded.length() == 5) {
                sourceFile = State.FILES.indexOf(padded.substring(1, 2));
                sourceRank = Integer.parseInt(padded.substring(2, 3)) - 1;
            }
            else if (padded.length() == 4) {
                sourceFile = State.FILES.indexOf(padded.substring(1, 2));
                if (sourceFile == -1) {
                    sourceRank = Integer.parseInt(padded.substring(1, 2)) - 1;
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

//        buffer.clear();
//        state = State.initial();
//        skipUntilNext = true;
        throw new IllegalStateException("Unable to process: " + move);
    }


    private void apply(int[] legalMoves, int moveIndex, String moveName) {
        int legalMove = legalMoves[moveIndex];

        double[] moveScores = new double[legalMoves.length];

        moveScores[moveIndex] = 1000;
        buffer.add(state, legalMoves, moveScores);
        Move.apply(legalMove, state);

//        System.out.println("------------------------------------");
//        System.out.println(moveName + " - " + Move.toString(legalMove));
//        System.out.println(state);
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
