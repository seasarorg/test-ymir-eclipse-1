package org.seasar.ymir.eclipse.maven;

import java.net.URL;
import java.util.Properties;

import org.seasar.ymir.eclipse.maven.impl.DefaultExtendedConfiguration;
import org.seasar.ymir.eclipse.maven.impl.DefaultExtendedContext;
import org.seasar.ymir.eclipse.maven.impl.ExtendedRemoteRepository;
import org.seasar.ymir.eclipse.maven.impl.NonTransitiveContext;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.StAXArtifactBuilder;

public class ArtifactResolver {
    public static final String SNAPSHOT = "SNAPSHOT";

    public static final String SUFFIX_SNAPSHOT = "-" + SNAPSHOT;

    private ExtendedConfiguration configuration;

    private ArtifactBuilder builder;

    public ArtifactResolver() {
        configuration = new DefaultExtendedConfiguration(new Properties());
        builder = new StAXArtifactBuilder();
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2", false, builder));
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2-snapshot", true,
                builder));
    }

    public ExtendedContext newContext(boolean transitive) {
        return transitive ? new DefaultExtendedContext(configuration) : new NonTransitiveContext(configuration);
    }

    public Artifact resolve(String groupId, String artifactId, String version, boolean transitive) {
        return resolve(newContext(transitive), groupId, artifactId, version);
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
        return getLatestVersion(newContext(false), groupId, artifactId, containsSnapshot);

    }

    public String getLatestVersion(ExtendedContext context, String groupId, String artifactId, boolean containsSnapshot) {
        return context.getLatestVersion(groupId, artifactId, containsSnapshot);
    }

    public void setOffline(boolean offline) {
        configuration.setOffline(offline);
    }
}
