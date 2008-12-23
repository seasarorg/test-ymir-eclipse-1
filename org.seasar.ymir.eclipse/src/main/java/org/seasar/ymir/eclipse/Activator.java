package org.seasar.ymir.eclipse;

import static org.seasar.ymir.eclipse.Globals.PATH_POM_XML;

import java.beans.Introspector;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParser;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.impl.ViliBehaviorImpl;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.maven.util.MavenUtils;
import org.seasar.ymir.eclipse.natures.YmirProjectNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.preferences.impl.ViliNewProjectPreferencesProvider;
import org.seasar.ymir.eclipse.preferences.impl.ViliProjectPreferencesImpl;
import org.seasar.ymir.eclipse.preferences.impl.ViliProjectPreferencesProviderImpl;
import org.seasar.ymir.eclipse.util.BeanMap;
import org.seasar.ymir.eclipse.util.CascadeMap;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.eclipse.util.URLUtils;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.ViliProjectPreferencesProvider;
import org.seasar.ymir.vili.maven.Dependencies;
import org.seasar.ymir.vili.maven.Dependency;
import org.seasar.ymir.vili.maven.PluginRepositories;
import org.seasar.ymir.vili.maven.PluginRepository;
import org.seasar.ymir.vili.maven.Profile;
import org.seasar.ymir.vili.maven.Profiles;
import org.seasar.ymir.vili.maven.Project;
import org.seasar.ymir.vili.maven.Repositories;
import org.seasar.ymir.vili.maven.Repository;
import org.seasar.ymir.vili.model.Action;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.model.TemplateEntry;

