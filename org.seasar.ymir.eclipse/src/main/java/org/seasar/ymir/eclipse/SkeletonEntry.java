package org.seasar.ymir.eclipse;

public class SkeletonEntry {
    private static final String DEFULAT_GROUPID = "org.seasar.ymir.skeleton";

    private String groupId;

    private String artifactId;

    private String name;

    private String description;

    public SkeletonEntry(String artifactId, String name, String description) {
        this(DEFULAT_GROUPID, artifactId, name, description);
    }

    public SkeletonEntry(String groupId, String artifactId, String name, String description) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.name = name;
        this.description = description;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
