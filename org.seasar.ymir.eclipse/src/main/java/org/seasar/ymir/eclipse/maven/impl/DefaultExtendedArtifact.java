package org.seasar.ymir.eclipse.maven.impl;

import java.util.Set;

import org.seasar.ymir.vili.maven.ExtendedArtifact;

import werkzeugkasten.mvnhack.repository.Artifact;

public class DefaultExtendedArtifact implements ExtendedArtifact {
    private Artifact artifact;

    private String actualVersion;

    private long lastUpdated;

    public static Artifact newInstance(Artifact artifact) {
        return newInstance(artifact, null, 0L);
    }

    public static DefaultExtendedArtifact newInstance(Artifact artifact, String actualVersion, long lastUpdated) {
        if (artifact == null) {
            return null;
        }
        return new DefaultExtendedArtifact(artifact, actualVersion, lastUpdated);
    }

    DefaultExtendedArtifact(Artifact artifact, String actualVersion, long lastUpdated) {
        this.artifact = artifact;
        this.actualVersion = actualVersion;
        this.lastUpdated = lastUpdated;
    }

    @Override
    public String toString() {
        return artifact.toString() + " actualVersion :" + actualVersion + " lastUpdated :" + lastUpdated; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public String getArtifactId() {
        return artifact.getArtifactId();
    }

    public Set<Artifact> getDependencies() {
        return artifact.getDependencies();
    }

    public String getGroupId() {
        return artifact.getGroupId();
    }

    public String getType() {
        return artifact.getType();
    }

    public String getVersion() {
        return artifact.getVersion();
    }

    public boolean isOptional() {
        return artifact.isOptional();
    }

    public String getActualVersion() {
        return actualVersion;
    }

    public long getLastUpdated() {
        return lastUpdated;
    }
}
