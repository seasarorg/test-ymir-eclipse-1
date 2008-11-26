package org.seasar.ymir.eclipse;

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
import java.util.Enumeration;
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
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
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
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.natures.ViliNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferencesProvider;
import org.seasar.ymir.eclipse.preferences.impl.DefaultViliProjectPreferencesProvider;
import org.seasar.ymir.eclipse.preferences.impl.ViliProjectPreferencesImpl;
import org.seasar.ymir.eclipse.preferences.impl.ViliProjectPreferencesProviderImpl;
import org.seasar.ymir.eclipse.util.CascadeMap;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.eclipse.util.URLUtils;

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

    private static final String PATHPREFIX_SRC_MAIN_WEBAPP_LIB = Globals.PATH_SRC_MAIN_WEBAPP_WEBINF_LIB + "/"; //$NON-NLS-1$

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
    }).setStrict(false);

    private XMLParser parser = XMLParserFactory.newInstance();

    private ViliBehavior systemBehavior;

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

        artifactResolver = new ArtifactResolver();
        IPreferenceStore preferenceStore = getPreferenceStore();
        artifactResolver.setOffline(preferenceStore.getBoolean(PreferenceConstants.P_OFFLINE));

        setUpTemplateEngine();
        readSystemBehavior();

        getPreferenceStore().addPropertyChangeListener(new IPropertyChangeListener() {
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
        systemBehavior = new ViliBehavior(getClass().getResource(Globals.VILI_BEHAVIOR_PROPERTIES));
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
    public void expandSkeleton(IProject project, ArtifactPair pair, Map<String, Object> parameterMap,
            IProgressMonitor monitor) throws IOException, CoreException {
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
                Map<String, Object> actualParameterMap = new CascadeMap<String, Object>(pair.getParameterMap(),
                        parameterMap);

                for (Enumeration<JarEntry> enm = jarFile.entries(); enm.hasMoreElements();) {
                    JarEntry entry = enm.nextElement();
                    String name = entry.getName();
                    expand(project, name, cfg, actualParameterMap, jarFile, behavior,
                            new SubProgressMonitor(monitor, 1));
                }
            } finally {
                jarFile.close();
            }
        } finally {
            monitor.done();
        }
    }

    private void expand(IProject project, String path, Configuration cfg, Map<String, Object> parameterMap,
            JarFile jarFile, ViliBehavior behavior, IProgressMonitor monitor) throws IOException, CoreException {
        if (shouldIgnore(path, behavior)) {
            return;
        }

        String resolvedPath = resolvePath(path, cfg, parameterMap);
        if (path.endsWith("/")) { //$NON-NLS-1$
            mkdirs(project.getFolder(resolvedPath), new SubProgressMonitor(monitor, 1));
        } else {
            InputStream in;
            if (shouldEvaluateAsTemplate(path, behavior)) {
                String templateEncoding = getTemplateEncoding(path, behavior);
                byte[] evaluated;
                try {
                    StringWriter sw = new StringWriter();
                    cfg.setEncoding(Locale.getDefault(), templateEncoding);
                    cfg.getTemplate(path).process(parameterMap, sw);
                    String evaluatedString = sw.toString();
                    if (shouldIgnore(path, evaluatedString, behavior)) {
                        return;
                    }
                    evaluated = evaluatedString.getBytes(templateEncoding);
                } catch (TemplateException ex) {
                    IOException ioex = new IOException();
                    ioex.initCause(ex);
                    throw ioex;
                } finally {
                    cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
                }
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
        if (behavior.getExpansionMerges().matches(path)) {
            return true;
        }
        if (systemBehavior.getExpansionMerges().matches(path)) {
            return true;
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

    private boolean shouldIgnore(String path, ViliBehavior behavior) {
        if (behavior.getExpansionExcludes().matches(path)) {
            return true;
        }
        if (systemBehavior.getExpansionExcludes().matches(path)) {
            return true;
        }
        if (path.equals(PATHPREFIX_SRC_MAIN_WEBAPP_LIB)) {
            return false;
        }
        if (path.startsWith(PATHPREFIX_SRC_MAIN_WEBAPP_LIB) && libsAreManagedAutomatically()) {
            return true;
        }

        return false;
    }

    private boolean shouldIgnore(String path, String evaluatedString, ViliBehavior behavior) {
        return behavior.getExpansionExcludesIfEmpty().matches(path) && evaluatedString.trim().length() == 0;
    }

    private boolean shouldEvaluateAsTemplate(String path, ViliBehavior behavior) {
        if (behavior.getTemplateExcludes().matches(path)) {
            return false;
        }
        if (behavior.getTemplateIncludes().matches(path)) {
            return true;
        }
        if (systemBehavior.getTemplateExcludes().matches(path)) {
            return false;
        }
        if (systemBehavior.getTemplateIncludes().matches(path)) {
            return true;
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

    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    public boolean libsAreManagedAutomatically() {
        return (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null || Platform
                .getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null)
                && Platform.getBundle(Globals.BUNDLENAME_MAVEN2ADDITIONAL) != null;
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
    public JarFile getJarFile(Artifact artifact) throws IOException {
        URL artifactURL = artifactResolver.getURL(artifact);
        File artifactFile = URLUtils.toFile(artifactURL);
        if (artifactFile == null) {
            artifactFile = File.createTempFile("ymir", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
            artifactFile.deleteOnExit();
            InputStream is = null;
            OutputStream os = null;
            try {
                is = artifactURL.openStream();
                os = new FileOutputStream(artifactFile);
                System.out.println("URL=" + artifactURL + ", file=" + artifactFile);
                StreamUtils.copyStream(is, os);
            } finally {
                StreamUtils.close(is);
                StreamUtils.close(os);
            }
        }
        return new JarFile(artifactFile);
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
            throw new RuntimeException("Can't happen!", ex);
        }
    }

    public ViliProjectPreferences newViliProjectPreferences() {
        return new ViliProjectPreferencesImpl(new DefaultViliProjectPreferencesProvider());
    }

    public ViliProjectPreferences getViliProjectPreferences(IProject project) {
        ViliProjectPreferencesProvider provider;
        try {
            provider = new ViliProjectPreferencesProviderImpl(project);
        } catch (CoreException ex) {
            getLog().log(ex.getStatus());
            provider = new DefaultViliProjectPreferencesProvider();
        }
        return new ViliProjectPreferencesImpl(provider);
    }

    public MapProperties loadApplicationProperties(IProject project) {
        MapProperties properties = new MapProperties(new TreeMap<String, String>());
        boolean isViliProject;
        try {
            isViliProject = project.hasNature(ViliNature.ID);
        } catch (CoreException ex) {
            isViliProject = false;
        }
        if (isViliProject) {
            IFile file = project.getFile(Globals.PATH_APP_PROPERTIES);
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = file.getContents();
                    properties.load(is);
                } catch (IOException ex) {
                    getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, "Can't load: " + file, ex));
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

    public void saveApplicationProperties(IProject project, MapProperties properties) throws IOException {
        boolean isViliProject;
        try {
            isViliProject = project.hasNature(ViliNature.ID);
        } catch (CoreException ex) {
            isViliProject = false;
        }
        if (isViliProject) {
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                properties.store(baos);
            } catch (IOException ex) {
                getLog().log(new Status(IStatus.ERROR, PLUGIN_ID, "Can't happen!", ex));
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
    }
}
