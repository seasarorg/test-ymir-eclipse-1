package org.seasar.ymir.vili;

public class ViliProjectPreferencesDelta {
    private String name;

    private Object oldValue;

    private Object newValue;

    public ViliProjectPreferencesDelta(String name, Object oldValue,
            Object newValue) {
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getName() {
        return name;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }
}
