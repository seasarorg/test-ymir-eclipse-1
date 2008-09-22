package org.seasar.ymir.eclipse;

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
