package org.seasar.ymir.eclipse.maven.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.URL;
import java.util.Date;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.maven.ExtendedArtifact;
import org.seasar.ymir.eclipse.util.JarUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.eclipse.util.URLUtils;
import org.seasar.ymir.eclipse.util.XOMUtils;
import org.seasar.ymir.vili.Activator;
import org.seasar.ymir.vili.Globals;
import org.seasar.ymir.vili.model.maven.Metadata;
import org.seasar.ymir.vili.model.maven.Snapshot;
import org.seasar.ymir.vili.model.maven.Versioning;
import org.seasar.ymir.vili.model.maven.Versions;

import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.Artifact;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;

public class ArtifactUtils {
    private static final Pattern PATTERN_COMPARE_VRESIONS_SEGMENT = Pattern
            .compile("([^\\.-]+)(.*)"); //$NON-NLS-1$

    private static final Pattern PATTERN_COMPARE_VERSIONS_DELIMITER = Pattern
            .compile("([\\.-]+)(.*)"); //$NON-NLS-1$

    private static final Pattern PATTERN_GET_ARTIFACT_NAME_DELIMITER = Pattern
            .compile("-\\d"); //$NON-NLS-1$

    private static final String SUFFIX_SNAPSHOT = "-SUFFIX_SNAPSHOT"; //$NON-NLS-1$

    private ArtifactUtils() {
    }

    public static int compareVersions(ExtendedArtifact artifact1,
            ExtendedArtifact artifact2) {
        int cmp = compareVersions(artifact1.getVersion(), artifact2
                .getVersion());
        if (cmp == 0 && isSnapshot(artifact1.getVersion())) {
            // 同じバージョンでかつSNAPSHOTの場合はlastUpdatedを比較する。
            // actualVersionはリモートリポジトリの場合はXXX-YYYYMMDD.HHMMSS-B形式で取れるのに対して
            // ローカルリポジトリの場合XXX-SNAPSHOT形式で取れてしまうため、actualVersionの比較では
            // リモートにローカルより新しいSNAPSHOTがあった場合に正しい比較が行なわれない。
            cmp = (int) (artifact1.getLastUpdated() - artifact2
                    .getLastUpdated());
        }
        return cmp;
    }

