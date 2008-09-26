package org.seasar.ymir.eclipse.maven;

import java.net.URL;
import java.util.Properties;

import org.seasar.ymir.eclipse.maven.impl.ExtendedConfiguration;
import org.seasar.ymir.eclipse.maven.impl.ExtendedContext;
import org.seasar.ymir.eclipse.maven.impl.ExtendedRemoteRepository;
import org.seasar.ymir.eclipse.maven.impl.NonTransitiveContext;
import org.seasar.ymir.eclipse.util.ArtifactUtils;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Configuration;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.StAXArtifactBuilder;

public class ArtifactResolver {
    public static final String SNAPSHOT = "SNAPSHOT";

    public static final String SUFFIX_SNAPSHOT = "-" + SNAPSHOT;

    private Configuration configuration;

    private ArtifactBuilder builder;

    public ArtifactResolver() {
        configuration = new ExtendedConfiguration(new Properties());
        builder = new StAXArtifactBuilder();
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2", false, builder));
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2-snapshot", false,
                builder));
    }

    public ExtendedContext newContext(boolean transitive) {
        return transitive ? new ExtendedContext(configuration) : new NonTransitiveContext(configuration);
    }

    public Artifact resolve(String groupId, String artifactId, String version, boolean transitive) {
        return newContext(transitive).resolve(groupId, artifactId, version);
    }

    public Artifact resolve(ExtendedContext context, String groupId, String artifactId, String version) {
        return context.resolve(groupId, artifactId, version);
    }

    public URL getURL(Artifact artifact) {
        String suffix = "." + artifact.getType();
        for (Repository repo : configuration.getRepositories()) {
            for (URL url : repo.getLocation(artifact)) {
                if (url.toExternalForm().endsWith(suffix)) {
                    return url;
                }
            }
        }
        return null;
    }

    public String getLatestVersion(String groupId, String artifactId, boolean containsSnapshot) {
        return getLatestVersion(new NonTransitiveContext(configuration), groupId, artifactId, containsSnapshot);

    }

    public String getLatestVersion(ExtendedContext context, String groupId, String artifactId, boolean containsSnapshot) {
        String version = null;
        if (containsSnapshot) {
            for (Metadata metadata : context.resolveMetadatas(groupId, artifactId, true)) {
                String v = getLatestVersion(metadata, true);
                if (ArtifactUtils.compareVersions(version, v) < 0) {
                    version = v;
                }
            }
        } else {
            Metadata metadata = context.resolveMetadata(groupId, artifactId, false);
            if (metadata != null) {
                version = getLatestVersion(metadata, false);
            }
        }
        return version;
    }

    String getLatestVersion(Metadata metadata, boolean containsSnapshot) {
        String version = null;
        Versioning versioning = metadata.getVersioning();
        if (versioning != null) {
            version = versioning.getRelease();
            if (!containsSnapshot && version != null && ArtifactUtils.isSnapshot(version)) {
                version = null;
            }
        }
        if (version == null) {
            Versions versions = versioning.getVersions();
            if (versions != null) {
                String[] vs = versions.getVersions();
                for (int i = vs.length - 1; i >= 0 && version == null; i--) {
                    version = vs[i];
                    if (!containsSnapshot && ArtifactUtils.isSnapshot(version)) {
                        version = null;
                    }
                }
            }
        }
        if (version == null) {
            version = metadata.getVersion();
            if (!containsSnapshot && version != null && ArtifactUtils.isSnapshot(version)) {
                version = null;
            }
        }
        return version;
    }
}
