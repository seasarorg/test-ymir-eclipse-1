package org.seasar.ymir.eclipse;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.Enumeration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.seasar.kvasir.util.ClassUtils;
import org.seasar.kvasir.util.LocaleUtils;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.util.ArrayUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.Configurator;
import org.seasar.ymir.vili.ParameterType;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.maven.Project;
import org.seasar.ymir.vili.util.AntPathPatterns;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ViliBehaviorImpl implements ViliBehavior {
    private Artifact artifact;

    private MapProperties properties;

    private Project pom;

    private AntPathPatterns expansionExcludes;

    private AntPathPatterns expansionExcludesIfEmpty;

    private AntPathPatterns expansionMerges;

    private AntPathPatterns templateIncludes;

    private AntPathPatterns templateExcludes;

    private String[] templateParameters;

    private EnumSet<ProjectType> projectTypeSet;

    private Pair[] templateEncodingPairs = new Pair[0];

    public ViliBehaviorImpl(URL url) throws IOException {
        properties = readProperties(url);

        initialize(properties);
    }

    public ViliBehaviorImpl(Artifact artifact) throws IOException {
        this.artifact = artifact;
        properties = readProperties(artifact);
        pom = readPom(artifact);

        initialize(properties);
    }

    private MapProperties readProperties(URL url) throws IOException {
        @SuppressWarnings("unchecked")//$NON-NLS-1$
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

        @SuppressWarnings("unchecked")//$NON-NLS-1$
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
                    @SuppressWarnings("unchecked")//$NON-NLS-1$
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

        projectTypeSet = ProjectType.createEnumSet(properties.getProperty(PROJECTTYPE));
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

    public boolean isProjectOf(ProjectType type) {
        return projectTypeSet.contains(type);
    }

    public Project getPom() {
        return pom;
    }

    public Configurator newConfigurator(ClassLoader projectClassLoader) {
        String configuratorName = properties.getProperty(CONFIGURATOR);
        if (configuratorName != null) {
            ClassLoader classLoader = createViliClassLoader(projectClassLoader);
            try {
                return (Configurator) classLoader.loadClass(configuratorName).newInstance();
            } catch (Throwable t) {
                Activator.getDefault().getLog().log(
                        new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't create configurator", t)); //$NON-NLS-1$
            }
        }

        return new NullConfigurator();
    }

    ClassLoader createViliClassLoader(ClassLoader parent) {
        File rootDir;
        try {
            rootDir = File.createTempFile("vili-", ".tmp"); //$NON-NLS-1$ //$NON-NLS-2$
        } catch (IOException ex) {
            Activator.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't create temp file", ex)); //$NON-NLS-1$
            if (parent != null) {
                return parent;
            } else {
                return getClass().getClassLoader();
            }
        }
        rootDir.delete();
        rootDir.mkdirs();
        rootDir.deleteOnExit();

        File classesDir = new File(rootDir, Globals.VILI_CLASSES);
        classesDir.mkdir();
        classesDir.deleteOnExit();

        File libDir = new File(rootDir, Globals.VILI_LIB);
        libDir.mkdir();
        libDir.deleteOnExit();

        List<URL> urlList = new ArrayList<URL>();
        urlList.add(ClassUtils.getURLForURLClassLoader(classesDir));

        JarFile jarFile;
        try {
            jarFile = Activator.getDefault().getJarFile(artifact);
        } catch (IOException ex) {
            Activator.getDefault().getLog().log(
                    new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't open jar file: " + artifact, ex)); //$NON-NLS-1$
            throw new RuntimeException(ex);
        }
        try {
            for (Enumeration<JarEntry> enm = jarFile.entries(); enm.hasMoreElements();) {
                JarEntry entry = enm.nextElement();
                String name = entry.getName();
                if (name.endsWith("/")) { //$NON-NLS-1$
                    continue;
                }

                if (name.startsWith(Globals.PREFIX_VILI_CLASSES)) {
                    expand(classesDir, jarFile, entry);
                } else if (name.startsWith(Globals.PREFIX_VILI_LIB)) {
                    urlList.add(ClassUtils.getURLForURLClassLoader(expand(libDir, jarFile, entry)));
                }
            }
        } finally {
            StreamUtils.close(jarFile);
        }

        if (parent != null) {
            return new URLClassLoader(urlList.toArray(new URL[0]), parent);
        } else {
            return new URLClassLoader(urlList.toArray(new URL[0]));
        }
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
            Activator.getDefault().getLog()
                    .log(
                            new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't expand " + entry.getName() + " to " //$NON-NLS-1$ //$NON-NLS-2$
                                    + dir, ex));
        } finally {
            IOUtils.closeQuietly(os);
            IOUtils.closeQuietly(is);
        }

        file.deleteOnExit();

        return file;
    }
}
