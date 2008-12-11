package org.seasar.ymir.vili;

public enum ArtifactType {
    SKELETON, FRAGMENT;

    public static ArtifactType enumOf(String name) {
        ArtifactType artifactTyee = SKELETON;
        if (name != null) {
            try {
                artifactTyee = valueOf(name.toUpperCase());
            } catch (IllegalArgumentException ignore) {
            }
        }
        return artifactTyee;
    }
}
