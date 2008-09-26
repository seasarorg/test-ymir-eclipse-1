package org.seasar.ymir.eclipse.maven;

import werkzeugkasten.mvnhack.repository.Repository;

public interface ExtendedRepository extends Repository {
    byte[] resolveMetadata(String groupId, String artifactId);

    byte[] resolveMetadata(String groupId, String artifactId, String version);

    boolean isSnapshot();
}
