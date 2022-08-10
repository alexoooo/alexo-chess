package ao.chess.v2.engine.neuro.rollout.store;


import com.google.common.base.Preconditions;
import com.google.common.util.concurrent.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.function.Function;


public class FileRolloutReadStore {
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(FileRolloutReadStore.class);


    //-----------------------------------------------------------------------------------------------------------------
    private final Path file;
    private final int fileHandleCount;
    private final BlockingQueue<HandleContext> handles;


    //-----------------------------------------------------------------------------------------------------------------
    interface RandomAccessFileUser<T> {
        T use(RandomAccessFile handle) throws IOException;
    }

    private class HandleContext {
        RandomAccessFile handle;

        public void openIfRequired() {
            if (handle != null) {
                return;
            }

            try {
                handle = new RandomAccessFile(file.toFile(), "r");
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public void closeIfRequired() {
            if (handle == null) {
                return;
            }

            try {
                handle.close();
                handle = null;
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }

        public <T> T useHandle(RandomAccessFileUser<T> user) {
            try {
                return user.use(handle);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public FileRolloutReadStore(Path file, int fileHandleCount) {
        this.file = file;
        this.fileHandleCount = fileHandleCount;
        handles = new ArrayBlockingQueue<>(fileHandleCount);
        for (int i = 0; i < fileHandleCount; i++) {
            handles.add(new HandleContext());
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void openIfRequired() {
        int size = handles.size();
        Preconditions.checkState(size == fileHandleCount);
        for (HandleContext context : handles) {
            context.openIfRequired();
        }
    }


    public void closeIfRequired() {
        int size = handles.size();
        Preconditions.checkState(size == fileHandleCount);
        for (HandleContext context : handles) {
            context.closeIfRequired();
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    private <T> T useContext(Function<HandleContext, T> user) {
        HandleContext handle = Uninterruptibles.takeUninterruptibly(handles);
        try {
            return user.apply(handle);
        }
        finally {
            handles.add(handle);
        }
    }


    private <T> T useHandle(RandomAccessFileUser<T> user) {
        return useContext(context -> context.useHandle(user));
    }


    //-----------------------------------------------------------------------------------------------------------------
    public long nextIndex() {
        return useHandle(
                RandomAccessFile::length);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public RolloutStoreNode load(long nodeIndex) {
        return useHandle(handle -> {
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
        });
    }
}
