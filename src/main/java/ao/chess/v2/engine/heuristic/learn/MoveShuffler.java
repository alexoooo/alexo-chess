package ao.chess.v2.engine.heuristic.learn;


import com.google.common.io.Closer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


public class MoveShuffler {
    private static final Random rand = new Random();
    private static final int partitions = 3000;


    public static void main(String[] args) throws IOException {
        Path inputDir = Paths.get("lookup/train/history");
        Path outputDir = Paths.get("lookup/mix-big");

        Files.createDirectories(outputDir);

        try (Closer outputCloser = Closer.create();
             var files = Files.newDirectoryStream(inputDir)
        ) {
            List<PrintWriter> outputs = new ArrayList<>();
            for (int i = 0; i < partitions; i++) {
                Path partitionPath = outputDir.resolve(i + ".txt.gz");
                PrintWriter partitionWriter = new PrintWriter(
                        new GZIPOutputStream(Files.newOutputStream(partitionPath)));
                outputCloser.register(partitionWriter);
                outputs.add(partitionWriter);
            }

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
                        int partitionIndex = rand.nextInt(outputs.size());
                        PrintWriter output = outputs.get(partitionIndex);
                        output.println(line);
                    }
                }

                outputs.forEach(PrintWriter::flush);
            }
        }
    }
}
