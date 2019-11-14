package ao.chess.v2.engine.heuristic.learn;


import com.google.common.io.Closer;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class MoveShuffler {
    private static final Random rand = new Random();
    private static final int partitions = 250;

    public static void main(String[] args) throws IOException {
        Path inputDir = Paths.get("lookup/human");
        Path outputDir = Paths.get("lookup/mix");

        Closer outputCloser = Closer.create();
        try (var files = Files.newDirectoryStream(inputDir)) {
            List<PrintWriter> outputs = new ArrayList<>();
            for (int i = 0; i < partitions; i++) {
                Path partitionPath = outputDir.resolve(i + ".txt");
                PrintWriter partitionWriter = new PrintWriter(Files.newBufferedWriter(partitionPath));
                outputCloser.register(partitionWriter);
                outputs.add(partitionWriter);
            }

            for (var historyFile : files) {
                String filename = historyFile.getFileName().toString();
                if (! filename.endsWith(".txt")) {
                    continue;
                }
                System.out.println("> " + historyFile);

                try (var lines = Files.lines(historyFile)) {
                    lines.map(MoveHistory::new).forEach(example -> {
                        int partitionIndex = rand.nextInt(outputs.size());
                        PrintWriter output = outputs.get(partitionIndex);
                        output.println(example.asString());
                    });
                }

                outputs.forEach(PrintWriter::flush);
            }
        }
        finally {
            outputCloser.close();
        }
    }
}
