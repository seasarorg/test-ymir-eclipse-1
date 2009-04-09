package org.seasar.ymir.eclipse;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "org.seasar.ymir.eclipse"; //$NON-NLS-1$

    // The shared instance
    private static Activator plugin;

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
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.plugin.AbstractUIPlugin#stop(org.osgi.framework.BundleContext)
     */
    public void stop(BundleContext context) throws Exception {
        plugin = null;

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

    public IPreferenceStore getPreferenceStore(IProject project) {
        return getPreferenceStore(project, PLUGIN_ID);
    }

    public IPreferenceStore getPreferenceStore(IProject project, String qualifier) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(new ProjectScope(project), PLUGIN_ID);
        store.setSearchContexts(new IScopeContext[] { new ProjectScope(project), new InstanceScope() });
        return store;
    }
}
