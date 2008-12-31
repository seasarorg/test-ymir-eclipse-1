package org.seasar.ymir.eclipse.maven.impl;

import java.net.URL;
import java.util.Properties;

import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.maven.ExtendedArtifact;
import org.seasar.ymir.eclipse.maven.ExtendedConfiguration;
import org.seasar.ymir.eclipse.maven.ExtendedContext;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.StAXArtifactBuilder;

public class ArtifactResolverImpl implements ArtifactResolver {
    private ExtendedConfiguration configuration;

    private ArtifactBuilder builder;

    public ArtifactResolverImpl() {
        configuration = new DefaultExtendedConfiguration(new Properties());
        builder = new StAXArtifactBuilder();
        addRemoteRepository("http://maven.seasar.org/maven2", false); //$NON-NLS-1$
        addRemoteRepository("http://maven.seasar.org/maven2-snapshot", true); //$NON-NLS-1$
    }

    public void addRemoteRepository(String url, boolean snapshot) {
        configuration.addRepository(new RemoteExtendedRepository(url, snapshot, builder));
    }

    public ExtendedContext newContext(boolean transitive) {
        return transitive ? new DefaultExtendedContext(configuration) : new NonTransitiveContext(configuration);
    }

    public ExtendedArtifact resolve(String groupId, String artifactId, String version, boolean transitive) {
        return resolve(newContext(transitive), groupId, artifactId, version);
    }

    public ExtendedArtifact resolve(ExtendedContext context, String groupId, String artifactId, String version) {
        return (ExtendedArtifact) context.resolve(groupId, artifactId, version);
    }

    public URL getURL(Artifact artifact) {
        String suffix = "/" + ArtifactUtils.toPath(artifact); //$NON-NLS-1$
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
