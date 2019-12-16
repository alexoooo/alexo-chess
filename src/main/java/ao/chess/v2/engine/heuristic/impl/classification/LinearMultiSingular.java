//package ao.chess.v2.engine.heuristic.impl.classification;
//
//import ao.ai.classify.linear.SpaLearner;
//import ao.ai.ml.model.algo.OnlineMultiScoreLearner;
//import ao.ai.ml.model.input.RealList;
//import ao.ai.ml.model.output.MultiScoreClass;
//import ao.chess.v2.engine.heuristic.MoveHeuristic;
//import ao.chess.v2.engine.run.Config;
//import ao.chess.v2.state.Move;
//import ao.chess.v2.state.Outcome;
//import ao.chess.v2.state.State;
//import ao.util.persist.PersistentObjects;
//import org.apache.log4j.Logger;
//
//import java.io.File;
//import java.io.Serializable;
//
///**
// * User: AO
// * Date: Oct 16, 2010
// * Time: 8:46:56 PM
// */
//public class LinearMultiSingular
//        implements MoveHeuristic, Serializable
//{
//    //--------------------------------------------------------------------
//    private static final long serialVersionUID = 2010 * 10 * 31;
//
//    private static final Logger LOG =
//            Logger.getLogger(LinearMultiSingular.class);
//
//    private static final File dir = Config.dir(
//            "lookup/heuristic/linear-spa");
//
//
//    //------------------------------------------------------------------------
//    private final String                             id;
//    private final OnlineMultiScoreLearner<RealList> learner;
//
//
//    //------------------------------------------------------------------------
//    public LinearMultiSingular(String id)
//    {
//        this.id = id;
//
//        OnlineMultiScoreLearner<RealList> rememberedLearner =
//                PersistentObjects.retrieve( new File(dir, id) );
//
//        learner = ((rememberedLearner == null)
//                   ? new SpaLearner()
//                   : rememberedLearner);
//    }
//
//
//    //------------------------------------------------------------------------
//    @Override
//    public double evaluate(State state, int move)
//    {
//        State beingEvaluated = state.prototype();
//        Move.apply( move, beingEvaluated );
//
//        MultiScoreClass classification =
//                learner.classify(ChessClassUtils.encode(
//                        beingEvaluated));
//
//        double drawConf = classification.scoreOfClass(
//                Outcome.DRAW.ordinal());
//
//        double winConf = classification.scoreOfClass(
//                Outcome.wins( state.nextToAct() ).ordinal());
//
//        double lossConf = classification.scoreOfClass(
//                Outcome.loses( state.nextToAct() ).ordinal());
//
//        double minConf = Math.min(drawConf, Math.min(winConf, lossConf));
//        if (minConf > 0)
//        {
//            return (winConf + drawConf / 2.0) / (winConf + drawConf + lossConf);
//        }
//        else
//        {
//            double drawAbs = (drawConf < 0 ? -drawConf : drawConf - minConf);
//            double winAbs  = (winConf  < 0 ? -winConf  : winConf  - minConf);
//            double lossAbs = (lossConf < 0 ? -lossConf : lossConf - minConf);
//
//            return (winAbs + drawAbs / 2.0) / (winAbs + drawAbs + lossAbs);
//        }
//
////        double totalConf = drawConf + winConf + lossConf;
//
////        return winConf;
////        return winConf + drawConf / 2.0;
////        return -drawConf;
//    }
//
//
//    //------------------------------------------------------------------------
//    @Override
//    public void update(State fromState, int move, Outcome outcome)
//    {
//        State beingEvaluated = fromState.prototype();
//        Move.apply( move, beingEvaluated );
//
//        learner.learn(
//                ChessClassUtils.encode(
//                        beingEvaluated),
//                MultiScoreClass.create(
//                        outcome.ordinal()));
//    }
//
//
//    //------------------------------------------------------------------------
//    @Override
//    public void persist()
//    {
//        LOG.debug("persisting " + id + " with " + learner);
//        PersistentObjects.persist(learner, new File(dir, id));
//    }
//
//
//    //------------------------------------------------------------------------
//    @Override
//    public String toString()
//    {
//        return learner.toString();
//    }
//}
