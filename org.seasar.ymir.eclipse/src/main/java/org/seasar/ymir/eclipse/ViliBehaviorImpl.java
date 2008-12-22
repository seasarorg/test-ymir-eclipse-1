package org.seasar.ymir.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.seasar.kvasir.util.LocaleUtils;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.util.ArrayUtils;
import org.seasar.ymir.eclipse.util.JarClassLoader;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.Configurator;
import org.seasar.ymir.vili.InclusionType;
import org.seasar.ymir.vili.ParameterType;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.maven.Project;
import org.seasar.ymir.vili.model.Action;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.util.AntPathPatterns;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ViliBehaviorImpl implements ViliBehavior {
    private Artifact artifact;

    private MapProperties properties;

    private Project pom;

    private Actions actions;

    private ClassLoader classLoader;

    private Configurator configurator;

    private AntPathPatterns expansionIncludes;

    private AntPathPatterns expansionExcludes;

    private AntPathPatterns expansionIncludesIfExists;

    private AntPathPatterns expansionExcludesIfExists;

    private AntPathPatterns expansionIncludesIfEmpty;

    private AntPathPatterns expansionExcludesIfEmpty;

    private AntPathPatterns expansionMerges;

    private AntPathPatterns templateIncludes;

    private AntPathPatterns templateExcludes;

    private String[] templateParameters;

    private EnumSet<ProjectType> projectTypeSet;

    private Pair[] templateEncodingPairs = new Pair[0];

    private AntPathPatterns viewTemplateIncludes;

    private AntPathPatterns viewTemplateExcludes;

    private Set<String> tieUpBundleSet = new HashSet<String>();

    public ViliBehaviorImpl(URL url) throws IOException {
        properties = readProperties(url);

        initialize(properties);
    }

    public ViliBehaviorImpl(Artifact artifact, ClassLoader projectClassLoader) throws IOException {
        this.artifact = artifact;
        properties = readProperties(artifact);
        pom = readPom(artifact);
        actions = readActions(artifact);
        classLoader = createViliClassLoader(projectClassLoader);
        configurator = newConfigurator();
        initializeTieUpBundleSet(artifact);

        initialize(properties);
    }

    private ClassLoader createViliClassLoader(ClassLoader parent) {
        try {
            JarClassLoader classLoader = new JarClassLoader(Activator.getDefault().getURL(artifact), parent);
            classLoader.setClassesPath(Globals.PATH_CLASSES);
            classLoader.setLibPath(Globals.PATH_LIB);
            return classLoader;
        } catch (IOException ex) {
            Activator.getDefault().log(ex);
            return parent;
        }
    }

    private void initializeTieUpBundleSet(Artifact artifact) throws IOException {
        if (getArtifactType() != ArtifactType.SKELETON) {
            return;
        }

        Activator activator = Activator.getDefault();
        if (activator.exists(artifact, Globals.PATH_M2ECLIPSE_LIGHT_PREFS)) {
            tieUpBundleSet.add(Globals.BUNDLENAME_M2ECLIPSE_LIGHT);
        }
        if (activator.exists(artifact, Globals.PATH_M2ECLIPSE_PREFS)) {
            tieUpBundleSet.add(Globals.BUNDLENAME_M2ECLIPSE);
        }
        if (activator.exists(artifact, Globals.PATH_MAVEN2ADDITIONAL_PREFS)) {
            tieUpBundleSet.add(Globals.BUNDLENAME_MAVEN2ADDITIONAL);
        }
    }

    private MapProperties readProperties(URL url) throws IOException {
        @SuppressWarnings("unchecked")
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

        @SuppressWarnings("unchecked")
        MapProperties properties = new MapProperties(new LinkedHashMap());
        JarFile jarFile = activator.getJarFile(artifact);
        try {
            JarEntry entry = jarFile.getJarEntry(Globals.PATH_BEHAVIOR_PROPERTIES);
            if (entry != null) {
                InputStream is = jarFile.getInputStream(entry);
                try {
                    properties.load(is);
                } finally {
                    StreamUtils.close(is);
                }

                String[] suffixes = LocaleUtils.getSuffixes(Locale.getDefault());
                for (int i = suffixes.length - 1; i >= 0; i--) {
                    entry = jarFile.getJarEntry(Globals.HEAD_BEHAVIOR_PROPERTIES + suffixes[i]
                            + Globals.TAIL_BEHAVIOR_PROPERTIES);
                    if (entry == null) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
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

    private Project readPom(Artifact artifact) throws IOException {
        if (getArtifactType() == ArtifactType.FRAGMENT) {
            Activator activator = Activator.getDefault();
            try {
                String text = activator.getResourceAsString(artifact, Globals.PATH_POM_XML, Globals.ENCODING,
                        new NullProgressMonitor());
                if (text != null) {
                    return activator.getXOMapper().toBean(
                            activator.getXMLParser().parse(new StringReader(text)).getRootElement(), Project.class);
                }
            } catch (Throwable t) {
                IOException ioe = new IOException("Can't read " + Globals.PATH_POM_XML + " in " + artifact); //$NON-NLS-1$ //$NON-NLS-2$
                ioe.initCause(t);
                throw ioe;
            }
        }
        return new Project();
    }

    private Actions readActions(Artifact artifact) throws IOException {
        Activator activator = Activator.getDefault();
        Actions actions = null;
        try {
            actions = activator.getAsBean(activator.getResourceAsString(artifact, Globals.PATH_ACTIONS_XML,
                    Globals.ENCODING, new NullProgressMonitor()), Actions.class);
        } catch (Throwable t) {
            IOException ioe = new IOException("Can't read " + Globals.PATH_ACTIONS_XML + " in " + artifact); //$NON-NLS-1$ //$NON-NLS-2$
            ioe.initCause(t);
            throw ioe;
        }

        if (actions != null) {
            for (Action action : actions.getActions()) {
                action.setGroupId(artifact.getGroupId());
                action.setArtifactId(artifact.getArtifactId());
                action.setVersion(artifact.getVersion());
            }
        }

        return actions;
    }

    private void initialize(MapProperties properties) {
        expansionIncludes = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_INCLUDES));
        expansionExcludes = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_EXCLUDES));
        expansionIncludesIfExists = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_INCLUDESIFEXISTS));
        expansionExcludesIfExists = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_EXCLUDESIFEXISTS));
        expansionIncludesIfEmpty = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_INCLUDESIFEMPTY));
        expansionExcludesIfEmpty = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_EXCLUDESIFEMPTY));
        expansionMerges = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_MERGES));
        templateIncludes = AntPathPatterns.newInstance(properties.getProperty(TEMPLATE_INCLUDES));
        templateExcludes = AntPathPatterns.newInstance(properties.getProperty(TEMPLATE_EXCLUDES));
        templateParameters = PropertyUtils.toLines(properties.getProperty(TEMPLATE_PARAMETERS));
        viewTemplateIncludes = AntPathPatterns.newInstance(properties.getProperty(VIEWTEMPLATE_INCLUDES));
        viewTemplateExcludes = AntPathPatterns.newInstance(properties.getProperty(VIEWTEMPLATE_EXCLUDES));

        for (Enumeration<?> enm = properties.propertyNames(); enm.hasMoreElements();) {
            String name = (String) enm.nextElement();
            if (name.startsWith(PREFIX_TEMPLATE_ENCODING)) {
                templateEncodingPairs = ArrayUtils.add(templateEncodingPairs, new Pair(AntPathPatterns
                        .newInstance(properties.getProperty(name)), name.substring(PREFIX_TEMPLATE_ENCODING.length())));
            }
        }

        projectTypeSet = ProjectType.createEnumSet(properties.getProperty(PROJECTTYPE));
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

    public boolean isProjectOf(ProjectType type) {
        return projectTypeSet.contains(type);
    }

    public Project getPom() {
        return pom;
    }

    Configurator newConfigurator() {
        String configuratorName = properties.getProperty(CONFIGURATOR);
        if (configuratorName != null) {
            try {
                return (Configurator) classLoader.loadClass(configuratorName).newInstance();
            } catch (Throwable t) {
                Activator.getDefault().getLog().log(
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't create configurator", t)); //$NON-NLS-1$
            }
        }

        return new NullConfigurator();
    }

    File expand(File dir, JarFile jarFile, JarEntry entry) {
        File file = new File(dir, entry.getName());
        file.getParentFile().mkdirs();

        InputStream is = null;
        OutputStream os = null;
        try {
            is = jarFile.getInputStream(entry);
            os = new FileOutputStream(file);
            IOUtils.pipe(is, os, false, false);
        } catch (IOException ex) {
            Activator.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't expand " + entry.getName() + " to " //$NON-NLS-1$ //$NON-NLS-2$
                            + dir, ex));
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        file.deleteOnExit();

        return file;
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

    public InclusionType shouldEvaluateAsTemplate(String path) {
        if (templateExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (templateIncludes.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldExpand(String path) {
        if (expansionExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (expansionIncludes.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldExpandIfExists(String path) {
        if (expansionExcludesIfExists.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (expansionIncludesIfExists.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldExpandIfExpansionResultIsEmpty(String path) {
        if (expansionExcludesIfEmpty.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (expansionIncludesIfEmpty.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldMerge(String path) {
        if (expansionMerges.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldTreatAsViewTemplate(String path) {
        if (viewTemplateExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (viewTemplateIncludes.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public boolean isTieUpWithBundle(String bundleName) {
        return tieUpBundleSet.contains(bundleName);
    }

    public MapProperties getProperties() {
        return properties;
    }

    public Configurator getConfigurator() {
        return configurator;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Actions getActions() {
        return actions;
    }

    public void notifyPropertiesUpdated() {
        initialize(properties);
    }
}
