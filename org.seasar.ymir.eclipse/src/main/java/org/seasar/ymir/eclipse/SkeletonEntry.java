package org.seasar.ymir.eclipse;

import net.skirnir.xom.annotation.Bean;
import net.skirnir.xom.annotation.Child;
import net.skirnir.xom.annotation.Default;
import net.skirnir.xom.annotation.Required;

@Bean("skeleton") //$NON-NLS-1$
public class SkeletonEntry implements MavenArtifact {
    static final String DEFULAT_GROUPID = "org.seasar.ymir.skeleton"; //$NON-NLS-1$

    private String groupId;

    private String artifactId;

    private String version;

    private String name;

    private String description;

    private FragmentEntries fragments;

    public SkeletonEntry() {
        name = ""; //$NON-NLS-1$
        description = ""; //$NON-NLS-1$
    }

    public SkeletonEntry(String artifactId, String name, String description, FragmentEntry... fragments) {
        this(DEFULAT_GROUPID, artifactId, null, name, description, fragments);
    }

    public SkeletonEntry(String groupId, String artifactId, String name, String description, FragmentEntry... fragments) {
        this(groupId, artifactId, null, name, description, fragments);
    }

    public SkeletonEntry(String groupId, String artifactId, String version, String name, String description,
            FragmentEntry... fragments) {
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
        this.name = name;
        this.description = description;
        if (fragments.length > 0) {
            this.fragments = new FragmentEntries(fragments);
        }
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
    @Default("") //$NON-NLS-1$
    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    @Child(order = 5)
    @Default("") //$NON-NLS-1$
    public void setDescription(String description) {
        this.description = description;
    }

    public FragmentEntries getFragments() {
        return fragments;
    }

    public FragmentEntry[] getAllFragments() {
        if (fragments != null) {
            return fragments.getFragments();
        } else {
            return new FragmentEntry[0];
        }
    }

    @Child(order = 6)
    public void setFragments(FragmentEntries fragments) {
        this.fragments = fragments;
    }
}
