package org.seasar.ymir.eclipse.maven;

import net.skirnir.xom.annotation.Child;

public class Versioning {
    private Versions versions;

    private Long lastUpdated;

    public Versions getVersions() {
        return versions;
    }

    @Child
    public void setVersions(Versions versions) {
        this.versions = versions;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    @Child
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
