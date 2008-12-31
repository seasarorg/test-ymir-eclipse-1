package org.seasar.ymir.eclipse.impl;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Set;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.seasar.kvasir.util.LocaleUtils;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;
import org.seasar.ymir.eclipse.util.ArrayUtils;
import org.seasar.ymir.eclipse.util.JarClassLoader;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.eclipse.util.XOMUtils;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.IConfigurator;
import org.seasar.ymir.vili.InclusionType;
import org.seasar.ymir.vili.NullConfigurator;
import org.seasar.ymir.vili.ParameterType;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.model.Action;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.model.maven.Project;
import org.seasar.ymir.vili.util.AntPathPatterns;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ViliBehaviorImpl implements ViliBehavior {
    private static final String DEFAULT_VILIVERSION = "0.0.1";

    private Artifact artifact;

    private MapProperties properties;

    private Project pom;

    private Actions actions;

    private ClassLoader classLoader;

    private IConfigurator configurator;

    private AntPathPatterns expansionIncludes;

    private AntPathPatterns expansionExcludes;

    private AntPathPatterns expansionMergeIncludes;

    private AntPathPatterns expansionMergeExcludes;

    private AntPathPatterns templateIncludes;

    private AntPathPatterns templateExcludes;

    private String[] templateParameters;

    private EnumSet<ProjectType> projectTypeSet;

    private ArtifactVersion viliVersion;

    private Pair[] templateEncodingPairs = new Pair[0];

    private AntPathPatterns viewTemplateIncludes;

    private AntPathPatterns viewTemplateExcludes;

    private Set<String> tieUpBundleSet = new HashSet<String>();

    public ViliBehaviorImpl(URL url) throws IOException {
        properties = readProperties(url);

        initialize(properties);
    }

    public ViliBehaviorImpl(Artifact artifact, ClassLoader projectClassLoader) throws CoreException {
        try {
            this.artifact = artifact;
            properties = readProperties(artifact);
            pom = readPom(artifact);
            actions = readActions(artifact);
            classLoader = createViliClassLoader(projectClassLoader);
            configurator = newConfigurator();
            initializeTieUpBundleSet(artifact);

            initialize(properties);
        } catch (IOException ex) {
            Activator.getDefault().throwCoreException("Can't create ViliBehavior instance", ex);
        }
    }

    private ClassLoader createViliClassLoader(ClassLoader parent) throws IOException {
        JarClassLoader classLoader = new JarClassLoader(Activator.getDefault().getArtifactResolver().getURL(artifact),
                parent);
        classLoader.setClassesPath(Globals.PATH_CLASSES);
        classLoader.setLibPath(Globals.PATH_LIB);
        return classLoader;
    }

    private void initializeTieUpBundleSet(Artifact artifact) throws CoreException {
        if (getArtifactType() != ArtifactType.SKELETON) {
            return;
        }

        if (ArtifactUtils.exists(artifact, Globals.PATH_M2ECLIPSE_LIGHT_PREFS)) {
            tieUpBundleSet.add(Globals.BUNDLENAME_M2ECLIPSE_LIGHT);
        }
        if (ArtifactUtils.exists(artifact, Globals.PATH_M2ECLIPSE_PREFS)) {
            tieUpBundleSet.add(Globals.BUNDLENAME_M2ECLIPSE);
        }
        if (ArtifactUtils.exists(artifact, Globals.PATH_MAVEN2ADDITIONAL_PREFS)) {
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

    private MapProperties readProperties(Artifact artifact) throws CoreException {
        @SuppressWarnings("unchecked")
        MapProperties properties = new MapProperties(new LinkedHashMap());
        JarFile jarFile = ArtifactUtils.getJarFile(artifact);
        try {
            JarEntry entry = jarFile.getJarEntry(Globals.PATH_BEHAVIOR_PROPERTIES);
            if (entry != null) {
                InputStream is = null;
                try {
                    is = jarFile.getInputStream(entry);
                    properties.load(is);
                } catch (IOException ex) {
                    throw new CoreException(new Status(IStatus.ERROR, Globals.PLUGIN_ID, "Can't load "
                            + Globals.PATH_BEHAVIOR_PROPERTIES + ": " + artifact, ex));
                } finally {
                    StreamUtils.close(is);
                    is = null;
                }

                String[] suffixes = LocaleUtils.getSuffixes(Locale.getDefault());
                for (int i = suffixes.length - 1; i >= 0; i--) {
                    String name = Globals.HEAD_BEHAVIOR_PROPERTIES + suffixes[i] + Globals.TAIL_BEHAVIOR_PROPERTIES;
                    entry = jarFile.getJarEntry(name);
                    if (entry == null) {
                        continue;
                    }
                    @SuppressWarnings("unchecked")
                    MapProperties newProperties = new MapProperties(new LinkedHashMap(), properties);
                    properties = newProperties;
                    try {
                        is = jarFile.getInputStream(entry);
                        properties.load(is);
                    } catch (IOException ex) {
                        throw new CoreException(new Status(IStatus.ERROR, Globals.PLUGIN_ID, "Can't load " + name
                                + ": " + artifact, ex));
                    } finally {
                        StreamUtils.close(is);
                        is = null;
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
            try {
                return XOMUtils.getAsBean(ArtifactUtils.getResourceAsString(artifact, Globals.PATH_POM_XML,
                        Globals.ENCODING, new NullProgressMonitor()), Project.class);
            } catch (Throwable t) {
                IOException ioe = new IOException("Can't read " + Globals.PATH_POM_XML + " in " + artifact); //$NON-NLS-1$ //$NON-NLS-2$
                ioe.initCause(t);
                throw ioe;
            }
        }
        return new Project();
    }

    private Actions readActions(Artifact artifact) throws IOException {
        Actions actions = null;
        try {
            actions = XOMUtils.getAsBean(ArtifactUtils.getResourceAsString(artifact, Globals.PATH_ACTIONS_XML,
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
        expansionMergeIncludes = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_MERGE_INCLUDES));
        expansionMergeExcludes = AntPathPatterns.newInstance(properties.getProperty(EXPANSION_MERGE_EXCLUDES));
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

        String viliVersionString = properties.getProperty(VILIVERSION);
        viliVersion = new ArtifactVersion(viliVersionString != null ? viliVersionString : DEFAULT_VILIVERSION);
    }

    public String[] getTemplateParameters() {
        return templateParameters;
    }

    public ParameterType getTemplateParameterType(String name) {
        return ParameterType.enumOf(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_TYPE));
    }

    public String[] getTemplateParameterCandidates(String name) {
        return PropertyUtils.toLines(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_CANDIDATES));
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

    public ArtifactVersion getViliVersion() {
        return viliVersion;
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

    IConfigurator newConfigurator() {
        String configuratorName = properties.getProperty(CONFIGURATOR);
        if (configuratorName != null) {
            try {
                return (IConfigurator) classLoader.loadClass(configuratorName).newInstance();
            } catch (Throwable t) {
                Activator.getDefault().getLog().log(
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't create configurator", t)); //$NON-NLS-1$
                throw new RuntimeException(t);
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

    public InclusionType shouldMergeWhenExpanding(String path) {
        if (expansionMergeExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (expansionMergeIncludes.matches(path)) {
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

    public void notifyPropertiesChanged() {
        initialize(properties);
    }

    public IConfigurator getConfigurator() {
        return configurator;
    }

    public ClassLoader getClassLoader() {
        return classLoader;
    }

    public Actions getActions() {
        return actions;
    }
}
