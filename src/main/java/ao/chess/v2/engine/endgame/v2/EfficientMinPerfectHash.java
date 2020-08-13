package ao.chess.v2.engine.endgame.v2;

import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.State;
import com.google.common.base.Stopwatch;
import com.google.common.collect.AbstractIterator;
import com.google.common.primitives.Ints;
import com.google.common.primitives.Longs;
import it.unimi.dsi.bits.TransformationStrategies;
import it.unimi.dsi.fastutil.longs.LongIterator;
import it.unimi.dsi.fastutil.longs.LongOpenHashSet;
import it.unimi.dsi.fastutil.longs.LongSet;
import it.unimi.dsi.sux4j.mph.GOVMinimalPerfectHashFunction;
import org.apache.commons.lang3.SerializationUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;


public class EfficientMinPerfectHash
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(EfficientMinPerfectHash.class);


    //-----------------------------------------------------------------------------------------------------------------
//    private final List<Piece> material;
    private final Path store;

    private final byte[] buffer = new byte[8];

    private GOVMinimalPerfectHashFunction<byte[]> hash;


    //-----------------------------------------------------------------------------------------------------------------
    public EfficientMinPerfectHash(
//            List<Piece> material,
            Path store)
    {
//        this.material = material;
        this.store = store;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public boolean isStored() {
        return Files.exists(store);
    }


    public void computeAndStore(List<Piece> material) {
        LongSet states = new LongOpenHashSet();

        logger.info("Traversing states");
        Stopwatch traverseStopwatch = Stopwatch.createStarted();

        long[] count = {0};
        Stopwatch[] traverseProgressStopwatch = {Stopwatch.createStarted()};

        new PositionTraverser().traverse(
                material,
                state -> {
                    if (++count[0] % 100_000_000 == 0) {
                        logger.info("Traversing - total {} - unique {} - took {}",
                                count[0], states.size(), traverseProgressStopwatch[0]);
                        traverseProgressStopwatch[0] = Stopwatch.createStarted();
                    }

                    states.add(state.staticHashCode());
                });

        logger.info("Done, found: {} - took: {}", states.size(), traverseStopwatch);

        LongIterator iterator = states.iterator();
        Iterable<byte[]> encoded = () -> new AbstractIterator<>() {
            @Override
            protected byte[] computeNext() {
                if (! iterator.hasNext()) {
                    return endOfData();
                }
                return Longs.toByteArray(iterator.nextLong());
            }
        };

        Stopwatch computeStopwatch = Stopwatch.createStarted();
        try {
            hash = new GOVMinimalPerfectHashFunction.Builder<byte[]>()
                    .keys(encoded)
                    .transform(TransformationStrategies.byteArray())
                    .build();
        }
        catch (IOException e) {
            throw new UncheckedIOException( e );
        }

        logger.info("Computed hash, took: {}", computeStopwatch);

        Stopwatch storeStopwatch = Stopwatch.createStarted();
        byte[] serialized = SerializationUtils.serialize(hash);
        try {
            Files.write(store, serialized);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        logger.info("Saved hash ({}), took: {}", serialized.length, storeStopwatch);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void loadIfRequired() {
        if (hash != null) {
            return;
        }

        Stopwatch stopwatch = Stopwatch.createStarted();

        byte[] serialized;
        try {
            serialized = Files.readAllBytes(store);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }

        hash = SerializationUtils.deserialize(serialized);

        logger.info("Loaded {}, took {}", hash.size64(), stopwatch);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public int index(long staticHash) {
        loadIfRequired();

        long value = staticHash;
        for (int i = 7; i >= 0; i--) {
            buffer[i] = (byte) (value & 0xffL);
            value >>= 8;
        }

//        return (int) hash.getLong(Longs.toByteArray(staticHash));
        return (int) hash.getLong(buffer);
    }


    public int index(State state) {
        return index( state.staticHashCode() );
    }


    public int size() {
        return Ints.checkedCast(hash.size64());
    }
}
