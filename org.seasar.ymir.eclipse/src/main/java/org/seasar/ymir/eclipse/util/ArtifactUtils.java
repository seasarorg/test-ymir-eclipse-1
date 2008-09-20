package org.seasar.ymir.eclipse.util;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParserFactory;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.maven.Metadata;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ArtifactUtils {
    private static final Pattern PATTERN_COMPARE_VRESIONS_SEGMENT = Pattern.compile("(\\d+)(.*)");

    private static final Pattern PATTERN_COMPARE_VERSIONS_DELIMITER = Pattern.compile("(\\D+)(.*)");

    private static final Pattern PATTERN_GET_ARTIFACT_NAME_DELIMITER = Pattern.compile("-\\d");

    private static final String SNAPSHOT = "-SNAPSHOT";

    private ArtifactUtils() {
    }

    public static int compareVersions(String version1, String version2) {
        while (true) {
            Matcher matcher1 = PATTERN_COMPARE_VRESIONS_SEGMENT.matcher(version1);
            Matcher matcher2 = PATTERN_COMPARE_VRESIONS_SEGMENT.matcher(version2);
            if (matcher1.matches() && matcher2.matches()) {
                int cmp = Integer.parseInt(matcher1.group(1)) - Integer.parseInt(matcher2.group(1));
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
            return (Metadata) Activator.getDefault().getXOMapper().toBean(
                    XMLParserFactory.newInstance().parse(
                            new InputStreamReader(new ByteArrayInputStream(bytes), "UTF-8"))
                            .getRootElement(), Metadata.class);
        } catch (ValidationException ex) {
            return null;
        } catch (IllegalSyntaxException ex) {
            return null;
        } catch (IOException ex) {
            return null;
        }
    }
}
