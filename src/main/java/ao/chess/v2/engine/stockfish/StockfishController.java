package ao.chess.v2.engine.stockfish;


import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;


// https://lichess.org/blog/W3WeMyQAACQAdfAL/7-piece-syzygy-tablebases-are-complete
// https://www.chessprogramming.org/Pawn_Advantage,_Win_Percentage,_and_Elo
// https://gist.github.com/aliostad/f4470274f39d29b788c1b09519e67372
public class StockfishController {
    //-----------------------------------------------------------------------------------------------------------------
    public static Builder builder(Path executable) {
        return new Builder(executable);
    }


    public static class Builder {
        private final Path executable;

        private final List<Path> syzygyFolders = new ArrayList<>();

//        private boolean built;


        private Builder(Path executable) {
            this.executable = executable;
        }


        public Builder addSyzygyFolders(List<Path> syzygyFolders) {
            this.syzygyFolders.addAll(syzygyFolders);
//            return setCheck();
            return this;
        }


//        private Builder setCheck() {
//            checkState(built);
//            return this;
//        }


        public StockfishController build() {
            return new StockfishController(this);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final Builder builder;


    //-----------------------------------------------------------------------------------------------------------------
    private StockfishController(Builder builder) {
        this.builder = builder;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public StockfishInstance start(int threads, int hashMb) {
        ProcessBuilder processBuilder = new ProcessBuilder(builder.executable.toString());

        try {
            Process process = processBuilder.start();
            StockfishInstance instance = new StockfishInstance(process);
            instance.sendCommand("setoption name Threads value " + threads);
            instance.sendCommand("setoption name Hash value " + hashMb);
            instance.sendCommand("setoption name UCI_ShowWDL value true");
            if (! builder.syzygyFolders.isEmpty()) {
                String folders = builder.syzygyFolders
                        .stream()
                        .map(Path::toString)
                        .collect(Collectors.joining(";"));
                instance.sendCommand("setoption name SyzygyPath value " + folders);
            }
            return instance;
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
