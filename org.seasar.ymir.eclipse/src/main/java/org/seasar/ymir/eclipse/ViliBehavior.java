package org.seasar.ymir.eclipse;

import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.util.AntPathPatterns;

public class ViliBehavior {
    public static final String EXPANSION_EXCLUDES = "expansion.excludes";

    public static final String TEMPLATE_INCLUDES = "template.includes";

    public static final String TEMPLATE_EXCLUDES = "template.excludes";

    private MapProperties properties;

    private AntPathPatterns expansionExcludes;

    private AntPathPatterns templateIncludes;

    private AntPathPatterns templateExcludes;

    public ViliBehavior(MapProperties properties) {
        if (properties == null) {
            properties = new MapProperties();
        }

        this.properties = properties;

        expansionExcludes = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_EXCLUDES));
        templateIncludes = AntPathPatterns.newInstance(properties.getProperty(TEMPLATE_INCLUDES));
        templateExcludes = AntPathPatterns.newInstance(properties.getProperty(TEMPLATE_EXCLUDES));
    }

    public AntPathPatterns getExpansionExcludes() {
        return expansionExcludes;
    }

    public AntPathPatterns getTemplateIncludes() {
        return templateIncludes;
    }

    public AntPathPatterns getTemplateExcludes() {
        return templateExcludes;
    }
}
