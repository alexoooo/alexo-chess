package ao.chess.v2.engine.heuristic.impl.classification;

import ao.ai.classify.linear.PassiveAggressive;
import ao.ai.ml.model.algo.OnlineBinaryScoreLearner;
import ao.ai.ml.model.input.RealList;
import ao.ai.ml.model.output.BinaryClass;
import ao.ai.ml.model.output.BinaryScoreClass;
import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.persist.PersistentObjects;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;

/**
 * User: AO
 * Date: Oct 16, 2010
 * Time: 8:46:56 PM
 */
public class LinearByMaterial
        implements MoveHeuristic, Serializable
{
    //--------------------------------------------------------------------
    private static final long serialVersionUID = 2010 * 10 * 31;

    private static final Logger LOG =
            Logger.getLogger(LinearByMaterial.class);

    private static final File dir = Config.dir(
            "lookup/heuristic/linear-mat-pa");


    //------------------------------------------------------------------------
    private final String id;

    private final Int2ObjectMap<OnlineBinaryScoreLearner<RealList>>
            materialToLearner;

    private final Int2ObjectMap<Multiset<Piece>>
            materialToPieces = new Int2ObjectOpenHashMap<Multiset<Piece>>();


    //------------------------------------------------------------------------
    public LinearByMaterial(String id)
    {
        this.id = id;

        Int2ObjectMap<OnlineBinaryScoreLearner<RealList>> rememberedLearners
                = PersistentObjects.retrieve( new File(dir, id) );

        materialToLearner = ((rememberedLearners == null)
                   ? new Int2ObjectOpenHashMap
                            <OnlineBinaryScoreLearner<RealList>>()
                   : rememberedLearners);
    }

    private OnlineBinaryScoreLearner<RealList> newLearner()
    {
        return new PassiveAggressive();
    }


    //------------------------------------------------------------------------
    @Override
    public double evaluate(State state, int move)
    {
//        State beingEvaluated = state.prototype();
//        Move.apply( move, beingEvaluated );

        int undo = Move.apply( move, state );

        OnlineBinaryScoreLearner<RealList> learner = lookup(
                state);

        BinaryScoreClass classification =
                learner.classify(ChessClassUtils.encodeByMaterial(
                        state));
//                        beingEvaluated));

        Move.unApply(undo, state);

//        return (state.nextToAct() == Colour.WHITE ? 1 : -1) *
//                classification.positiveScore();
        return classification.positiveScore();
    }


    //------------------------------------------------------------------------
    @Override
    public void update(State fromState, int move, Outcome outcome)
    {
//        State beingEvaluated = fromState.prototype();
//        Move.apply( move, beingEvaluated );

        int undo = Move.apply( move, fromState );

        OnlineBinaryScoreLearner<RealList> learner = lookup(
                fromState);
//                beingEvaluated);

        learner.learn(
                ChessClassUtils.encodeByMaterial(
//                        beingEvaluated),
                        fromState),
                BinaryClass.create(outcome.winner() ==
//                        fromState.nextToAct()));
                        fromState.nextToAct().invert()));

        Move.unApply(undo, fromState);
    }


    //------------------------------------------------------------------------
    private OnlineBinaryScoreLearner<RealList> lookup(State state)
    {
        int tally = state.tallyAllMaterial();

        OnlineBinaryScoreLearner<RealList> learner =
                materialToLearner.get(tally);

        Multiset<Piece> pieceMultiset =
                ImmutableMultiset.copyOf(
                        state.material().values());

        if (pieceMultiset.size() != state.pieceCount())
        {
            System.out.println("!!! PIECE COUNT MISMATCH !!!");
        }

        if (learner == null)
        {
            learner = newLearner();
            materialToLearner.put(tally, learner);

            materialToPieces.put(tally, pieceMultiset);
        }
        else
        {
            Multiset<Piece> existingPieces =
                    materialToPieces.get( tally );

            if (! existingPieces.equals( pieceMultiset ))
            {
                System.out.println("!!! PIECE TALLY COLLISION !!!");
            }
        }

        return learner;
    }


    //------------------------------------------------------------------------
    @Override
    public void persist()
    {
        LOG.debug("persisting " + id + " with " + materialToLearner.size());
        PersistentObjects.persist(materialToLearner, new File(dir, id));
    }


    //------------------------------------------------------------------------
    @Override
    public String toString()
    {
        return "LinearByMaterial: " + materialToLearner.size();
    }
}
