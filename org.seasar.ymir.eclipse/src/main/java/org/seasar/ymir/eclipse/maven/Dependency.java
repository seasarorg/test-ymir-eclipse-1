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

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Dependency o = (Dependency) obj;
        if (!equals(o.groupId, groupId)) {
            return false;
        }
        if (!equals(o.artifactId, artifactId)) {
            return false;
        }

        return true;
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        if (groupId != null) {
            h += groupId.hashCode();
        }
        if (artifactId != null) {
            h += artifactId.hashCode();
        }
        return h;
    }

    @Override
    public String toString() {
        return groupId + ":" + artifactId + "-" + version + ", scope=" + scope;
    }

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

    public String getScope() {
        return scope;
    }

    @Child(order = 4)
    public void setScope(String scope) {
        this.scope = scope;
    }
}
