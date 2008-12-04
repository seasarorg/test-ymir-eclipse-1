package org.seasar.ymir.eclipse.maven;

import net.skirnir.xom.annotation.Child;

public class Project {
    private Parent parent;

    private String groupId;

    private String artifactId;

    private String version;

    private Repositories repositories;

    private Dependencies dependencies;

    public Parent getParent() {
        return parent;
    }

    @Child(order = 1)
    public void setParent(Parent parent) {
        this.parent = parent;
    }

    public String getGroupId() {
        return groupId;
    }

    @Child(order = 2)
    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    @Child(order = 3)
    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    @Child(order = 4)
    public void setVersion(String version) {
        this.version = version;
    }

    public String findGroupId() {
        if (groupId == null && parent != null) {
            return parent.getGroupId();
        } else {
            return groupId;
        }
    }

    public String findArtifactId() {
        if (artifactId == null && parent != null) {
            return parent.getArtifactId();
        } else {
            return artifactId;
        }
    }

    public String findVersion() {
        if (version == null && parent != null) {
            return parent.getVersion();
        } else {
            return version;
        }
    }

    public Repositories getRepositories() {
        return repositories;
    }

    @Child(order = 5)
    public void setRepositories(Repositories repositories) {
        this.repositories = repositories;
    }

    public Dependencies getDependencies() {
        return dependencies;
    }

    @Child(order = 6)
    public void setDependencies(Dependencies dependencies) {
        this.dependencies = dependencies;
    }
}
