package ao.chess.v2.engine.endgame.v2;


import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;


public class EfficientOutcomeStore {
    //-----------------------------------------------------------------------------------------------------------------
//    private final List<Piece> material;
    private final Path store;

    private byte[] outcomes;


    //-----------------------------------------------------------------------------------------------------------------
    public EfficientOutcomeStore(/*List<Piece> material,*/ Path store) {
//        this.material = material;
        this.store = store;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public boolean isStored() {
        return outcomes != null ||
                Files.exists(store);
    }


    public void startCompute(int count) {
        outcomes = new byte[count];
    }


    public void set(int index, byte value) {
        outcomes[index] = value;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private void loadIfRequired() {
        if (outcomes != null) {
            return;
        }

        try {
            outcomes = Files.readAllBytes(store);
        }
        catch (IOException e) {
            e.printStackTrace();
        }
    }


    public void save() {
        try {
            Files.write(store, outcomes);
        }
        catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }


    //--------------------------------------------------------------------
    public int whiteWinCount() {
        int count = 0;
        for (byte outcome : outcomes) {
            if (outcome > 0) {
                count++;
            }
        }
        return count;
    }


    public int blackWinCount() {
        int count = 0;
        for (byte outcome : outcomes) {
            if (outcome < 0) {
                count++;
            }
        }
        return count;
    }


    //-----------------------------------------------------------------------------------------------------------------
    public byte get(int index) {
        loadIfRequired();
        return outcomes[index];
    }
}
