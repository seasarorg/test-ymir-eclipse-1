package org.seasar.ymir.eclipse;

import java.io.ByteArrayInputStream;
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
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.Bundle;
import org.osgi.framework.BundleContext;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.util.StreamUtils;

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

    private static final String PATH_META_INF = "META-INF/"; //$NON-NLS-1$

    private static final String PREFIX_ECLIPSE_SETTING = "."; //$NON-NLS-1$

    private static final String SUFFIX_XML = ".xml"; //$NON-NLS-1$

    private static final String SUFFIX_DICON = ".dicon"; //$NON-NLS-1$

    private static final String SUFFIX_PROPERTIES = ".properties"; //$NON-NLS-1$

    private static final String SUFFIX_PREFS = ".prefs"; //$NON-NLS-1$

    private static final String SUFFIX_JAVA = ".java"; //$NON-NLS-1$

    private static final char PATH_DELIMITER_CHAR = '/';

    // The shared instance
    private static Activator plugin;

    private ArtifactResolver artifactResolver;

    private SkeletonEntry[] skeletonEntries;

    private Configuration cfg;

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
        setUpTemplateEngine();
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        artifactResolver = null;
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

    private void setUpSkeletonEntries() {
        skeletonEntries = new SkeletonEntry[] { new SkeletonEntry("ymir-skeleton-generic", "Ymir+ZPT+S2Dao", //$NON-NLS-1$ //$NON-NLS-2$
                Messages.getString("Activator.10")), //$NON-NLS-1$
                new SkeletonEntry("ymir-skeleton-dbflute", "Ymir+ZPT+DBFlute", //$NON-NLS-1$ //$NON-NLS-2$
                        Messages.getString("Activator.13")), }; //$NON-NLS-1$
    }

    public SkeletonEntry[] getSkeletonEntries() {
        return skeletonEntries;
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

    public Artifact resolveArtifact(String groupId, String artifactId, String version, IProgressMonitor monitor)
            throws ArtifactNotFoundException {
        monitor.beginTask(Messages.getString("Activator.14"), 1); //$NON-NLS-1$
        try {
            return resolveArtifact(groupId, artifactId, version);
        } finally {
            monitor.done();
        }
    }

    private Artifact resolveArtifact(String groupId, String artifactId, String version)
            throws ArtifactNotFoundException {
        Artifact artifact = artifactResolver.resolve(groupId, artifactId, version);
        if (artifact == null) {
            throw new ArtifactNotFoundException();
        }
        return artifact;
    }

    public void expandSkeleton(IProject project, Artifact artifact, Map<String, Object> parameterMap,
            IProgressMonitor monitor) throws IOException, CoreException {
        monitor.beginTask(Messages.getString("Activator.15"), 1); //$NON-NLS-1$

        URL artifactURL = artifactResolver.getURL(artifact, "jar"); //$NON-NLS-1$
        if (artifactURL == null) {
            return;
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
        final JarFile jarFile = new JarFile(artifactFile);
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

            for (Enumeration<JarEntry> enm = jarFile.entries(); enm.hasMoreElements();) {
                JarEntry entry = enm.nextElement();
                String name = entry.getName();
                expand(project, name, cfg, parameterMap, jarFile, new NullProgressMonitor());
            }
        } finally {
            jarFile.close();
        }

        monitor.done();
    }

    private void expand(IProject project, String path, Configuration cfg, Map<String, Object> parameterMap,
            JarFile jarFile, IProgressMonitor monitor) throws IOException, CoreException {
        if (shouldIgnore(path)) {
            return;
        } else if (path.endsWith("/")) { //$NON-NLS-1$
            mkdirs(project.getFolder(resolvePath(path, cfg, parameterMap)), new SubProgressMonitor(monitor, 1));
        } else {
            InputStream in;
            if (shouldEvaluateAsTemplate(path)) {
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

    private boolean shouldIgnore(String path) {
        return path.startsWith(PATH_META_INF);
    }

    private boolean shouldEvaluateAsTemplate(String path) {
        String name;
        int slash = path.lastIndexOf(PATH_DELIMITER_CHAR);
        if (slash < 0) {
            name = path;
        } else {
            name = path.substring(slash + 1);
        }
        return name.startsWith(PREFIX_ECLIPSE_SETTING) || name.endsWith(SUFFIX_XML) || name.endsWith(SUFFIX_DICON)
                || name.endsWith(SUFFIX_PROPERTIES) || name.endsWith(SUFFIX_PREFS) || name.endsWith(SUFFIX_JAVA);
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
        monitor.beginTask(Messages.getString("Activator.21"), 2); //$NON-NLS-1$
        InputStream is;
        try {
            is = new ByteArrayInputStream(body.getBytes(Globals.ENCODING));
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
        }
        if (file.exists()) {
            file.setContents(is, false, false, new SubProgressMonitor(monitor, 2));
        } else {
            mkdirs(file.getParent(), new SubProgressMonitor(monitor, 1));
            file.create(is, false, new SubProgressMonitor(monitor, 1));
        }
        monitor.done();
    }
}
