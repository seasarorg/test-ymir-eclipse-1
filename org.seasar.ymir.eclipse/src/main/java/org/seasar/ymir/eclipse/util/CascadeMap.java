package org.seasar.ymir.eclipse.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class CascadeMap<K, V> implements Map<K, V> {
    private Map<K, V>[] maps;

    @SuppressWarnings("unchecked")
    public CascadeMap(Map<K, V> map, Map<K, V>... maps) {
        this.maps = new Map[maps.length + 1];
        this.maps[0] = map;
        System.arraycopy(maps, 0, this.maps, 1, maps.length);
    }

    public void clear() {
        maps[0].clear();
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
        return maps[0].put(key, value);
    }

    public void putAll(Map<? extends K, ? extends V> t) {
        maps[0].putAll(t);
    }

    public V remove(Object key) {
        return maps[0].remove(key);
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
