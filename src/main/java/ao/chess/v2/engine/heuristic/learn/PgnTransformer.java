package ao.chess.v2.engine.heuristic.learn;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;


public class PgnTransformer {
    public static void main(String[] args) throws IOException {
        PgnParser parser = new PgnParser();

        // https://www.pgnmentor.com/files.html
        // https://www.chess.com/forum/view/general/6-8-million-game-database
        // https://chesstempo.com/pgn-viewer.html

        Path pgnDir = Paths.get("lookup/human");

        try (var files = Files.newDirectoryStream(pgnDir)) {
            for (var pgnFile : files) {
                String filename = pgnFile.getFileName().toString();
                if (! filename.endsWith(".pgn")) {
                    continue;
                }

                System.out.println("------------------------------");
                System.out.println("!! file: " + pgnFile);

                Path historyFile = pgnFile.resolveSibling(
                        filename.substring(0, filename.length() - 3) + "txt");
                if (Files.exists(historyFile)) {
                    continue;
                }

                try (var lines = Files.lines(pgnFile, StandardCharsets.ISO_8859_1);
                     var out = new PrintWriter(Files.newBufferedWriter(historyFile))
                ) {
                    lines.flatMap(line -> parser.process(line).stream())
                            .flatMap(Collection::stream)
                            .forEach(history -> out.println(history.asString()));
                }
            }
        }

//        Path pgnFile = Paths.get("lookup/history/Spassky.pgn");
//        Path historyFile = Paths.get("lookup/history/Spassky.txt");
//        Files.createDirectories(historyFile.getParent());

    }
}
