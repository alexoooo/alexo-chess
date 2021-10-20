package ao.chess.v2.engine.stockfish;


import java.io.IOException;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;


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

        private Path syzygy;

        private boolean built;


        private Builder(Path executable) {
            this.executable = executable;
        }


        public Builder syzygyPath(Path syzygy) {
            this.syzygy = syzygy;
            return setCheck();
        }


        private Builder setCheck() {
            checkState(built);
            return this;
        }


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
    public StockfishInstance start(int threads, int hashMb, int nodes) {
        ProcessBuilder processBuilder = new ProcessBuilder(builder.executable.toString());

        try {
            Process process = processBuilder.start();
            StockfishInstance instance = new StockfishInstance(process, nodes);
            instance.sendCommand("setoption name Threads value " + threads);
            instance.sendCommand("setoption name Hash value " + hashMb);
            instance.sendCommand("setoption name UCI_ShowWDL value true");
            if (builder.syzygy != null) {
                instance.sendCommand("setoption name SyzygyPath value " + builder.syzygy);
            }
            return instance;
        }
        catch (IOException e) {
            throw new IllegalStateException(e);
        }
    }
}
