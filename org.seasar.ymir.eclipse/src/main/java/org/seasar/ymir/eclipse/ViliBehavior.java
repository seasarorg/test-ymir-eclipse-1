package org.seasar.ymir.eclipse;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.seasar.kvasir.util.LocaleUtils;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.util.AntPathPatterns;
import org.seasar.ymir.eclipse.util.ArrayUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ViliBehavior {
    private static final String EXPANSION_EXCLUDES = "expansion.excludes"; //$NON-NLS-1$

    private static final String EXPANSION_EXCLUDESIFEMPTY = "expansion.excludesIfEmpty"; //$NON-NLS-1$

    private static final String EXPANSION_MERGES = "expansion.merges"; //$NON-NLS-1$

    private static final String TEMPLATE_INCLUDES = "template.includes"; //$NON-NLS-1$

    private static final String TEMPLATE_EXCLUDES = "template.excludes"; //$NON-NLS-1$

    private static final String TEMPLATE_PARAMETERS = "template.parameters"; //$NON-NLS-1$

    private static final String PREFIX_TEMPLATE_ENCODING = "template.encoding."; //$NON-NLS-1$

    private static final String PREFIX_TEMPLATE_PARAMETER = "template.parameter."; //$NON-NLS-1$

    private static final String SUFFIX_TEMPLATE_PARAMETER_TYPE = ".type"; //$NON-NLS-1$

    private static final String SUFFIX_TEMPLATE_PARAMETER_DEFAULT = ".default"; //$NON-NLS-1$

    private static final String SUFFIX_TEMPLATE_PARAMETER_REQUIRED = ".required"; //$NON-NLS-1$

    private static final String SUFFIX_TEMPLATE_PARAMETER_LABEL = ".label"; //$NON-NLS-1$

    private static final String SUFFIX_TEMPLATE_PARAMETER_DESCRIPTION = ".description"; //$NON-NLS-1$

    private static final String LABEL = "label"; //$NON-NLS-1$

    private static final String DESCRIPTION = "description"; //$NON-NLS-1$

    private static final String TYPE = "type"; //$NON-NLS-1$

    private static final String ISNONYMIRPROJECT = "isNonYmirProject"; //$NON-NLS-1$

    private static final String ISNONJAVAPROJECT = "isNonJavaProject"; //$NON-NLS-1$

    private Artifact artifact;

    private MapProperties properties;

    private AntPathPatterns expansionExcludes;

    private AntPathPatterns expansionExcludesIfEmpty;

    private AntPathPatterns expansionMerges;

    private AntPathPatterns templateIncludes;

    private AntPathPatterns templateExcludes;

    private String[] templateParameters;

    private Pair[] templateEncodingPairs = new Pair[0];

    public ViliBehavior(URL url) throws IOException {
        properties = readProperties(url);

        initialize(properties);
    }

    public ViliBehavior(Artifact artifact) throws IOException {
        this.artifact = artifact;
        properties = readProperties(artifact);

        initialize(properties);
    }

    private MapProperties readProperties(URL url) throws IOException {
        @SuppressWarnings("unchecked") //$NON-NLS-1$
        MapProperties properties = new MapProperties(new LinkedHashMap());
        InputStream is = url.openStream();
        try {
            properties.load(is);
        } finally {
            StreamUtils.close(is);
        }
        return properties;
    }

    private MapProperties readProperties(Artifact artifact) throws IOException {
        Activator activator = Activator.getDefault();

        @SuppressWarnings("unchecked") //$NON-NLS-1$
        MapProperties properties = new MapProperties(new LinkedHashMap());
        JarFile jarFile = activator.getJarFile(artifact);
        try {
            JarEntry entry = jarFile.getJarEntry(Globals.VILI_BEHAVIOR_PROPERTIES);
            if (entry != null) {
                InputStream is = jarFile.getInputStream(entry);
                try {
                    properties.load(is);
                } finally {
                    StreamUtils.close(is);
                }

                String[] suffixes = LocaleUtils.getSuffixes(Locale.getDefault());
                for (int i = suffixes.length - 1; i >= 0; i--) {
                    entry = jarFile.getJarEntry(Globals.HEAD_VILI_BEHAVIOR_PROPERTIES + suffixes[i]
                            + Globals.TAIL_VILI_BEHAVIOR_PROPERTIES);
                    if (entry == null) {
                        continue;
                    }
                    @SuppressWarnings("unchecked") //$NON-NLS-1$
                    MapProperties newProperties = new MapProperties(new LinkedHashMap(), properties);
                    properties = newProperties;
                    is = jarFile.getInputStream(entry);
                    try {
                        properties.load(is);
                    } finally {
                        StreamUtils.close(is);
                    }
                }
            }
            return properties;
        } finally {
            StreamUtils.close(jarFile);
        }
    }

    private void initialize(MapProperties properties) {
        expansionExcludes = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_EXCLUDES));
        expansionExcludesIfEmpty = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_EXCLUDESIFEMPTY));
        expansionMerges = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_MERGES));
        templateIncludes = AntPathPatterns.newInstance(properties.getProperty(TEMPLATE_INCLUDES));
        templateExcludes = AntPathPatterns.newInstance(properties.getProperty(TEMPLATE_EXCLUDES));
        templateParameters = PropertyUtils.toLines(properties.getProperty(TEMPLATE_PARAMETERS));

        for (Enumeration<?> enm = properties.propertyNames(); enm.hasMoreElements();) {
            String name = (String) enm.nextElement();
            if (name.startsWith(PREFIX_TEMPLATE_ENCODING)) {
                templateEncodingPairs = ArrayUtils.add(templateEncodingPairs, new Pair(AntPathPatterns
                        .newInstance(properties.getProperty(name)), name.substring(PREFIX_TEMPLATE_ENCODING.length())));
            }
        }
    }

    public AntPathPatterns getExpansionExcludes() {
        return expansionExcludes;
    }

    public AntPathPatterns getExpansionExcludesIfEmpty() {
        return expansionExcludesIfEmpty;
    }

    public AntPathPatterns getExpansionMerges() {
        return expansionMerges;
    }

    public AntPathPatterns getTemplateIncludes() {
        return templateIncludes;
    }

    public AntPathPatterns getTemplateExcludes() {
        return templateExcludes;
    }

    public String[] getTemplateParameters() {
        return templateParameters;
    }

    public ParameterType getTemplateParameterType(String name) {
        return ParameterType.enumOf(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_TYPE));
    }

    public String getTemplateParameterDefault(String name) {
        return properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_DEFAULT, ""); //$NON-NLS-1$
    }

    public boolean isTemplateParameterRequired(String name) {
        return PropertyUtils.valueOf(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_REQUIRED), false);
    }

    public String getTemplateParameterLabel(String name) {
        return properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_LABEL, name);
    }

    public String getTemplateParameterDescription(String name) {
        return properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_DESCRIPTION, ""); //$NON-NLS-1$
    }

    public String getLabel() {
        return properties.getProperty(LABEL, artifact != null ? artifact.getArtifactId() : ""); //$NON-NLS-1$
    }

    public String getDescription() {
        return properties.getProperty(DESCRIPTION, ""); //$NON-NLS-1$
    }

    public boolean isYmirProject() {
        return !PropertyUtils.valueOf(properties.getProperty(ISNONYMIRPROJECT), false) && isJavaProject();
    }

    public boolean isJavaProject() {
        return !PropertyUtils.valueOf(properties.getProperty(ISNONJAVAPROJECT), false);
    }

    public ArtifactType getArtifactType() {
        return ArtifactType.enumOf(properties.getProperty(TYPE));
    }

    public String getTemplateEncoding(String path) {
        for (int i = 0; i < templateEncodingPairs.length; i++) {
            if (templateEncodingPairs[i].getAntPathPatterns().matches(path)) {
                return templateEncodingPairs[i].getValue();
            }
        }
        return null;
    }

    static class Pair {
        private AntPathPatterns antPathPatterns;

        private String value;

        public Pair(AntPathPatterns antPathPatterns, String value) {
            this.antPathPatterns = antPathPatterns;
            this.value = value;
        }

        public AntPathPatterns getAntPathPatterns() {
            return antPathPatterns;
        }

        public String getValue() {
            return value;
        }
    }
}
