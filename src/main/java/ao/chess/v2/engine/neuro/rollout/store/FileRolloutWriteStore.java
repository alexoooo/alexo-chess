package ao.chess.v2.engine.neuro.rollout.store;


import com.google.common.base.Preconditions;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.util.concurrent.Uninterruptibles;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.io.SyncFailedException;
import java.io.UncheckedIOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.*;


public class FileRolloutWriteStore {
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(FileRolloutWriteStore.class);


    //-----------------------------------------------------------------------------------------------------------------
    private static void flush(RandomAccessFile handle, String tag) {
        Stopwatch stopwatch = Stopwatch.createStarted();
        for (int tryCount = 0; tryCount < FileRolloutStore.retryAttempts; tryCount++) {
            try {
                handle.getFD().sync();
            }
            catch (SyncFailedException e) {
                logger.warn("Sync {} failed, try number {} - {}", tag, tryCount + 1, e.getMessage());
                Uninterruptibles.sleepUninterruptibly(1, TimeUnit.SECONDS);
            }
            catch (IOException e) {
                throw new UncheckedIOException(e);
            }
        }
        logger.info("Sync {} to disk took: {}", tag, stopwatch);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final Path file;
    private final int fileHandleCount;


    //-----------------------------------------------------------------------------------------------------------------
    public FileRolloutWriteStore(Path file, int fileHandleCount) {
        this.file = file;
        this.fileHandleCount = fileHandleCount;

        try {
            Files.createDirectories(file.getParent());
            if (! Files.exists(file)) {
                Files.createFile(file);
            }
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public synchronized void initRoot(int moveCount) {
        try (RandomAccessFile handle = new RandomAccessFile(file.toFile(), "rw")) {
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

            flush(handle, "root");
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public synchronized void storeAll(List<RolloutStoreNode> sequencedNodes) {
        logger.info("Storing nodes: {}", sequencedNodes.size());

        if (sequencedNodes.isEmpty()) {
            return;
        }

        long length = fileSize();

        List<RolloutStoreNode> overwriteSequencedNodes;
        long lastIndex = sequencedNodes.get(sequencedNodes.size() - 1).index();
        if (lastIndex < length) {
            overwriteSequencedNodes = sequencedNodes;
        }
        else {
            int encodedAppendIndex = Collections.binarySearch(
                    sequencedNodes, RolloutStoreNode.emptyOfIndex(length - 1), RolloutStoreNode.byIndex);
            Preconditions.checkState(encodedAppendIndex < 0);
            int appendIndex = -(encodedAppendIndex + 1);
            overwriteSequencedNodes = sequencedNodes.subList(0, appendIndex);

            List<RolloutStoreNode> appendSequenceNodes = sequencedNodes.subList(appendIndex, sequencedNodes.size());
            Preconditions.checkState(length == appendSequenceNodes.get(0).index());

            storeAllWithHandle(appendSequenceNodes, "append");
        }

        List<Iterable<RolloutStoreNode>> overwriteSequencedNodePartitions = partition(overwriteSequencedNodes);

        ExecutorService executor = Executors.newFixedThreadPool(fileHandleCount);
        List<Future<?>> results = new ArrayList<>();

        for (int i = 0; i < overwriteSequencedNodePartitions.size(); i++) {
            Iterable<RolloutStoreNode> overwriteSequencedNodePartition = overwriteSequencedNodePartitions.get(i);

            int number = i + 1;
            Future<?> result = executor.submit(() ->
                storeAllWithHandle(overwriteSequencedNodePartition, "replace-" + number));
            results.add(result);
        }

        for (Future<?> result : results) {
            try {
                Uninterruptibles.getUninterruptibly(result);
            }
            catch (ExecutionException e) {
                throw new RuntimeException(e);
            }
        }

        executor.shutdown();
        try {
            boolean terminated = executor.awaitTermination(1, TimeUnit.MINUTES);
            Preconditions.checkState(terminated);
        }
        catch (InterruptedException e) {
            throw new RuntimeException(e);
        }
    }


    private List<Iterable<RolloutStoreNode>> partition(List<RolloutStoreNode> overwriteSequencedNodes) {
        if (overwriteSequencedNodes.isEmpty()) {
            return List.of();
        }

        int size = Math.max(1, overwriteSequencedNodes.size() / fileHandleCount);
        List<List<RolloutStoreNode>> overwriteSequencedNodePartitions = Lists.partition(overwriteSequencedNodes, size);

        List<Iterable<RolloutStoreNode>> adjustedOverwriteSequencedNodePartitions = new ArrayList<>(
                overwriteSequencedNodePartitions);
        if (adjustedOverwriteSequencedNodePartitions.size() > fileHandleCount) {
            Preconditions.checkState(adjustedOverwriteSequencedNodePartitions.size() == fileHandleCount + 1);
            adjustedOverwriteSequencedNodePartitions.set(fileHandleCount - 1,
                    Iterables.concat(
                            adjustedOverwriteSequencedNodePartitions.get(fileHandleCount - 1),
                            adjustedOverwriteSequencedNodePartitions.get(fileHandleCount)));
            adjustedOverwriteSequencedNodePartitions.remove(fileHandleCount);
        }

        return adjustedOverwriteSequencedNodePartitions;
    }


    private long fileSize() {
        try {
            return Files.size(file);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private void storeAllWithHandle(Iterable<RolloutStoreNode> sequencedNodes, String tag) {
        try (RandomAccessFile handle = new RandomAccessFile(file.toFile(), "rw")) {
            storeAllChecked(sequencedNodes, handle, tag);
            flush(handle, tag);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    private void storeAllChecked(
            Iterable<RolloutStoreNode> sequencedNodes,
            RandomAccessFile handle,
            String tag) throws IOException
    {
        logger.info("Storing {}", tag);

        Stopwatch stopwatch = Stopwatch.createStarted();
        int writeCount = 0;
        int seekCount = 0;

        byte[] bufferArray = new byte[FileRolloutStore.bufferSize];
        ByteBuffer buffer = ByteBuffer.wrap(bufferArray);

        long previousPosition = handle.getFilePointer();
        int bufferSize = 0;

        int nodeCount = 0;
        for (RolloutStoreNode node : sequencedNodes) {
            nodeCount++;

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

            int size = FileRolloutStore.sizeOf(node.moveCount());
            bufferSize += size;

            if (bufferSize >= FileRolloutStore.bufferLimit) {
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

        logger.info("Stored {} | seek {} | write {} | nodes {} | took: {}",
                tag, seekCount, writeCount, nodeCount, stopwatch);
    }
}
