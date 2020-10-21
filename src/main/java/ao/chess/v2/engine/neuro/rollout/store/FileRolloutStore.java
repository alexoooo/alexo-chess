package ao.chess.v2.engine.neuro.rollout.store;


import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;
import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionKey;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Longs;
import com.google.common.util.concurrent.Uninterruptibles;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class FileRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(FileRolloutStore.class);

    private static final int countOffset = 0;
    private static final int sumOffset = countOffset + Long.BYTES;
    private static final int sumSquareOffset = sumOffset + Long.BYTES;
    private static final int outcomeOffset = sumSquareOffset + Double.BYTES;
    private static final int moveCountOffset = outcomeOffset + Byte.BYTES;
    private static final int childrenOffset = moveCountOffset + Byte.BYTES;
    private static final int childSize = Long.BYTES;

    private static final int bufferMargin = 1024;
    private static final int bufferSize = bufferMargin * 64;
    private static final int bufferLimit = bufferSize - bufferMargin;
    private static final int retryAttempts = 64;


    //-----------------------------------------------------------------------------------------------------------------
    public static int sizeOf(int moveCount) {
        return childrenOffset + childSize * moveCount;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final RandomAccessFile handle;
    private final byte[] bufferArray = new byte[bufferSize];

    private final MVStore transpositionStore;
    private final MVMap<byte[], byte[]> transpositionMap;


    //-----------------------------------------------------------------------------------------------------------------
    public FileRolloutStore(Path file, Path transpositionFileOrNull) {
        try {
            Files.createDirectories(file.getParent());
            handle = new RandomAccessFile(file.toFile(), "rw");

            if (transpositionFileOrNull != null) {
                transpositionStore = new MVStore.Builder()
                        .fileName(transpositionFileOrNull.toString())
                        .open();
                transpositionMap = transpositionStore
                        .openMap("transposition");
            }
            else {
                transpositionStore = null;
                transpositionMap = null;
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public boolean initRootIfRequired(int moveCount) {
        long nextIndex = nextIndex();
        if (nextIndex != RolloutStore.rootIndex) {
            return false;
        }

        try {
            addNode(moveCount);
            return true;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public void incrementVisitCount(long nodeIndex) {
        try {
            handle.seek(nodeIndex + countOffset);
            long previousCount = handle.readLong();

            handle.seek(nodeIndex + countOffset);
            handle.writeLong(previousCount + 1);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public void addValue(long nodeIndex, double value) {
        if (value == 0) {
            return;
        }

        try {
            handle.seek(nodeIndex + sumOffset);
            double previousSum = handle.readDouble();
            double previousSquareSum = handle.readDouble();

            handle.seek(nodeIndex + sumOffset);
            handle.writeDouble(previousSum + value);
            handle.writeDouble(previousSquareSum + value * value);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public void setKnownOutcome(long nodeIndex, KnownOutcome knownOutcome) {
        try {
            handle.seek(nodeIndex + outcomeOffset);
            handle.writeByte(knownOutcome.ordinal());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public long expandChildIfMissing(long nodeIndex, int moveIndex, int childMoveCount) {
        long childOffset = childOffset(nodeIndex, moveIndex);

        try {
            handle.seek(childOffset);
            long existingChildIndex = handle.readLong();
            if (existingChildIndex != -1) {
                return -(existingChildIndex + 1);
            }

            long newIndex = addNode(childMoveCount);

            handle.seek(childOffset);
            handle.writeLong(newIndex);

            return newIndex;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private long addNode(int moveCount) throws IOException {
        long newIndex = nextIndex();
        handle.seek(newIndex);

        // count
        handle.writeLong(0);

        // sum
        handle.writeDouble(0.0);

        // square sum
        handle.writeDouble(0.0);

        // outcome
        handle.writeByte(0);

        handle.writeByte((byte) moveCount);

        // child indexes
        for (int i = 0; i < moveCount; i++) {
            handle.writeLong(-1);
        }

        return newIndex;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public long nextIndex() {
        try {
            return handle.length();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public long getVisitCount(long nodeIndex) {
        try {
            handle.seek(nodeIndex + countOffset);
            return handle.readLong();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public double getValueSum(long nodeIndex) {
        try {
            handle.seek(nodeIndex + sumOffset);
            return handle.readDouble();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public double getValueSquareSum(long nodeIndex) {
        try {
            handle.seek(nodeIndex + sumSquareOffset);
            return handle.readDouble();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public double getAverageValue(long nodeIndex, double defaultValue) {
        try {
            handle.seek(nodeIndex);
            long count = handle.readLong();
            double sum = handle.readDouble();

            return count == 0
                    ? defaultValue
                    : sum / count;
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public KnownOutcome getKnownOutcome(long nodeIndex) {
        try {
            handle.seek(nodeIndex + outcomeOffset);
            return KnownOutcome.values.get(handle.readByte());
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    @Override
    public long getChildIndex(long nodeIndex, int moveIndex) {
        long childOffset = childOffset(nodeIndex, moveIndex);

        try {
            handle.seek(childOffset);
            return handle.readLong();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private long childOffset(long nodeIndex, int moveIndex) {
        return nodeIndex + childrenOffset + moveIndex * childSize;
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public TranspositionInfo getTranspositionOrNull(long hashHigh, long hashLow) {
        byte[] key = toKey(hashHigh, hashLow);
        byte[] value = transpositionMap.get(key);
        if (value == null) {
            return null;
        }
        return fromValue(value);
    }


    @Override
    public void setTransposition(long hashHigh, long hashLow, long nodeIndex, double valueSum, long visitCount) {
        byte[] key = toKey(hashHigh, hashLow);
        byte[] value = toValue(nodeIndex, valueSum, visitCount);
        transpositionMap.put(key, value);
    }


    public void storeAllTranspositions(Iterator<Map.Entry<TranspositionKey, TranspositionInfo>> transpositions) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        int count = 0;
        while (transpositions.hasNext()) {
            Map.Entry<TranspositionKey, TranspositionInfo> entry = transpositions.next();

            setTransposition(
                    entry.getKey().hashHigh(),
                    entry.getKey().hashLow(),
                    entry.getValue().nodeIndex(),
                    entry.getValue().valueSum(),
                    entry.getValue().visitCount());

            count++;
        }

        logger.info("Stored transpositions: {} - {}", count, stopwatch);
    }


    private byte[] toKey(long hashHigh, long hashLow) {
        return new byte[] {
                (byte) hashHigh,
                (byte) (hashHigh >> 8),
                (byte) (hashHigh >> 16),
                (byte) (hashHigh >> 24),
                (byte) (hashHigh >> 32),
                (byte) (hashHigh >> 40),
                (byte) (hashHigh >> 48),
                (byte) (hashHigh >> 56),
                (byte) hashLow,
                (byte) (hashLow >> 8),
                (byte) (hashLow >> 16),
                (byte) (hashLow >> 24),
                (byte) (hashLow >> 32),
                (byte) (hashLow >> 40),
                (byte) (hashLow >> 48),
                (byte) (hashLow >> 56)};
    }


    private TranspositionInfo fromValue(byte[] value) {
        long nodeIndex = Longs.fromBytes(
                value[7], value[6], value[5], value[4], value[3], value[2], value[1], value[0]);

        long valueSumLong = Longs.fromBytes(
                value[15], value[14], value[13], value[12], value[11], value[10], value[9], value[8]);
        double valueSum = Double.longBitsToDouble(valueSumLong);

        long visitCount = Longs.fromBytes(
                value[23], value[22], value[21], value[20], value[19], value[18], value[17], value[16]);

        return new TranspositionInfo(nodeIndex, valueSum, visitCount);
    }


    private byte[] toValue(long nodeIndex, double valueSum, long visitCount) {
        long valueSumLong = Double.doubleToRawLongBits(valueSum);
        return new byte[] {
                (byte) nodeIndex,
                (byte) (nodeIndex >> 8),
                (byte) (nodeIndex >> 16),
                (byte) (nodeIndex >> 24),
                (byte) (nodeIndex >> 32),
                (byte) (nodeIndex >> 40),
                (byte) (nodeIndex >> 48),
                (byte) (nodeIndex >> 56),
                (byte) valueSumLong,
                (byte) (valueSumLong >> 8),
                (byte) (valueSumLong >> 16),
                (byte) (valueSumLong >> 24),
                (byte) (valueSumLong >> 32),
                (byte) (valueSumLong >> 40),
                (byte) (valueSumLong >> 48),
                (byte) (valueSumLong >> 56),
                (byte) visitCount,
                (byte) (visitCount >> 8),
                (byte) (visitCount >> 16),
                (byte) (visitCount >> 24),
                (byte) (visitCount >> 32),
                (byte) (visitCount >> 40),
                (byte) (visitCount >> 48),
                (byte) (visitCount >> 56)};
    }


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutStoreNode load(long nodeIndex) {
        try {
            handle.seek(nodeIndex);

            long visitCount = handle.readLong();
            double valueSum = handle.readDouble();
            double valueSquareSum = handle.readDouble();
            KnownOutcome knownOutcome = KnownOutcome.values.get(handle.readByte());
            int moveCount = Byte.toUnsignedInt(handle.readByte());

            long[] childIndexes = new long[moveCount];
            for (int i = 0; i < moveCount; i++) {
                childIndexes[i] = handle.readLong();
            }

            return new RolloutStoreNode(
                    nodeIndex, visitCount, valueSum, valueSquareSum, knownOutcome, childIndexes);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public void storeAll(Iterator<RolloutStoreNode> nodes) {
        try {
            storeAllChecked(nodes);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private void storeAllChecked(Iterator<RolloutStoreNode> nodes) throws IOException {
        Stopwatch stopwatch = Stopwatch.createStarted();
        int writeCount = 0;
        int seekCount = 0;

        ByteBuffer buffer = ByteBuffer.wrap(bufferArray);
        long previousPosition = handle.getFilePointer();
        int bufferSize = 0;

        while (nodes.hasNext()) {
            RolloutStoreNode node = nodes.next();

            if (previousPosition != node.index()) {
                if (bufferSize != 0) {
                    handle.write(bufferArray, 0, bufferSize);
                    buffer.clear();
                    bufferSize = 0;
                    writeCount++;
                }

                handle.seek(node.index());
                previousPosition = node.index();
                seekCount++;
            }

            buffer.putLong(node.visitCount());
            buffer.putDouble(node.valueSum());
            buffer.putDouble(node.valueSquareSum());
            buffer.put((byte) node.knownOutcome().ordinal());
            buffer.put((byte) node.moveCount());

            for (int i = 0; i < node.moveCount(); i++) {
                buffer.putLong(node.childIndex(i));
            }

            int size = sizeOf(node.moveCount());
            bufferSize += size;

            if (bufferSize >= bufferLimit) {
                // https://serverfault.com/questions/306751/ntfs-the-requested-operation-could-not-be-completed-due-to-a-file-system-limit
                handle.write(bufferArray, 0, bufferSize);
                buffer.clear();
                bufferSize = 0;
                writeCount++;
            }

            previousPosition += size;
        }

        if (bufferSize != 0) {
            handle.write(bufferArray, 0, bufferSize);
            writeCount++;
        }

        logger.info("Storing | seek {} | write {} | took: {}", seekCount, writeCount, stopwatch);
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() throws Exception {
        handle.close();
        if (transpositionStore != null) {
            transpositionStore.close();
        }
    }


    @Override
    public long flush() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int tryCount = 0; tryCount < retryAttempts; tryCount++) {
            try {
                handle.getFD().sync();
            }
            catch (SyncFailedException e) {
                logger.warn("Sync failed, try number {} - {}", tryCount + 1, e.getMessage());
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        logger.info("Sync to disk took: {}", stopwatch);

        transpositionStore.commit();
        logger.info("Transposition commit");

        return 0;
    }
}
