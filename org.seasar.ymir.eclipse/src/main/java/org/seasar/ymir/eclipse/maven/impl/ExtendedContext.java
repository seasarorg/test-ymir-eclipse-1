package org.seasar.ymir.eclipse.maven.impl;

import java.util.ArrayList;
import java.util.List;

import org.seasar.ymir.eclipse.maven.ExtendedArtifact;
import org.seasar.ymir.eclipse.maven.ExtendedRepository;
import org.seasar.ymir.eclipse.maven.Metadata;
import org.seasar.ymir.eclipse.util.ArtifactUtils;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.Configuration;
import werkzeugkasten.mvnhack.repository.Destination;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.DefaultContext;

public class ExtendedContext extends DefaultContext {
    public ExtendedContext(Configuration configuration) {
        super(configuration);
    }

    @Override
    public Artifact resolve(String groupId, String artifactId, String version) {
        if (ArtifactUtils.isSnapshot(version)) {
            return resolveSnapshot(groupId, artifactId, version);
        } else {
            return super.resolve(groupId, artifactId, version);
        }
    }

    protected Artifact resolveSnapshot(String groupId, String artifactId, String version) {
        String id = toId(groupId, artifactId, version);
        ExtendedArtifact artifact = (ExtendedArtifact) resolved.get(id);
        if (artifact == null) {
            Repository repo = null;
            for (Repository r : configuration.getRepositories()) {
                ExtendedArtifact a = (ExtendedArtifact) r.load(this, groupId, artifactId, version);
                if (a != null) {
                    if (artifact == null || artifact.getLastUpdated() < a.getLastUpdated()) {
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

    public Metadata resolveMetadata(String groupId, String artifactId, boolean containsSnapshot) {
        Metadata[] metadatas = resolveMetadatas(groupId, artifactId, containsSnapshot, true);
        if (metadatas.length > 0) {
            return metadatas[0];
        } else {
            return null;
        }
    }

    public Metadata[] resolveMetadatas(String groupId, String artifactId, boolean containsSnapshot) {
        return resolveMetadatas(groupId, artifactId, containsSnapshot, false);
    }

    protected Metadata[] resolveMetadatas(String groupId, String artifactId, boolean containsSnapshot, boolean onlyOne) {
        List<Metadata> list = new ArrayList<Metadata>();
        for (Repository r : configuration.getRepositories()) {
            if (!(r instanceof ExtendedRepository)) {
                continue;
            }

            ExtendedRepository re = (ExtendedRepository) r;
            if (!containsSnapshot && re.isSnapshot()) {
                continue;
            }

            byte[] bytes = re.resolveMetadata(groupId, artifactId);
            if (bytes != null) {
                // Mavenはリモートからメタデータをコピーしてきていないようなのでコメントアウト。
                // if (re instanceof RemoteRepository) {
                // for (Destination d : configuration.getDestinations()) {
                // if (d instanceof ExtendedLocalRepository) {
                // ExtendedLocalRepository local = (ExtendedLocalRepository) d;
                // File file = local.getMetadataFile(groupId, artifactId);
                // file.getParentFile().mkdirs();
                // try {
                // StreamUtils.copyStream(new ByteArrayInputStream(bytes), new FileOutputStream(file));
                // } catch (IOException ignore) {
                // }
                // }
                // }
                // }
                Metadata metadata = ArtifactUtils.createMetadata(bytes);
                if (metadata != null) {
                    list.add(metadata);
                    if (onlyOne) {
                        break;
                    }
                }
            }
        }
        return list.toArray(new Metadata[0]);
    }

    public Metadata resolveMetadata(String groupId, String artifactId, String version) {
        Metadata[] metadatas = resolveMetadatas(groupId, artifactId, version, true);
        if (metadatas.length > 0) {
            return metadatas[0];
        } else {
            return null;
        }
    }

    public Metadata[] resolveMetadatas(String groupId, String artifactId, String version) {
        return resolveMetadatas(groupId, artifactId, version, false);
    }

    protected Metadata[] resolveMetadatas(String groupId, String artifactId, String version, boolean onlyOne) {
        boolean snapshot = ArtifactUtils.isSnapshot(version);

        List<Metadata> list = new ArrayList<Metadata>();
        for (Repository r : configuration.getRepositories()) {
            if (!(r instanceof ExtendedRepository)) {
                continue;
            }

            ExtendedRepository re = (ExtendedRepository) r;

            if (!snapshot && re.isSnapshot()) {
                continue;
            }

            byte[] bytes = re.resolveMetadata(groupId, artifactId, version);
            if (bytes != null) {
                Metadata metadata = ArtifactUtils.createMetadata(bytes);
                if (metadata != null) {
                    list.add(metadata);
                    if (onlyOne) {
                        break;
                    }
                }
            }
        }
        return list.toArray(new Metadata[0]);
    }
}
