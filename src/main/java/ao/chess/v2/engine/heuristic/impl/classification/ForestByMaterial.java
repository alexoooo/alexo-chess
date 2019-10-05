package ao.chess.v2.engine.heuristic.impl.classification;


import ao.ai.classify.online.forest.OnlineRandomForest;
import ao.ai.ml.model.output.MultiClass;
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


public class ForestByMaterial
        implements MoveHeuristic, Serializable
{
    //--------------------------------------------------------------------
    private static final long serialVersionUID = 2019_10_05;

    private static final Logger LOG =
            Logger.getLogger(ForestByMaterial.class);

    private static final File dir = Config.dir(
            "lookup/heuristic/forest-mat-pa");


    //------------------------------------------------------------------------
    private final String id;

    private final Int2ObjectMap<OnlineRandomForest>
            materialToLearner;

    private final Int2ObjectMap<Multiset<Piece>>
            materialToPieces = new Int2ObjectOpenHashMap<Multiset<Piece>>();


    //------------------------------------------------------------------------
    public ForestByMaterial(String id)
    {
        this.id = id;

        Int2ObjectMap<OnlineRandomForest> rememberedLearners
                = PersistentObjects.retrieve( new File(dir, id) );

        materialToLearner = ((rememberedLearners == null)
                ? new Int2ObjectOpenHashMap<>()
                : rememberedLearners);
    }

    private OnlineRandomForest newLearner()
    {
        return new OnlineRandomForest();
    }


    //------------------------------------------------------------------------
    @Override
    public double evaluate(State state, int move)
    {
        int undo = Move.apply( move, state );

        OnlineRandomForest learner = lookup(
                state);

        MultiClass classification =
                learner.classify(ChessClassUtils.encodeByMaterial(state));

        Move.unApply(undo, state);

        Outcome outcome = Outcome.values[classification.best()];

        return outcome == Outcome.DRAW
                ? 0.5
                : outcome.winner() == state.nextToAct()
                ? 1
                : 0;
    }


    //------------------------------------------------------------------------
    @Override
    public void update(State fromState, int move, Outcome outcome)
    {
        int undo = Move.apply( move, fromState );

        OnlineRandomForest learner = lookup(
                fromState);

        learner.learn(
                ChessClassUtils.encodeByMaterial(fromState),
                MultiClass.create(outcome.ordinal()));

        Move.unApply(undo, fromState);
    }


    //------------------------------------------------------------------------
    private OnlineRandomForest lookup(State state)
    {
        int tally = state.tallyAllMaterial();

        OnlineRandomForest learner =
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
        return "ForestByMaterial: " + materialToLearner.size();
    }
}
