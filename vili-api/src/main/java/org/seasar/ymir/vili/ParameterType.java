package org.seasar.ymir.vili;

public enum ParameterType {
    TEXT, CHECKBOX;

    public static ParameterType enumOf(String name) {
        if (name != null) {
            try {
                return valueOf(name.toUpperCase());
            } catch (IllegalArgumentException ignore) {
            }
        }
        return TEXT;
    }
}