import werkzeugkasten.mvnhack.repository.Artifact;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "org.seasar.ymir.eclipse"; //$NON-NLS-1$

    private static final String EXTENSION_PROPERTIES = "properties"; //$NON-NLS-1$

    private static final String EXTENSION_XPROPERTIES = "xproperties"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private ArtifactResolver artifactResolver;

    private Configuration cfg;

    private XOMapper mapper = XOMapperFactory.newInstance().setBeanAccessorFactory(new BeanAccessorFactory() {
        public BeanAccessor newInstance() {
            return new AnnotationBeanAccessor() {
                @Override
                protected String toXMLName(String javaName) {
                    return Introspector.decapitalize(javaName);
                }
            };
        }
    }).setStrict(false).setTrimContent(true);

    private XMLParser parser = XMLParserFactory.newInstance();

    private ViliBehavior systemBehavior;

    private Map<IProject, ProjectRelative> projectRelativeMap = new HashMap<IProject, ProjectRelative>();

    /**
     * The constructor
     */
    public Activator() {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#start(org.osgi.framework.BundleContext)
     */
    public void start(BundleContext context) throws Exception {
        super.start(context);
        plugin = this;

        setUpTemplateEngine();
        readSystemBehavior();

        artifactResolver = new ArtifactResolver();
        IPreferenceStore preferenceStore = getPreferenceStore();
        artifactResolver.setOffline(preferenceStore.getBoolean(PreferenceConstants.P_OFFLINE));

        preferenceStore.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                if (PreferenceConstants.P_OFFLINE.equals(event.getProperty())) {
                    artifactResolver.setOffline(((Boolean) event.getNewValue()).booleanValue());
                }
            }
        });
    }

    @Override
    protected void initializeImageRegistry(ImageRegistry reg) {
        registerImage(reg, Globals.IMAGE_YMIR);
    }

    private void registerImage(ImageRegistry reg, String pathName) {
        IPath path = new Path(pathName);
        URL url = FileLocator.find(getBundle(), path, null);
        if (url != null) {
            ImageDescriptor desc = ImageDescriptor.createFromURL(url);
            reg.put(pathName, desc);
        }
    }

    private void readSystemBehavior() throws IOException {
        systemBehavior = new ViliBehaviorImpl(getClass().getResource(Globals.BEHAVIOR_PROPERTIES));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        artifactResolver = null;
        mapper = null;
        parser = null;
        systemBehavior = null;
        plugin = null;

        synchronized (projectRelativeMap) {
            for (ProjectRelative relative : projectRelativeMap.values()) {
                JavaCore.removeElementChangedListener(relative);
            }
            projectRelativeMap.clear();
        }

        super.stop(context);
    }

    /**
     * Returns the shared instance
     *
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /**
     * Returns an image descriptor for the image file at the given
     * plug-in relative path
     *
     * @param path the path
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static String getId() {
        return PLUGIN_ID;
    }

    public TemplateEntry getTemplateEntry() {
        try {
            return createTemplateEntry(getPreferenceStore().getString(PreferenceConstants.P_TEMPLATE));
        } catch (ValidationException ex) {
            getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, ex.toString(), ex));
            return new TemplateEntry();
        } catch (IllegalSyntaxException ex) {
            getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, ex.toString(), ex));
            return new TemplateEntry();
        }
    }

    private void setUpTemplateEngine() {
        cfg = new Configuration();
        cfg.setLocalizedLookup(false);
        cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
        final Bundle bundle = getBundle();
        cfg.setTemplateLoader(new URLTemplateLoader() {
            protected URL getURL(String path) {
                return bundle.getEntry(path);
            }
        });
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void expandArtifact(IProject project, ViliProjectPreferences preferences, ArtifactPair pair,
            Map<String, Object> parameters, IProgressMonitor monitor) throws IOException, CoreException {
        monitor.beginTask(Messages.getString("Activator.15"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        try {
            final JarFile jarFile = getJarFile(pair.getArtifact());
            try {
                Configuration cfg = new Configuration();
                cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
                cfg.setLocalizedLookup(false);
                cfg.setTemplateLoader(new TemplateLoader() {
                    public void closeTemplateSource(Object name) throws IOException {
                    }

                    public Object findTemplateSource(String name) throws IOException {
                        return jarFile.getJarEntry(name);
                    }

                    public long getLastModified(Object name) {
                        return 0;
                    }

                    public Reader getReader(Object templateSource, String encoding) throws IOException {
                        return new InputStreamReader(jarFile.getInputStream((JarEntry) templateSource), encoding);
                    }
                });
                cfg.setObjectWrapper(new DefaultObjectWrapper());

                ViliBehavior behavior = pair.getBehavior();
                behavior.getConfigurator().processBeforeExpanding(project, behavior, preferences, parameters,
                        new SubProgressMonitor(monitor, 1));

                for (Enumeration<JarEntry> enm = jarFile.entries(); enm.hasMoreElements();) {
                    JarEntry entry = enm.nextElement();
                    String name = entry.getName();
                    expand(name, jarFile, project, behavior, preferences, parameters, cfg, new SubProgressMonitor(
                            monitor, 1));
                }

                behavior.getConfigurator().processAfterExpanded(project, behavior, preferences, parameters,
                        new SubProgressMonitor(monitor, 1));
            } finally {
                jarFile.close();
            }
        } finally {
            monitor.done();
        }
    }

    private void expand(String path, JarFile jarFile, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters, Configuration cfg,
            IProgressMonitor monitor) throws IOException, CoreException {
        String resolvedPath = resolvePath(path, cfg, parameters);
        if (!shouldExpand(path, resolvedPath, project, behavior, preferences, parameters)) {
            return;
        }

        if (path.endsWith("/")) { //$NON-NLS-1$
            mkdirs(project.getFolder(resolvedPath), new SubProgressMonitor(monitor, 1));
        } else {
            InputStream in;
            boolean evaluateAsTemplate = shouldEvaluateAsTemplate(path, behavior);
            boolean viewTemplate = isViewTemplate(path, behavior);
            if (evaluateAsTemplate || viewTemplate) {
                String templateEncoding = getTemplateEncoding(path, behavior);

                String evaluatedString;
                if (evaluateAsTemplate) {
                    try {
                        StringWriter sw = new StringWriter();
                        cfg.setEncoding(Locale.getDefault(), templateEncoding);
                        cfg.getTemplate(path).process(parameters, sw);
                        evaluatedString = sw.toString();
                    } catch (TemplateException ex) {
                        IOException ioex = new IOException();
                        ioex.initCause(ex);
                        throw ioex;
                    } finally {
                        cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
                    }
                } else {
                    evaluatedString = IOUtils.readString(jarFile.getInputStream(jarFile.getJarEntry(path)),
                            templateEncoding, false);
                }

                byte[] evaluated = evaluatedString.getBytes(viewTemplate ? getValidEncoding(preferences
                        .getViewEncoding(), templateEncoding) : templateEncoding);
                in = new ByteArrayInputStream(evaluated);
            } else {
                in = jarFile.getInputStream(jarFile.getJarEntry(path));
            }
            try {
                IFile outputFile = project.getFile(resolvedPath);
                if (outputFile.exists()) {
                    if (shouldMerge(path, behavior)) {
                        in = mergeFile(outputFile, in);
                    }
                    outputFile.setContents(in, false, false, new SubProgressMonitor(monitor, 1));
                } else {
                    mkdirs(outputFile.getParent(), new SubProgressMonitor(monitor, 1));
                    outputFile.create(in, false, new SubProgressMonitor(monitor, 1));
                }
            } finally {
                StreamUtils.close(in);
            }
        }
    }

    private boolean isViewTemplate(String path, ViliBehavior behavior) {
        switch (behavior.shouldTreatAsViewTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldTreatAsViewTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return false;
    }

    String getValidEncoding(String viewEncoding, String defaultEncoding) {
        if (viewEncoding == null || viewEncoding.trim().length() == 0) {
            return defaultEncoding;
        }
        return viewEncoding;
    }

    private InputStream mergeFile(IFile file, InputStream in) throws CoreException {
        String extension = file.getLocation().getFileExtension();
        if (EXTENSION_PROPERTIES.equals(extension)) {
            return mergeFileAsProperties(file, in, false);
        } else if (EXTENSION_XPROPERTIES.equals(extension)) {
            return mergeFileAsProperties(file, in, true);
        } else {
            // TODO 警告などを出すようにする？
            return in;
        }
    }

    private InputStream mergeFileAsProperties(IFile file, InputStream in, boolean xproeprties) throws CoreException {
        String encoding = xproeprties ? "UTF-8" : "ISO-8859-1"; //$NON-NLS-1$ //$NON-NLS-2$

        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        InputStream is = file.getContents();
        try {
            prop.load(is, encoding);
        } catch (IOException ex) {
            throwCoreException("Can't load " + file, ex); //$NON-NLS-1$
            return null;
        } finally {
            StreamUtils.close(is);
        }

        MapProperties fragment = new MapProperties(new TreeMap<String, String>());
        try {
            fragment.load(in, encoding);
        } catch (IOException ex) {
            throwCoreException("Can't load fragment for " + file, ex); //$NON-NLS-1$
            return null;
        } finally {
            StreamUtils.close(in);
        }

        for (Enumeration<?> enm = fragment.propertyNames(); enm.hasMoreElements();) {
            String name = (String) enm.nextElement();
            prop.setProperty(name, fragment.getProperty(name));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            prop.store(baos, encoding);
        } catch (IOException ex) {
            throwCoreException("Can't store " + file, ex); //$NON-NLS-1$
            return null;
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    private boolean shouldMerge(String path, ViliBehavior behavior) {
        switch (behavior.shouldMerge(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldMerge(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return false;
    }

    private String getTemplateEncoding(String path, ViliBehavior behavior) {
        String encoding = behavior.getTemplateEncoding(path);
        if (encoding != null) {
            return encoding;
        }
        encoding = systemBehavior.getTemplateEncoding(path);
        if (encoding != null) {
            return encoding;
        }

        return Globals.ENCODING;
    }

    private String resolvePath(String path, Configuration cfg, Map<String, Object> parameterMap) throws IOException {
        try {
            StringWriter sw = new StringWriter();
            new Template("pathName", new StringReader(path), cfg).process(parameterMap, sw); //$NON-NLS-1$
            return sw.toString();
        } catch (TemplateException ex) {
            IOException ioex = new IOException();
            ioex.initCause(ex);
            throw ioex;
        }
    }

    private boolean shouldExpand(String path, String resolvedPath, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        if (behavior.getArtifactType() == ArtifactType.SKELETON) {
            if (path.equals(Globals.PATH_M2ECLIPSE_LIGHT_PREFS)
                    && Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) == null) {
                return false;
            } else if (path.equals(Globals.PATH_M2ECLIPSE_PREFS)
                    && (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null || Platform
                            .getBundle(Globals.BUNDLENAME_M2ECLIPSE) == null)) {
                return false;
            }
        }

        if (behavior.getArtifactType() == ArtifactType.FRAGMENT) {
            if (path.equals(PATH_POM_XML)) {
                return false;
            }
        }

        switch (behavior.getConfigurator().shouldExpand(path, resolvedPath, project, behavior, preferences, parameters)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (behavior.shouldExpand(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldExpand(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return true;
    }

    private boolean shouldEvaluateAsTemplate(String path, ViliBehavior behavior) {
        switch (behavior.shouldEvaluateAsTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldEvaluateAsTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return false;
    }

    private void mkdirs(IResource container, IProgressMonitor monitor) throws CoreException {
        if (container.getType() != IResource.FOLDER) {
            return;
        }
        IFolder folder = (IFolder) container;
        if (!folder.exists()) {
            mkdirs(folder.getParent(), monitor);
            folder.create(false, true, new SubProgressMonitor(monitor, 1));
        }
    }

    public String evaluateTemplate(String path, Map<String, Object> parameterMap) throws IOException {
        StringWriter sw = new StringWriter();
        try {
            cfg.getTemplate(path).process(parameterMap, sw);
        } catch (TemplateException ex) {
            IOException ioex = new IOException();
            ioex.initCause(ex);
            throw ioex;
        }
        return sw.toString();
    }

    public void writeFile(IFile file, String body, IProgressMonitor monitor) throws CoreException {
        try {
            writeFile(file, new ByteArrayInputStream(body.getBytes(Globals.ENCODING)), monitor);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
        }
    }

    public void writeFile(IFile file, InputStream is, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("Activator.21"), 2); //$NON-NLS-1$
        if (file.exists()) {
            file.setContents(is, false, false, new SubProgressMonitor(monitor, 2));
        } else {
            mkdirs(file.getParent(), new SubProgressMonitor(monitor, 1));
            file.create(is, false, new SubProgressMonitor(monitor, 1));
        }
        monitor.done();
    }

    public void mergeProperties(IFile file, URL entry, IProgressMonitor monitor) throws CoreException {
        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        InputStream is = null;
        try {
            is = entry.openStream();
            prop.load(is);
        } catch (IOException ex) {
            throwCoreException("Can't load: " + entry, ex); //$NON-NLS-1$
        } finally {
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignore) {
                }
            }
        }

        mergeProperties(file, prop, monitor);
    }

    public void mergeProperties(IFile file, MapProperties properties, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Merge properties", 1); //$NON-NLS-1$
        try {
            MapProperties prop = new MapProperties(new TreeMap<String, String>());
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = file.getContents();
                    prop.load(is);
                } catch (IOException ex) {
                    throwCoreException("Can't load: " + file, ex); //$NON-NLS-1$
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }

            for (Enumeration<?> enm = properties.propertyNames(); enm.hasMoreElements();) {
                String name = (String) enm.nextElement();
                prop.setProperty(name, properties.getProperty(name));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                prop.store(baos);
            } catch (IOException ex) {
                throwCoreException("Can't happen!", ex); //$NON-NLS-1$
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            writeFile(file, bais, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void throwCoreException(String message, Throwable cause) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, cause);
        throw new CoreException(status);
    }

    public XOMapper getXOMapper() {
        return mapper;
    }

    public XMLParser getXMLParser() {
        return parser;
    }

    public <T> T getAsBean(String content, Class<T> clazz) {
        if (content == null) {
            return null;
        }
        try {
            return getAsBean(new StringReader(content), clazz);
        } catch (CoreException ex) {
            log(ex);
            return null;
        }
    }

    public <T> T getAsBean(Reader reader, Class<T> clazz) throws CoreException {
        try {
            return mapper.toBean(parser.parse(reader).getRootElement(), //$NON-NLS-1$
                    clazz);
        } catch (Throwable t) {
            throwCoreException(t.toString(), t);
            return null;
        }
    }

    public <T> T getAsBean(IFile file, Class<T> clazz) {
        if (!file.exists()) {
            return null;
        }

        try {
            return getAsBean(new InputStreamReader(file.getContents(), "UTF-8"), clazz);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Can't happen!", ex);
        } catch (CoreException ex) {
            log(ex);
            return null;
        }
    }

    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    public String getResourceAsString(Artifact artifact, String path, String encoding, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.getString("Activator.55"), 1); //$NON-NLS-1$

        JarFile jarFile = null;
        InputStream is = null;
        try {
            jarFile = getJarFile(artifact);
            if (jarFile == null) {
                return null;
            }

            JarEntry entry = jarFile.getJarEntry(path);
            if (entry == null) {
                return null;
            }

            is = jarFile.getInputStream(entry);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            StreamUtils.copyStream(is, baos);
            return new String(baos.toByteArray(), encoding);
        } catch (IOException ex) {
            throwCoreException("Can't read resource: artifact=" + artifact + ", path=" + path, ex); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } finally {
            StreamUtils.close(is);
            StreamUtils.close(jarFile);
            monitor.done();
        }
    }

    // TODO JarInputStreamを使って一度ファイルシステムにコピーしないようにできるか検討する。
    public File getFile(Artifact artifact) throws IOException {
        URL artifactURL = getURL(artifact);
        File artifactFile = URLUtils.toFile(artifactURL);
        if (artifactFile == null) {
            artifactFile = File.createTempFile("ymir", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
            artifactFile.deleteOnExit();
            InputStream is = null;
            OutputStream os = null;
            try {
                is = artifactURL.openStream();
                os = new FileOutputStream(artifactFile);
                System.out.println("URL=" + artifactURL + ", file=" + artifactFile); //$NON-NLS-1$ //$NON-NLS-2$
                StreamUtils.copyStream(is, os);
            } finally {
                StreamUtils.close(is);
                StreamUtils.close(os);
            }
        }
        return artifactFile;
    }

    // TODO JarInputStreamを使って一度ファイルシステムにコピーしないようにできるか検討する。
    public JarFile getJarFile(Artifact artifact) throws IOException {
        return new JarFile(getFile(artifact));
    }

    public URL getURL(Artifact artifact) {
        return artifactResolver.getURL(artifact);
    }

    public MapProperties getPropertiesResource(Artifact artifact, String path, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.getString("Activator.58"), 1); //$NON-NLS-1$

        JarFile jarFile = null;
        try {
            jarFile = getJarFile(artifact);
            if (jarFile == null) {
                return null;
            }

            return getPropertiesResource(jarFile, path, new SubProgressMonitor(monitor, 1));
        } catch (IOException ex) {
            throwCoreException("Can't read resource: artifact=" + artifact + ", path=" + path, ex); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } finally {
            StreamUtils.close(jarFile);
            monitor.done();
        }
    }

    public MapProperties getPropertiesResource(JarFile jarFile, String path, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.getString("Activator.58"), 1); //$NON-NLS-1$

        InputStream is = null;
        try {
            JarEntry entry = jarFile.getJarEntry(path);
            if (entry == null) {
                return null;
            }

            is = jarFile.getInputStream(entry);

            MapProperties prop = new MapProperties(new TreeMap<String, String>());
            prop.load(is);
            return prop;
        } catch (IOException ex) {
            throwCoreException("Can't read resource: jarFile=" + jarFile.getName() + ", path=" + path, ex); //$NON-NLS-1$ //$NON-NLS-2$
            return null;
        } finally {
            StreamUtils.close(is);
            monitor.done();
        }
    }

    public IPreferenceStore getPreferenceStore(IProject project) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(new ProjectScope(project), PLUGIN_ID);
        store.setSearchContexts(new IScopeContext[] { new ProjectScope(project), new InstanceScope() });
        return store;
    }

    public TemplateEntry createTemplateEntry(String template) throws ValidationException, IllegalSyntaxException {
        if (template == null) {
            return null;
        }
        try {
            return mapper.toBean(parser.parse(new StringReader(template)).getRootElement(), TemplateEntry.class);
        } catch (IOException ex) {
            throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
        }
    }

    public ViliProjectPreferences newViliProjectPreferences() {
        return new ViliProjectPreferencesImpl(new ViliNewProjectPreferencesProvider());
    }

    public ViliProjectPreferences getViliProjectPreferences(IProject project) {
        ViliProjectPreferencesProvider provider;
        try {
            provider = new ViliProjectPreferencesProviderImpl(project);
        } catch (CoreException ex) {
            getLog().log(ex.getStatus());
            provider = new ViliNewProjectPreferencesProvider();
        }
        return new ViliProjectPreferencesImpl(provider);
    }

    public MapProperties loadApplicationProperties(IProject project) {
        MapProperties properties = new MapProperties(new TreeMap<String, String>());
        boolean isYmirProject;
        try {
            isYmirProject = project.hasNature(YmirProjectNature.ID);
        } catch (CoreException ex) {
            isYmirProject = false;
        }
        if (isYmirProject) {
            IFile file = project.getFile(Globals.PATH_APP_PROPERTIES);
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = file.getContents();
                    properties.load(is);
                } catch (IOException ex) {
                    getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, "Can't load: " + file, ex)); //$NON-NLS-1$
                } catch (CoreException ex) {
                    getLog().log(ex.getStatus());
                } finally {
                    if (is != null) {
                        try {
                            is.close();
                        } catch (IOException ignore) {
                        }
                    }
                }
            }
        }
        return properties;
    }

    public void saveApplicationProperties(IProject project, MapProperties properties, boolean merge) throws IOException {
        boolean isYmirProject;
        try {
            isYmirProject = project.hasNature(YmirProjectNature.ID);
        } catch (CoreException ex) {
            isYmirProject = false;
        }
        if (!isYmirProject) {
            return;
        }

        if (merge) {
            MapProperties base = loadApplicationProperties(project);
            for (Enumeration<?> enm = properties.propertyNames(); enm.hasMoreElements();) {
                String name = (String) enm.nextElement();
                base.setProperty(name, properties.getProperty(name));
            }
            properties = base;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            properties.store(baos);
        } catch (IOException ex) {
            getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Can't happen!", ex)); //$NON-NLS-1$
            return;
        }
        try {
            writeFile(project.getFile(Globals.PATH_APP_PROPERTIES), new ByteArrayInputStream(baos.toByteArray()),
                    new NullProgressMonitor());
        } catch (CoreException ex) {
            IOException ioe = new IOException();
            ioe.initCause(ex);
            throw ioe;
        }
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    public void addFragments(IProject project, ViliProjectPreferences preferences, ArtifactPair[] fragments,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("Activator.8"), fragments.length + 1); //$NON-NLS-1$
        try {
            Project pom = new Project();
            Dependencies dependencies = new Dependencies();
            pom.setDependencies(dependencies);
            Repositories repositories = new Repositories();
            pom.setRepositories(repositories);
            PluginRepositories pluginRepositories = new PluginRepositories();
            pom.setPluginRepositories(pluginRepositories);
            Profiles profiles = new Profiles();
            pom.setProfiles(profiles);

            IPreferenceStore store = getPreferenceStore(project);
            Actions actions = getAsBean(store.getString(PreferenceConstants.P_ACTIONS), Actions.class);
            if (actions == null) {
                actions = new Actions();
            }

            for (ArtifactPair fragment : fragments) {
                Artifact artifact = fragment.getArtifact();
                ViliBehavior behavior = fragment.getBehavior();
                @SuppressWarnings("unchecked")//$NON-NLS-1$
                Map<String, Object> parameters = new CascadeMap<String, Object>(new HashMap<String, Object>(), fragment
                        .getParameterMap(), new BeanMap(preferences));

                try {
                    expandArtifact(project, preferences, fragment, parameters, new SubProgressMonitor(monitor, 1));
                } catch (IOException ex) {
                    throwCoreException("Failed to add fragments", ex); //$NON-NLS-1$
                    return;
                }
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                Project fPom = fragment.getBehavior().getPom();
                if (fPom.getDependencies() != null) {
                    for (Dependency fDependency : fPom.getDependencies().getDependencies()) {
                        dependencies.addDependency(fDependency);
                    }
                }
                if (fPom.getRepositories() != null) {
                    for (Repository fRepository : fPom.getRepositories().getRepositories()) {
                        repositories.addRepository(fRepository);
                    }
                }
                if (fPom.getPluginRepositories() != null) {
                    for (PluginRepository fPluignRepository : fPom.getPluginRepositories().getPluginRepositories()) {
                        pluginRepositories.addPluginRepository(fPluignRepository);
                    }
                }
                if (fPom.getProfiles() != null) {
                    for (Profile fProfile : fPom.getProfiles().getProfiles()) {
                        profiles.addProfile(fProfile);
                    }
                }

                Actions fragmentActions = behavior.getActions();
                if (fragmentActions != null) {
                    List<Action> actionList = new ArrayList<Action>();
                    for (Action action : actions.getActions()) {
                        if (action.getGroupId().equals(artifact.getGroupId())
                                && action.getArtifactId().equals(artifact.getArtifactId())) {
                            continue;
                        }
                        actionList.add(action);
                    }
                    for (Action action : fragmentActions.getActions()) {
                        actionList.add(action);
                    }
                    actions.setActions(actionList.toArray(new Action[0]));
                }
            }

            MavenUtils.addToPom(project.getFile(Globals.PATH_POM_XML), pom, new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            try {
                StringWriter sw = new StringWriter();
                mapper.toXML(actions, sw);
                store.putValue(PreferenceConstants.P_ACTIONS, sw.toString());
                ((IPersistentPreferenceStore) store).save();
            } catch (Throwable t) {
                log(t);
            }
        } finally {
            monitor.done();
        }
    }

    public boolean exists(Artifact artifact, String path) throws IOException {
        JarFile jarFile = null;
        try {
            jarFile = getJarFile(artifact);
            if (jarFile == null) {
                return false;
            }

            return jarFile.getJarEntry(path) != null;
        } finally {
            StreamUtils.close(jarFile);
        }
    }

    public ProjectRelative getProjectRelative(IProject project) {
        synchronized (projectRelativeMap) {
            ProjectRelative relative = projectRelativeMap.get(project);
            if (relative == null) {
                relative = new ProjectRelative(project);
                projectRelativeMap.put(project, relative);
                JavaCore.addElementChangedListener(relative);
                project.getWorkspace().addResourceChangeListener(relative);
                getPreferenceStore(project).addPropertyChangeListener(relative);
            }
            return relative;
        }
    }

    public void removeProjectRelative(IProject project) {
        synchronized (projectRelativeMap) {
            projectRelativeMap.remove(project);
        }
    }

    public void log(Throwable t) {
        IStatus status;
        if (t instanceof CoreException) {
            status = ((CoreException) t).getStatus();
        } else {
            status = new Status(IStatus.ERROR, PLUGIN_ID, "Problem has occured", t);
        }
        getLog().log(status);
    }
}
