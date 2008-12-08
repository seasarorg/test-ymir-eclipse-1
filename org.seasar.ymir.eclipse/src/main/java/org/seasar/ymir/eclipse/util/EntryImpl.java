package org.seasar.ymir.eclipse.util;

import java.util.Map;

class EntryImpl<K, V> implements Map.Entry<K, V> {
    private K key;

    private V value;

    protected EntryImpl(K key, V value) {
        this.key = key;
        this.value = value;
    }

    public K getKey() {
        return key;
    }

    public V getValue() {
        return value;
    }

    public V setValue(V value) {
        V old = this.value;
        this.value = value;
        return old;
    }
}
