package org.seasar.ymir.eclipse.maven.impl;

import java.io.File;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;

import org.seasar.ymir.eclipse.maven.ExtendedArtifact;
import org.seasar.ymir.eclipse.maven.ExtendedConfiguration;
import org.seasar.ymir.eclipse.maven.ExtendedContext;
import org.seasar.ymir.eclipse.maven.ExtendedRepository;
import org.seasar.ymir.eclipse.maven.ExtendedRepository.Type;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.vili.model.maven.Metadata;

import werkzeugkasten.common.util.UrlUtil;
import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.Destination;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.ArtifactUtil;

public class DefaultExtendedContext implements ExtendedContext {
    protected Map<String, ExtendedArtifact> resolved;

    protected Map<String, Metadata[]> resolvedMetadatas;

    private Map<String, String> resolvedVersion;

    protected ExtendedConfiguration configuration;

    protected Map<String, String> managedDependencies;

    public DefaultExtendedContext(ExtendedConfiguration configuration) {
        this(configuration, new HashMap<String, ExtendedArtifact>());
    }

    public DefaultExtendedContext(ExtendedConfiguration configuration, Map<String, ExtendedArtifact> resolved) {
        this.configuration = configuration;
        this.resolved = resolved;
        this.resolvedMetadatas = new HashMap<String, Metadata[]>();
        this.resolvedVersion = new HashMap<String, String>();
        this.managedDependencies = new HashMap<String, String>();
    }

    protected void addResolved(ExtendedArtifact artifact) {
        resolved.put(toId(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion()), artifact);
    }

    protected String toId(String groupId, String artifactId, String version) {
        return ArtifactUtil.toPath(groupId, artifactId, version, ""); //$NON-NLS-1$
    }

    protected String toId(String groupId, String artifactId, boolean containsSnapshot) {
        return groupId + ":" + artifactId + "/" + containsSnapshot; //$NON-NLS-1$ //$NON-NLS-2$
    }

    public void addManagedDependency(Artifact artifact) {
        this.managedDependencies.put(toManagedId(artifact), artifact.getVersion());
    }

    public String getManagedDependency(Artifact artifact) {
        return this.managedDependencies.get(toManagedId(artifact));
    }

    protected String toManagedId(Artifact artifact) {
        return artifact.getGroupId() + '/' + artifact.getArtifactId();
    }

    public Artifact resolve(String groupId, String artifactId, String version) {
        if (ArtifactUtils.isSnapshot(version)) {
            return resolveSnapshot(groupId, artifactId, version);
        } else {
            return resolveReleased(groupId, artifactId, version);
        }
    }

    protected ExtendedArtifact resolveReleased(String groupId, String artifactId, String version) {
        String id = toId(groupId, artifactId, version);
        ExtendedArtifact result = resolved.get(id);
        if (result == null) {
            for (ExtendedRepository r : configuration.getRepositoriesToResolveRelased()) {
                ExtendedArtifact a = (ExtendedArtifact) r.load(this, groupId, artifactId, version);
                if (a != null) {
                    for (Destination d : configuration.getDestinations()) {
                        d.copyFrom(this, r, a);
                    }
                    addResolved(a);
                    resolveDependencies(a);
                    return a;
                }
            }
        }
        return result;
    }

