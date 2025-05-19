package ao.chess.v2.engine.eval;


import ao.chess.v2.engine.stockfish.StockfishController;
import ao.chess.v2.state.State;
import com.google.common.base.Stopwatch;

import java.nio.file.Path;
import java.util.DoubleSummaryStatistics;
import java.util.List;


public class StockfishMain {
    public static final Path stockfishExe = Path.of(
//            "C:/~/prog/stockfish/stockfish_14.1_win_x64_avx2/stockfish_14.1_win_x64_avx2.exe");
//            "C:/~/prog/stockfish/stockfish_15_win_x64_avx2/stockfish_15_x64_avx2.exe");
//            "C:/~/prog/stockfish/stockfish_15.1_win_x64_avx2/stockfish-windows-2022-x86-64-avx2.exe");
//            "C:/~/prog/stockfish/stockfish_16_win_x64_avx2/stockfish-windows-x86-64-avx2.exe");
//            "C:/~/prog/stockfish/stockfish_16.1_win_x64_avx2/stockfish-windows-x86-64-avx2.exe");
//            "C:/~/prog/stockfish/stockfish_17_win_x64_avx2/stockfish-windows-x86-64-avx2.exe");
            "C:/~/prog/stockfish/stockfish_17.1_win_x64_avx2/stockfish-windows-x86-64-avx2.exe");


    public static final List<Path> syzygyFolders = List.of(
            Path.of("C:/~/prog/syzygy"));


    public static void main(String[] args) {
        StockfishController controller = StockfishController
                .builder(stockfishExe)
                .addSyzygyFolders(syzygyFolders)
                .build();

//        int rolloutNodes = 32_000;
        int rolloutNodes = 65_000;
//        int rolloutRandomNodes = 0;
        int rolloutRandomNodes = 10_000;
        System.out.println("rolloutNodes: " + rolloutNodes);

        try (StockfishEval eval = StockfishEval.create(
//                controller, 1, 1024, 1_000_000)
//                controller, 1, 1024, 1_000_000, false, 10_000)
//                controller, 1, 1024, 32_000, false, 10_000)
                controller, 1, 512, rolloutNodes, rolloutRandomNodes, true, rolloutNodes)
//                controller, 1, 1024, 100_000_000)
        ) {
//            State state = State.fromFen("r2q1rk1/p2nppbp/3p1np1/2pP4/2P1P3/5N2/P2N1PPP/1RBQ1RK1 w - - 1 12");
            State state = State.fromFen(
                    "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1"
//                    "b7/5k2/6p1/6P1/8/8/7K/2B5 b - - 99 199"
//                    "8/5p2/2R5/6pk/7p/7P/5PPK/3r4 w - - 4 n"

//                    "2q1nk1r/4Rp2/1ppp1P2/6Pp/3p1B2/3P3P/PPP1Q3/6K1 w"

//                "2K1k1br/2qp1n1r/2p2pN1/3p1N2/2P4P/8/P2P4/8 w - - 0 14"
//                "4k1br/2Kp1n1r/2p2pN1/3p1N2/2P4P/8/P2P4/8 b - - 0 14"
//                "4k1br/2Kp1n1r/2p2pN1/5N2/2p4P/8/P2P4/8 w - - 0 15" // or here?
//                "2K1k1br/3p1n1r/2p2pN1/5N2/2p4P/8/P2P4/8 b - - 1 15"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/2p4P/8/P2P4/8 w - d6 0 16"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/P1p4P/8/3P4/8 b - a3 0 16" // 14b fail, 14 2-22 works!
//                    "2K1k1br/5n1r/2p2pN1/3p1N2/P6P/2p5/3P4/8 w - - 0 17"
//                "2K1k1br/5n1r/2p2pN1/3p1N2/P6P/2P5/8/8 b - - 0 17" // chess.com sees mate here
//                "2K1k1br/5n1r/5pN1/2pp1N2/P6P/2P5/8/8 w - - 0 18"
//                "2K1k1br/5n1r/5pN1/P1pp1N2/7P/2P5/8/8 b - - 0 18"
//                "2K1k1br/5n1r/5pN1/P1p2N2/3p3P/2P5/8/8 w - - 0 19"
//                "2K1k1br/5n1r/P4pN1/2p2N2/3p3P/2P5/8/8 b - - 0 19"
//                "2K1k1br/5n1r/P4pN1/2p2N2/7P/2Pp4/8/8 w - - 0 20"
//                "2K1k1br/P4n1r/5pN1/2p2N2/7P/2Pp4/8/8 b - - 0 20"
//                "2K1k1br/P4n1r/5pN1/5N2/2p4P/2Pp4/8/8 w - - 0 21"
//                "Q1K1k1br/5n1r/5pN1/5N2/2p4P/2Pp4/8/8 b - - 0 21"
            );
            System.out.println(state);

            Stopwatch stopwatch = Stopwatch.createStarted();
            DoubleSummaryStatistics stats = new DoubleSummaryStatistics();
            for (int i = 0; i < 1_000; i++) {
                double value = eval.evaluate(state);
                stats.accept(value);
                System.out.println("value " + i + ": " + value + " avg " + stats.getAverage() + " took " + stopwatch);
                stopwatch.reset().start();
            }
        }
    }
}
