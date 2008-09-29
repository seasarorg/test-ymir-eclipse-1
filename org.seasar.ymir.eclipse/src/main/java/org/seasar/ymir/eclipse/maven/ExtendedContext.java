package org.seasar.ymir.eclipse.maven;

import werkzeugkasten.mvnhack.repository.Context;

public interface ExtendedContext extends Context {
    String getLatestVersion(String groupId, String artifactId, boolean containsSnapshot);

    Metadata[] resolveMetadatas(String groupId, String artifactId, boolean containsSnapshot);
}
