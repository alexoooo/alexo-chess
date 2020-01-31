package ao.chess.v2.engine.heuristic.learn;


import com.google.common.collect.ImmutableList;
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


    public static void main(String[] args) throws IOException {
        List<Path> inputs = List.of(
//                Paths.get("lookup/train/pieces/2.txt.gz"),
//                Paths.get("lookup/train/pieces/3.txt.gz"),
//                Paths.get("lookup/train/pieces/4.txt.gz"),
//                Paths.get("lookup/train/pieces/5.txt.gz"),
//                Paths.get("lookup/train/pieces/6.txt.gz"),
//                Paths.get("lookup/train/pieces/7.txt.gz"),
//                Paths.get("lookup/train/pieces/8.txt.gz"),
//                Paths.get("lookup/train/pieces/9.txt.gz"),
//                Paths.get("lookup/train/pieces/10.txt.gz"),
//                Paths.get("lookup/train/pieces/11.txt.gz"),
//                Paths.get("lookup/train/pieces/12.txt.gz"),
//                Paths.get("lookup/train/pieces/13.txt.gz"),
//                Paths.get("lookup/train/pieces/14.txt.gz"),
//                Paths.get("lookup/train/pieces/15.txt.gz"),
//                Paths.get("lookup/train/pieces/16.txt.gz"),
//                Paths.get("lookup/train/pieces/17.txt.gz"),
//                Paths.get("lookup/train/pieces/18.txt.gz"),
//                Paths.get("lookup/train/pieces/19.txt.gz"),
//                Paths.get("lookup/train/pieces/20.txt.gz"),
//                Paths.get("lookup/train/pieces/21.txt.gz"),
//                Paths.get("lookup/train/pieces/22.txt.gz")
                Paths.get("lookup/train/pieces/23.txt.gz"),
                Paths.get("lookup/train/pieces/24.txt.gz"),
                Paths.get("lookup/train/pieces/25.txt.gz"),
                Paths.get("lookup/train/pieces/26.txt.gz"),
                Paths.get("lookup/train/pieces/27.txt.gz"),
                Paths.get("lookup/train/pieces/28.txt.gz"),
                Paths.get("lookup/train/pieces/29.txt.gz"),
                Paths.get("lookup/train/pieces/30.txt.gz"),
                Paths.get("lookup/train/pieces/31.txt.gz"),
                Paths.get("lookup/train/pieces/32.txt.gz")
        );

        Path outputDir =
//                Paths.get("lookup/train/pieces/p_2_12");
//                Paths.get("lookup/train/pieces/p_13_22");
                Paths.get("lookup/train/pieces/p_23_32");

        partition(inputs, outputDir, 1800);

//        Path dir = Paths.get("lookup/train/pieces");
//        for (int i = 12; i <= 32; i++)
//        {
//            Path input = dir.resolve(i + ".txt.gz");
//            Path outputDir = dir.resolve(String.valueOf(i));
//
//            long size = Files.size(input);
//            int partitions = (int) (size / 1024 / 1024 / 10 + 1);
//
//            partition(List.of(input), outputDir, partitions);
//        }
    }


    public static void partition(
            List<Path> inputs,
            Path outputDir,
            int partitions) throws IOException
    {
        List<Path> historyFiles = historyFiles(inputs);

        Files.createDirectories(outputDir);

        try (Closer outputCloser = Closer.create()) {
            List<PrintWriter> outputs = new ArrayList<>();
            for (int i = 0; i < partitions; i++) {
                Path partitionPath = outputDir.resolve(i + ".txt.gz");
                PrintWriter partitionWriter = new PrintWriter(
                        new GZIPOutputStream(Files.newOutputStream(partitionPath)));
                outputCloser.register(partitionWriter);
                outputs.add(partitionWriter);
            }

            for (var historyFile : historyFiles) {
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


    public static List<Path> historyFiles(List<Path> paths) throws IOException
    {
        ImmutableList.Builder<Path> builder = ImmutableList.builder();

        for (var path : paths)
        {
            if (Files.isDirectory(path))
            {
                try (var files = Files.newDirectoryStream(path))
                {
                    for (var historyFile : files)
                    {
                        String filename = historyFile.getFileName().toString();
                        if (! filename.endsWith(".txt.gz")) {
                            continue;
                        }

                        builder.add(path);
                    }
                }
            }
            else
            {
                builder.add(path);
            }
        }

        return builder.build();
    }
}
