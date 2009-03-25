package org.seasar.ymir.vili.model.maven;

import net.skirnir.xom.annotation.Child;

public class Exclusion {
    private String groupId;

    private String artifactId;

    public Exclusion() {
    }

    public Exclusion(String groupId, String artifactId) {
        this.groupId = groupId;
        this.artifactId = artifactId;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Exclusion o = (Exclusion) obj;
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
        return groupId + ":" + artifactId;
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
}
