package org.seasar.ymir.eclipse.maven.util;

import java.util.Iterator;

public class CompositeIterator<T> implements Iterator<T> {
    private Iterator<? extends T>[] iterators;

    private int idx;

    public CompositeIterator(Iterator<? extends T>... iterators) {
        this.iterators = iterators;
    }

    public boolean hasNext() {
        return idx < iterators.length && iterators[idx].hasNext();
    }

    public T next() {
        T next = iterators[idx].next();
        if (!iterators[idx].hasNext()) {
            idx++;
        }
        return next;
    }

    public void remove() {
        throw new UnsupportedOperationException();
    }
}
