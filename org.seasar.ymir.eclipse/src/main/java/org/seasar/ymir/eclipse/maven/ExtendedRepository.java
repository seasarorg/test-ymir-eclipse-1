package org.seasar.ymir.eclipse.maven;

import java.net.URL;

import werkzeugkasten.mvnhack.repository.Repository;

public interface ExtendedRepository extends Repository {
    URL getMetadataLocation(String groupId, String artifactId);
}
