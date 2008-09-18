package org.seasar.ymir.eclipse;

public class FragmentEntry implements MavenArtifact {
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    public FragmentEntry(String artifactId, String name, String description) {
        this(SkeletonEntry.DEFULAT_GROUPID, artifactId, null, name, description);
    }

    public FragmentEntry(String groupId, String artifactId, String name, String description) {
        this(groupId, artifactId, null, name, description);
    }

    public FragmentEntry(String groupId, String artifactId, String version, String name, String description) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
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
}
