package org.seasar.ymir.eclipse.maven;

import org.seasar.ymir.vili.model.maven.Metadata;

import werkzeugkasten.mvnhack.repository.Context;

public interface ExtendedContext extends Context {
    String getLatestVersion(String groupId, String artifactId, boolean containsSnapshot);

    Metadata[] resolveMetadatas(String groupId, String artifactId, boolean containsSnapshot);
}
