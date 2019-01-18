package ao.chess.v2.engine.heuristic.impl.classification;

import ao.ai.classify.linear.PassiveAggressive;
import ao.ai.ml.model.algo.OnlineBinaryScoreLearner;
import ao.ai.ml.model.input.RealList;
import ao.ai.ml.model.output.BinaryClass;
import ao.ai.ml.model.output.BinaryScoreClass;
import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.persist.PersistentObjects;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;

/**
 * User: AO
 * Date: Oct 16, 2010
 * Time: 8:46:56 PM
 */
public class LinearBinarySingular
        implements MoveHeuristic, Serializable
{
    //--------------------------------------------------------------------
    private static final long serialVersionUID = 2010 * 10 * 31;

    private static final Logger LOG =
            Logger.getLogger(LinearBinarySingular.class);

    private static final File dir = Config.dir(
            "lookup/heuristic/linear-pa");


    //------------------------------------------------------------------------
    private final String                             id;
    private final OnlineBinaryScoreLearner<RealList> learner;


    //------------------------------------------------------------------------
    public LinearBinarySingular(String id)
    {
        this.id = id;

        OnlineBinaryScoreLearner<RealList> rememberedLearner =
                PersistentObjects.retrieve( new File(dir, id) );

        learner = ((rememberedLearner == null)
                   ? new PassiveAggressive()
                   : rememberedLearner);
    }


    //------------------------------------------------------------------------
    @Override
    public double evaluate(State state, int move)
    {
        State beingEvaluated = state.prototype();
        Move.apply( move, beingEvaluated );

        BinaryScoreClass classification =
                learner.classify(ChessClassUtils.encode(
                        beingEvaluated));

//        return (state.nextToAct() == Colour.WHITE ? 1 : -1) *
//                classification.positiveScore();
        return classification.positiveScore();
    }


    //------------------------------------------------------------------------
    @Override
    public void update(State fromState, int move, Outcome outcome)
    {
//        if (outcome == Outcome.DRAW)
//        {
//            // binary classification for now
//            return;
//        }

        State beingEvaluated = fromState.prototype();
        Move.apply( move, beingEvaluated );

        learner.learn(
                ChessClassUtils.encode(
                        beingEvaluated),
                BinaryClass.create(
                        outcome.winner() == fromState.nextToAct()));
//                        outcome == Outcome.WHITE_WINS));
    }


    //------------------------------------------------------------------------
    @Override
    public void persist()
    {
        LOG.debug("persisting " + id + " with " + learner);
        PersistentObjects.persist(learner, new File(dir, id));
    }


    //------------------------------------------------------------------------
    @Override
    public String toString()
    {
        return learner.toString();
    }
}
