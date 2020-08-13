package ao.chess.v2.engine.endgame.v2.index;


public class EfficientOffsetIndex {
    //-----------------------------------------------------------------------------------------------------------------
    public static EfficientOffsetIndex build(
            EfficientLengthIndex lengthIndex,
            int factor)
    {
        int size = lengthIndex.size();
        EfficientOffsetIndex instance = new EfficientOffsetIndex(size);

        long next = 0;
        for (int i = 0; i < size; i++) {
            instance.set(i, next);
            next += lengthIndex.get(i) * factor;
        }
        instance.set(size, next);

        return instance;
    }


    //-----------------------------------------------------------------------------------------------------------------
    private final long[] offsets;


    //-----------------------------------------------------------------------------------------------------------------
    private EfficientOffsetIndex(int size) {
        offsets = new long[size + 1];
    }


    //-----------------------------------------------------------------------------------------------------------------
//    public void store(Path path) {
//        try (DataOutputStream out = new DataOutputStream(
//                new BufferedOutputStream(
//                        Files.newOutputStream(path)))
//        ) {
//            for (int offset :)
//            out.writeInt();
//        }
//        catch (IOException e) {
//            throw new UncheckedIOException(e);
//        }
//
////        Files.write(offsets, path);
//    }


    //-----------------------------------------------------------------------------------------------------------------
    private void set(int index, long offset) {
        offsets[index] = offset;
    }


    public long offset(int index) {
        return offsets[index];
    }


    public int length(int index) {
        return (int) (offset(index + 1) - offset(index));
    }


    public int size() {
        return offsets.length - 1;
    }


    public long totalLength() {
        return offsets[size()];
    }
}
