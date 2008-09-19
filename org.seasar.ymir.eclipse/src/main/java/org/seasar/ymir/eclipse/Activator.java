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
import java.util.zip.ZipEntry;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.maven.Dependency;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.mvnhack.repository.Artifact;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.Template;
import freemarker.template.TemplateException;

import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParser;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

    // The plug-in ID
    public static final String PLUGIN_ID = "org.seasar.ymir.eclipse"; //$NON-NLS-1$

    private static final String PATHPREFIX_SRC_MAIN_WEBAPP_LIB = Globals.PATH_SRC_MAIN_WEBAPP_WEBINF_LIB + "/"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private ArtifactResolver artifactResolver;

    private SkeletonEntry[] skeletonEntries;

    private FragmentEntry[] fragmentEntries;

    private DatabaseEntry[] databaseEntries;

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

    private ViliBehavior viliBehavior;

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
        setUpSkeletonEntries();
        setUpFragmentEntries();
        setUpDatabaseEntries();
        setUpTemplateEngine();
        readViliBehavior();
    }

    private void readViliBehavior() throws CoreException {
        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        InputStream is = getClass().getResourceAsStream(Globals.VILI_BEHAVIOR_PROPERTIES);
        try {
            prop.load(is);
        } catch (IOException ex) {
            throwCoreException("Can't load " + Globals.VILI_BEHAVIOR_PROPERTIES, ex); //$NON-NLS-1$
        } finally {
            StreamUtils.close(is);
        }

        viliBehavior = new ViliBehavior(prop);
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
        skeletonEntries = null;
        fragmentEntries = null;
        databaseEntries = null;
        viliBehavior = null;
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

    private void setUpSkeletonEntries() {
        skeletonEntries = new SkeletonEntry[] { new SkeletonEntry("ymir-skeleton-generic", "Ymir+ZPT+S2Dao", //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("Activator.10")), //$NON-NLS-1$
                new SkeletonEntry("ymir-skeleton-generic", "Ymir+ZPT+DBFlute", //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.getString("Activator.13"), new FragmentEntry("ymir-fragment-dbflute", "", "")), }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    private void setUpFragmentEntries() {
        fragmentEntries = new FragmentEntry[] {
                new FragmentEntry(
                        "ymir-fragment-utility", Messages.getString("Activator.63"), Messages.getString("Activator.64")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                new FragmentEntry(
                        "ymir-fragment-dbflute", Messages.getString("Activator.4"), Messages.getString("Activator.3")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                new FragmentEntry(
                        "ymir-fragment-json", Messages.getString("Activator.1"), Messages.getString("Activator.0")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                new FragmentEntry("ymir-fragment-amf", Messages.getString("Activator.60"), //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.getString("Activator.61")), }; //$NON-NLS-1$
    }

    public SkeletonEntry[] getSkeletonEntries() {
        return skeletonEntries;
    }

    public FragmentEntry[] getFragmentEntries() {
        return fragmentEntries;
    }

    private void setUpDatabaseEntries() {
        databaseEntries = new DatabaseEntry[] {
                new DatabaseEntry(
                        "H2 Database Engine", "h2", "org.h2.Driver", "jdbc:h2:file:%WEBAPP%/WEB-INF/h2/h2", "sa", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                        "", new Dependency("com.h2database", "h2", "1.0.78", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                new DatabaseEntry("MySQL Community Server", "mysql", "com.mysql.jdbc.Driver", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        "jdbc:mysql://localhost:3306/[DBNAME]", "", "", new Dependency("mysql", "mysql-connector-java", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                "5.1.6", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$
                new DatabaseEntry("PostgreSQL 8.3 database (JDBC-3.0)", "postgresql", "org.postgresql.Driver", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        "jdbc:postgresql://localhost:5432/[DBNAME]", "", "", new Dependency("postgresql", "postgresql", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                "8.3-603.jdbc3", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$
                new DatabaseEntry("PostgreSQL 8.3 database (JDBC-4.0)", "postgresql", "org.postgresql.Driver", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        "jdbc:postgresql://localhost:5432/[DBNAME]", "", "", new Dependency("postgresql", "postgresql", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                "8.3-603.jdbc4", "runtime")), //$NON-NLS-1$ //$NON-NLS-2$
                new DatabaseEntry(Messages.getString("Activator.50"), "", "", "", "", "", null), }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
    }

    public DatabaseEntry[] getDatabaseEntries() {
        return databaseEntries;
    }

    private void setUpTemplateEngine() {
        cfg = new Configuration();
        cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
        final Bundle bundle = getBundle();
        cfg.setTemplateLoader(new URLTemplateLoader() {
            protected URL getURL(String path) {
                return bundle.getEntry(path);
            }
        });
    }

    public String getArtifactLatestVersion(String groupId, String artifactId) {
        return artifactResolver.getLatestVersion(groupId, artifactId);
    }

    public Artifact resolveArtifact(String groupId, String artifactId, String version, boolean recursive,
            IProgressMonitor monitor) throws ArtifactNotFoundException {
        monitor.beginTask(Messages.getString("Activator.14"), 1); //$NON-NLS-1$
        try {
            return resolveArtifact(groupId, artifactId, version, recursive);
        } finally {
            monitor.done();
        }
    }

    private Artifact resolveArtifact(String groupId, String artifactId, String version, boolean recursive)
            throws ArtifactNotFoundException {
        Artifact artifact = artifactResolver.resolve(groupId, artifactId, version, recursive);
        if (artifact == null) {
            throw new ArtifactNotFoundException();
        }
        return artifact;
    }

    public void expandSkeleton(IProject project, Artifact skeletonArtifact, Map<String, Object> parameterMap,
            IProgressMonitor monitor) throws IOException, CoreException {
        monitor.beginTask(Messages.getString("Activator.15"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        try {
            final JarFile jarFile = getJarFile(skeletonArtifact);
            try {
                Configuration cfg = new Configuration();
                cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
                cfg.setTemplateLoader(new TemplateLoader() {
                    public void closeTemplateSource(Object name) throws IOException {
                    }

                    public Object findTemplateSource(String name) throws IOException {
                        return jarFile.getEntry(name);
                    }

                    public long getLastModified(Object name) {
                        return 0;
                    }

                    public Reader getReader(Object templateSource, String encoding) throws IOException {
                        return new InputStreamReader(jarFile.getInputStream((JarEntry) templateSource), encoding);
                    }
                });
                cfg.setObjectWrapper(new DefaultObjectWrapper());

                ViliBehavior localBehavior = new ViliBehavior(getPropertiesResource(jarFile,
                        Globals.VILI_BEHAVIOR_PROPERTIES, new SubProgressMonitor(monitor, 1)));

                for (Enumeration<JarEntry> enm = jarFile.entries(); enm.hasMoreElements();) {
                    JarEntry entry = enm.nextElement();
                    String name = entry.getName();
                    expand(project, name, cfg, parameterMap, jarFile, localBehavior, new SubProgressMonitor(monitor, 1));
                }
            } finally {
                jarFile.close();
            }
        } finally {
            monitor.done();
        }
    }

    private void expand(IProject project, String path, Configuration cfg, Map<String, Object> parameterMap,
            JarFile jarFile, ViliBehavior localBehavior, IProgressMonitor monitor) throws IOException, CoreException {
        if (shouldIgnore(path, localBehavior)) {
            return;
        } else if (path.endsWith("/")) { //$NON-NLS-1$
            mkdirs(project.getFolder(resolvePath(path, cfg, parameterMap)), new SubProgressMonitor(monitor, 1));
        } else {
            InputStream in;
            if (shouldEvaluateAsTemplate(path, localBehavior)) {
                byte[] evaluated;
                try {
                    StringWriter sw = new StringWriter();
                    cfg.getTemplate(path).process(parameterMap, sw);
                    evaluated = sw.toString().getBytes(Globals.ENCODING);
                } catch (TemplateException ex) {
                    IOException ioex = new IOException();
                    ioex.initCause(ex);
                    throw ioex;
                }
                in = new ByteArrayInputStream(evaluated);
            } else {
                in = jarFile.getInputStream(jarFile.getEntry(path));
            }
            try {
                String resolvedPath = resolvePath(path, cfg, parameterMap);
                IFile outputFile = project.getFile(resolvedPath);
                if (outputFile.exists()) {
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

    private boolean shouldIgnore(String path, ViliBehavior localBehavior) {
        if (localBehavior.getExpansionExcludes().matches(path)) {
            return true;
        }
        if (viliBehavior.getExpansionExcludes().matches(path)) {
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

    private boolean shouldEvaluateAsTemplate(String path, ViliBehavior localBehavior) {
        if (localBehavior.getTemplateExcludes().matches(path)) {
            return false;
        }
        if (localBehavior.getTemplateIncludes().matches(path)) {
            return true;
        }
        if (viliBehavior.getTemplateExcludes().matches(path)) {
            return false;
        }
        if (viliBehavior.getTemplateIncludes().matches(path)) {
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
        IStatus status = new Status(IStatus.ERROR, Activator.getId(), IStatus.OK, message, cause); //$NON-NLS-1$
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
        return Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null
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

            ZipEntry entry = jarFile.getEntry(path);
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

    private JarFile getJarFile(Artifact artifact) throws IOException {
        URL artifactURL = artifactResolver.getURL(artifact);
        if (artifactURL == null) {
            return null;
        }

        File artifactFile = File.createTempFile("ymir", ".jar"); //$NON-NLS-1$ //$NON-NLS-2$
        artifactFile.deleteOnExit();
        InputStream is = null;
        OutputStream os = null;
        try {
            is = artifactURL.openStream();
            os = new FileOutputStream(artifactFile);
            StreamUtils.copyStream(is, os);
        } finally {
            StreamUtils.close(is);
            StreamUtils.close(os);
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
            ZipEntry entry = jarFile.getEntry(path);
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
}
