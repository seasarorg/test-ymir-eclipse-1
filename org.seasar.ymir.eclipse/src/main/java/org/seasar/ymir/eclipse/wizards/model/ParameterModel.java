package org.seasar.ymir.eclipse.wizards.model;

public interface ParameterModel {
    boolean valueExists();

    void notifyChanged();

    String getLabelText();

    Object getValue();

    void setValue(Object value);

    void setEnabled(boolean enabled);
}
