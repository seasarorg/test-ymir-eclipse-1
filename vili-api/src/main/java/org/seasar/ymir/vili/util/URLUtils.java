package org.seasar.ymir.vili.util;

import java.io.File;
import java.net.URISyntaxException;
import java.net.URL;

public class URLUtils {
    private static final String PROTOCOL_FILE = "file"; //$NON-NLS-1$

    private URLUtils() {
    }

    public static File toFile(URL url) {
        if (url == null || !PROTOCOL_FILE.equals(url.getProtocol())) {
            return null;
        }

        try {
            return new File(url.toURI());
        } catch (URISyntaxException ex) {
            return null;
        }
    }
}
