package org.seasar.ymir.eclipse.preferences.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.natures.YmirProjectNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.util.JdtUtils;
import org.seasar.ymir.eclipse.util.XOMUtils;
import org.seasar.ymir.vili.model.Database;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.Project;

public class ViliProjectPreferencesProviderImpl extends ViliProjectPreferencesProviderBase {
    private static final String PATH_JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    private IProject project;

    private IJavaProject javaProject;

    private IPreferenceStore store;

    private boolean isYmirProject;

    private MapProperties applicationProperties;

    public ViliProjectPreferencesProviderImpl(IProject project) throws CoreException {
        this.project = project;
        javaProject = JavaCore.create(project);
        this.store = Activator.getDefault().getPreferenceStore(project);
        isYmirProject = project.hasNature(YmirProjectNature.ID);
        applicationProperties = Activator.getDefault().getProjectBuilder().loadApplicationProperties(project);
    }

    public boolean isProjectSpecificTemplateEnabled() {
        return store.getBoolean(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED);
    }

    public String getTemplate() {
        return store.getString(PreferenceConstants.P_TEMPLATE);
    }

    public String getRootPackageName() {
        if (isYmirProject) {
            return applicationProperties.getProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME);
        } else {
            return store.getString(ParameterKeys.ROOT_PACKAGE_NAME);
        }
    }

    public String getViewEncoding() {
        return store.getString(ParameterKeys.VIEW_ENCODING);
    }

    public boolean isUseDatabase() {
        return store.getBoolean(ParameterKeys.USE_DATABASE);
    }

    public Database getDatabase() {
        String type = store.getString(ParameterKeys.DATABASE_TYPE);
        return new Database(store.getString(ParameterKeys.DATABASE_NAME), type, store
                .getString(ParameterKeys.DATABASE_DRIVER_CLASS_NAME), store.getString(ParameterKeys.DATABASE_URL),
                store.getString(ParameterKeys.DATABASE_USER), store.getString(ParameterKeys.DATABASE_PASSWORD),
                getDatabaseDependency(type));
    }

    private Dependency getDatabaseDependency(String type) {
        for (Database database : getDatabases()) {
            if (database.getType().equals(type)) {
                return database.getDependency();
            }
        }
        return null;
    }

    public String getProjectName() {
        return project.getName();
    }

    public String getGroupId() {
        String groupId = null;
        try {
            Project pom = XOMUtils.getAsBean(project.getFile(Globals.PATH_POM_XML), Project.class);
            if (pom != null) {
                groupId = pom.findGroupId();
            }
        } catch (CoreException ex) {
            Activator.getDefault().log(ex);
        }
        if (groupId == null) {
            groupId = ""; //$NON-NLS-1$
        }
        return groupId;
    }

    public String getArtifactId() {
        String artifactId = null;
        try {
            Project pom = XOMUtils.getAsBean(project.getFile(Globals.PATH_POM_XML), Project.class);
            if (pom != null) {
                artifactId = pom.findArtifactId();
            }
        } catch (CoreException ex) {
            Activator.getDefault().log(ex);
        }
        if (artifactId == null) {
            artifactId = ""; //$NON-NLS-1$
        }
        return artifactId;
    }

    public String getVersion() {
        String version = null;
        try {
            Project pom = XOMUtils.getAsBean(project.getFile(Globals.PATH_POM_XML), Project.class);
            if (pom != null) {
                version = pom.findVersion();
            }
        } catch (CoreException ex) {
            Activator.getDefault().log(ex);
        }
        if (version == null) {
            version = ""; //$NON-NLS-1$
        }
        return version;
    }

    public IPath getJREContainerPath() {
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                IPath path = entry.getPath();
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && PATH_JRE_CONTAINER.equals(path.segment(0))) {
                    return path;
                }
            }
        } catch (JavaModelException ignore) {
        }
        return null;
    }

    public String getFieldPrefix() {
        return JdtUtils.getFieldPrefix(project);
    }

    public String getFieldSuffix() {
        return JdtUtils.getFieldSuffix(project);
    }

    public MapProperties getApplicationProperties() {
        return applicationProperties;
    }
}
