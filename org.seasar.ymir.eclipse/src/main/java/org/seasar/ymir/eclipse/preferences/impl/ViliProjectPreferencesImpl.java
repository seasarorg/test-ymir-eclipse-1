package org.seasar.ymir.eclipse.preferences.impl;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.natures.ViliNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferencesProvider;

public class ViliProjectPreferencesImpl implements ViliProjectPreferences {
    private ViliProjectPreferencesProvider provider;

    private Boolean projectSpecificTemplateEnabled;

    private String template;

    private String rootPackageName;

    private String viewEncoding;

    private Boolean useDatabase;

    private DatabaseEntry databaseEntry;

    public ViliProjectPreferencesImpl(ViliProjectPreferencesProvider provider) {
        this.provider = provider;
    }

    public boolean isProjectSpecificTemplateEnabled() {
        if (projectSpecificTemplateEnabled == null) {
            return provider.isProjectSpecificTemplateEnabled();
        }
        return projectSpecificTemplateEnabled.booleanValue();
    }

    public String getTemplate() {
        if (template == null) {
            return provider.getTemplate();
        }
        return template;
    }

    public String getRootPackageName() {
        if (rootPackageName == null) {
            return provider.getRootPackageName();
        }
        return rootPackageName;
    }

    public String getViewEncoding() {
        if (viewEncoding == null) {
            return provider.getViewEncoding();
        }
        return viewEncoding;
    }

    public DatabaseEntry[] getDatabaseEntries() {
        return provider.getDatabaseEntries();
    }

    public boolean isUseDatabase() {
        if (useDatabase == null) {
            return Boolean.valueOf(provider.isUseDatabase());
        }
        return useDatabase.booleanValue();
    }

    public DatabaseEntry getDatabaseEntry() {
        if (databaseEntry == null) {
            return provider.getDatabaseEntry();
        }
        return databaseEntry;
    }

    public void setProjectSpecificTemplateEnabled(boolean projectSpecificTemplateEnabled) {
        this.projectSpecificTemplateEnabled = projectSpecificTemplateEnabled;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
    }

    public void setViewEncoding(String viewEncoding) {
        this.viewEncoding = viewEncoding;
    }

    public void setUseDatabase(boolean databaseUsed) {
        this.useDatabase = Boolean.valueOf(databaseUsed);
    }

    public void setDatabaseEntry(DatabaseEntry databaseEntry) {
        this.databaseEntry = databaseEntry;
    }

    public void save(IProject project) throws IOException {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore(project);
        boolean isYmirProject;
        try {
            isYmirProject = project.hasNature(ViliNature.ID);
        } catch (CoreException ex) {
            isYmirProject = false;
        }
        MapProperties properties = Activator.getDefault().loadApplicationProperties(project);

        DatabaseEntry entry = getDatabaseEntry();
        store.putValue(ParameterKeys.DATABASE_DRIVER_CLASS_NAME, entry.getDriverClassName());
        store.putValue(ParameterKeys.DATABASE_PASSWORD, entry.getPassword());
        store.putValue(ParameterKeys.DATABASE_NAME, entry.getName());
        store.putValue(ParameterKeys.DATABASE_TYPE, entry.getType());
        store.putValue(ParameterKeys.DATABASE_URL, entry.getURL());
        store.putValue(ParameterKeys.USE_DATABASE, String.valueOf(isUseDatabase()));
        store.putValue(ParameterKeys.DATABASE_USER, entry.getUser());
        if (isYmirProject) {
            properties.setProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME, getRootPackageName());
        } else {
            store.putValue(ParameterKeys.ROOT_PACKAGE_NAME, getRootPackageName());
        }
        store.putValue(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED, String
                .valueOf(isProjectSpecificTemplateEnabled()));
        if (isProjectSpecificTemplateEnabled()) {
            store.putValue(PreferenceConstants.P_TEMPLATE, getTemplate());
        } else {
            store.setToDefault(PreferenceConstants.P_TEMPLATE);
        }
        store.putValue(ParameterKeys.VIEW_ENCODING, getViewEncoding());

        ((IPersistentPreferenceStore) store).save();
        if (isYmirProject) {
            Activator.getDefault().saveApplicationProperties(project, properties);
        }
    }
}
