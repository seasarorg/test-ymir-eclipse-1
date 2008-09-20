package org.seasar.ymir.eclipse.maven.impl;

import java.io.IOException;
import java.net.URL;

import org.seasar.ymir.eclipse.maven.ExtendedRepository;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.impl.RemoteRepository;

public class ExtendedRemoteRepository extends RemoteRepository implements ExtendedRepository {
    private static final Object NAME_MAVEN_METADATA = "maven-metadata.xml";

    public ExtendedRemoteRepository(String url, ArtifactBuilder builder) {
        super(url, builder);
    }

    public byte[] resolveMetadata(String groupId, String artifactId) {
        URL location = getMetadataLocation(groupId, artifactId);
        try {
            return StreamUtils.read(location);
        } catch (IOException ex) {
            return null;
        }
    }

    protected URL getMetadataLocation(String groupId, String artifactId) {
        return toURL(toMetadataPath(groupId, artifactId));
    }

    protected String toMetadataPath(String groupId, String artifactId) {
        char ps = '/';
        StringBuilder sb = new StringBuilder();
        sb.append(groupId.replace('.', ps));
        sb.append(ps);
        sb.append(artifactId);
        sb.append(ps);
        sb.append(NAME_MAVEN_METADATA);
        return sb.toString();
    }
}
