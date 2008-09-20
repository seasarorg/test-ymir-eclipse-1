package org.seasar.ymir.eclipse.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CascadeMap<K, V> implements Map<K, V> {
    private Map<K, V>[] maps;

    public CascadeMap(Map<K, V>... maps) {
        this.maps = maps;
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        for (Map<K, V> map : maps) {
            if (map != null) {
                if (map.containsKey(key)) {
                    return true;
                }
            }
        }
        return false;
    }

    public boolean containsValue(Object value) {
        for (Map<K, V> map : maps) {
            if (map != null) {
                if (map.containsValue(value)) {
                    return true;
                }
            }
        }
        return false;
    }

    public Set<Map.Entry<K, V>> entrySet() {
        Set<Map.Entry<K, V>> set = new HashSet<Entry<K, V>>();
        Set<K> keySet = new HashSet<K>();
        for (Map<K, V> map : maps) {
            if (map != null) {
                for (Entry<K, V> e : map.entrySet()) {
                    if (!keySet.contains(e.getKey())) {
                        keySet.add(e.getKey());
                        set.add(e);
                    }
                }
            }
        }
        return set;
    }

    public V get(Object key) {
        return _get(key);
    }

    private V _get(Object key) {
        for (Map<K, V> map : maps) {
            if (map != null) {
                if (map.containsKey(key)) {
                    return map.get(key);
                }
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return _keySet().isEmpty();
    }

    public Set<K> keySet() {
        return _keySet();
    }

    private Set<K> _keySet() {
        Set<K> set = new HashSet<K>();
        for (Map<K, V> map : maps) {
            if (map != null) {
                set.addAll(map.keySet());
            }
        }
        return set;
    }

    public V put(K key, V value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        throw new UnsupportedOperationException();
    }

    public V remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return _keySet().size();
    }

    public Collection<V> values() {
        List<V> list = new ArrayList<V>();
        for (K key : _keySet()) {
            list.add(_get(key));
        }
        return list;
    }
}