    public static int compareVersions(String version1, String version2) {
        if (version1 == null) {
            if (version2 == null) {
                return 0;
            } else {
                return -1;
            }
        } else {
            if (version2 == null) {
                return 1;
            }
        }

        while (true) {
            Matcher matcher1 = PATTERN_COMPARE_VRESIONS_SEGMENT
                    .matcher(version1);
            Matcher matcher2 = PATTERN_COMPARE_VRESIONS_SEGMENT
                    .matcher(version2);
            if (matcher1.matches() && matcher2.matches()) {
                int cmp = compareSegment(matcher1.group(1), matcher2.group(1));
                if (cmp != 0) {
                    return cmp;
                }
                version1 = matcher1.group(2);
                version2 = matcher2.group(2);

                // SNAPSHOTは正式リリースより前だが他のどのリリースよりも後。
                if (version1.equals(SUFFIX_SNAPSHOT)) {
                    if (version2.equals(SUFFIX_SNAPSHOT)) {
                        return 0;
                    } else if (version2.length() == 0) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (version2.equals(SUFFIX_SNAPSHOT)) {
                    if (version1.length() == 0) {
                        return 1;
                    } else {
                        return -1;
                    }
                }

                matcher1 = PATTERN_COMPARE_VERSIONS_DELIMITER.matcher(version1);
                matcher2 = PATTERN_COMPARE_VERSIONS_DELIMITER.matcher(version2);
                if (matcher1.matches() && matcher2.matches()) {
                    cmp = matcher1.group(1).compareTo(matcher2.group(1));
                    if (cmp != 0) {
                        return cmp;
                    }
                    version1 = matcher1.group(2);
                    version2 = matcher2.group(2);
                } else {
                    return version1.compareTo(version2);
                }
            } else {
                return version1.compareTo(version2);
            }
        }
    }

    static int compareSegment(String segment1, String segment2) {
        long long1 = 0L;
        boolean isSegment1Number = false;
        try {
            long1 = Long.parseLong(segment1);
            isSegment1Number = true;
        } catch (NumberFormatException ignore) {
        }
        long long2 = 0L;
        boolean isSegment2Number = false;
        try {
            long2 = Long.parseLong(segment2);
            isSegment2Number = true;
        } catch (NumberFormatException ignore) {
        }
        if (isSegment1Number) {
            if (isSegment2Number) {
                return (int) (long1 - long2);
            } else {
                return 1;
            }
        } else {
            if (isSegment2Number) {
                return -1;
            } else {
                return segment1.compareTo(segment2);
            }
        }
    }

    public static String getFileName(Artifact artifact) {
        return artifact.getArtifactId()
                + "-" + artifact.getVersion() + "." + artifact.getType(); //$NON-NLS-1$ //$NON-NLS-2$
    }

    public static String getArtifactId(String path) {
        int slash = path.lastIndexOf('/');
        String name;
        if (slash < 0) {
            name = path;
        } else {
            name = path.substring(slash + 1);
        }

        int dot = name.lastIndexOf('.');
        if (dot >= 0) {
            name = name.substring(0, dot);
        }

        Matcher matcher = PATTERN_GET_ARTIFACT_NAME_DELIMITER.matcher(name);
        if (matcher.find()) {
            return name.substring(0, matcher.start());
        } else {
            return name;
        }
    }

    public static String getId(Artifact artifact) {
        return artifact.getGroupId()
                + ":" + artifact.getArtifactId() + "-" + artifact.getVersion() + "." //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                + artifact.getType();
    }

    public static String getUniqueId(Artifact artifact) {
        return getUniqueId(artifact.getGroupId(), artifact.getArtifactId());
    }

    public static String getUniqueId(String groupId, String artifactId) {
        return groupId + ":" + artifactId; //$NON-NLS-1$
    }

    public static Metadata createMetadata(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            return XOMUtils.getXOMapper().toBean(
                    XOMUtils.getXMLParser().parse(
                            new InputStreamReader(new ByteArrayInputStream(
                                    bytes), "UTF-8")).getRootElement(), //$NON-NLS-1$
                    Metadata.class);
        } catch (ValidationException ex) {
            return null;
        } catch (IllegalSyntaxException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public static String toPom(String groupId, String artifactId,
            String version, String actualVersion) {
        return toPath(groupId, artifactId, version, actualVersion,
                Constants.POM);
    }

    public static String toPath(Artifact artifact) {
        String actualVersion = null;
        if (artifact instanceof ExtendedArtifact) {
            actualVersion = ((ExtendedArtifact) artifact).getActualVersion();
        }
        return toPath(artifact.getGroupId(), artifact.getArtifactId(), artifact
                .getVersion(), actualVersion, "." //$NON-NLS-1$
                + artifact.getType());
    }

    public static String toPath(Artifact artifact, String extension) {
        String actualVersion = null;
        if (artifact instanceof ExtendedArtifact) {
            actualVersion = ((ExtendedArtifact) artifact).getActualVersion();
        }
        return toPath(artifact.getGroupId(), artifact.getArtifactId(), artifact
                .getVersion(), actualVersion, extension);
    }

    public static String toPath(String groupId, String artifactId,
            String version, String actualVersion, String extension) {
        char ps = '/';
        StringBuilder stb = new StringBuilder();
        stb.append(groupId.replace('.', '/'));
        stb.append(ps);
        stb.append(artifactId);
        stb.append(ps);
        stb.append(version);
        stb.append(ps);
        stb.append(artifactId);
        stb.append('-');
        stb.append(actualVersion != null ? actualVersion : version);
        stb.append(extension);
        return stb.toString();
    }

    public static String resolveSnapshotVersion(String version,
            Metadata metadata, long localCopyLastUpdated) {
        Versioning versioning = metadata.getVersioning();
        if (versioning != null) {
            Snapshot snapshot = versioning.getSnapshot();
            if (snapshot != null) {
                String timestamp = snapshot.getTimestamp();
                Integer buildNumber = snapshot.getBuildNumber();
                if (timestamp != null && buildNumber != null) {
                    Date date = versioning.getLastUpdatedDate();
                    long lastUpdated = date != null ? date.getTime() : 0L;
                    if (lastUpdated > localCopyLastUpdated) {
                        return version.substring(0, version.length()
                                - SUFFIX_SNAPSHOT.length())
                                + "-" + timestamp //$NON-NLS-1$
                                + "-" + buildNumber; //$NON-NLS-1$
                    }
                }
            }
        }
        return null;
    }

    public static boolean isSnapshot(String version) {
        return version != null && version.endsWith(SUFFIX_SNAPSHOT);
    }

    public static String getLatestVersion(Metadata metadata,
            boolean containsSnapshot) {
        String version = null;
        Versioning versioning = metadata.getVersioning();
        if (versioning != null) {
            version = versioning.getRelease();
            if (!containsSnapshot && version != null
                    && ArtifactUtils.isSnapshot(version)) {
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
            if (!containsSnapshot && version != null
                    && ArtifactUtils.isSnapshot(version)) {
                version = null;
            }
        }
        return version;
    }

    // TODO JarInputStreamを使って一度ファイルシステムにコピーしないようにできるか検討する。
    public static File getFile(Artifact artifact) throws IOException {
        URL artifactURL = Activator.getArtifactResolver().getURL(artifact);
        File artifactFile = URLUtils.toFile(artifactURL);
        if (artifactFile == null) {
            artifactFile = File.createTempFile("ymir", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
            artifactFile.deleteOnExit();
            InputStream is = null;
            OutputStream os = null;
            try {
                is = artifactURL.openStream();
                os = new FileOutputStream(artifactFile);
                System.out
                        .println("URL=" + artifactURL + ", file=" + artifactFile); //$NON-NLS-1$ //$NON-NLS-2$
                StreamUtils.copyStream(is, os);
            } finally {
                StreamUtils.close(is);
                StreamUtils.close(os);
            }
        }
        return artifactFile;
    }

    // TODO JarInputStreamを使って一度ファイルシステムにコピーしないようにできるか検討する。
    public static JarFile getJarFile(Artifact artifact) throws CoreException {
        try {
            return new JarFile(getFile(artifact));
        } catch (IOException ex) {
            throw new CoreException(new Status(IStatus.ERROR,
                    Globals.PLUGIN_ID, "Can't create JarFile instance: "
                            + artifact, ex));
        }
    }

    public static MapProperties getPropertiesResource(Artifact artifact,
            String path, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("Activator.58"), 1); //$NON-NLS-1$

        JarFile jarFile = null;
        try {
            jarFile = getJarFile(artifact);
            if (jarFile == null) {
                return null;
            }

            return JarUtils.getPropertiesResource(jarFile, path,
                    new SubProgressMonitor(monitor, 1));
        } finally {
            StreamUtils.close(jarFile);
            monitor.done();
        }
    }

    public static boolean exists(Artifact artifact, String path)
            throws CoreException {
        JarFile jarFile = null;
        try {
            jarFile = getJarFile(artifact);

            return jarFile.getJarEntry(path) != null;
        } finally {
            StreamUtils.close(jarFile);
        }
    }

    public static String getResourceAsString(Artifact artifact, String path,
            String encoding, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("Activator.55"), 1); //$NON-NLS-1$

        JarFile jarFile = null;
        InputStream is = null;
        try {
            jarFile = ArtifactUtils.getJarFile(artifact);

            JarEntry entry = jarFile.getJarEntry(path);
            if (entry == null) {
                return null;
            }

            is = jarFile.getInputStream(entry);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamUtils.copyStream(is, baos);
            return new String(baos.toByteArray(), encoding);
        } catch (IOException ex) {
            Activator
                    .throwCoreException(
                            "Can't read resource: artifact=" + artifact + ", path=" + path, ex); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } finally {
            StreamUtils.close(is);
            StreamUtils.close(jarFile);
            monitor.done();
        }
    }
}
