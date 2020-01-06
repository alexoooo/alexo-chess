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
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;


// split moves into partitions
public class MovePartition {
    private static final int partitions = 8 * 4 + 1;


    public static void main(String[] args) throws IOException {
        Path inputDir = Paths.get("lookup/train/mix-big");
        Path outputDir = Paths.get("lookup/train/pieces");

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
                        MoveHistory parsed = new MoveHistory(line);
                        int partitionIndex = parsed.state().material().size();
                        PrintWriter output = outputs.get(partitionIndex);
                        output.println(line);
                    }
                }

                outputs.forEach(PrintWriter::flush);
            }
        }
    }
}
