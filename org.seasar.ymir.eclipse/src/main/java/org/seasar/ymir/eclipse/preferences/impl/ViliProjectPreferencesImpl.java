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
import org.seasar.ymir.eclipse.PlatformDelegate;
import org.seasar.ymir.eclipse.natures.ViliNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferencesProvider;
import org.seasar.ymir.eclipse.util.MapAdapter;

public class ViliProjectPreferencesImpl implements ViliProjectPreferences {
    private ViliProjectPreferencesProvider provider;

    private boolean projectSpecificTemplateEnabled;

    private String template;

    private String rootPackageName;

    private String viewEncoding;

    private boolean useDatabase;

    private DatabaseEntry databaseEntry;

    public ViliProjectPreferencesImpl(ViliProjectPreferencesProvider provider) {
        this.provider = provider;

        projectSpecificTemplateEnabled = provider.isProjectSpecificTemplateEnabled();
        template = provider.getTemplate();
        rootPackageName = provider.getRootPackageName();
        viewEncoding = provider.getViewEncoding();
        useDatabase = provider.isUseDatabase();
        databaseEntry = provider.getDatabaseEntry();
    }

    public boolean isProjectSpecificTemplateEnabled() {
        return projectSpecificTemplateEnabled;
    }

    public String getTemplate() {
        return template;
    }

    public String getRootPackageName() {
        return rootPackageName;
    }

    public String getRootPackagePath() {
        // TODO Auto-generated method stub
        return null;
    }

    public String getViewEncoding() {
        return viewEncoding;
    }

    public DatabaseEntry[] getDatabaseEntries() {
        return provider.getDatabaseEntries();
    }

    public boolean isUseDatabase() {
        return useDatabase;
    }

    public DatabaseEntry getDatabaseEntry() {
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

    public void setUseDatabase(boolean useDatabase) {
        this.useDatabase = useDatabase;
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

        store.putValue(ParameterKeys.DATABASE_DRIVER_CLASS_NAME, databaseEntry.getDriverClassName());
        store.putValue(ParameterKeys.DATABASE_PASSWORD, databaseEntry.getPassword());
        store.putValue(ParameterKeys.DATABASE_NAME, databaseEntry.getName());
        store.putValue(ParameterKeys.DATABASE_TYPE, databaseEntry.getType());
        store.putValue(ParameterKeys.DATABASE_URL, databaseEntry.getURL());
        store.putValue(ParameterKeys.USE_DATABASE, String.valueOf(useDatabase));
        store.putValue(ParameterKeys.DATABASE_USER, databaseEntry.getUser());
        if (isYmirProject) {
            properties.setProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME, rootPackageName);
        } else {
            store.putValue(ParameterKeys.ROOT_PACKAGE_NAME, rootPackageName);
        }
        store.putValue(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED, String
                .valueOf(projectSpecificTemplateEnabled));
        if (isProjectSpecificTemplateEnabled()) {
            store.putValue(PreferenceConstants.P_TEMPLATE, template);
        } else {
            store.setToDefault(PreferenceConstants.P_TEMPLATE);
        }
        store.putValue(ParameterKeys.VIEW_ENCODING, viewEncoding);

        ((IPersistentPreferenceStore) store).save();
        if (isYmirProject) {
            Activator.getDefault().saveApplicationProperties(project, properties);
        }
    }

    public String getSlash() {
        return provider.getSlash();
    }

    public String getDollar() {
        return provider.getDollar();
    }

    public String getProjectName() {
        return provider.getProjectName();
    }

    public String getGroupId() {
        return provider.getGroupId();
    }

    public String getArtifactId() {
        return provider.getArtifactId();
    }

    public String getVersion() {
        return provider.getVersion();
    }

    public String getJREVersion() {
        return provider.getJREVersion();
    }

    public PlatformDelegate getPlatform() {
        return provider.getPlatform();
    }

    public String getFieldPrefix() {
        return provider.getFieldPrefix();
    }

    public String getFieldSuffix() {
        return provider.getFieldSuffix();
    }

    public String getFieldSpecialPrefix() {
        return provider.getFieldSpecialPrefix();
    }

    public MapAdapter getYmir() {
        return provider.getYmir();
    }
}
