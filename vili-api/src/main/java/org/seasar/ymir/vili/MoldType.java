package org.seasar.ymir.vili;

public enum MoldType {
    SKELETON, FRAGMENT;

    public static MoldType enumOf(String name) {
        MoldType moldTyee = SKELETON;
        if (name != null) {
            try {
                moldTyee = valueOf(name.toUpperCase());
            } catch (IllegalArgumentException ignore) {
            }
        }
        return moldTyee;
    }
}
