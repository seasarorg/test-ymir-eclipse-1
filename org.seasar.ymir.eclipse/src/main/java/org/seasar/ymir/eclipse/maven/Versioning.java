package org.seasar.ymir.eclipse.maven;

import net.skirnir.xom.annotation.Child;

public class Versioning {
    private String release;

    private Snapshot snapshot;

    private Versions versions;

    private Long lastUpdated;

    public Versions getVersions() {
        return versions;
    }

    public String getRelease() {
        return release;
    }

    @Child(order = 1)
    public void setRelease(String release) {
        this.release = release;
    }

    public Snapshot getSnapshot() {
        return snapshot;
    }

    @Child(order = 2)
    public void setSnapshot(Snapshot snapshot) {
        this.snapshot = snapshot;
    }

    @Child(order = 3)
    public void setVersions(Versions versions) {
        this.versions = versions;
    }

    public Long getLastUpdated() {
        return lastUpdated;
    }

    @Child(order = 4)
    public void setLastUpdated(Long lastUpdated) {
        this.lastUpdated = lastUpdated;
    }
}
