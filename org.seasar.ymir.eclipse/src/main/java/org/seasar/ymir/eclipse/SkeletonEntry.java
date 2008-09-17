package org.seasar.ymir.eclipse;

public class SkeletonEntry implements MavenArtifact {
    static final String DEFULAT_GROUPID = "org.seasar.ymir.skeleton";

    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    private SkeletonFragment[] fragments;

    public SkeletonEntry(String artifactId, String name, String description, SkeletonFragment... fragments) {
        this(DEFULAT_GROUPID, artifactId, null, name, description, fragments);
    }

    public SkeletonEntry(String groupId, String artifactId, String name, String description,
            SkeletonFragment... fragments) {
        this(groupId, artifactId, null, name, description, fragments);
    }

    public SkeletonEntry(String groupId, String artifactId, String version, String name, String description,
            SkeletonFragment... fragments) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.fragments = fragments;
    }

    @Override
    public String toString() {
        return name + "(" + groupId + ":" + artifactId + "-" + version + ")";
    }

    public String getGroupId() {
        return groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public String getVersion() {
        return version;
    }

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public SkeletonFragment[] getFragments() {
        return fragments;
    }
}
