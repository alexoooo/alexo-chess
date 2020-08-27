package ao.chess.v2.engine.neuro.rollout.store;


import com.google.common.base.Stopwatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Iterator;


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


    //-----------------------------------------------------------------------------------------------------------------
    public static int sizeOf(int moveCount) {
        return childrenOffset + childSize * moveCount;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final RandomAccessFile handle;


    //-----------------------------------------------------------------------------------------------------------------
    public FileRolloutStore(Path file) {
        try {
            Files.createDirectories(file.getParent());
            handle = new RandomAccessFile(file.toFile(), "rw");
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
        byte[] bufferArray = new byte[1024];
        ByteBuffer buffer = ByteBuffer.wrap(bufferArray);

        try {
            long previousPosition = handle.getFilePointer();

            while (nodes.hasNext()) {
                RolloutStoreNode node = nodes.next();

                if (previousPosition != node.index()) {
                    handle.seek(node.index());
                    previousPosition = node.index();
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
                handle.write(bufferArray, 0, size);
                buffer.clear();

                previousPosition += size;
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    @Override
    public void close() throws Exception {
        handle.close();
    }


    @Override
    public long flush() {
        Stopwatch stopwatch = Stopwatch.createStarted();
        try {
            handle.getFD().sync();
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
        logger.info("Sync to disk took: {}", stopwatch);
        return 0;
    }
}
