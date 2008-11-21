package org.seasar.ymir.eclipse.maven.impl;

import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.seasar.ymir.eclipse.maven.ExtendedRepository;
import org.seasar.ymir.eclipse.maven.Metadata;
import org.seasar.ymir.eclipse.maven.Versioning;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.common.util.UrlUtil;
import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Context;
import werkzeugkasten.mvnhack.repository.impl.ArtifactUtil;
import werkzeugkasten.mvnhack.repository.impl.RemoteRepository;

public class ExtendedRemoteRepository extends RemoteRepository implements ExtendedRepository {
    private static final Object NAME_MAVEN_METADATA = "maven-metadata.xml";

    private boolean snapshot;

    public ExtendedRemoteRepository(String url, boolean snapshot, ArtifactBuilder builder) {
        super(url, builder);
        this.snapshot = snapshot;
    }

    public byte[] resolveMetadata(String groupId, String artifactId) {
        return resolveMetadata(groupId, artifactId, null);
    }

    public byte[] resolveMetadata(String groupId, String artifactId, String version) {
        URL location = getMetadataLocation(groupId, artifactId, version);
        try {
            return StreamUtils.read(location);
        } catch (IOException ex) {
            return null;
        }
    }

    protected URL getMetadataLocation(String groupId, String artifactId, String version) {
        return toURL(toMetadataPath(groupId, artifactId, version));
    }

    protected String toMetadataPath(String groupId, String artifactId, String version) {
        char ps = '/';
        StringBuilder sb = new StringBuilder();
        sb.append(groupId.replace('.', ps));
        sb.append(ps);
        sb.append(artifactId);
        if (version != null) {
            sb.append(ps);
            sb.append(version);
        }
        sb.append(ps);
        sb.append(NAME_MAVEN_METADATA);
        return sb.toString();
    }

    public Type getType() {
        return snapshot ? Type.SNAPSHOT : Type.REMOTE;
    }

    @Override
    public Artifact load(Context context, String groupId, String artifactId, String version) {
        if (!ArtifactUtils.isSnapshot(version) || !(context instanceof DefaultExtendedContext)) {
            return ExtendedDefaultArtifact.newInstance(super.load(context, groupId, artifactId, version));
        }

        Metadata metadata = ArtifactUtils.createMetadata(resolveMetadata(groupId, artifactId, version));
        if (metadata == null) {
            return null;
        }

        String actualVersion = ArtifactUtils.resolveSnapshotVersion(version, metadata, 0L);
        long lastUpdated = 0L;
        if (actualVersion != null) {
            Versioning versioning = metadata.getVersioning();
            if (versioning != null) {
                if (versioning.getLastUpdated() != null) {
                    lastUpdated = versioning.getLastUpdated().longValue();
                }
            }
        }

        StringBuilder stb = new StringBuilder();
        stb.append(baseUrl);
        stb.append(ArtifactUtils.toPom(groupId, artifactId, version, actualVersion));
        URL url = UrlUtil.toURL(stb.toString());
        return ExtendedDefaultArtifact.newInstance(builder.build(context, context.open(ArtifactUtil.create(groupId,
                artifactId, version), url)), actualVersion, lastUpdated);
    }

    @Override
    public Set<URL> getLocation(Artifact artifact) {
        Set<URL> urls = new HashSet<URL>();
        urls.add(toURL(ArtifactUtils.toPath(artifact)));
        urls.add(toURL(ArtifactUtils.toPath(artifact, Constants.POM)));
        urls.add(toURL(ArtifactUtils.toPath(artifact, "-sources." + artifact.getType())));
        return urls;
    }
}
