package org.seasar.ymir.eclipse.maven;

import net.skirnir.xom.annotation.Child;

public class Parent {
    private String groupId;

    private String artifactId;

    private String version;

    public String getGroupId() {
        return groupId;
    }

    @Child(order = 1)
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Child(order = 2)
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Child(order = 3)
    public void setVersion(String version) {
        this.version = version;
    }
}
