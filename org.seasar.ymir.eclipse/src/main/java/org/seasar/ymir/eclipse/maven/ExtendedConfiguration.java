package org.seasar.ymir.eclipse.maven;

import java.util.Set;

import org.seasar.ymir.eclipse.maven.impl.LocalExtendedRepository;
import org.seasar.ymir.eclipse.maven.impl.RemoteExtendedRepository;

import werkzeugkasten.mvnhack.repository.Configuration;

public interface ExtendedConfiguration extends Configuration {
    Set<LocalExtendedRepository> getLocalRepositories();

    Set<RemoteExtendedRepository> getRemoteRepositories();

    Set<RemoteExtendedRepository> getSnapshotRepositories();

    Iterable<ExtendedRepository> getRepositoriesToResolveRelased();

    Iterable<ExtendedRepository> getRepositoriesToResolveSnapshot();

    Iterable<ExtendedRepository> getRepositoriesToGetLatestVersion(boolean containsSnapshot);

    void setOffline(boolean offline);
}
