package org.seasar.ymir.vili.maven;

import java.util.Set;

import werkzeugkasten.mvnhack.repository.Configuration;

public interface ExtendedConfiguration extends Configuration {
    Set<ExtendedRepository> getLocalRepositories();

    Set<ExtendedRepository> getRemoteRepositories();

    Set<ExtendedRepository> getSnapshotRepositories();

    Iterable<ExtendedRepository> getRepositoriesToResolveRelased();

    Iterable<ExtendedRepository> getRepositoriesToResolveSnapshot();

    Iterable<ExtendedRepository> getRepositoriesToGetLatestVersion(
            boolean containsSnapshot);

    void setOffline(boolean offline);
}
