package org.seasar.ymir.eclipse.maven;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Properties;

import org.seasar.ymir.eclipse.maven.impl.ExtendedConfiguration;
import org.seasar.ymir.eclipse.maven.impl.ExtendedContext;
import org.seasar.ymir.eclipse.maven.impl.ExtendedRemoteRepository;
import org.seasar.ymir.eclipse.maven.impl.NonTransitiveContext;
import org.seasar.ymir.eclipse.util.ArtifactUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Configuration;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.StAXArtifactBuilder;

public class ArtifactResolver {
    private Configuration configuration;

    private ArtifactBuilder builder;

    private ExtendedContext context;

    private ExtendedContext nonTransitiveContext;

    public ArtifactResolver() {
        configuration = new ExtendedConfiguration(new Properties());
        builder = new StAXArtifactBuilder();
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2", builder));
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2-snapshot", builder));
        context = new ExtendedContext(configuration);
        nonTransitiveContext = new NonTransitiveContext(configuration);
    }

    public Artifact resolve(String groupId, String artifactId, String version, boolean transitive) {
        if (transitive) {
            return context.resolve(groupId, artifactId, version);
        } else {
            return nonTransitiveContext.resolve(groupId, artifactId, version);
        }
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

    public String getLatestVersion(String groupId, String artifactId) {
        Metadata metadata = context.resolveMetadata(groupId, artifactId);
        if (metadata != null) {
            return metadata.getVersion();
        }
        return null;
    }

    Metadata readMetadata(URL url) {
        if (url == null) {
            return null;
        }

        InputStream is;
        try {
            is = url.openStream();
        } catch (IOException ex) {
            return null;
        }

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        try {
            StreamUtils.copyStream(is, os);
        } catch (IOException ex) {
            return null;
        } finally {
            StreamUtils.close(is);
        }
        return ArtifactUtils.createMetadata(os.toByteArray());
    }

}