    protected ExtendedArtifact resolveSnapshot(String groupId, String artifactId, String version) {
        String id = toId(groupId, artifactId, version);
        ExtendedArtifact artifact = (ExtendedArtifact) resolved.get(id);
        if (artifact == null) {
            boolean resolvedFromSnapshotRepository = false;
            boolean resolvedFromRemoteRepository = false;
            Repository repo = null;
            for (ExtendedRepository r : configuration.getRepositoriesToResolveSnapshot()) {
                Type type = r.getType();
                if (type == Type.SNAPSHOT && resolvedFromSnapshotRepository || type == Type.REMOTE
                        && resolvedFromRemoteRepository) {
                    // パフォーマンス向上のため、1つのアーティファクトが複数のSNAPSHOTリポジトリ/複数のREMOTEリポジトリに存在することはないと仮定。
                    continue;
                }

                ExtendedArtifact a = (ExtendedArtifact) r.load(this, groupId, artifactId, version);
                if (a != null) {
                    if (type == Type.SNAPSHOT) {
                        resolvedFromSnapshotRepository = true;
                    } else if (type == Type.REMOTE) {
                        resolvedFromRemoteRepository = true;
                    }

                    if (artifact == null || ArtifactUtils.compareVersions(artifact, a) < 0) {
                        artifact = a;
                        repo = r;
                    }
                }
            }
            if (artifact != null) {
                for (Destination d : configuration.getDestinations()) {
                    d.copyFrom(this, repo, artifact);
                }
                addResolved(artifact);
                resolveDependencies(artifact);
            }
        }
        return artifact;
    }

    protected void resolveDependencies(Artifact a) {
        List<Artifact> list = new ArrayList<Artifact>();
        for (Artifact d : a.getDependencies()) {
            Artifact resolve = resolve(d.getGroupId(), d.getArtifactId(), d.getVersion());
            list.add(resolve);
        }
        a.getDependencies().clear();
        a.getDependencies().addAll(list);
    }

    public InputStream open(Artifact artifact, URL url) {
        URL from = findLocal(artifact, url);
        Constants.LOG.log(Level.INFO, "read from {0}", from); //$NON-NLS-1$
        InputStream result = UrlUtil.open(from);
        return result;
    }

    protected URL findLocal(Artifact artifact, URL url) {
        for (Destination d : configuration.getDestinations()) {
            File f = d.toDestination(artifact, url);
            if (f != null && f.exists()) {
                return UrlUtil.toURL(f);
            }
        }
        return url;
    }

    public void close(InputStream stream) {
        StreamUtils.close(stream);
    }

    public String getLatestVersion(String groupId, String artifactId, boolean containsSnapshot) {
        String id = toId(groupId, artifactId, containsSnapshot);
        String version = resolvedVersion.get(id);
        if (version == null) {
            for (Metadata metadata : resolveMetadatas(groupId, artifactId, containsSnapshot)) {
                String v = ArtifactUtils.getLatestVersion(metadata, containsSnapshot);
                if (ArtifactUtils.compareVersions(version, v) < 0) {
                    version = v;
                }
            }
            resolvedVersion.put(id, version);
        }
        return version;
    }

    public Metadata[] resolveMetadatas(String groupId, String artifactId, boolean containsSnapshot) {
        String id = toId(groupId, artifactId, containsSnapshot);
        Metadata[] metadatas = resolvedMetadatas.get(id);
        if (metadatas == null) {
            List<Metadata> list = new ArrayList<Metadata>();
            boolean resolvedFromSnapshotRepository = false;
            boolean resolvedFromRemoteRepository = false;
            for (ExtendedRepository r : configuration.getRepositoriesToGetLatestVersion(containsSnapshot)) {
                Type type = r.getType();
                if (type == Type.SNAPSHOT && resolvedFromSnapshotRepository || type == Type.REMOTE
                        && resolvedFromRemoteRepository) {
                    // パフォーマンス向上のため、1つのアーティファクトが複数のSNAPSHOTリポジトリ/複数のREMOTEリポジトリに存在することはないと仮定。
                    continue;
                }

                byte[] bytes = r.resolveMetadata(groupId, artifactId);
                if (bytes != null) {
                    if (type == Type.SNAPSHOT) {
                        resolvedFromSnapshotRepository = true;
                    } else if (type == Type.REMOTE) {
                        resolvedFromRemoteRepository = true;
                    }

                    Metadata metadata = ArtifactUtils.createMetadata(bytes);
                    if (metadata != null) {
                        list.add(metadata);
                    }
                }
            }
            metadatas = list.toArray(new Metadata[0]);
            resolvedMetadatas.put(id, metadatas);
        }
        return metadatas;
    }
}
