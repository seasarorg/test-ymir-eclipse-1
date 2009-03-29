package org.seasar.ymir.vili.model.maven;

import net.skirnir.xom.annotation.Child;

public class Dependency {
    private String groupId;

    private String artifactId;

    private String version;

    private String classifier;

    private String type;

    private String scope;

    private String systemPath;

    private String optional;

    private Exclusions exclusions;

    public Dependency() {
    }

    public Dependency(String groupId, String artifactId) {
        this(groupId, artifactId, null, null, null);
    }

    public Dependency(String groupId, String artifactId, String version) {
        this(groupId, artifactId, version, null);
    }

    public Dependency(String groupId, String artifactId, String version,
            String scope) {
        this(groupId, artifactId, version, scope, null);
    }

    public Dependency(String groupId, String artifactId, String version,
            String scope, String classifier) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.scope = scope;
        this.classifier = classifier;
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
        if (!equals(o.classifier, classifier)) {
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
        return groupId + ":" + artifactId + "-" + version + "-" + classifier
                + ", type=" + type + ", scope=" + scope + ", systemPath="
                + systemPath + ", optional=" + optional;
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

    public String getClassifier() {
        return classifier;
    }

    @Child(order = 4)
    public void setClassifier(String classifier) {
        this.classifier = classifier;
    }

    public String getType() {
        return type;
    }

    @Child(order = 5)
    public void setType(String type) {
        this.type = type;
    }

    public String getScope() {
        return scope;
    }

    @Child(order = 6)
    public void setScope(String scope) {
        this.scope = scope;
    }

    public String getSystemPath() {
        return systemPath;
    }

    @Child(order = 7)
    public void setSystemPath(String systemPath) {
        this.systemPath = systemPath;
    }

    public String getOptional() {
        return optional;
    }

    @Child(order = 8)
    public void setOptional(String optional) {
        this.optional = optional;
    }

    public Exclusions getExclusions() {
        return exclusions;
    }

    @Child(order = 9)
    public void setExclusions(Exclusions exclusions) {
        this.exclusions = exclusions;
    }
}
