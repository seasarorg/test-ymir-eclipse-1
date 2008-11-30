package org.seasar.ymir.eclipse.util;

import java.util.Collection;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.seasar.kvasir.util.collection.MapProperties;

public class MapAdapter implements Map<String, String> {
    private MapProperties prop;

    public MapAdapter(MapProperties prop) {
        this.prop = prop;
    }

    public MapProperties getMapProperties() {
        return prop;
    }

    public void clear() {
        prop.clearProperties();
    }

    public boolean containsKey(Object key) {
        if (!(key instanceof String)) {
            return false;
        }
        return prop.containsPropertyName((String) key);
    }

    public boolean containsValue(Object value) {
        if (value == null) {
            return false;
        }
        for (@SuppressWarnings("unchecked") //$NON-NLS-1$
        Enumeration enm = prop.propertyNames(); enm.hasMoreElements();) {
            String name = (String) enm.nextElement();
            if (value.equals(prop.getProperty(name))) {
                return true;
            }
        }
        return false;
    }

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public Set entrySet() {
        Set<Map.Entry<String, String>> set = new HashSet<Map.Entry<String, String>>();
        for (Enumeration enm = prop.propertyNames(); enm.hasMoreElements();) {
            String name = (String) enm.nextElement();
            set.add(new EntryImpl<String, String>(name, prop.getProperty(name)));
        }
        return set;
    }

    public String get(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        return prop.getProperty((String) key);
    }

    public boolean isEmpty() {
        return prop.size() == 0;
    }

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    public Set keySet() {
        Set<String> set = new HashSet<String>();
        for (Enumeration enm = prop.propertyNames(); enm.hasMoreElements();) {
            set.add((String) enm.nextElement());
        }
        return set;
    }

    public String put(String key, String value) {
        String old = prop.getProperty(key);
        prop.setProperty(key, value);
        return old;
    }

    public void putAll(Map<? extends String, ? extends String> m) {
        for (Iterator<?> itr = m.entrySet().iterator(); itr.hasNext();) {
            @SuppressWarnings("unchecked") //$NON-NLS-1$
            Map.Entry<? extends String, ? extends String> entry = (Entry<? extends String, ? extends String>) itr
                    .next();
            prop.setProperty(entry.getKey(), entry.getValue());
        }
    }

    public String remove(Object key) {
        if (!(key instanceof String)) {
            return null;
        }
        String old = prop.getProperty((String) key);
        prop.removeProperty((String) key);
        return old;
    }

    public int size() {
        return prop.size();
    }

    public Collection<String> values() {
        Set<String> set = new HashSet<String>();
        for (@SuppressWarnings("unchecked") //$NON-NLS-1$
        Enumeration enm = prop.propertyNames(); enm.hasMoreElements();) {
            set.add(prop.getProperty((String) enm.nextElement()));
        }
        return set;
    }

    public static class EntryImpl<K, V> implements Map.Entry<K, V> {
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
}
