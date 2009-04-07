package org.seasar.ymir.eclipse;

import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
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
import org.seasar.kvasir.util.PropertyUtils;
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
import org.seasar.ymir.vili.model.Fragment;
import org.seasar.ymir.vili.model.Fragments;
import org.seasar.ymir.vili.model.Skeleton;
import org.seasar.ymir.vili.model.Skeletons;
import org.seasar.ymir.vili.model.Template;
import org.seasar.ymir.vili.util.XOMUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {
    // The plug-in ID
    public static final String PLUGIN_ID = "org.seasar.ymir.eclipse"; //$NON-NLS-1$

    private static final String EXTENSIONPOINTID_SKELETONS = "skeletons";

    private static final String EXTENSIONPOINTID_FRAGMENTS = "fragments";

    // The shared instance
    private static Activator plugin;

    private ArtifactResolver artifactResolver;

    private MoldResolver moldResolver;

    private ProjectBuilder projectBuilder;

    private Template contributedTemplate;

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

        contributedTemplate = createContributedTemplate();
    }

    private Template createContributedTemplate() {
        IExtensionRegistry registry = Platform.getExtensionRegistry();

        IExtensionPoint skeletonsPoint = registry.getExtensionPoint(getBundle().getSymbolicName() + "."
                + EXTENSIONPOINTID_SKELETONS);
        List<Skeleton> skeletonList = new ArrayList<Skeleton>();
        for (IExtension extension : skeletonsPoint.getExtensions()) {
            for (IConfigurationElement element : extension.getConfigurationElements()) {
                if ("skeleton".equals(element.getName())) {
                    skeletonList.add(buildSkeleton(element));
                }
            }
        }

        IExtensionPoint fragmentsPoint = registry.getExtensionPoint(getBundle().getSymbolicName() + "."
                + EXTENSIONPOINTID_FRAGMENTS);
        List<Fragment> fragmentList = new ArrayList<Fragment>();
        for (IExtension extension : fragmentsPoint.getExtensions()) {
            for (IConfigurationElement element : extension.getConfigurationElements()) {
                if ("fragment".equals(element.getName())) {
                    fragmentList.add(buildFragment(element));
                }
            }
        }
        Template template = new Template();
        template.setSkeletons(new Skeletons(skeletonList.toArray(new Skeleton[0])));
        template.setFragments(new Fragments(fragmentList.toArray(new Fragment[0])));
        return template;
    }

    private Skeleton buildSkeleton(IConfigurationElement element) {
        Skeleton skeleton = new Skeleton();
        skeleton.setGroupId(valueOf(element.getChildren("groupId")));
        skeleton.setArtifactId(valueOf(element.getChildren("artifactId")));
        skeleton.setVersion(valueOf(element.getChildren("version")));
        skeleton.setName(localize(valueOf(element.getChildren("name"), "")));
        skeleton.setDescription(localize(valueOf(element.getChildren("description"), "")));
        IConfigurationElement[] elems = element.getChildren("fragments");
        if (elems.length > 0) {
            skeleton.setFragments(buildFragments(elems[0]));
        }
        return skeleton;
    }

    private String localize(String string) {
        if (string == null || !string.startsWith("%")) {
            return string;
        }
        String localized = Platform.getResourceString(getBundle(), string.substring(1/* ="%".length() */));
        if (localized == null) {
            localized = string;
        }
        return localized;

    }

    private String valueOf(IConfigurationElement[] elements) {
        if (elements.length > 0) {
            return elements[0].getValue().trim();
        }
        return null;
    }

    private String valueOf(IConfigurationElement[] elements, String defaultValue) {
        String value = valueOf(elements);
        if (value == null) {
            value = defaultValue;
        }
        return value;
    }

    private boolean valueOf(IConfigurationElement[] elements, boolean defaultValue) {
        return PropertyUtils.valueOf(valueOf(elements), defaultValue);
    }

    private Fragments buildFragments(IConfigurationElement element) {
        Fragments fragments = new Fragments();
        for (IConfigurationElement elem : element.getChildren("fragment")) {
            fragments.addFragment(buildFragment(elem));
        }
        return fragments;
    }

    private Fragment buildFragment(IConfigurationElement element) {
        Fragment fragment = new Fragment();
        fragment.setGroupId(valueOf(element.getChildren("groupId")));
        fragment.setArtifactId(valueOf(element.getChildren("artifactId")));
        fragment.setVersion(valueOf(element.getChildren("version")));
        fragment.setName(localize(valueOf(element.getChildren("name"), "")));
        fragment.setDescription(localize(valueOf(element.getChildren("description"), "")));
        fragment.setAvailableOnlyIfProjectExists(valueOf(element.getChildren("availableOnlyIfProjectExists"), false));
        return fragment;
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

        contributedTemplate = null;

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
        Template customTemplate;
        try {
            customTemplate = createTemplate(getPreferenceStore().getString(PreferenceConstants.P_TEMPLATE));
        } catch (CoreException ex) {
            throw new RuntimeException(ex);
        }

        Template template = new Template();

        Skeletons skeletons = new Skeletons();
        for (Skeleton skeleton : contributedTemplate.getAllSkeletons()) {
            skeletons.addSkeleton(skeleton);
        }
        for (Skeleton skeleton : customTemplate.getAllSkeletons()) {
            skeletons.addSkeleton(skeleton);
        }
        template.setSkeletons(skeletons);

        Fragments fragments = new Fragments();
        for (Fragment fragment : contributedTemplate.getAllFragments()) {
            fragments.addFragment(fragment);
        }
        for (Fragment fragment : customTemplate.getAllFragments()) {
            fragments.addFragment(fragment);
        }
        template.setFragments(fragments);

        return template;
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
