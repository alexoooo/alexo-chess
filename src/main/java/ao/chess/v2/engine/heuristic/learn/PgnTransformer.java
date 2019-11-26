package ao.chess.v2.engine.heuristic.learn;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;
import java.util.zip.GZIPOutputStream;


public class PgnTransformer {
    public static void main(String[] args) throws IOException {
        // https://www.pgnmentor.com/files.html
        // https://www.chess.com/forum/view/general/6-8-million-game-database
        // https://chesstempo.com/pgn-viewer.html
        // https://www.computerchess.org.uk/ccrl/4040/games.html
        // http://www.kingbase-chess.net/
        // http://www.chessgameslinks.lars-balzer.info/

        Path pgnDir = Paths.get("lookup/pgn");

        try (var files = Files.newDirectoryStream(pgnDir)) {
            for (var pgnFile : files) {
                if (! pgnFile.toString().endsWith(".pgn")) {
                    continue;
                }

                transform(pgnFile);
            }
        }
    }

    private static void transform(Path pgnFile) throws IOException {
        String filename = pgnFile.getFileName().toString();

        System.out.println("------------------------------");
        System.out.println("!! file: " + pgnFile);

        Path historyFile = pgnFile.resolveSibling(
                filename.substring(0, filename.length() - 3) + "txt.gz");
        if (Files.exists(historyFile)) {
            return;
        }

        PgnParser parser = new PgnParser();

        try (var lines = Files.lines(pgnFile, StandardCharsets.ISO_8859_1);
             var out = new PrintWriter(new GZIPOutputStream(
                     Files.newOutputStream(historyFile)))
        ) {
            lines.flatMap(line -> parser.process(line).stream())
                    .flatMap(Collection::stream)
                    .forEach(history -> out.println(history.asString()));
        }
    }
}
