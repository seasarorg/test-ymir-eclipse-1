package org.seasar.ymir.eclipse.util;

import java.beans.BeanInfo;
import java.beans.IntrospectionException;
import java.beans.Introspector;
import java.beans.PropertyDescriptor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class BeanMap implements Map<String, Object> {
    private Object obj;

    private Map<String, Method> readMethodMap = new HashMap<String, Method>();

    public BeanMap(Object obj) {
        this.obj = obj;

        BeanInfo beanInfo;
        try {
            beanInfo = Introspector.getBeanInfo(obj.getClass());
        } catch (IntrospectionException ex) {
            throw new RuntimeException("Can't introspect: " + obj.getClass().getName(), ex);
        }

        for (PropertyDescriptor pd : beanInfo.getPropertyDescriptors()) {
            if (pd.getReadMethod() != null) {
                readMethodMap.put(pd.getName(), pd.getReadMethod());
            }
        }
    }

    public void clear() {
        throw new UnsupportedOperationException();
    }

    public boolean containsKey(Object key) {
        return readMethodMap.containsKey(key);
    }

    public boolean containsValue(Object value) {
        throw new UnsupportedOperationException();
    }

    public Set<Entry<String, Object>> entrySet() {
        Set<Entry<String, Object>> entrySet = new HashSet<Entry<String, Object>>();
        for (String key : keySet()) {
            entrySet.add(new EntryImpl<String, Object>(key, null) {
                @Override
                public Object getValue() {
                    return get(getKey());
                }
            });
        }
        return entrySet;
    }

    public Object get(Object key) {
        Method method = readMethodMap.get(key);
        if (method != null) {
            try {
                return method.invoke(obj, new Object[0]);
            } catch (IllegalArgumentException ignore) {
            } catch (IllegalAccessException ignore) {
            } catch (InvocationTargetException ignore) {
            }
        }
        return null;
    }

    public boolean isEmpty() {
        return readMethodMap.isEmpty();
    }

    public Set<String> keySet() {
        return readMethodMap.keySet();
    }

    public Object put(String key, Object value) {
        throw new UnsupportedOperationException();
    }

    public void putAll(Map<? extends String, ? extends Object> t) {
        throw new UnsupportedOperationException();
    }

    public Object remove(Object key) {
        throw new UnsupportedOperationException();
    }

    public int size() {
        return readMethodMap.size();
    }

    public Collection<Object> values() {
        throw new UnsupportedOperationException();
    }
}
