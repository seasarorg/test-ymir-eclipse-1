package org.seasar.ymir.eclipse.maven;

import net.skirnir.xom.annotation.Child;

public class Dependency {
    private String groupId;

    private String artifactId;

    private String version;

    private String scope;

    public Dependency() {
    }

    public Dependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null);
    }

    public Dependency(String groupId, String artifactId, String version, String scope) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
    }

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

    public String getScope() {
        return scope;
    }

    @Child
    public void setScope(String scope) {
        this.scope = scope;
    }
}
