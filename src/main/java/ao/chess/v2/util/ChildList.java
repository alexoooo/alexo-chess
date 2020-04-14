package ao.chess.v2.util;


import com.google.common.collect.ImmutableList;

import java.util.Iterator;

// based on CopyOnWriteArrayList
public class ChildList<E>
        implements Iterable<E>
{
    @SuppressWarnings("unchecked")
    static <E> E elementAt(Object[] a, int index) {
        return a == null ? null : (E) a[index];
    }


    private final transient Object lock = new Object();
    private transient volatile Object[] array;


    public ChildList(int length)
    {
        array = new Object[length];
    }


    public Object[] getArray() {
        return array;
    }


    final void setArray(Object[] a) {
        array = a;
    }


    public E get(int index) {
        return elementAt(getArray(), index);
    }


    public Object[] close() {
        synchronized (lock) {
            Object[] es = getArray();
            if (es == null) {
                return null;
            }

            setArray(null);

            return es;
        }
    }


//    public boolean clear(int index) {
//        synchronized (lock) {
//            Object[] es = getArray();
//            E oldValue = elementAt(es, index);
//
//            if (oldValue == null) {
//                return false;
//            }
//
//            es = es.clone();
//            es[index] = null;
//
//            setArray(es);
//
//            return true;
//        }
//    }


    public boolean setIfAbsent(int index, E element) {
        synchronized (lock) {
            Object[] es = getArray();
            if (es == null) {
                return false;
            }

            E oldValue = elementAt(es, index);

            if (oldValue != null) {
                return false;
            }

            es = es.clone();
            es[index] = element;

            setArray(es);

            return true;
        }
    }


    @Override
    public Iterator<E> iterator() {
        Object[] es = getArray();
        if (es == null) {
            return ImmutableList.<E>of().iterator();
        }

        return new Iterator<>() {
            private int next = 0;

            @Override
            public boolean hasNext() {
                return next < es.length;
            }

            @Override
            public E next() {
                return (E) es[next++];
            }
        };
    }
}
