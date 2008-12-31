package org.seasar.ymir.eclipse.maven.impl;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.HashSet;
import java.util.Set;

import net.skirnir.xom.ValidationException;

import org.seasar.ymir.vili.maven.ExtendedRepository;
import org.seasar.ymir.vili.maven.util.ArtifactUtils;
import org.seasar.ymir.vili.model.maven.Metadata;
import org.seasar.ymir.vili.model.maven.Snapshot;
import org.seasar.ymir.vili.model.maven.Versioning;
import org.seasar.ymir.vili.model.maven.Versions;
import org.seasar.ymir.vili.util.StreamUtils;
import org.seasar.ymir.vili.util.XOMUtils;

import werkzeugkasten.common.util.UrlUtil;
import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.ArtifactBuilder;
import werkzeugkasten.mvnhack.repository.Context;
import werkzeugkasten.mvnhack.repository.impl.LocalRepository;

public class LocalExtendedRepository extends LocalRepository implements ExtendedRepository {
    private static final Object NAME_MAVEN_METADATA = "maven-metadata-local.xml"; //$NON-NLS-1$

    private File root;

    public LocalExtendedRepository(File root, ArtifactBuilder builder) {
        super(root, builder);
        this.root = root;
    }

    public byte[] resolveMetadata(String groupId, String artifactId) {
        return resolveMetadata(groupId, artifactId, null);
    }

    public byte[] resolveMetadata(String groupId, String artifactId, String version) {
        URL location = getMetadataLocation(groupId, artifactId, version);
        if (location == null && version == null) {
            // version無指定でmetadataがない場合は、バージョン番号を取得するためのダミーのmetadataを生成する。
            File dir = new File(root, toArtifactDirectoryPath(groupId, artifactId));
            if (!dir.exists() || !dir.isDirectory()) {
                return null;
            }
            String latestVersion = null;
            long lastUpdated = 0L;
            for (File file : dir.listFiles()) {
                String v = file.getName();
                if (ArtifactUtils.compareVersions(v, latestVersion) > 0) {
                    latestVersion = v;
                    lastUpdated = file.lastModified();
                }
            }
            if (latestVersion == null) {
                return null;
            }
            Metadata metadata = new Metadata();
            metadata.setGroupId(groupId);
            metadata.setArtifactId(artifactId);
            metadata.setVersion(latestVersion);
            Versioning versioning = new Versioning();
            Versions versions = new Versions();
            versions.addVersion(latestVersion);
            versioning.setVersions(versions);
            versioning.setLastUpdatedDate(new Date(lastUpdated));
            metadata.setVersioning(versioning);
            StringWriter sw = new StringWriter();
            try {
                XOMUtils.getXOMapper().toXML(metadata, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException("Can't happen!", ex);
            } catch (IOException ex) {
                throw new RuntimeException("Can't happen!", ex);
            }
            try {
                return sw.toString().getBytes("UTF-8");
            } catch (UnsupportedEncodingException ex) {
                throw new RuntimeException("Can't happen!", ex);
            }
        } else {
            try {
                return StreamUtils.read(location);
            } catch (IOException ex) {
                return null;
            }
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

    private String toArtifactDirectoryPath(String groupId, String artifactId) {
        char ps = '/';
        StringBuilder sb = new StringBuilder();
        sb.append(groupId.replace('.', ps));
        sb.append(ps);
        sb.append(artifactId);
        return sb.toString();
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
                    Date date = versioning.getLastUpdatedDate();
                    if (date != null) {
                        lastUpdated = date.getTime();
                    }
                }
            }
        }

        return DefaultExtendedArtifact.newInstance(super.load(context, groupId, artifactId, version), actualVersion,
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
