package ao.chess.v2.engine.endgame.v2.index;


import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.google.common.base.Preconditions.checkState;


public class EfficientLengthIndex {
    //-----------------------------------------------------------------------------------------------------------------
    public static EfficientLengthIndex load(Path path) throws IOException {
        byte[] lengths = Files.readAllBytes(path);
        return new EfficientLengthIndex(lengths);
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final byte[] lengths;


    //-----------------------------------------------------------------------------------------------------------------
    public EfficientLengthIndex(int size) {
        lengths = new byte[size];
    }


    private EfficientLengthIndex(byte[] lengths) {
        this.lengths = lengths;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void store(Path path) throws IOException {
        Files.write(path, lengths);
    }


    //-----------------------------------------------------------------------------------------------------------------
    public void add(int index, byte amount) {
        byte value = lengths[index];
        int added = value + amount;
        checkState(added == (byte) added);

        lengths[index] = (byte) added;
    }


    public void increment(int index) {
        add(index, (byte) 1);
    }


    public byte get(int index) {
        return lengths[index];
    }


    public int size() {
        return lengths.length;
    }
}
