package ao.chess.v2.engine.neuro.rollout.store;

import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionInfo;
import ao.chess.v2.engine.neuro.rollout.store.transposition.TranspositionKey;
import com.google.common.base.Stopwatch;
import com.google.common.primitives.Longs;
import org.h2.mvstore.MVMap;
import org.h2.mvstore.MVStore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.file.Path;
import java.util.Map;
import java.util.function.Consumer;


public class FileTranspositionStore
{
    //-----------------------------------------------------------------------------------------------------------------
    private static final Logger logger = LoggerFactory.getLogger(FileTranspositionStore.class);


    //-----------------------------------------------------------------------------------------------------------------
    public static byte[] toKey(long hashHigh, long hashLow) {
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


    public static TranspositionInfo fromValue(byte[] value) {
        long nodeIndex = Longs.fromBytes(
                value[7], value[6], value[5], value[4], value[3], value[2], value[1], value[0]);

        long valueSumLong = Longs.fromBytes(
                value[15], value[14], value[13], value[12], value[11], value[10], value[9], value[8]);
        double valueSum = Double.longBitsToDouble(valueSumLong);

        long visitCount = Longs.fromBytes(
                value[23], value[22], value[21], value[20], value[19], value[18], value[17], value[16]);

        return new TranspositionInfo(nodeIndex, valueSum, visitCount);
    }


    public static byte[] toValue(long nodeIndex, double valueSum, long visitCount) {
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
    private final MVStore transpositionStore;
    private final MVMap<byte[], byte[]> transpositionMap;


    //-----------------------------------------------------------------------------------------------------------------
    public FileTranspositionStore(Path transpositionFileOrNull) {
        if (transpositionFileOrNull != null) {
            transpositionStore = new MVStore.Builder()
                    .fileName(transpositionFileOrNull.toString())
//                        .cacheSize(128)
                    .cacheSize(1024)
                    .autoCommitBufferSize(32 * 1024)
                    .open();
            transpositionMap = transpositionStore
                    .openMap("transposition");
        }
        else {
            transpositionStore = null;
            transpositionMap = null;
        }
    }


    //-----------------------------------------------------------------------------------------------------------------
    public TranspositionInfo getTranspositionOrNull(long hashHigh, long hashLow) {
        byte[] key = toKey(hashHigh, hashLow);
        byte[] value = transpositionMap.get(key);
        if (value == null) {
            return null;
        }
        return fromValue(value);
    }


    public void setTransposition(long hashHigh, long hashLow, long nodeIndex, double valueSum, long visitCount) {
        byte[] key = toKey(hashHigh, hashLow);
        byte[] value = toValue(nodeIndex, valueSum, visitCount);
        transpositionMap.put(key, value);
    }


    public void storeAllTranspositions(Consumer<Consumer<Map.Entry<TranspositionKey, TranspositionInfo>>> transpositions) {
        Stopwatch stopwatch = Stopwatch.createStarted();

        int[] count = {0};

        transpositions.accept(entry -> {
            setTransposition(
                    entry.getKey().hashHigh(),
                    entry.getKey().hashLow(),
                    entry.getValue().nodeIndex(),
                    entry.getValue().valueSum(),
                    entry.getValue().visitCount());
            count[0]++;
        });

        logger.info("Stored transpositions: {} - {}", count[0], stopwatch);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void flush() {
        transpositionStore.commit();
    }


    public void close() {
        transpositionStore.close();
    }
}
