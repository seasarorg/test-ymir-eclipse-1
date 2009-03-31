package org.seasar.ymir.vili.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;

import org.seasar.ymir.vili.maven.ArtifactVersion;

public class ViliUtils {
    public static final String LS = System.getProperty("line.separator");

    private ViliUtils() {
    }

    public static boolean isCompatible(String versionOfPlugin,
            String versionOfArtifact) {
        if (versionOfArtifact == null) {
            return false;
        }

        return isCompatible(new ArtifactVersion(versionOfPlugin),
                new ArtifactVersion(versionOfArtifact));
    }

    public static boolean isCompatible(ArtifactVersion versionOfPlugin,
            ArtifactVersion versionOfArtifact) {
        if (versionOfArtifact == null) {
            return false;
        }

        if (versionOfPlugin.getMajorVersion() != versionOfArtifact
                .getMajorVersion()
                || versionOfPlugin.getMinorVersion() != versionOfArtifact
                        .getMinorVersion()) {
            return false;
        }

        return versionOfPlugin.getIncrementalVersion() >= versionOfArtifact
                .getIncrementalVersion();
    }

    public static String addIndent(Object obj, String indent) {
        if (obj == null) {
            return null;
        }
        String text = obj.toString();
        if (text.length() == 0) {
            return text;
        }

        BufferedReader br = new BufferedReader(new StringReader(text));
        StringBuilder sb = new StringBuilder();
        String line;
        String delim = "";
        try {
            while ((line = br.readLine()) != null) {
                sb.append(delim).append(indent).append(line);
                delim = LS;
            }
        } catch (IOException ex) {
            throw new RuntimeException("Can't happen!", ex);
        }

        char lastChar = text.charAt(text.length() - 1);
        if (lastChar == '\n' || lastChar == '\r') {
            sb.append(LS);
        }

        return sb.toString();
    }

    public static String padding(int length) {
        StringBuilder sb = new StringBuilder(length);
        for (int i = 0; i < length; i++) {
            sb.append(' ');
        }
        return sb.toString();
    }

    public static String trimLastSpaces(String str) {
        if (str == null) {
            return null;
        }
        for (int i = str.length() - 1; i >= 0; i--) {
            if (str.charAt(i) != ' ') {
                return str.substring(0, i + 1);
            }
        }
        return "";
    }
}
