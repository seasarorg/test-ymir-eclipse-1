package org.seasar.ymir.eclipse.maven.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.logging.Level;

import org.seasar.ymir.vili.maven.ExtendedConfiguration;
import org.seasar.ymir.vili.maven.ExtendedRepository;
import org.seasar.ymir.vili.maven.ExtendedRepository.Type;

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

    protected Set<ExtendedRepository> localRepositories = new LinkedHashSet<ExtendedRepository>();;

    protected Set<ExtendedRepository> remoteRepositories = new LinkedHashSet<ExtendedRepository>();;

    protected Set<ExtendedRepository> snapshotRepositories = new LinkedHashSet<ExtendedRepository>();;

    protected boolean offline;

    public DefaultExtendedConfiguration() {
    }

    public DefaultExtendedConfiguration(Properties properties) {
        load();
        load(properties);
    }

    protected void load() {
        StringBuilder stb = new StringBuilder();
        stb.append(".m2"); //$NON-NLS-1$
        stb.append('/');
        stb.append(Constants.DIR_REPOSITORY);
        File usr = new File(System.getProperty("user.home"), stb.toString()); //$NON-NLS-1$
        usr.mkdirs();
        // .m2ディレクトリがない場合でもローカルリポジトリとして登録しておく。でないとMavenを外で実行したことがないような環境で正しく動作しない。
        addLocalRepository(usr);

        addRepository(new RemoteExtendedRepository(Constants.CENTRAL_REPOSITORY, false, builder));
    }

    protected void addLocalRepository(File rep) {
        Constants.LOG.log(Level.INFO, "LocalRepository :{0}", rep.toString()); //$NON-NLS-1$
        LocalExtendedRepository lr = new LocalExtendedRepository(rep, builder);
        addRepository(lr);
        addDestination(lr);
    }

    protected void load(Properties properties) {
        String rs = properties.getProperty(Constants.PROP_REPOSITORY);
        if (StringUtil.isEmpty(rs) == false) {
            for (String s : rs.split(",")) { //$NON-NLS-1$
                if (validateURL(s)) {
                    addRepository(new RemoteRepository(s, builder));
                }
            }
        }
        UrlUtil.setUpProxy(properties.getProperty(Constants.PROP_PROXY));
        String hosts = properties.getProperty("http.nonProxyHosts"); //$NON-NLS-1$
        if (StringUtil.isEmpty(hosts) == false) {
            System.setProperty("http.nonProxyHosts", hosts); //$NON-NLS-1$
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
            throw new IllegalArgumentException("Must be an instance of ExtendedRepository: " + repository); //$NON-NLS-1$
        }

        ExtendedRepository er = (ExtendedRepository) repository;
        Type type = er.getType();
        if (type == Type.LOCAL) {
            this.localRepositories.add(er);
        } else if (type == Type.SNAPSHOT) {
            this.snapshotRepositories.add(er);
        } else {
            this.remoteRepositories.add(er);
        }

        this.repositories.add(repository);
    }

    public Set<Destination> getDestinations() {
        return this.destinations;
    }

    public Set<Repository> getRepositories() {
        return this.repositories;
    }

    public Set<ExtendedRepository> getLocalRepositories() {
        return this.localRepositories;
    }

    public Set<ExtendedRepository> getRemoteRepositories() {
        return this.remoteRepositories;
    }

    public Set<ExtendedRepository> getSnapshotRepositories() {
        return this.snapshotRepositories;
    }

    public Iterable<ExtendedRepository> getRepositoriesToResolveRelased() {
        return new Iterable<ExtendedRepository>() {
            @SuppressWarnings("unchecked")//$NON-NLS-1$
            public Iterator<ExtendedRepository> iterator() {
                if (offline) {
                    return new CompositeIterator<ExtendedRepository>(localRepositories.iterator());
                } else {
                    return new CompositeIterator<ExtendedRepository>(localRepositories.iterator(), remoteRepositories
                            .iterator());
                }
            }
        };
    }

    public Iterable<ExtendedRepository> getRepositoriesToResolveSnapshot() {
        return new Iterable<ExtendedRepository>() {
            @SuppressWarnings("unchecked")//$NON-NLS-1$
            public Iterator<ExtendedRepository> iterator() {
                if (offline) {
                    return new CompositeIterator<ExtendedRepository>(localRepositories.iterator());
                } else {
                    return new CompositeIterator<ExtendedRepository>(snapshotRepositories.iterator(), localRepositories
                            .iterator());
                }
            }
        };
    }

    public Iterable<ExtendedRepository> getRepositoriesToGetLatestVersion(final boolean containsSnapshot) {
        return new Iterable<ExtendedRepository>() {
            @SuppressWarnings("unchecked")//$NON-NLS-1$
            public Iterator<ExtendedRepository> iterator() {
                if (containsSnapshot) {
                    if (offline) {
                        return new CompositeIterator<ExtendedRepository>(localRepositories.iterator());
                    } else {
                        return new CompositeIterator<ExtendedRepository>(snapshotRepositories.iterator(),
                                remoteRepositories.iterator(), localRepositories.iterator());
                    }
                } else {
                    if (offline) {
                        return new CompositeIterator<ExtendedRepository>(localRepositories.iterator());
                    } else {
                        return new CompositeIterator<ExtendedRepository>(remoteRepositories.iterator(),
                                localRepositories.iterator());
                    }
                }
            }
        };
    }

    public void setOffline(boolean offline) {
        this.offline = offline;
    }
}
