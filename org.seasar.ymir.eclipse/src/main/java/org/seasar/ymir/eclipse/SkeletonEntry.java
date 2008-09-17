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

    public String getArtifactId() {
        return artifactId;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }
}
