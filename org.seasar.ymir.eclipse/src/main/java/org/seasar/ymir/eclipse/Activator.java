package org.seasar.ymir.eclipse;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;
import org.seasar.ymir.eclipse.impl.MoldResolverImpl;
import org.seasar.ymir.eclipse.impl.ProjectBuilderImpl;
import org.seasar.ymir.eclipse.impl.ViliBehaviorImpl;
import org.seasar.ymir.eclipse.maven.impl.ArtifactResolverImpl;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.preferences.impl.ViliNewProjectPreferencesProvider;
import org.seasar.ymir.eclipse.preferences.impl.ViliProjectPreferencesImpl;
import org.seasar.ymir.eclipse.preferences.impl.ViliProjectPreferencesProviderImpl;
import org.seasar.ymir.vili.MoldResolver;
import org.seasar.ymir.vili.ProcessContext;
import org.seasar.ymir.vili.ProjectBuilder;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.ViliProjectPreferencesProvider;
import org.seasar.ymir.vili.maven.ArtifactResolver;
import org.seasar.ymir.vili.model.Template;
import org.seasar.ymir.vili.util.XOMUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "org.seasar.ymir.eclipse"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

    private ArtifactResolver artifactResolver;

    private MoldResolver moldResolver;

    private ProjectBuilder projectBuilder;

    private Map<IProject, ProjectRelative> projectRelativeMap = new HashMap<IProject, ProjectRelative>();

    /*
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

        projectBuilder = new ProjectBuilderImpl(getBundle());

        moldResolver = new MoldResolverImpl();

        artifactResolver = new ArtifactResolverImpl();
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

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        artifactResolver = null;
        moldResolver = null;
        projectBuilder = null;
        plugin = null;

        synchronized (projectRelativeMap) {
            for (ProjectRelative relative : projectRelativeMap.values()) {
                JavaCore.removeElementChangedListener(relative);
            }
            projectRelativeMap.clear();
        }

        super.stop(context);
    }

    /*
     * Returns the shared instance
     * 
     * @return the shared instance
     */
    public static Activator getDefault() {
        return plugin;
    }

    /*
     * Returns an image descriptor for the image file at the given plug-in relative path
     * 
     * @param path the path
     * 
     * @return the image descriptor
     */
    public static ImageDescriptor getImageDescriptor(String path) {
        return imageDescriptorFromPlugin(PLUGIN_ID, path);
    }

    public static String getId() {
        return PLUGIN_ID;
    }

    public void throwCoreException(String message, Throwable t) throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, t));
    }

    public void log(Throwable t) {
        log("Problem has occured", t); //$NON-NLS-1$
    }

    public void log(String message) {
        log(message, null);
    }

    public void log(String message, Throwable t) {
        IStatus status;
        if (t instanceof CoreException) {
            status = ((CoreException) t).getStatus();
        } else {
            status = new Status(IStatus.ERROR, PLUGIN_ID, message, t);
        }
        getLog().log(status);
    }

    public ArtifactResolver getArtifactResolver() {
        return artifactResolver;
    }

    public MoldResolver getMoldResolver() {
        return moldResolver;
    }

    public ProjectBuilder getProjectBuilder() {
        return projectBuilder;
    }

    public IPreferenceStore getPreferenceStore(IProject project) {
        return getPreferenceStore(project, PLUGIN_ID);
    }

    public IPreferenceStore getPreferenceStore(IProject project, String qualifier) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(new ProjectScope(project), qualifier);
        store.setSearchContexts(new IScopeContext[] { new ProjectScope(project), new InstanceScope() });
        return store;
    }

    public Template getTemplate() {
        try {
            return createTemplate(getPreferenceStore().getString(PreferenceConstants.P_TEMPLATE));
        } catch (CoreException ex) {
            getLog().log(ex.getStatus());
            return new Template();
        }
    }

    public Template createTemplate(String template) throws CoreException {
        return XOMUtils.getAsBean(template, Template.class);
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

    public ViliBehavior newViliBehavior(Artifact artifact, ClassLoader projectClassLoader, ProcessContext context)
            throws CoreException {
        return new ViliBehaviorImpl(artifact, projectClassLoader, context);
    }
}
