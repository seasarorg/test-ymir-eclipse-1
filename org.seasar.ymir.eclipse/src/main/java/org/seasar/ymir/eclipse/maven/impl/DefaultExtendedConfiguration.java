package org.seasar.ymir.eclipse.maven.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.seasar.ymir.eclipse.maven.ExtendedConfiguration;
import org.seasar.ymir.eclipse.maven.ExtendedRepository;
import org.seasar.ymir.eclipse.maven.ExtendedRepository.Type;
import org.seasar.ymir.eclipse.maven.util.CompositeIterator;

import werkzeugkasten.common.util.StringUtil;
import werkzeugkasten.common.util.UrlUtil;
import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Destination;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.RemoteRepository;
import werkzeugkasten.mvnhack.repository.impl.StAXArtifactBuilder;

public class DefaultExtendedConfiguration implements ExtendedConfiguration {
    protected Set<Repository> repositories = new LinkedHashSet<Repository>();

    protected Set<Destination> destinations = new LinkedHashSet<Destination>();

    protected ArtifactBuilder builder = new StAXArtifactBuilder();

    protected Set<ExtendedLocalRepository> localRepositories = new LinkedHashSet<ExtendedLocalRepository>();;

    protected Set<ExtendedRemoteRepository> remoteRepositories = new LinkedHashSet<ExtendedRemoteRepository>();;

    protected Set<ExtendedRemoteRepository> snapshotRepositories = new LinkedHashSet<ExtendedRemoteRepository>();;

    public DefaultExtendedConfiguration() {
    }

    public DefaultExtendedConfiguration(Properties properties) {
        load();
        load(properties);
    }

    protected void load() {
        File cur = new File(".", Constants.DIR_REPOSITORY);
        if (cur.exists()) {
            addLocalRepository(cur);
        }
        StringBuilder stb = new StringBuilder();
        stb.append(".m2");
        stb.append('/');
        stb.append(Constants.DIR_REPOSITORY);
        File usr = new File(System.getProperty("user.home"), stb.toString());
        if (usr.exists()) {
            addLocalRepository(usr);
        }

        addRepository(new ExtendedRemoteRepository(Constants.CENTRAL_REPOSITORY, false, builder));
    }

    protected void addLocalRepository(File rep) {
        Constants.LOG.log(Level.INFO, "LocalRepository :{0}", rep.toString());
        ExtendedLocalRepository lr = new ExtendedLocalRepository(rep, builder);
        addRepository(lr);
        addDestination(lr);
    }

    protected void load(Properties properties) {
        String rs = properties.getProperty(Constants.PROP_REPOSITORY);
        if (StringUtil.isEmpty(rs) == false) {
            for (String s : rs.split(",")) {
                if (validateURL(s)) {
                    addRepository(new RemoteRepository(s, builder));
                }
            }
        }
        UrlUtil.setUpProxy(properties.getProperty(Constants.PROP_PROXY));
        String hosts = properties.getProperty("http.nonProxyHosts");
        if (StringUtil.isEmpty(hosts) == false) {
            System.setProperty("http.nonProxyHosts", hosts);
        }
    }

    protected boolean validateURL(String url) {
        try {
            new URL(url);
            return true;
        } catch (MalformedURLException e) {
            return false;
        }
    }

    public void addDestination(Destination destination) {
        this.destinations.add(destination);
    }

    public void addRepository(Repository repository) {
        if (!(repository instanceof ExtendedRepository)) {
            throw new IllegalArgumentException("Must be an instance of ExtendedRepository: " + repository);
        }

        ExtendedRepository er = (ExtendedRepository) repository;
        Type type = er.getType();
        if (type == Type.LOCAL) {
            this.localRepositories.add((ExtendedLocalRepository) er);
        } else if (type == Type.SNAPSHOT) {
            this.snapshotRepositories.add((ExtendedRemoteRepository) er);
        } else {
            this.remoteRepositories.add((ExtendedRemoteRepository) er);
        }

        this.repositories.add(repository);
    }

    public Set<Destination> getDestinations() {
        return this.destinations;
    }

    public Set<Repository> getRepositories() {
        return this.repositories;
    }

    public Set<ExtendedLocalRepository> getLocalRepositories() {
        return this.localRepositories;
    }

    public Set<ExtendedRemoteRepository> getRemoteRepositories() {
        return this.remoteRepositories;
    }

    public Set<ExtendedRemoteRepository> getSnapshotRepositories() {
        return this.snapshotRepositories;
    }

    public Iterable<ExtendedRepository> getRepositoriesToResolveRelased() {
        return new Iterable<ExtendedRepository>() {
            @SuppressWarnings("unchecked")
            public Iterator<ExtendedRepository> iterator() {
                return new CompositeIterator<ExtendedRepository>(localRepositories.iterator(), remoteRepositories
                        .iterator());
            }
        };
    }

    public Iterable<ExtendedRepository> getRepositoriesToResolveSnapshot() {
        return new Iterable<ExtendedRepository>() {
            @SuppressWarnings("unchecked")
            public Iterator<ExtendedRepository> iterator() {
                return new CompositeIterator<ExtendedRepository>(snapshotRepositories.iterator(), localRepositories
                        .iterator());
            }
        };
    }

    public Iterable<ExtendedRepository> getRepositoriesToGetLatestVersion(final boolean containsSnapshot) {
        return new Iterable<ExtendedRepository>() {
            @SuppressWarnings("unchecked")
            public Iterator<ExtendedRepository> iterator() {
                if (containsSnapshot) {
                    return new CompositeIterator<ExtendedRepository>(snapshotRepositories.iterator(),
                            remoteRepositories.iterator(), localRepositories.iterator());
                } else {
                    return new CompositeIterator<ExtendedRepository>(remoteRepositories.iterator(), localRepositories
                            .iterator());
                }
            }
        };
    }
}