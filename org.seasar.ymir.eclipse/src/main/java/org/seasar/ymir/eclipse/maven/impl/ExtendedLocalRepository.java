package org.seasar.ymir.eclipse.maven.impl;

import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;

import org.seasar.ymir.eclipse.maven.ExtendedRepository;

import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.impl.LocalRepository;

public class ExtendedLocalRepository extends LocalRepository implements ExtendedRepository {
    private static final Object NAME_MAVEN_METADATA = "maven-metadata-local.xml";

    private File root;

    public ExtendedLocalRepository(File root, ArtifactBuilder builder) {
        super(root, builder);
        this.root = root;
    }

    public URL getMetadataLocation(String groupId, String artifactId) {
        File metadata = new File(root, toMetadataPath(groupId, artifactId));
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

    public String toMetadataPath(String groupId, String artifactId) {
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
