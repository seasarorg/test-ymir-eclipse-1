package org.seasar.ymir.vili.util;

import org.seasar.ymir.vili.maven.ArtifactVersion;

public class ViliUtils {
    private ViliUtils() {
    }

    public static boolean isCompatible(String versionOfPlugin,
            String versionOfArtifact) {
        if (versionOfArtifact == null) {
            return false;
        }

        return isCompatible(new ArtifactVersion(versionOfPlugin),
                new ArtifactVersion(versionOfArtifact));
    }

    public static boolean isCompatible(ArtifactVersion versionOfPlugin,
            ArtifactVersion versionOfArtifact) {
        if (versionOfArtifact == null) {
            return false;
        }

        if (versionOfPlugin.getMajorVersion() != versionOfArtifact
                .getMajorVersion()
                || versionOfPlugin.getMinorVersion() != versionOfArtifact
                        .getMinorVersion()) {
            return false;
        }

        return versionOfPlugin.getIncrementalVersion() >= versionOfArtifact
                .getIncrementalVersion();
    }
}
