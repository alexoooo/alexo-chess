package ao.chess.v2.engine.heuristic.impl.classification;

import ao.ai.classify.linear.PassiveAggressive;
import ao.ai.ml.model.algo.OnlineBinaryScoreLearner;
import ao.ai.ml.model.input.RealList;
import ao.ai.ml.model.output.BinaryClass;
import ao.ai.ml.model.output.BinaryScoreClass;
import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Colour;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import com.google.common.collect.ImmutableMultiset;
import com.google.common.collect.Multiset;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import it.unimi.dsi.fastutil.ints.IntIterator;
import org.apache.log4j.Logger;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;

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


    public static LinearByMaterial retrieve(String id)
    {
        try
        {
            return retrieveChecked(id);
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    private static LinearByMaterial retrieveChecked(String id) throws IOException
    {
        Path inFile = new File(dir, id).toPath();
        LinearByMaterial instance = new LinearByMaterial(id);

        if (! Files.exists(inFile)) {
            return instance;
        }

        try (InputStream in = Files.newInputStream(inFile);
                ObjectInputStream ois = new ObjectInputStream(in))
        {
            int size = ois.readInt();
            for (int i = 0; i < size; i++) {
                int key = ois.readInt();
                PassiveAggressive pa = PassiveAggressive.restore(ois);
                instance.materialToLearner.put(key, pa);
            }
        }

        return instance;
    }


    //------------------------------------------------------------------------
    private final String id;

    private final Int2ObjectMap<PassiveAggressive>
            materialToLearner;

//    private final Int2ObjectMap<Multiset<Piece>>
//            materialToPieces = new Int2ObjectOpenHashMap<>();


    //------------------------------------------------------------------------
    public LinearByMaterial(String id)
    {
        this.id = id;
        materialToLearner = new Int2ObjectOpenHashMap<>();
    }


    private PassiveAggressive newLearner()
    {
        return new PassiveAggressive();
    }


    //------------------------------------------------------------------------
    @Override
    public double evaluate(State state, int move)
    {
        int undo = Move.apply( move, state );

        OnlineBinaryScoreLearner<RealList> learner = lookup(state);

        BinaryScoreClass classification =
                learner.classify(ChessClassUtils.encodeByMaterial(state));

        Move.unApply(undo, state);

        double whiteWinScore = classification.positiveScore();
        return state.nextToAct() == Colour.WHITE
                ? whiteWinScore
                : -whiteWinScore;
    }


    //------------------------------------------------------------------------
    @Override
    public void update(State fromState, int move, Outcome outcome)
    {
//        int undo = Move.apply( move, fromState );

        OnlineBinaryScoreLearner<RealList> learner = lookup(fromState);

        learner.learn(
                ChessClassUtils.encodeByMaterial(fromState),
                BinaryClass.create(outcome.winner() == Colour.WHITE));

//        Move.unApply(undo, fromState);
    }


    //------------------------------------------------------------------------
    private OnlineBinaryScoreLearner<RealList> lookup(State state)
    {
        int tally = state.tallyAllMaterial();

        PassiveAggressive learner =
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

//            materialToPieces.put(tally, pieceMultiset);
        }
        else
        {
//            Multiset<Piece> existingPieces =
//                    materialToPieces.get( tally );
//
//            if (! existingPieces.equals( pieceMultiset ))
//            {
//                System.out.println("!!! PIECE TALLY COLLISION !!!");
//            }
        }

        return learner;
    }


    //------------------------------------------------------------------------
    @Override
    public void persist()
    {
        LOG.debug("persisting " + id + " with " + materialToLearner.size());
        try
        {
            persistChecked();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
    }


    private void persistChecked() throws IOException
    {
        Path outFile = new File(dir, id).toPath();

        Files.createDirectories(outFile.getParent());

        try (OutputStream out = Files.newOutputStream(
                outFile, StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.TRUNCATE_EXISTING);
                ObjectOutputStream oos = new ObjectOutputStream(out))
        {
            oos.writeInt(materialToLearner.size());

            IntIterator iterator = materialToLearner.keySet().iterator();
            while (iterator.hasNext()) {
                int next = iterator.nextInt();
                oos.writeInt(next);
                PassiveAggressive pa = materialToLearner.get(next);
                PassiveAggressive.persist(pa, oos);
            }
        }
    }


    //------------------------------------------------------------------------
    @Override
    public String toString()
    {
        return "LinearByMaterial: " + materialToLearner.size();
    }
}
