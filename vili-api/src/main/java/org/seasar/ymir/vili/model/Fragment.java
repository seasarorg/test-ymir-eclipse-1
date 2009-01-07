package org.seasar.ymir.vili.model;

import net.skirnir.xom.annotation.Child;
import net.skirnir.xom.annotation.Default;
import net.skirnir.xom.annotation.Required;

public class Fragment implements MavenArtifact {
    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    private boolean availableOnlyIfProjectExists;

    public Fragment() {
        name = ""; //$NON-NLS-1$
        description = ""; //$NON-NLS-1$
    }

    public Fragment(String artifactId, String name, String description) {
        this(Skeleton.DEFULAT_GROUPID, artifactId, null, name, description);
    }

    public Fragment(String artifactId, String name, String description,
            boolean availableOnlyIfProjectExists) {
        this(Skeleton.DEFULAT_GROUPID, artifactId, null, name, description,
                availableOnlyIfProjectExists);
    }

    public Fragment(String groupId, String artifactId, String name,
            String description) {
        this(groupId, artifactId, null, name, description);
    }

    public Fragment(String groupId, String artifactId, String name,
            String description, boolean availableOnlyIfProjectExists) {
        this(groupId, artifactId, null, name, description,
                availableOnlyIfProjectExists);
    }

    public Fragment(String groupId, String artifactId, String version,
            String name, String description) {
        this(groupId, artifactId, version, name, description, false);
    }

    public Fragment(String groupId, String artifactId, String version,
            String name, String description,
            boolean availableOnlyIfProjectExists) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
        this.availableOnlyIfProjectExists = availableOnlyIfProjectExists;
    }

    @Override
    public String toString() {
        return name + "(" + groupId + ":" + artifactId + "-" + version + ")"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
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
    @Default("")//$NON-NLS-1$
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @Child(order = 5)
    @Default("")//$NON-NLS-1$
    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isAvailableOnlyIfProjectExists() {
        return availableOnlyIfProjectExists;
    }

    @Child(order = 6)
    @Default("false")//$NON-NLS-1$
    public void setAvailableOnlyIfProjectExists(
            boolean availableOnlyIfProjectExists) {
        this.availableOnlyIfProjectExists = availableOnlyIfProjectExists;
    }
}
