package ao.chess.v2.test;

import ao.chess.v2.engine.mcts.heuristic.MctsCaptureHeuristic;
import ao.chess.v2.engine.mcts.heuristic.MctsHeuristicImpl;
import ao.chess.v2.engine.mcts.node.MctsNodeImpl;
import ao.chess.v2.engine.mcts.player.MctsPlayer;
import ao.chess.v2.engine.mcts.rollout.MaterialFallbackRollout;
import ao.chess.v2.engine.mcts.rollout.MaterialMixedRollout;
import ao.chess.v2.engine.mcts.rollout.MaterialPureRollout;
import ao.chess.v2.engine.mcts.rollout.MctsRolloutImpl;
import ao.chess.v2.engine.mcts.scheduler.MctsSchedulerImpl;
import ao.chess.v2.engine.mcts.transposition.NullTransTable;
import ao.chess.v2.engine.mcts.value.*;


public enum MctsPrototypes {
    ;


    public static final MctsPlayer mctsPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MctsRolloutImpl(false),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "random");

    public static final MctsPlayer mctsFallbackPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(8,false)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random");

    public static final MctsPlayer mctsFallbackOptPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(1,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-1");

    public static final MctsPlayer mctsFallbackOpt2Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(2,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-2");

    public static final MctsPlayer mctsFallbackOpt4Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(4,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-4");

    public static final MctsPlayer mctsFallbackOpt8Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(8,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-8");

    public static final MctsPlayer mctsFallbackOpt16Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(16,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-16");

    public static final MctsPlayer mctsFallbackOpt32Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(32,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-32");

    public static final MctsPlayer mctsFallbackOpt64Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(64,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-64");

    public static final MctsPlayer mctsFallbackOpt128Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(128,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-128");

    public static final MctsPlayer mctsFallbackOpt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-192");

    public static final MctsPlayer mctsFallbackOpt256Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(256,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-256");

    public static final MctsPlayer mctsFallbackOpt512Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(512,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-512");

    public static final MctsPlayer mctsFallbackOpt1024Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(1024,true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-opt-1024");

    public static final MctsPlayer mctsFallbackDeepPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(8, false)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep");

    public static final MctsPlayer mctsFallbackDeepOpt16Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(16, true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-opt-16");


    public static final MctsPlayer mctsFallbackDeepOpt32Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(32, true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-opt-32");


    public static final MctsPlayer mctsFallbackDeepOpt64Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(64, true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-opt-64");

    public static final MctsPlayer mctsFallbackDeepOpt128Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(128, true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-opt-128");


    public static final MctsPlayer mctsFallbackDeepOpt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-opt-192");

    public static final MctsPlayer mctsFallbackDeepOpt256Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(256, true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-opt-256");


    public static final MctsPlayer mctsFallbackDeep2Opt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-2-opt-192");

    public static final MctsPlayer mctsFallbackDeep3Opt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value3.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value3.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-3-opt-192");

    public static final MctsPlayer mctsFallbackDeep4Opt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value4.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value4.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-4-opt-192");

    public static final MctsPlayer mctsFallbackDeep5Opt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value5.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value5.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-5-opt-192");

    public static final MctsPlayer mctsFallbackDeep6Opt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value6.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value6.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-6-opt-192");

    public static final MctsPlayer mctsFallbackDeep7Opt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value7.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value7.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-7-opt-192");

    public static final MctsPlayer mctsFallbackDeep8Opt192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value8.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1Value8.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-8-opt-192");


    public static final MctsPlayer mctsFallbackDeep1Tune192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1TunedValue1.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1TunedValue1.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-5-tune-192");

    public static final MctsPlayer mctsFallbackDeep5Tune192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1TunedValue5.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1TunedValue5.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-5-tune-192");

    public static final MctsPlayer mctsFallbackDeep10Tune192Prototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1TunedValue10.Factory(),
            new MaterialFallbackRollout(new MctsRolloutImpl(192, true)),
            new Ucb1TunedValue10.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "fallback-random-deep-5-tune-192");


    public static final MctsPlayer mctsMaterialPurePrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialPureRollout(),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "material-pure");

    public static final MctsPlayer mctsMaterialPureDeepPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialPureRollout(),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "material-pure-deep");

    public static final MctsPlayer mctsMaterialMixedPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MaterialMixedRollout()),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "material-mixed");

    public static final MctsPlayer mctsMaterialMixedRandomPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value.Factory(),
            new MaterialFallbackRollout(new MaterialMixedRollout(true)),
            new Ucb1Value.VisitSelector(),
            new MctsHeuristicImpl(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "material-mixed-random");

    public static final MctsPlayer mctsMaterialMixedDeepPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MaterialMixedRollout()),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "material-mixed-deep");

    public static final MctsPlayer mctsMaterialMixedRandomDeepPrototype = new MctsPlayer(
            new MctsNodeImpl.Factory<>(),
            new Ucb1Value2.Factory(),
            new MaterialFallbackRollout(new MaterialMixedRollout(true)),
            new Ucb1Value2.VisitSelector(),
            new MctsCaptureHeuristic(),
            new NullTransTable<>(),
            new MctsSchedulerImpl.Factory(),
            "material-mixed-random-deep");
}
