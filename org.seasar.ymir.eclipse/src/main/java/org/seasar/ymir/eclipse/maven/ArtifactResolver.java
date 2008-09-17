package org.seasar.ymir.eclipse.maven;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Properties;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.maven.impl.ExtendedConfiguration;
import org.seasar.ymir.eclipse.maven.impl.ExtendedRemoteRepository;
import org.seasar.ymir.eclipse.maven.impl.NonRecursiveContext;

import werkzeugkasten.common.util.StreamUtil;
import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Configuration;
import werkzeugkasten.mvnhack.repository.Context;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.DefaultContext;
import werkzeugkasten.mvnhack.repository.impl.StAXArtifactBuilder;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;

public class ArtifactResolver {
    private Configuration configuration;

    private ArtifactBuilder builder;

    private Context context;

    private Context nonRecursiveContext;

    private XOMapper mapper;

    public ArtifactResolver() {
        configuration = new ExtendedConfiguration(new Properties());
        builder = new StAXArtifactBuilder();
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2", builder));
        configuration.addRepository(new ExtendedRemoteRepository("http://maven.seasar.org/maven2-snapshot", builder));
        context = new DefaultContext(configuration);
        nonRecursiveContext = new NonRecursiveContext(configuration);

        mapper = Activator.getDefault().getXOMapper();
    }

    public Artifact resolve(String groupId, String artifactId, String version, boolean recursive) {
        if (recursive) {
            return context.resolve(groupId, artifactId, version);
        } else {
            return nonRecursiveContext.resolve(groupId, artifactId, version);
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
        String latestVersion = null;
        long lastUpdatedTime = 0L;
        for (Repository r : configuration.getRepositories()) {
            if (r instanceof ExtendedRepository) {
                URL url = ((ExtendedRepository) r).getMetadataLocation(groupId, artifactId);
                Metadata metadata = readMetadata(url);
                if (metadata != null) {
                    if (latestVersion == null) {
                        latestVersion = metadata.getVersion();
                    } else {
                        Versioning versioning = metadata.getVersioning();
                        if (versioning != null) {
                            Long lastUpdated = versioning.getLastUpdated();
                            if (lastUpdated != null && lastUpdated.longValue() > lastUpdatedTime) {
                                latestVersion = metadata.getVersion();
                            }
                        }
                    }
                }
            }
        }
        return latestVersion;
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
        try {
            ByteArrayOutputStream os = new ByteArrayOutputStream();
            StreamUtil.copy(is, os);
            return (Metadata) mapper.toBean(XMLParserFactory.newInstance().parse(
                    new InputStreamReader(new ByteArrayInputStream(os.toByteArray()), "UTF-8")).getRootElement(),
                    Metadata.class);
        } catch (ValidationException ex) {
            return null;
        } catch (IllegalSyntaxException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            try {
                is.close();
            } catch (IOException ignore) {
            }
        }
    }

}
