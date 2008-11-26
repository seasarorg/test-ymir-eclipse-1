package org.seasar.ymir.eclipse.preferences.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.maven.Dependency;
import org.seasar.ymir.eclipse.natures.ViliNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;

public class ViliProjectPreferencesProviderImpl extends ViliProjectPreferencesProviderBase {
    private IPreferenceStore store;

    private boolean isYmirProject;

    private MapProperties properties;

    public ViliProjectPreferencesProviderImpl(IProject project) throws CoreException {
        this.store = Activator.getDefault().getPreferenceStore(project);
        isYmirProject = project.hasNature(ViliNature.ID);
        properties = Activator.getDefault().loadApplicationProperties(project);
    }

    public boolean isProjectSpecificTemplateEnabled() {
        return store.getBoolean(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED);
    }

    public String getTemplate() {
        return store.getString(PreferenceConstants.P_TEMPLATE);
    }

    public String getRootPackageName() {
        if (isYmirProject) {
            return properties.getProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME);
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
}
