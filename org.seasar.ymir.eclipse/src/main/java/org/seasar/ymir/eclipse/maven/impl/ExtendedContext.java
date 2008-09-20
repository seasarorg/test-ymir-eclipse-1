package org.seasar.ymir.eclipse.maven.impl;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import org.seasar.ymir.eclipse.maven.ExtendedRepository;
import org.seasar.ymir.eclipse.maven.Metadata;
import org.seasar.ymir.eclipse.util.ArtifactUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.Configuration;
import werkzeugkasten.mvnhack.repository.Destination;
import werkzeugkasten.mvnhack.repository.Repository;
import werkzeugkasten.mvnhack.repository.impl.DefaultContext;
import werkzeugkasten.mvnhack.repository.impl.RemoteRepository;

public class ExtendedContext extends DefaultContext {
    private static final String SUFFIX_SNAPSHOT = "-SNAPSHOT";

    public ExtendedContext(Configuration configuration) {
        super(configuration);
    }

    @Override
    public Artifact resolve(String groupId, String artifactId, String version) {
        if (version.endsWith(SUFFIX_SNAPSHOT)) {
            resolved.remove(toId(groupId, artifactId, version));
        }
        return super.resolve(groupId, artifactId, version);
    }

    public Metadata resolveMetadata(String groupId, String artifactId) {
        for (Repository r : configuration.getRepositories()) {
            if (!(r instanceof ExtendedRepository)) {
                continue;
            }
            ExtendedRepository re = (ExtendedRepository) r;
            byte[] bytes = re.resolveMetadata(groupId, artifactId);
            if (bytes != null) {
                if (re instanceof RemoteRepository) {
                    for (Destination d : configuration.getDestinations()) {
                        if (d instanceof ExtendedLocalRepository) {
                            ExtendedLocalRepository local = (ExtendedLocalRepository) d;
                            File file = local.getMetadataFile(groupId, artifactId);
                            file.getParentFile().mkdirs();
                            try {
                                StreamUtils.copyStream(new ByteArrayInputStream(bytes), new FileOutputStream(file));
                            } catch (IOException ignore) {
                            }
                        }
                    }
                }
                return ArtifactUtils.createMetadata(bytes);
            }
        }
        return null;
    }
}
