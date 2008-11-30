package org.seasar.ymir.eclipse.preferences.impl;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

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
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.maven.Dependency;
import org.seasar.ymir.eclipse.natures.ViliNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.util.JdtUtils;
import org.seasar.ymir.eclipse.util.MapAdapter;

public class ViliProjectPreferencesProviderImpl extends ViliProjectPreferencesProviderBase {
    private static final String PATH_JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    private static final Map<String, String> JRE_VERSION_MAP;

    private static final String DEFAULT_JREVERSION = "1.6";

    private IProject project;

    private IJavaProject javaProject;

    private IPreferenceStore store;

    private boolean isYmirProject;

    private MapAdapter ymir;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("J2SE-1.3", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("J2SE-1.4", "1.4"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("J2SE-1.5", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("JavaSE-1.6", "1.6"); //$NON-NLS-1$ //$NON-NLS-2$
        JRE_VERSION_MAP = Collections.unmodifiableMap(map);
    }

    public ViliProjectPreferencesProviderImpl(IProject project) throws CoreException {
        this.project = project;
        javaProject = JavaCore.create(project);
        this.store = Activator.getDefault().getPreferenceStore(project);
        isYmirProject = project.hasNature(ViliNature.ID);
        if (isYmirProject) {
            ymir = new MapAdapter(Activator.getDefault().loadApplicationProperties(project));
        } else {
            ymir = new MapAdapter(new MapProperties());
        }
    }

    public boolean isProjectSpecificTemplateEnabled() {
        return store.getBoolean(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED);
    }

    public String getTemplate() {
        return store.getString(PreferenceConstants.P_TEMPLATE);
    }

    public String getRootPackageName() {
        if (isYmirProject) {
            return ymir.get(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME);
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

    public DatabaseEntry getDatabaseEntry() {
        String type = store.getString(ParameterKeys.DATABASE_TYPE);
        return new DatabaseEntry(store.getString(ParameterKeys.DATABASE_NAME), type, store
                .getString(ParameterKeys.DATABASE_DRIVER_CLASS_NAME), store.getString(ParameterKeys.DATABASE_URL),
                store.getString(ParameterKeys.DATABASE_USER), store.getString(ParameterKeys.DATABASE_PASSWORD),
                getDatabaseDependency(type));
    }

    private Dependency getDatabaseDependency(String type) {
        for (DatabaseEntry entry : getDatabaseEntries()) {
            if (entry.getType().equals(type)) {
                return entry.getDependency();
            }
        }
        return null;
    }

    public String getProjectName() {
        return project.getName();
    }

    public String getGroupId() {
        // TODO XOMを使ってpom.xmlからgroupIdを取り出すメソッドを作成する。
        return "";
    }

    public String getArtifactId() {
        // TODO
        return "";
    }

    public String getVersion() {
        // TODO
        return "0.0.1-SNAPSHOT";
    }

    public String getJREVersion() {
        try {
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                IPath path = entry.getPath();
                if (entry.getEntryKind() == IClasspathEntry.CPE_CONTAINER && PATH_JRE_CONTAINER.equals(path.segment(0))) {
                    String version = JRE_VERSION_MAP.get(path.lastSegment());
                    if (version != null) {
                        return version;
                    } else {
                        return DEFAULT_JREVERSION;
                    }
                }
            }
        } catch (JavaModelException ignore) {
        }
        return DEFAULT_JREVERSION;
    }

    public String getFieldPrefix() {
        return JdtUtils.getFieldPrefix(project);
    }

    public String getFieldSuffix() {
        return JdtUtils.getFieldSuffix(project);
    }

    public MapAdapter getYmir() {
        return ymir;
    }
}
