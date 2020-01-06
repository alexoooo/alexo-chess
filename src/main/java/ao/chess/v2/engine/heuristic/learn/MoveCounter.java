package ao.chess.v2.engine.heuristic.learn;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.zip.GZIPInputStream;

public class MoveCounter {
    public static void main(String[] args) throws IOException {
        long[] histogram = new long[8 * 4 + 1];

        Path inputDir = Paths.get("lookup/train/mix-big");

        try (
                var files = Files.newDirectoryStream(inputDir)
        ) {
            for (var historyFile : files) {
                String filename = historyFile.getFileName().toString();
                if (! filename.endsWith(".txt.gz")) {
                    continue;
                }
                System.out.println("> " + historyFile);

                try (BufferedReader reader = new BufferedReader(
                        new InputStreamReader(new GZIPInputStream(
                                Files.newInputStream(historyFile)))
                )) {
                    while (reader.ready()) {
                        String line = reader.readLine();
                        MoveHistory parsed = new MoveHistory(line);
                        int pieceCount = parsed.state().material().size();
                        histogram[pieceCount]++;
                    }
                }

                System.out.println(Arrays.toString(histogram));
            }
        }
    }
}
