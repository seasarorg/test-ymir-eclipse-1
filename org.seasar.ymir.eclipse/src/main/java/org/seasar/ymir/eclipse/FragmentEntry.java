package org.seasar.ymir.eclipse;

import net.skirnir.xom.annotation.Bean;
import net.skirnir.xom.annotation.Child;
import net.skirnir.xom.annotation.Default;
import net.skirnir.xom.annotation.Required;

@Bean("fragment")
public class FragmentEntry implements MavenArtifact {
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    public FragmentEntry() {
        name = "";
        description = "";
    }

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

    @Child(order = 1)
    @Required
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Child(order = 2)
    @Required
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

    public String getName() {
        return name;
    }

    @Child(order = 4)
    @Default("")
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @Child(order = 5)
    @Default("")
    public void setDescription(String description) {
        this.description = description;
    }
}
