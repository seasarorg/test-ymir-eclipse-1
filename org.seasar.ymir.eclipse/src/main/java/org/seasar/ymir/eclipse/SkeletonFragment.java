package org.seasar.ymir.eclipse;

public class SkeletonFragment implements MavenArtifact {
    private String groupId;

    private String artifactId;

    private String version;

    public SkeletonFragment(String artifactId) {
        this(SkeletonEntry.DEFULAT_GROUPID, artifactId);
    }

    public SkeletonFragment(String groupId, String artifactId) {
        this(groupId, artifactId, null);
    }

    public SkeletonFragment(String groupId, String artifactId, String version) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
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
}
