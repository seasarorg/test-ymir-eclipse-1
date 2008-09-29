package org.seasar.ymir.eclipse.maven;

import java.util.Set;

import org.seasar.ymir.eclipse.maven.impl.ExtendedLocalRepository;
import org.seasar.ymir.eclipse.maven.impl.ExtendedRemoteRepository;

import werkzeugkasten.mvnhack.repository.Configuration;

public interface ExtendedConfiguration extends Configuration {
    Set<ExtendedLocalRepository> getLocalRepositories();

    Set<ExtendedRemoteRepository> getRemoteRepositories();

    Set<ExtendedRemoteRepository> getSnapshotRepositories();

    Iterable<ExtendedRepository> getRepositoriesToResolveRelased();

    Iterable<ExtendedRepository> getRepositoriesToResolveSnapshot();

    Iterable<ExtendedRepository> getRepositoriesToGetLatestVersion(boolean containsSnapshot);
}
