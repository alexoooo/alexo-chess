package ao.chess.v2.engine.stockfish;


import ao.chess.v2.state.State;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UncheckedIOException;
import java.util.ArrayList;
import java.util.List;


// NB: not thread safe
public class StockfishInstance implements AutoCloseable {
    private static final String bestMovePrefix = "bestmove ";
    private static final String wdlPrefix = "wdl ";


    private final Process process;
    private final int nodes;
    private final BufferedReader reader;


    StockfishInstance(Process process, int nodes) {
        this.process = process;
        this.nodes = nodes;

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


    public double evaluate(State state) {
        sendCommand("position fen " + state.toFen());
        sendCommand("go nodes " + nodes);

//        try {
//            Thread.sleep(100);
//        }
//        catch (InterruptedException e) {
//            throw new IllegalStateException(e);
//        }

        List<String> lines = awaitBestMove(state);
        String lastInfoLine = lines.get(lines.size() - 2);
        WinDrawLoss winDrawLoss = new WinDrawLoss(lastInfoLine);

        return winDrawLoss.value();
    }


    @Override
    public void close() {
        process.destroy();
    }


    //-----------------------------------------------------------------------------------------------------------------
    static class WinDrawLoss {
        final int win;
        final int draw;
        final int loss;

        public WinDrawLoss(String infoLine) {
            int startOfWinIndex = infoLine.indexOf(wdlPrefix) + wdlPrefix.length();
            int endOfWinIndex = infoLine.indexOf(" ", startOfWinIndex);
            String winText = infoLine.substring(startOfWinIndex, endOfWinIndex);
            win = Integer.parseInt(winText);

            int startOfDrawIndex = endOfWinIndex + 1;
            int endOfDrawIndex = infoLine.indexOf(" ", startOfDrawIndex);
            String drawText = infoLine.substring(startOfDrawIndex, endOfDrawIndex);
            draw = Integer.parseInt(drawText);

            int startOfLossIndex = endOfDrawIndex + 1;
            int endOfLossIndex = infoLine.indexOf(" ", startOfLossIndex);
            String lossText = infoLine.substring(startOfLossIndex, endOfLossIndex);
            loss = Integer.parseInt(lossText);
        }

        public double value() {
            return (win + ((double) draw) / 2) / 1000;
        }
    }
}
