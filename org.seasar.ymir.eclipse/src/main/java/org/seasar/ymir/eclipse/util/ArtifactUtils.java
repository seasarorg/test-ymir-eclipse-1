package org.seasar.ymir.eclipse.util;

import static org.seasar.ymir.eclipse.maven.ArtifactResolver.SUFFIX_SNAPSHOT;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.maven.ExtendedArtifact;
import org.seasar.ymir.eclipse.maven.Metadata;
import org.seasar.ymir.eclipse.maven.Snapshot;
import org.seasar.ymir.eclipse.maven.Versioning;

import werkzeugkasten.mvnhack.Constants;
import werkzeugkasten.mvnhack.repository.Artifact;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParserFactory;

public class ArtifactUtils {
    private static final Pattern PATTERN_COMPARE_VRESIONS_SEGMENT = Pattern.compile("(\\d+)(.*)");

    private static final Pattern PATTERN_COMPARE_VERSIONS_DELIMITER = Pattern.compile("(\\D+)(.*)");

    private static final Pattern PATTERN_GET_ARTIFACT_NAME_DELIMITER = Pattern.compile("-\\d");

    private static final String SNAPSHOT = "-SNAPSHOT";

    private ArtifactUtils() {
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
            Matcher matcher1 = PATTERN_COMPARE_VRESIONS_SEGMENT.matcher(version1);
            Matcher matcher2 = PATTERN_COMPARE_VRESIONS_SEGMENT.matcher(version2);
            if (matcher1.matches() && matcher2.matches()) {
                int cmp = (int) (Long.parseLong(matcher1.group(1)) - Long.parseLong(matcher2.group(1)));
                if (cmp != 0) {
                    return cmp;
                }
                version1 = matcher1.group(2);
                version2 = matcher2.group(2);

                // SNAPSHOTは正式リリースより前だが他のどのリリースよりも後。
                if (version1.equals(SNAPSHOT)) {
                    if (version2.equals(SNAPSHOT)) {
                        return 0;
                    } else if (version2.length() == 0) {
                        return -1;
                    } else {
                        return 1;
                    }
                } else if (version2.equals(SNAPSHOT)) {
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

    public static String getFileName(Artifact artifact) {
        return artifact.getArtifactId() + "-" + artifact.getVersion() + "." + artifact.getType();
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
        return artifact.getGroupId() + ":" + artifact.getArtifactId() + "-" + artifact.getVersion() + "."
                + artifact.getType();
    }

    public static String getUniqueId(Artifact artifact) {
        return getUniqueId(artifact.getGroupId(), artifact.getArtifactId());
    }

    public static String getUniqueId(String groupId, String artifactId) {
        return groupId + ":" + artifactId;
    }

    public static Metadata createMetadata(byte[] bytes) {
        if (bytes == null) {
            return null;
        }

        try {
            return Activator.getDefault().getXOMapper().toBean(
                    XMLParserFactory.newInstance().parse(
                            new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8")).getRootElement(),
                    Metadata.class);
        } catch (ValidationException ex) {
            return null;
        } catch (IllegalSyntaxException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }

    public static String toPom(String groupId, String artifactId, String version, String actualVersion) {
        return toPath(groupId, artifactId, version, actualVersion, Constants.POM);
    }

    public static String toPath(Artifact artifact) {
        String actualVersion = null;
        if (artifact instanceof ExtendedArtifact) {
            actualVersion = ((ExtendedArtifact) artifact).getActualVersion();
        }
        return toPath(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), actualVersion, "."
                + artifact.getType());
    }

    public static String toPath(Artifact artifact, String extension) {
        String actualVersion = null;
        if (artifact instanceof ExtendedArtifact) {
            actualVersion = ((ExtendedArtifact) artifact).getActualVersion();
        }
        return toPath(artifact.getGroupId(), artifact.getArtifactId(), artifact.getVersion(), actualVersion, extension);
    }

    public static String toPath(String groupId, String artifactId, String version, String actualVersion,
            String extension) {
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

    public static String resolveSnapshotVersion(String version, Metadata metadata, long localCopyLastUpdated) {
        Versioning versioning = metadata.getVersioning();
        if (versioning != null) {
            Snapshot snapshot = versioning.getSnapshot();
            if (snapshot != null) {
                String timestamp = snapshot.getTimestamp();
                Integer buildNumber = snapshot.getBuildNumber();
                if (timestamp != null && buildNumber != null) {
                    long lastUpdated = versioning.getLastUpdated() != null ? versioning.getLastUpdated().longValue()
                            : 0L;
                    if (lastUpdated > localCopyLastUpdated) {
                        return version.substring(0, version.length() - SUFFIX_SNAPSHOT.length()) + "-" + timestamp
                                + "-" + buildNumber;
                    }
                }
            }
        }
        return null;
    }

    public static boolean isSnapshot(String version) {
        return version != null && version.endsWith(SUFFIX_SNAPSHOT);
    }
}
