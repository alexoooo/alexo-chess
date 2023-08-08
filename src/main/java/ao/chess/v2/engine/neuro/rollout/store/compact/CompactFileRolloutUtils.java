package ao.chess.v2.engine.neuro.rollout.store.compact;


import com.google.common.base.Preconditions;
import com.google.common.primitives.Longs;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;


public enum CompactFileRolloutUtils {
    ;

    //-----------------------------------------------------------------------------------------------------------------
    public static final int longBytes = 5;
    public static final long longMissing = -1;

    private static final long longMissingSentinel = Longs.fromBytes(
            (byte) 0, (byte) 0, (byte) 0, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF, (byte) 0xFF);


    //-----------------------------------------------------------------------------------------------------------------
    public static long readLong40(RandomAccessFile file) throws IOException {
        long byte1 = file.read();
        long byte2 = file.read();
        long byte3 = file.read();
        long byte4 = file.read();
        long byte5 = file.read();

        long value = (byte1 << 32) + (byte2 << 24) + (byte3 << 16) + (byte4 << 8) + byte5;
        return value == longMissingSentinel
                ? longMissing
                : value;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static void writeLong40(RandomAccessFile file, long value) throws IOException {
        Preconditions.checkArgument(value != longMissingSentinel);
        if (value == longMissing) {
            writeLong40Raw(file, longMissingSentinel);
        }
        else {
            writeLong40Raw(file, value);
        }
    }


    private static void writeLong40Raw(RandomAccessFile file, long value) throws IOException {
        Preconditions.checkArgument((((int) (value >>> 56)) & 0xFF) == 0);
        Preconditions.checkArgument((((int) (value >>> 48)) & 0xFF) == 0);
        Preconditions.checkArgument((((int) (value >>> 40)) & 0xFF) == 0);

        file.write(((int) (value >>> 32)) & 0xFF);
        file.write(((int) (value >>> 24)) & 0xFF);
        file.write(((int) (value >>> 16)) & 0xFF);
        file.write(((int) (value >>>  8)) & 0xFF);
        file.write(((int) value) & 0xFF);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public static void putLong40(ByteBuffer buffer, long value) {
        Preconditions.checkArgument(value != longMissingSentinel);
        if (value == longMissing) {
            putLong40Raw(buffer, longMissingSentinel);
        }
        else {
            putLong40Raw(buffer, value);
        }
    }


    private static void putLong40Raw(ByteBuffer buffer, long value) {
        Preconditions.checkArgument((((int) (value >>> 56)) & 0xFF) == 0);
        Preconditions.checkArgument((((int) (value >>> 48)) & 0xFF) == 0);
        Preconditions.checkArgument((((int) (value >>> 40)) & 0xFF) == 0);

        buffer.put((byte) (((int) (value >>> 32)) & 0xFF));
        buffer.put((byte) (((int) (value >>> 24)) & 0xFF));
        buffer.put((byte) (((int) (value >>> 16)) & 0xFF));
        buffer.put((byte) (((int) (value >>>  8)) & 0xFF));
        buffer.put((byte) (((int) value) & 0xFF));
    }
}
