package ao.chess.v2.engine.endgame.v2;

import ao.chess.v2.engine.endgame.v2.index.EfficientLengthIndex;
import ao.chess.v2.engine.endgame.v2.index.EfficientOffsetIndex;
import ao.chess.v2.state.Move;
import ao.chess.v2.state.State;
import ao.util.pass.Traverser;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Ints;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;

import static com.google.common.base.Preconditions.checkState;


public class EfficientMaterialRetrograde
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(EfficientMaterialRetrograde.class);

    private static final int[] emptyPrecedents = new int[0];


    //-----------------------------------------------------------------------------------------------------------------
    public static EfficientMaterialRetrograde loadOrCompute(
            int allMaterialTally,
            EfficientMinPerfectHash minHash,
            EfficientStateMap stateMap,
            Path indexStore,
            Path bodyStore
    ) {
        try {
            return loadOrComputeChecked(allMaterialTally, minHash, stateMap, indexStore, bodyStore);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private static EfficientMaterialRetrograde loadOrComputeChecked(
            int allMaterialTally,
            EfficientMinPerfectHash minHash,
            EfficientStateMap stateMap,
            Path indexStore,
            Path bodyStore
    ) throws IOException {
        int size = minHash.size();

        EfficientOffsetIndex offsetIndex;
        if (Files.exists(indexStore)) {
            EfficientLengthIndex lengthIndex = EfficientLengthIndex.load(indexStore);
            offsetIndex = EfficientOffsetIndex.build(lengthIndex, Integer.BYTES);
        }
        else {
            EfficientLengthIndex countIndex = new EfficientLengthIndex(size);
            Indexer indexer = new Indexer(allMaterialTally, minHash, countIndex);
            indexer.compute(stateMap);
            countIndex.store(indexStore);
            offsetIndex = EfficientOffsetIndex.build(countIndex, Integer.BYTES);
        }

        EfficientMaterialRetrograde retrograde;
        if (Files.exists(bodyStore)) {
//            precedents = new int[size][];

            Stopwatch stopwatch = Stopwatch.createStarted();

            RandomAccessFile file = new RandomAccessFile(bodyStore.toFile(), "r");
            retrograde = new EfficientMaterialRetrograde(file, offsetIndex);

//            try (DataInputStream in = new DataInputStream(new BufferedInputStream(
//                    Files.newInputStream(store)))
//            ) {
//                for (int i = 0; i < size; i++) {
//                    byte length = in.readByte();
//                    int[] buffer = new int[length];
//                    for (int j = 0; j < length; j++) {
//                        buffer[i] = in.readInt();
//                    }
//                    precedents[i] = buffer;
//                }
//            }
            logger.info("Loaded, took: {}", stopwatch);
        }
        else {
            RandomAccessFile rwFile = new RandomAccessFile(bodyStore.toFile(), "rw");

            Builder builder = new Builder(allMaterialTally, minHash, rwFile, offsetIndex);
            builder.compute(stateMap);
//            precedents = builder.precedents;
//
            Stopwatch stopwatch = Stopwatch.createStarted();
//            try (DataOutputStream out = new DataOutputStream(new BufferedOutputStream(
//                    Files.newOutputStream(store)))
//            ) {
//                for (int i = 0; i < size; i++) {
//                    if (precedents[i] == null) {
//                        out.writeByte(0);
//                    }
//                    else {
//                        out.writeByte(precedents[i].length);
//                        for (int index : precedents[i]) {
//                            out.writeInt(index);
//                        }
//                    }
//                }
//            }

            rwFile.getFD().sync();
            rwFile.close();

            RandomAccessFile file = new RandomAccessFile(bodyStore.toFile(), "r");
            retrograde = new EfficientMaterialRetrograde(file, offsetIndex);

            logger.info("Saved, took: {}", stopwatch);
        }

        return retrograde;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static class Indexer
            implements Traverser<State>
    {
        //--------------------------------------------------------------------
        private final int materialTally;
        private final EfficientMinPerfectHash minHash;
        private final EfficientLengthIndex countIndex;

        private final int[] moveBuffer = new int[Move.MAX_PER_PLY];
        private final int[] moveTemp = new int[Move.MAX_PER_PLY];

        private int count;
        private long relationCount;
        private Stopwatch stopwatch;


        //--------------------------------------------------------------------
        public Indexer(int allMaterialTally, EfficientMinPerfectHash minHash, EfficientLengthIndex countIndex) {
            this.materialTally = allMaterialTally;
            this.minHash = minHash;
            this.countIndex = countIndex;
        }


        //--------------------------------------------------------------------
        public void compute(EfficientStateMap stateMap) {
            logger.info("Computing");
            Stopwatch stopwatch = Stopwatch.createStarted();

            stopwatch = Stopwatch.createStarted();
            stateMap.traverse(this);

            logger.info("Done, took: {}", stopwatch);
        }


        //--------------------------------------------------------------------
        @Override
        public void traverse(State state)
        {
            count++;
            if (count % 25_000_000 == 0) {
                logger.info("Indexing progress {}, {} total relations - {} average relations, took {}",
                        count, relationCount, (double) relationCount / count, stopwatch);
                stopwatch = Stopwatch.createStarted();
            }

            int moveCount = state.legalMoves(moveBuffer, moveTemp);
            if (moveCount <= 0) {
                return;
            }

            for (int i = 0; i < moveCount; i++) {
                int legalMove = moveBuffer[i];

                int undo = Move.apply(legalMove, state);
                long childHash = state.staticHashCode();
                boolean materialMatches = (materialTally == state.tallyAllMaterial());
                Move.unApply(undo, state);

                if (materialMatches) {
                    countIndex.increment(minHash.index(childHash));
                    relationCount++;
                }
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private static class Builder
            implements Traverser<State>
    {
        //--------------------------------------------------------------------
        private final int materialTally;
        private final EfficientMinPerfectHash minHash;
        private final RandomAccessFile file;
        private final EfficientOffsetIndex offsetIndex;
//        private final int[][] precedents;
        private final int[] moveBuffer = new int[Move.MAX_PER_PLY];
        private final int[] moveTemp = new int[Move.MAX_PER_PLY];
        private final byte[] ioBuffer = new byte[1024];

        private int count;
        private long relationCount;
        private Stopwatch buildStopwatch;


        //--------------------------------------------------------------------
        public Builder(
                int allMaterialTally,
                EfficientMinPerfectHash minHash,
                RandomAccessFile file,
                EfficientOffsetIndex offsetIndex
        ) {
            this.minHash = minHash;
            materialTally = allMaterialTally;
            this.file = file;
            this.offsetIndex = offsetIndex;
        }


        //--------------------------------------------------------------------
        public void compute(EfficientStateMap stateMap) {
            clear();

            logger.info("Building");

            Stopwatch buildTotalStopwatch = Stopwatch.createStarted();
            buildStopwatch = Stopwatch.createStarted();

            stateMap.traverse(this);

            logger.info("Done building, took: {}", buildTotalStopwatch);
        }


        private void clear() {
            try {
                clearChecked();
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }


        private void clearChecked() throws IOException {
            logger.info("Clearing");

            byte[] sentinelBuffer = new byte[4 * 1024];
            Arrays.fill(sentinelBuffer, (byte) -1);

            Stopwatch clearStopwatch = Stopwatch.createStarted();
            long total = offsetIndex.totalLength();

            long wholeChunks = (int) (total / sentinelBuffer.length);
            for (int i = 0; i < wholeChunks; i++) {
                file.write(sentinelBuffer);
            }

            int remaining = (int) (total % sentinelBuffer.length);
            if (remaining > 0) {
                file.write(sentinelBuffer, 0, remaining);
            }

            file.getFD().sync();
            logger.info("Cleared values, took: {}", clearStopwatch);
        }


        //--------------------------------------------------------------------
        @Override
        public void traverse(State state)
        {
            count++;
            if (count % 1_000_000 == 0) {
                logger.info("Building progress {}, {} total relations - {} average relations, took: {}",
                        count, relationCount, (double) relationCount / count, buildStopwatch);
                buildStopwatch = Stopwatch.createStarted();
            }

            int moveCount = state.legalMoves(moveBuffer, moveTemp);
            if (moveCount <= 0) {
                return;
            }

            long parentHash = state.staticHashCode();
            int parentIndex = minHash.index(parentHash);

            for (int i = 0; i < moveCount; i++) {
                int legalMove = moveBuffer[i];

                int undo = Move.apply(legalMove, state);
                long childHash = state.staticHashCode();
                boolean materialMatches = (materialTally == state.tallyAllMaterial());
                Move.unApply(undo, state);

                if (materialMatches) {
                    add(minHash.index(childHash), parentIndex);
                }
            }
        }


        //--------------------------------------------------------------------
        private void add(int index, int parentIndex)
        {
            try {
                addChecked(index, parentIndex);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }


        private void addChecked(int index, int parentIndex) throws IOException {
            long offset = offsetIndex.offset(index);
            int byteLength = offsetIndex.length(index);

            file.seek(offset);

            int read = file.read(ioBuffer, 0, byteLength);
            checkState(read == byteLength, "Unexpected read: %s vs %s", read, byteLength);

            int insertIndex = 0;
            for (int j = 0; j < byteLength; j += Integer.BYTES) {
                int nextIndex = Ints.fromBytes(ioBuffer[j], ioBuffer[j + 1], ioBuffer[j + 2], ioBuffer[j + 3]);
                if (nextIndex == -1) {
                    break;
                }
                insertIndex++;
            }

            ioBuffer[0] = (byte) (parentIndex >> 24);
            ioBuffer[1] = (byte) (parentIndex >> 16);
            ioBuffer[2] = (byte) (parentIndex >> 8);
            ioBuffer[3] = (byte) (parentIndex);

            file.seek(offset + insertIndex * Integer.BYTES);

            file.write(ioBuffer, 0, Integer.BYTES);

//            int[] parents = precedents[ index ];
//            if (parents == null) {
//                precedents[index] = new int[]{ parentIndex };
//            }
//            else if (Arrs.indexOf(parents, parentIndex) == -1) {
//                int[] newParents = Arrays.copyOf(
//                        parents, parents.length + 1);
//                newParents[ parents.length ] = parentIndex;
//                precedents[index] = newParents;
//            }
//            else {
//                throw new IllegalStateException();
////                relationDuplicateCount++;
//            }

            relationCount++;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
//    private final int[][] precedents;
    private final RandomAccessFile file;
    private final EfficientOffsetIndex offsetIndex;

    private final byte[] buffer = new byte[Byte.MAX_VALUE * Integer.BYTES];
    private int nextIndex = 0;


    //-----------------------------------------------------------------------------------------------------------------
    private EfficientMaterialRetrograde(
            RandomAccessFile file,
            EfficientOffsetIndex offsetIndex)
    {
        this.file = file;
        this.offsetIndex = offsetIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void close() throws IOException {
        file.close();
    }


    //-----------------------------------------------------------------------------------------------------------------
    public int[] indexPrecedents(int ofIndex) {
        try {
            return indexPrecedentsChecked(ofIndex);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private int[] indexPrecedentsChecked(int ofIndex) throws IOException {
        int byteLength = offsetIndex.length(ofIndex);
        if (byteLength == 0) {
            return emptyPrecedents;
        }

        if (nextIndex != ofIndex) {
            long offset = offsetIndex.offset(ofIndex);
            file.seek(offset);
        }

        int read = file.read(buffer, 0, byteLength);
        checkState(read == byteLength);

        int[] parentIndexes = new int[byteLength];
        for (int i = 0, j = 0; j < byteLength; i++, j += Integer.BYTES) {
            parentIndexes[i] = Ints.fromBytes(buffer[j], buffer[j + 1], buffer[j + 2], buffer[j + 3]);
        }

        nextIndex = ofIndex;

        return parentIndexes;
    }
}
