package ao.chess.v2.engine.endgame.v2.index;


import it.unimi.dsi.fastutil.longs.*;

import java.util.ArrayList;
import java.util.List;


public class LongBigSet {
    private static final int maxSize = 800_000_000;


    private final List<LongSet> underlying = new ArrayList<>();


    public LongBigSet() {
        underlying.add(new LongOpenHashSet());
    }


    public void add(long value) {
        for (LongSet set : underlying) {
            if (set.contains(value)) {
                return;
            }
        }

        LongSet last = underlying.get(underlying.size() - 1);

        LongSet addend;
        if (last.size() >= maxSize) {
            addend = new LongOpenHashSet();
            underlying.add(addend);
        }
        else {
            addend = last;
        }

        addend.add(value);
    }


    public long size() {
        long total = 0;
        for (LongSet set : underlying) {
            total += set.size();
        }
        return total;
    }


    public LongIterator iterator() {
        LongIterator[] iterators = new LongIterator[underlying.size()];
        for (int i = 0; i < underlying.size(); i++) {
            iterators[i] = underlying.get(i).iterator();
        }
        return LongIterators.concat(iterators);
    }
}
