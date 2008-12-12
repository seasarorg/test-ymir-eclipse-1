package org.seasar.ymir.eclipse.maven.impl;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

import org.seasar.ymir.eclipse.maven.ExtendedRepository;
import org.seasar.ymir.eclipse.maven.Metadata;
import org.seasar.ymir.eclipse.maven.Snapshot;
import org.seasar.ymir.eclipse.maven.Versioning;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.common.util.UrlUtil;
import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Context;
import werkzeugkasten.mvnhack.repository.impl.LocalRepository;

public class ExtendedLocalRepository extends LocalRepository implements ExtendedRepository {
    private static final Object NAME_MAVEN_METADATA = "maven-metadata-local.xml"; //$NON-NLS-1$

    private File root;

    public ExtendedLocalRepository(File root, ArtifactBuilder builder) {
        super(root, builder);
        this.root = root;
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
        File metadata = new File(root, toMetadataPath(groupId, artifactId, version));
        if (metadata.exists()) {
            try {
                return metadata.toURI().toURL();
            } catch (MalformedURLException ex) {
                return null;
            }
        } else {
            return null;
        }
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
        return Type.LOCAL;
    }

    @Override
    public Artifact load(Context context, String groupId, String artifactId, String version) {
        String actualVersion = null;
        long lastUpdated = 0L;
        if (ArtifactUtils.isSnapshot(version)) {
            Metadata metadata = ArtifactUtils.createMetadata(resolveMetadata(groupId, artifactId, version));
            if (metadata == null) {
                return null;
            }

            Versioning versioning = metadata.getVersioning();
            if (versioning != null) {
                Snapshot snapshot = versioning.getSnapshot();
                if (snapshot != null && snapshot.isLocalCopy()) {
                    actualVersion = version;
                    if (versioning.getLastUpdated() != null) {
                        lastUpdated = versioning.getLastUpdated().longValue();
                    }
                }
            }
        }

        return ExtendedDefaultArtifact.newInstance(super.load(context, groupId, artifactId, version), actualVersion,
                lastUpdated);
    }

    @Override
    public Set<URL> getLocation(Artifact artifact) {
        Set<URL> urls = new HashSet<URL>();
        urls.add(UrlUtil.toURL(new File(root.getAbsolutePath(), ArtifactUtils.toPath(artifact))));
        urls.add(UrlUtil.toURL(new File(root.getAbsolutePath(), ArtifactUtils.toPath(artifact, Constants.POM))));
        urls.add(UrlUtil.toURL(new File(root.getAbsolutePath(), ArtifactUtils.toPath(artifact,
                "-sources." + artifact.getType())))); //$NON-NLS-1$
        return urls;
    }
}
