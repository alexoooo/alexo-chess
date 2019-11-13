package ao.chess.v2.engine.heuristic.learn;


import com.google.common.collect.Lists;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;


public class MoveMixer {
    private static final List<Path> inputs = List.of(
            Paths.get("lookup/history/Spassky.txt"),
            Paths.get("lookup/history/Fischer.txt"),
            Paths.get("lookup/history/Karpov.txt"),
            Paths.get("lookup/history/Kasparov.txt"),
            Paths.get("lookup/history/Kramnik.txt"),
            Paths.get("lookup/history/Anand.txt"),
            Paths.get("lookup/history/Carlsen.txt")
    );

    private static final Path outputDir = Paths.get("lookup/history/mix/");
    private static final String outputPrefix = "champions_";
//    private static final Path output = Paths.get("lookup/history/champions.txt");


    private static final int batchSize = 100_000;


    public static void main(String[] args) throws IOException {
        Files.createDirectories(outputDir);

        List<MoveHistory> allMoves = new ArrayList<>();

        for (var input : inputs) {
            List<MoveHistory> inputMoves = MoveTrainer.readMoves(input);
            allMoves.addAll(inputMoves);
        }

        Collections.shuffle(allMoves);

        int runningCount = 0;

        for (var batch : Lists.partition(allMoves, batchSize)) {
            runningCount += batch.size();
            var outputPath = outputDir.resolve(outputPrefix + runningCount + ".txt");

            try (PrintWriter out = new PrintWriter(outputPath.toFile())) {
                for (var move : batch) {
                    out.println(move.asString());
                }
            }
        }
    }
}
