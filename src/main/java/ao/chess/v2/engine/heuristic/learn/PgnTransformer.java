package ao.chess.v2.engine.heuristic.learn;


import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collection;


public class PgnTransformer {
    public static void main(String[] args) throws IOException {
        // https://www.pgnmentor.com/files.html
        // https://www.chess.com/forum/view/general/6-8-million-game-database
        // https://chesstempo.com/pgn-viewer.html

        Path pgnFile = Paths.get("lookup/history/Adams.pgn");
        Path historyFile = Paths.get("lookup/history/adams.txt");
        Files.createDirectories(historyFile.getParent());

        PgnParser parser = new PgnParser();

        try (var lines = Files.lines(pgnFile);
             var out = new PrintWriter(Files.newBufferedWriter(historyFile))
        ) {
            lines.flatMap(line -> parser.process(line).stream())
                    .flatMap(Collection::stream)
                    .forEach(history -> out.println(history.asString()));
        }
    }
}
