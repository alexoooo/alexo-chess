package ao.chess.v2.engine.neuro.rollout.store;


import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class FileRolloutStore implements RolloutStore {
    //-----------------------------------------------------------------------------------------------------------------
    private static final int countOffset = 0;
    private static final int sumOffset = countOffset + Long.BYTES;
    private static final int outcomeOffset = sumOffset + Double.BYTES;
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

            handle.seek(nodeIndex + sumOffset);
            handle.writeDouble(previousSum + value);
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
            KnownOutcome knownOutcome = KnownOutcome.values.get(handle.readByte());
            int moveCount = Byte.toUnsignedInt(handle.readByte());

            long[] childIndexes = new long[moveCount];
            for (int i = 0; i < moveCount; i++) {
                childIndexes[i] = handle.readLong();
            }

            return new RolloutStoreNode(
                    nodeIndex, visitCount, valueSum, knownOutcome, childIndexes);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    public void store(RolloutStoreNode node) {
        try {
            handle.seek(node.index());

            handle.writeLong(node.visitCount());
            handle.writeDouble(node.valueSum());
            handle.writeByte(node.knownOutcome().ordinal());
            handle.writeByte(node.moveCount());

            for (int i = 0; i < node.moveCount(); i++) {
                handle.writeLong(node.childIndex(i));
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
}
