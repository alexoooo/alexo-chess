package ao.chess.v2.engine.heuristic.impl.classification;

import ao.ai.classify.decision.impl.classification.raw.Prediction;
import ao.ai.classify.decision.impl.input.raw.example.Context;
import ao.ai.classify.decision.impl.input.raw.example.ContextImpl;
import ao.ai.classify.decision.impl.input.raw.example.Datum;
import ao.ai.classify.decision.impl.input.raw.example.ExampleImpl;
import ao.ai.classify.decision.impl.model.raw.Classifier;
import ao.ai.classify.decision.impl.model.raw.ClassifierImpl;
import ao.ai.classify.decision.impl.random.RandomLearner;
import ao.chess.v2.data.Location;
import ao.chess.v2.engine.heuristic.MoveHeuristic;
import ao.chess.v2.engine.heuristic.impl.simple.SimpleWinTally;
import ao.chess.v2.engine.run.Config;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Outcome;
import ao.chess.v2.state.State;
import ao.util.persist.PersistentObjects;
import ao.util.serial.Serializer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * User: aostrovsky
 * Date: 24-Oct-2009
 * Time: 4:58:04 PM
 */
public class ClassByMove implements MoveHeuristic, Serializable
{
    //--------------------------------------------------------------------
    private static final long serialVersionUID = -120528807166915154L;

    private final static Logger LOG =
            Logger.getLogger(SimpleWinTally.class);

    private static final File dir = Config.dir(
            "lookup/heuristic/random-class");


    //--------------------------------------------------------------------
    private final String                    id;
    private final Int2ObjectMap<Classifier> moveToStats;


    //--------------------------------------------------------------------
    public ClassByMove(String id) {
        this.id     = id;
        moveToStats = new Int2ObjectOpenHashMap<Classifier>();
    }


    //--------------------------------------------------------------------
    private Classifier classifier(int move) {
        Classifier classifier = moveToStats.get( move );
        if (classifier == null) {
            classifier = new ClassifierImpl(
                    new RandomLearner());
            moveToStats.put( move, classifier );
        }
        return classifier;
    }


    //--------------------------------------------------------------------
    @Override public double evaluate(State state, int move) {
        Prediction prediction =
                classifier( move ).classify(classifiable(state));

        double winRate =
                prediction.probabilityOf(
                        new Datum(state.nextToAct()));

        return Math.exp(winRate);
    }

    private Context classifiable(State state) {
        List<Datum> data = new ArrayList<Datum>();
        data.add(new Datum(state.nextToAct()));

        for (int rank = 0; rank < Location.RANKS; rank++) {
            for (int file = 0; file < Location.FILES; file++) {
                Piece piece = state.pieceAt(rank, file);
                data.add(new Datum(String.valueOf(
                        Location.squareIndex(rank, file)),
                        String.valueOf(piece)));
            }
        }

        return new ContextImpl(data);
    }


    //--------------------------------------------------------------------
    @Override public void update(
            State fromState, int move, Outcome outcome) {
        if (outcome == Outcome.DRAW) return;

        classifier( move ).add(new ExampleImpl(
                classifiable(fromState),
                new Datum(outcome.winner())));
    }


    //--------------------------------------------------------------------
    public static ClassByMove retrieve(String id) {
        ClassByMove tally =
                PersistentObjects.retrieve( new File(dir, id) );
        return (tally == null)
               ? new ClassByMove(id)
               : tally;
    }

    @Override public void persist() {
        LOG.debug("persisting " + id + " with " + moveToStats.size());
//        Serializer.toBytesFast(this);

        PersistentObjects.persist(this, new File(dir, id));
    }
}
