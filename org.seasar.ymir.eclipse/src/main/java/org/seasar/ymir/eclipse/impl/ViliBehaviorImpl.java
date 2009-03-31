package org.seasar.ymir.eclipse.impl;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
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
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.vili.IConfigurator;
import org.seasar.ymir.vili.InclusionType;
import org.seasar.ymir.vili.MoldType;
import org.seasar.ymir.vili.NullConfigurator;
import org.seasar.ymir.vili.ParameterType;
import org.seasar.ymir.vili.ProcessContext;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.maven.util.ArtifactUtils;
import org.seasar.ymir.vili.model.Action;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.model.maven.Project;
import org.seasar.ymir.vili.util.AntPathPatterns;
import org.seasar.ymir.vili.util.ArrayUtils;
import org.seasar.ymir.vili.util.JarClassLoader;
import org.seasar.ymir.vili.util.StreamUtils;
import org.seasar.ymir.vili.util.XOMUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ViliBehaviorImpl implements ViliBehavior {
    private static final String DEFAULT_VILIVERSION = "0.0.1"; //$NON-NLS-1$

    private static final String[] EMPTY_STRINGS = new String[0];

    private Artifact artifact;

    private ClassLoader projectClassLoader;

    private ProcessContext context;

    private MapProperties properties;

    private boolean initialized;

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

    private Map<String, Set<String>> templateParameterDependentSetMap = new HashMap<String, Set<String>>();

    public ViliBehaviorImpl(URL url) {
        properties = readProperties(url);
        initializeViliVersion(properties);
    }

    public ViliBehaviorImpl(Artifact artifact, ClassLoader projectClassLoader, ProcessContext context) {
        this.artifact = artifact;
        this.projectClassLoader = projectClassLoader;
        this.context = context;
        properties = readProperties(artifact);
        initializeViliVersion(properties);
    }

    private void initialize() {
        if (initialized) {
            return;
        }

        actions = readActions(artifact);
        initializeTieUpBundleSet(artifact);
        classLoader = createViliClassLoader(projectClassLoader);
        configurator = newConfigurator();

        initialize0();

        initialized = true;
    }

    private void initialize0() {
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

        templateParameterDependentSetMap.clear();

        for (String dependent : getAllTemplateParameters(templateParameters)) {
            for (String name : getTemplateParameterDepends(dependent)) {
                Set<String> set = templateParameterDependentSetMap.get(name);
                if (set == null) {
                    set = new HashSet<String>();
                    templateParameterDependentSetMap.put(name, set);
                }
                set.add(dependent);
            }
        }
    }

    private String[] getAllTemplateParameters(String[] parameters) {
        List<String> list = new ArrayList<String>();
        getAllTemplateParameters(parameters, list);
        return list.toArray(new String[0]);
    }

    private void getAllTemplateParameters(String[] parameters, List<String> parameterList) {
        for (String parameter : parameters) {
            parameterList.add(parameter);
            if (getTemplateParameterType(parameter) == ParameterType.GROUP) {
                getAllTemplateParameters(getTemplateParameters(parameter), parameterList);
            }
        }
    }

    private Actions readActions(Artifact artifact) {
        if (artifact == null) {
            return null;
        }

        Actions actions = null;
        try {
            actions = XOMUtils.getAsBean(ArtifactUtils.getResourceAsString(artifact, Globals.PATH_ACTIONS_XML,
                    Globals.ENCODING, new NullProgressMonitor()), Actions.class);
        } catch (CoreException ex) {
            Activator.getDefault().log(ex);
            throw new RuntimeException(ex);
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

    private void initializeTieUpBundleSet(Artifact artifact) {
        tieUpBundleSet.clear();

        if (artifact == null) {
            return;
        }
        if (getMoldType() != MoldType.SKELETON) {
            return;
        }

        try {
            if (ArtifactUtils.exists(artifact, Globals.PATH_M2ECLIPSE_LIGHT_PREFS)) {
                tieUpBundleSet.add(Globals.BUNDLENAME_M2ECLIPSE_LIGHT);
            }
            if (ArtifactUtils.exists(artifact, Globals.PATH_M2ECLIPSE_PREFS)) {
                tieUpBundleSet.add(Globals.BUNDLENAME_M2ECLIPSE);
            }
            if (ArtifactUtils.exists(artifact, Globals.PATH_MAVEN2ADDITIONAL_PREFS)) {
                tieUpBundleSet.add(Globals.BUNDLENAME_MAVEN2ADDITIONAL);
            }
        } catch (CoreException ex) {
            Activator.getDefault().log(ex);
        }
    }

    private ClassLoader createViliClassLoader(ClassLoader parent) {
        if (artifact == null) {
            return null;
        }

        JarClassLoader classLoader = new JarClassLoader(Activator.getDefault().getArtifactResolver().getURL(artifact),
                parent);
        classLoader.setClassesPath(Globals.PATH_CLASSES);
        classLoader.setLibPath(Globals.PATH_LIB);
        return classLoader;
    }

    private IConfigurator newConfigurator() {
        if (artifact != null) {
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
        }

        return new NullConfigurator();
    }

    private MapProperties readProperties(URL url) {
        @SuppressWarnings("unchecked")//$NON-NLS-1$
        MapProperties properties = new MapProperties(new LinkedHashMap());
        InputStream is = null;
        try {
            is = url.openStream();
            properties.load(is);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        } finally {
            StreamUtils.close(is);
        }
        return properties;
    }

    private MapProperties readProperties(Artifact artifact) {
        @SuppressWarnings("unchecked")//$NON-NLS-1$
        MapProperties properties = new MapProperties(new LinkedHashMap());
        JarFile jarFile = null;
        try {
            jarFile = ArtifactUtils.getJarFile(artifact);
            JarEntry entry = jarFile.getJarEntry(Globals.PATH_BEHAVIOR_PROPERTIES);
            if (entry != null) {
                InputStream is = null;
                try {
                    is = jarFile.getInputStream(entry);
                    properties.load(is);
                } catch (IOException ex) {
                    throw new RuntimeException(ex);
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
                    @SuppressWarnings("unchecked")//$NON-NLS-1$
                    MapProperties newProperties = new MapProperties(new LinkedHashMap(), properties);
                    properties = newProperties;
                    try {
                        is = jarFile.getInputStream(entry);
                        properties.load(is);
                    } catch (IOException ex) {
                        throw new RuntimeException(ex);
                    } finally {
                        StreamUtils.close(is);
                        is = null;
                    }
                }
            }
            return properties;
        } catch (CoreException ex) {
            Activator.getDefault().log(ex);
            throw new RuntimeException(ex);
        } finally {
            StreamUtils.close(jarFile);
        }
    }

    private void initializeViliVersion(MapProperties properties) {
        String viliVersionString = properties.getProperty(VILIVERSION);
        viliVersion = new ArtifactVersion(viliVersionString != null ? viliVersionString : DEFAULT_VILIVERSION);
    }

    public String[] getTemplateParameters() {
        initialize();

        return templateParameters;
    }

    public void setTemplateParameters(String[] names) {
        properties.setProperty(TEMPLATE_PARAMETERS, PropertyUtils.join(names));
    }

    public String[] getTemplateParameters(String groupName) {
        if (groupName == null) {
            return getTemplateParameters();
        } else {
            return PropertyUtils.toLines(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + groupName
                    + SUFFIX_TEMPLATE_PARAMETER_PARAMETERS));
        }
    }

    public void setTemplateParameters(String groupName, String[] names) {
        if (groupName == null) {
            setTemplateParameters(names);
        } else {
            String key = PREFIX_TEMPLATE_PARAMETER + groupName + SUFFIX_TEMPLATE_PARAMETER_PARAMETERS;
            if (names != null) {
                properties.setProperty(key, PropertyUtils.join(names));
            } else {
                properties.removeProperty(key);
            }
        }
    }

    public boolean isTemplateParameterModifiable(String name) {
        return PropertyUtils.valueOf(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_MODIFIABLE), false);
    }

    public void setTemplateParameterModifiable(String name, boolean modifiable) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_MODIFIABLE;
        if (modifiable) {
            properties.setProperty(key, String.valueOf(true));
        } else {
            properties.removeProperty(key);
        }
    }

    public ParameterType getTemplateParameterType(String name) {
        return ParameterType.enumOf(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_TYPE));
    }

    public void setTemplateParameterType(String name, ParameterType type) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_TYPE;
        if (type != null && type != ParameterType.TEXT) {
            properties.setProperty(key, type.name().toLowerCase());
        } else {
            properties.removeProperty(key);
        }
    }

    public String[] getTemplateParameterCandidates(String name) {
        return PropertyUtils.toLines(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_CANDIDATES));
    }

    public void setTemplateParameterCandidates(String name, String[] candidates) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_CANDIDATES;
        if (candidates != null) {
            properties.setProperty(key, PropertyUtils.join(candidates));
        } else {
            properties.removeProperty(key);
        }
    }

    public String[] getTemplateParameterDepends(String name) {
        return PropertyUtils.toLines(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_DEPENDS));
    }

    public void setTemplateParameterDepends(String name, String[] depends) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_DEPENDS;
        if (depends != null) {
            properties.setProperty(key, PropertyUtils.join(depends));
        } else {
            properties.removeProperty(key);
        }
    }

    public String getTemplateParameterDefault(String name) {
        return properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_DEFAULT, ""); //$NON-NLS-1$
    }

    public void setTemplateParameterDefault(String name, String defaultValue) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_DEFAULT;
        if (defaultValue != null) {
            properties.setProperty(key, defaultValue);
        } else {
            properties.removeProperty(key);
        }
    }

    public boolean isTemplateParameterRequired(String name) {
        return PropertyUtils.valueOf(properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name
                + SUFFIX_TEMPLATE_PARAMETER_REQUIRED), false);
    }

    public void setTemplateParameterRequired(String name, boolean required) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_REQUIRED;
        if (required) {
            properties.setProperty(key, String.valueOf(true));
        } else {
            properties.removeProperty(key);
        }
    }

    public String[] getTemplateParameterDependents(String name) {
        initialize();

        Set<String> set = templateParameterDependentSetMap.get(name);
        if (set == null) {
            return EMPTY_STRINGS;
        } else {
            return set.toArray(new String[0]);
        }
    }

    public String getTemplateParameterLabel(String name) {
        return properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_LABEL, name);
    }

    public void setTemplateParameterLabel(String name, String label) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_LABEL;
        if (label != null && !label.equals(name)) {
            properties.setProperty(key, label);
        } else {
            properties.removeProperty(key);
        }
    }

    public String getTemplateParameterDescription(String name) {
        return properties.getProperty(PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_DESCRIPTION, ""); //$NON-NLS-1$
    }

    public void setTemplateParameterDescription(String name, String description) {
        String key = PREFIX_TEMPLATE_PARAMETER + name + SUFFIX_TEMPLATE_PARAMETER_DESCRIPTION;
        if (description != null && !description.equals("")) {
            properties.setProperty(key, description);
        } else {
            properties.removeProperty(key);
        }
    }

    public String getLabel() {
        return properties.getProperty(LABEL, artifact != null ? artifact.getArtifactId() : ""); //$NON-NLS-1$
    }

    public String getDescription() {
        return properties.getProperty(DESCRIPTION, ""); //$NON-NLS-1$
    }

    public MoldType getMoldType() {
        return MoldType.enumOf(properties.getProperty(TYPE));
    }

    public ArtifactVersion getViliVersion() {
        return viliVersion;
    }

    public String getTemplateEncoding(String path) {
        initialize();

        for (int i = 0; i < templateEncodingPairs.length; i++) {
            if (templateEncodingPairs[i].getAntPathPatterns().matches(path)) {
                return templateEncodingPairs[i].getValue();
            }
        }
        return null;
    }

    public boolean isProjectOf(ProjectType type) {
        initialize();

        return projectTypeSet.contains(type);
    }

    public Project getPom(boolean evaluate, Map<String, Object> parameters) {
        try {
            return readPom(artifact, evaluate, parameters);
        } catch (CoreException ex) {
            Activator.getDefault().log(ex);
            throw new RuntimeException(ex);
        }
    }

    private Project readPom(Artifact artifact, boolean evaluate, Map<String, Object> parameters) throws CoreException {
        Project pom = null;
        if (getMoldType() == MoldType.FRAGMENT) {
            String content = ArtifactUtils.getResourceAsString(artifact, Globals.PATH_POM_XML, Globals.ENCODING,
                    new NullProgressMonitor());
            if (evaluate) {
                content = Activator.getDefault().getProjectBuilder().evaluate(content, parameters);
            }
            pom = XOMUtils.getAsBean(content, Project.class);
        }
        if (pom == null) {
            pom = new Project();
        }
        return pom;
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
        initialize();

        if (templateExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (templateIncludes.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldExpand(String path) {
        initialize();

        if (expansionExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (expansionIncludes.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldMergeWhenExpanding(String path) {
        initialize();

        if (expansionMergeExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (expansionMergeIncludes.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public InclusionType shouldTreatAsViewTemplate(String path) {
        initialize();

        if (viewTemplateExcludes.matches(path)) {
            return InclusionType.EXCLUDED;
        } else if (viewTemplateIncludes.matches(path)) {
            return InclusionType.INCLUDED;
        } else {
            return InclusionType.UNDEFINED;
        }
    }

    public boolean isTieUpWithBundle(String bundleName) {
        initialize();

        return tieUpBundleSet.contains(bundleName);
    }

    public String getProperty(String name) {
        return properties.getProperty(name);
    }

    public String getProperty(String name, String defaultValue) {
        return properties.getProperty(name, defaultValue);
    }

    public MapProperties getProperties() {
        return properties;
    }

    public void notifyPropertiesChanged() {
        update();
    }

    public IConfigurator getConfigurator() {
        initialize();

        return configurator;
    }

    public ClassLoader getClassLoader() {
        initialize();

        return classLoader;
    }

    public Actions getActions() {
        initialize();

        return actions;
    }

    public boolean isAvailableOnlyIfProjectExists() {
        return PropertyUtils.valueOf(properties.getProperty(AVAILABLEONLYIFPROJECTEXISTS), false);
    }

    public ProcessContext getProcessContext() {
        return context;
    }

    public void update() {
        if (initialized) {
            initialize0();
        }
    }
}
