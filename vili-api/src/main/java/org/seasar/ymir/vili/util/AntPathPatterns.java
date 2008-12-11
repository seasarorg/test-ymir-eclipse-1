package org.seasar.ymir.vili.util;

import java.util.regex.Pattern;

import org.seasar.kvasir.util.AntUtils;
import org.seasar.kvasir.util.PropertyUtils;

public class AntPathPatterns {
    public static final AntPathPatterns EMPTY = new AntPathPatterns(new Pattern[0]);

    private Pattern[] patterns;

    public static AntPathPatterns newInstance(String patternsString) {
        if (patternsString == null || patternsString.trim().length() == 0) {
            return EMPTY;
        }

        String[] tokens = PropertyUtils.toLines(patternsString);
        Pattern[] patterns = new Pattern[tokens.length];
        for (int i = 0; i < tokens.length; i++) {
            patterns[i] = Pattern.compile(AntUtils.buildRegexPatternStringFromPattern(tokens[i], '/'));
        }
        return new AntPathPatterns(patterns);
    }

    AntPathPatterns(Pattern[] patterns) {
        this.patterns = patterns;
    }

    public boolean matches(String path) {
        for (Pattern pattern : patterns) {
            if (pattern.matcher(path).matches()) {
                return true;
            }
        }
        return false;
    }
}
