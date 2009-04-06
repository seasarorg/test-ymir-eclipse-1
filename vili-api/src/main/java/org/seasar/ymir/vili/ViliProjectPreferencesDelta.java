package org.seasar.ymir.vili;

public class ViliProjectPreferencesDelta {
    private String name;

    private Object oldValue;

    private Object newValue;

    public ViliProjectPreferencesDelta() {
    }

    public ViliProjectPreferencesDelta(String name, Object oldValue,
            Object newValue) {
        this.name = name;
        this.oldValue = oldValue;
        this.newValue = newValue;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Object getOldValue() {
        return oldValue;
    }

    public void setOldValue(Object oldValue) {
        this.oldValue = oldValue;
    }

    public Object getNewValue() {
        return newValue;
    }

    public void setNewValue(Object newValue) {
        this.newValue = newValue;
    }
}
