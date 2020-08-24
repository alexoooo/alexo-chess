package ao.chess.v2.engine.endgame.v2;

import ao.chess.v2.engine.endgame.common.PositionTraverser;
import ao.chess.v2.engine.endgame.v2.index.EfficientLengthIndex;
import ao.chess.v2.engine.endgame.v2.index.EfficientOffsetIndex;
import ao.chess.v2.piece.Piece;
import ao.chess.v2.state.Representation;
import ao.chess.v2.state.State;
import ao.util.pass.Traverser;
import com.google.common.base.Stopwatch;
import it.unimi.dsi.fastutil.bytes.ByteArrayList;
import it.unimi.dsi.fastutil.bytes.ByteList;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.List;

import static com.google.common.base.Preconditions.checkState;


public class EfficientStateMap
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(EfficientStateMap.class);


    //-----------------------------------------------------------------------------------------------------------------
    public static EfficientStateMap loadOrCompute(
            EfficientMinPerfectHash minHash,
            List<Piece> pieces,
            Path indexStore,
            Path bodyStore
    ) {
        try {
            return loadOrComputeChecked(minHash, pieces, indexStore, bodyStore);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static EfficientStateMap loadOrComputeChecked(
            EfficientMinPerfectHash minHash,
            List<Piece> pieces,
            Path indexStore,
            Path bodyStore
    ) throws IOException {
        int size = minHash.size();

        EfficientOffsetIndex offsetIndex;
        if (Files.exists(indexStore)) {
            EfficientLengthIndex lengthIndex = EfficientLengthIndex.load(indexStore);
            offsetIndex = EfficientOffsetIndex.build(lengthIndex, Byte.BYTES);
        }
        else {
            EfficientLengthIndex lengthIndex = new EfficientLengthIndex(size);
            Indexer indexer = new Indexer(minHash, lengthIndex);
            indexer.populate(pieces);
            lengthIndex.store(indexStore);
            offsetIndex = EfficientOffsetIndex.build(lengthIndex, Byte.BYTES);
        }

        EfficientStateMap map;
        if (Files.exists(bodyStore)) {
            Stopwatch stopwatch = Stopwatch.createStarted();

            RandomAccessFile file = new RandomAccessFile(bodyStore.toFile(), "r");
            map = new EfficientStateMap(file, offsetIndex);

//            try (DataInputStream in = new DataInputStream(new BufferedInputStream(
//                    Files.newInputStream(store)))
//            ) {
//                byte[][] byIndex = new byte[size][];
//                byte[] buffer = new byte[Byte.MAX_VALUE];
//                for (int i = 0; i < size; i++) {
//                    byte length = in.readByte();
//                    int read = in.read(buffer, 0, length);
//                    checkState(read == length);
//                    byIndex[i] = Arrays.copyOf(buffer, length);
//                }
//                map = new EfficientStateMap(byIndex);
//            }

            logger.info("Loaded, took: {}", stopwatch);
        }
        else {
            RandomAccessFile rwFile = new RandomAccessFile(bodyStore.toFile(), "rw");
//            map = new EfficientStateMap(file, offsetIndex);

            Builder builder = new Builder(offsetIndex, minHash, rwFile);
            builder.populate(pieces);

            rwFile.getFD().sync();
            rwFile.close();

            RandomAccessFile file = new RandomAccessFile(bodyStore.toFile(), "r");
            map = new EfficientStateMap(file, offsetIndex);

//            try (DataOutputStream out = new DataOutputStream(
//                    new BufferedOutputStream(
//                            Files.newOutputStream(store)))
//            ) {
//                for (int i = 0; i < size; i++) {
//                    out.writeByte(builder.byIndex[i].length);
//                    out.write(builder.byIndex[i]);
//                }
//            }
        }

        return map;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static class Indexer
            implements Traverser<State>
    {
        private final EfficientMinPerfectHash minHash;
        private final EfficientLengthIndex lengthIndex;
        private final ByteList buffer = new ByteArrayList();

        private int count = 0;
        private int duplicates = 0;
        private Stopwatch partialStopwatch;

        public Indexer(EfficientMinPerfectHash minHash, EfficientLengthIndex lengthIndex) {
            this.minHash = minHash;
            this.lengthIndex = lengthIndex;
        }

        public void populate(List<Piece> pieces) {
            logger.info("Indexing");
            Stopwatch stopwatch = Stopwatch.createStarted();
            partialStopwatch = Stopwatch.createStarted();
            new PositionTraverser().traverse(pieces, this);
            logger.info("Finished indexing, took: {}", stopwatch);
        }

        @Override
        public void traverse(State state) {
            long staticHash = state.staticHashCode();
            int index = minHash.index(staticHash);

            Representation.packStream(state, buffer);
            int size = buffer.size();
            buffer.clear();

            byte previous = lengthIndex.get(index);
            if (previous != 0) {
                checkState(previous == size);
                duplicates++;
            }
            else {
                lengthIndex.add(index, (byte) size);
            }
            count++;

            if (count % 100_000_000 == 0) {
                logger.info("Indexing progress {}, {} unique / {} duplicates - took: {}",
                        count, count - duplicates, duplicates, partialStopwatch);
                partialStopwatch = Stopwatch.createStarted();
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static class Builder
            implements Traverser<State>
    {
        //--------------------------------------------------------------------
        private final EfficientOffsetIndex offsetIndex;
        private final EfficientMinPerfectHash minHash;
        private final RandomAccessFile file;
//        private final byte[][] byIndex;

        private final ByteList packBuffer = new ByteArrayList();
        private final byte[] fileBuffer = new byte[Byte.MAX_VALUE];

        private int count = 0;
        private int duplicates = 0;
        private Stopwatch partialStopwatch;


        //--------------------------------------------------------------------
        public Builder(EfficientOffsetIndex offsetIndex, EfficientMinPerfectHash minHash, RandomAccessFile file)
        {
            this.offsetIndex = offsetIndex;
            this.minHash = minHash;
            this.file = file;
//            byIndex = new byte[ minHash.size() ][];
        }


        //--------------------------------------------------------------------
        public void populate(List<Piece> pieces) {
            logger.info("Building");

            Stopwatch stopwatch = Stopwatch.createStarted();
            partialStopwatch = Stopwatch.createStarted();

            new PositionTraverser().traverse(
                    pieces, this);

            logger.info("Finished building, took: {}", stopwatch);
        }


        //--------------------------------------------------------------------
        @Override
        public void traverse(State state)
        {
            long staticHash = state.staticHashCode();
            State existing = get(staticHash);
            if (existing == null) {
                put(staticHash, state);
            }
            else if (! existing.equals(state)) {
                logger.error("staticHashCode COLLISION FOUND!!!");
                System.out.println(existing);
                System.out.println("vs");
                System.out.println(state);
                System.exit(1);
            }
            else {
                duplicates++;
            }
            count++;

            if (count % 10_000_000 == 0) {
                logger.info("Building progress {}, {} unique / {} duplicates - took: {}",
                        count, count - duplicates, duplicates, partialStopwatch);
                partialStopwatch = Stopwatch.createStarted();
            }
        }


        private void put(long staticHash, State state) {
            int index = minHash.index(staticHash);

            Representation.packStream(state, packBuffer);
            int length = packBuffer.size();
            packBuffer.toArray(fileBuffer);
            packBuffer.clear();

            long offset = offsetIndex.offset(index);
            int expectedLength = offsetIndex.length(index);
            checkState(length == expectedLength);

            try {
                file.seek(offset);
                file.write(fileBuffer, 0, length);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }

//            byIndex[ minHash.index(staticHash) ] =
//                    Representation.packStream(state);
        }


        private State get(long staticHash) {
            int index = minHash.index(staticHash);
            long offset = offsetIndex.offset(index);
            int length = offsetIndex.length(index);

            int read;
            try {
                file.seek(offset);
                read = file.read(fileBuffer, 0, length);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }

            boolean hasValue = false;
            if (read != -1) {
                checkState(read == length);
                for (int i = 0; i < length; i++) {
                    if (fileBuffer[i] != 0) {
                        hasValue = true;
                        break;
                    }
                }
            }

            return hasValue
                    ? Representation.unpackStream(fileBuffer, 0, length)
                    : null;

//            byte[] packed = byIndex[ index ];
//            return packed == null
//                    ? null
//                    : Representation.unpackStream(packed);
//
//            return getIndexed(  );
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
//    private final byte[][] byIndex;
    private final RandomAccessFile file;
//    private final MappedByteBuffer mmBuffer;
    private final EfficientOffsetIndex offsetIndex;

    private final byte[] stateBuffer = new byte[Byte.MAX_VALUE];
    private long nextOffset = 0;


    //-----------------------------------------------------------------------------------------------------------------
    private EfficientStateMap(
            RandomAccessFile file,
//            MappedByteBuffer mmBuffer,
            EfficientOffsetIndex offsetIndex)
    {
//        this.byIndex = byIndex;
        this.file = file;
//        this.mmBuffer = mmBuffer;
        this.offsetIndex = offsetIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void close() throws IOException {
        file.close();
    }


    //-----------------------------------------------------------------------------------------------------------------
    private State get(int index) {
        try {
            return getChecked(index);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private State getChecked(int index) throws IOException {
        long offset = offsetIndex.offset(index);
        int length = offsetIndex.length(index);

        if (nextOffset != offset) {
            file.seek(offset);
        }

        int read = file.read(stateBuffer, 0, length);
        checkState(read == length);

        nextOffset = offset + length;

        return Representation.unpackStream(stateBuffer, 0, length);
    }


    public void traverse(Traverser<State> visitor) {
        int size = offsetIndex.size();
        for (int i = 0; i < size; i++) {
            State state = get(i);
            visitor.traverse( state );
        }
    }


    public Iterable<State> states(final int[] indexes) {
        return () -> new Iterator<>() {
            private int nextIndex = 0;

            @Override public boolean hasNext() {
                return nextIndex < indexes.length;
            }

            @Override public State next() {
                return get( indexes[nextIndex++] );
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    public Iterable<State> states() {
        int size = offsetIndex.size();
        return () -> new Iterator<>() {
            private int nextIndex = 0;

            @Override public boolean hasNext() {
                return nextIndex < size;
            }

            @Override public State next() {
                return get(nextIndex++);
            }

            @Override public void remove() {
                throw new UnsupportedOperationException();
            }
        };
    }


    //--------------------------------------------------------------------
//    public int size() {
//        return byIndex.length;
//    }
}
