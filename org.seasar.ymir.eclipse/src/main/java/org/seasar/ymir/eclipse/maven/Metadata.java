package org.seasar.ymir.eclipse.maven;

import net.skirnir.xom.annotation.Child;

public class Metadata {
    private String groupId;

    private String artifactId;

    private String version;

    private Versioning versioning;

    public String getGroupId() {
        return groupId;
    }

    @Child
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Child
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Child
    public void setVersion(String version) {
        this.version = version;
    }

    public Versioning getVersioning() {
        return versioning;
    }

    @Child
    public void setVersioning(Versioning versioning) {
        this.versioning = versioning;
    }
}
