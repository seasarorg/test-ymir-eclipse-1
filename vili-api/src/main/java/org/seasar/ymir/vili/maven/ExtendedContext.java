package org.seasar.ymir.vili.maven;

import org.seasar.ymir.vili.model.maven.Metadata;

import werkzeugkasten.mvnhack.repository.Context;

public interface ExtendedContext extends Context {
    String getLatestVersion(String groupId, String artifactId,
            boolean containsSnapshot);

    String[] getVersions(String groupId, String artifactId,
            boolean containsSnapshot);

    Metadata[] resolveMetadatas(String groupId, String artifactId,
            boolean containsSnapshot);
}
