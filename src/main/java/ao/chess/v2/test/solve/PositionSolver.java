package ao.chess.v2.test.solve;


import ao.chess.v2.engine.Player;
import ao.chess.v2.engine.neuro.puct.PuctEnsembleModel;
import ao.chess.v2.engine.neuro.puct.PuctModel;
import ao.chess.v2.engine.neuro.rollout.RolloutPlayer;
import ao.chess.v2.engine.neuro.rollout.store.SynchronizedRolloutStore;
import ao.chess.v2.engine.neuro.rollout.store.TieredRolloutStore;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableList;

import java.nio.file.Path;
import java.nio.file.Paths;


public class PositionSolver {
    //-----------------------------------------------------------------------------------------------------------------
//    private static final int flushFrequencyMillis = 15 * 60 * 1_000;
    private static final int flushFrequencyMillis = 20 * 60 * 1_000;
    private static final int time = 7 * 24 * 60 * 60 * 1000;


    //-----------------------------------------------------------------------------------------------------------------
    public static void main(String[] args) {
//        PuctModel model = new PuctMultiModel(
//                ImmutableRangeMap.<Integer, Path>builder()
//                        .put(Range.closed(2, 22),
//                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
//                        .put(Range.closed(23, 32),
////                                        Paths.get("lookup/nn/res_14b_n811.zip"))
//                                Paths.get("lookup/nn/res_20_n1307.zip"))
//                        .build()
//        );
        PuctModel model = new PuctEnsembleModel(ImmutableList.of(
                Paths.get("lookup/nn/res_20_n1307.zip"),
                Paths.get("lookup/nn/res_20_b_n1008.zip")
        ));
//        PuctModel model = new PuctMixedModel(
//                ImmutableRangeMap.<Integer, Path>builder()
//                        .put(Range.closed(2, 20),
//                                Paths.get("lookup/nn/res_14_p_2_22_n1220.zip"))
//                        .put(Range.closed(21, 28),
//                                Paths.get("lookup/nn/res_14_p_16_28_n1209.zip"))
//                        .put(Range.closed(29, 32),
//                                Paths.get("lookup/nn/res_14_p_23_32_n956.zip"))
//                        .build()
//        );

        Player player = new RolloutPlayer.Builder(model)
                .rolloutLength(3)
//                .threads(1)
//                .threads(2)
//                .threads(48)
//                .threads(52)
//                .threads(64)
//                .threads(96)
//                .threads(128)
//                .threads(160)
//                .threads(192)
//                .threads(224)
//                .threads(256)
//                .threads(320)
//                .threads(384)
//                .threads(448)
                .threads(512)
//                .stochastic(true)
//                .store(new SynchronizedRolloutStore(new MapRolloutStore()))
                .store(new SynchronizedRolloutStore(
                        new TieredRolloutStore(Path.of(
                                "lookup/tree/root.bin"
//                                "lookup/tree/root-1.bin"
                        ))))
                .build();


        State state = State.fromFen(
                // initial
                "rnbqkbnr/pppppppp/8/8/8/8/PPPPPPPP/RNBQKBNR w KQkq - 0 1");

        System.out.println(state);

        boolean solved = false;
        int episodeCount = time / flushFrequencyMillis;
        for (int i = 0; i < episodeCount; i++) {
            int move = player.move(state, flushFrequencyMillis, flushFrequencyMillis, 0);
            System.out.println("Episode " + i + ") " + Move.toString(move));

            if (player.isSolved(state)) {
                solved = true;
                break;
            }
        }

        if (! solved) {
            int remainingTime = time % flushFrequencyMillis;
            int move = player.move(state, remainingTime, remainingTime, 0);
            System.out.println(Move.toString(move));
        }

        player.close();
    }
}
