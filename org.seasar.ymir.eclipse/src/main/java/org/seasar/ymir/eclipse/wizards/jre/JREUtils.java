package org.seasar.ymir.eclipse.wizards.jre;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JREUtils {
    private static final Pattern JRE_NAME_PATTERN = Pattern.compile("(?:j2re|j2sdk|jre|jdk)(\\d+\\.\\d+)\\.", //$NON-NLS-1$
            Pattern.CASE_INSENSITIVE);

    private static final String DEFAULT_JRE_VERSION = "1.6"; //$NON-NLS-1$

    private JREUtils() {
    }

    public static String getJREVersion(String name) {
        Matcher matcher = JRE_NAME_PATTERN.matcher(name);
        if (matcher.find()) {
            return matcher.group(1);
        } else {
            return DEFAULT_JRE_VERSION;
        }
    }
}
