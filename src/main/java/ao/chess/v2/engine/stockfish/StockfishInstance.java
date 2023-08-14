package ao.chess.v2.engine.stockfish;


import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;


// NB: not thread safe
public class StockfishInstance implements AutoCloseable
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final String bestMovePrefix = "bestmove ";
    private static final String wdlPrefix = "wdl ";


    //-----------------------------------------------------------------------------------------------------------------
    public static class OutcomeAndMove {
        private final WinDrawLoss outcome;
        private final int move;

        OutcomeAndMove(WinDrawLoss value, int move) {
            this.outcome = value;
            this.move = move;
        }

        public WinDrawLoss outcome() {
            return outcome;
        }

        public int move() {
            return move;
        }
    }


    public static class WinDrawLoss {
        public static final WinDrawLoss pureWin = new WinDrawLoss(1000, 0, 0);
        public static final WinDrawLoss pureDraw = new WinDrawLoss(0, 1000, 0);
        public static final WinDrawLoss pureLoss = new WinDrawLoss(0, 0, 1000);

        private final int winMills;
        private final int drawMills;
        private final int lossMills;

        WinDrawLoss(String infoLine) {
            int startOfWinIndex = infoLine.indexOf(wdlPrefix) + wdlPrefix.length();
            int endOfWinIndex = infoLine.indexOf(" ", startOfWinIndex);
            String winText = infoLine.substring(startOfWinIndex, endOfWinIndex);
            winMills = Integer.parseInt(winText);

            int startOfDrawIndex = endOfWinIndex + 1;
            int endOfDrawIndex = infoLine.indexOf(" ", startOfDrawIndex);
            String drawText = infoLine.substring(startOfDrawIndex, endOfDrawIndex);
            drawMills = Integer.parseInt(drawText);

            int startOfLossIndex = endOfDrawIndex + 1;
            int endOfLossIndex = infoLine.indexOf(" ", startOfLossIndex);
            String lossText = infoLine.substring(startOfLossIndex, endOfLossIndex);
            lossMills = Integer.parseInt(lossText);
        }

        WinDrawLoss(int winMills, int drawMills, int lossMills) {
            this.winMills = winMills;
            this.drawMills = drawMills;
            this.lossMills = lossMills;
        }

        public int winMills() {
            return winMills;
        }

        public int drawMills() {
            return drawMills;
        }

        public int lossMills() {
            return lossMills;
        }

        public double winProbability() {
            return (double) winMills / 1000;
        }

        public double drawProbability() {
            return (double) drawMills / 1000;
        }

        public double lossProbability() {
            return (double) lossMills / 1000;
        }

        public double value() {
            return (winMills + ((double) drawMills) / 2) / 1000;
        }

        public WinDrawLoss flip() {
            return new WinDrawLoss(lossMills, drawMills, winMills);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final Process process;
//    private final int nodes;
    private final BufferedReader reader;


    StockfishInstance(Process process) {
        this.process = process;
//        this.nodes = nodes;

        reader = new BufferedReader(
                new InputStreamReader(process.getInputStream()));
    }


    void sendCommand(String command) {
        byte[] commandBytes = (command + "\n").getBytes();
        try {
            process.getOutputStream().write(commandBytes);
            process.getOutputStream().flush();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private List<String> awaitBestMove(State state) {
        List<String> lines = new ArrayList<>();
        try {
            while (true) {
                String line = reader.readLine();
                if (line == null) {
                    throw new IllegalStateException("Crash: " + state.toFen());
                }

                lines.add(line);
                if (line.startsWith(bestMovePrefix)) {
                    break;
                }
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        return lines;
    }


    public WinDrawLoss evaluate(State state, int nodes) {
        sendCommand("position fen " + state.toFen());
        sendCommand("go nodes " + nodes);

        List<String> lines = awaitBestMove(state);
        String lastInfoLine = lines.get(lines.size() - 2);

        @SuppressWarnings("UnnecessaryLocalVariable")
        WinDrawLoss winDrawLoss = new WinDrawLoss(lastInfoLine);

        return winDrawLoss;
    }


    public OutcomeAndMove bestMove(State state, int[] legalMoves, int moveCount, int nodes) {
        sendCommand("position fen " + state.toFen());
        sendCommand("go nodes " + nodes);

        List<String> lines = awaitBestMove(state);

        String lastInfoLine = lines.get(lines.size() - 2);
        WinDrawLoss winDrawLoss = new WinDrawLoss(lastInfoLine);

        String bestMoveLine = lines.get(lines.size() - 1);
        int endOfBestMove = bestMoveLine.indexOf(' ', bestMovePrefix.length());
        String bestMoveText = bestMoveLine.substring(bestMovePrefix.length(), endOfBestMove);

        for (int i = 0; i < moveCount; i++) {
            int move = legalMoves[i];
            String moveNotation = Move.toInputNotation(move);
            if (bestMoveText.equals(moveNotation)) {
                return new OutcomeAndMove(winDrawLoss, move);
            }
        }

        throw new IllegalStateException("Unknown move: " + bestMoveText + " - " + state.toFen());
    }


    @Override
    public void close() {
        process.destroy();
    }
}
