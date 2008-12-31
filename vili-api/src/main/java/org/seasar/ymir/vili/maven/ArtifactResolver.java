package org.seasar.ymir.vili.maven;

import java.net.URL;

import werkzeugkasten.mvnhack.repository.Artifact;

public interface ArtifactResolver {
    String SNAPSHOT = "SNAPSHOT"; //$NON-NLS-1$

    void addRemoteRepository(String url, boolean snapshot);

    ExtendedContext newContext(boolean transitive);

    ExtendedArtifact resolve(String groupId, String artifactId, String version, boolean transitive);

    ExtendedArtifact resolve(ExtendedContext context, String groupId, String artifactId, String version);

    URL getURL(Artifact artifact);

    String getLatestVersion(String groupId, String artifactId, boolean containsSnapshot);

    String getLatestVersion(ExtendedContext context, String groupId, String artifactId, boolean containsSnapshot);

    void setOffline(boolean offline);
}
