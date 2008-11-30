package org.seasar.ymir.eclipse.preferences.impl;

import java.util.TreeMap;

import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.util.JdtUtils;
import org.seasar.ymir.eclipse.util.MapAdapter;

public class DefaultViliProjectPreferencesProvider extends ViliProjectPreferencesProviderBase {
    private static final int DEFAULT_DATABASE_INDEX = 0;

    @SuppressWarnings("unchecked")
    private MapAdapter ymir = new MapAdapter(new MapProperties(new TreeMap()));

    public boolean isProjectSpecificTemplateEnabled() {
        return false;
    }

    public String getTemplate() {
        return Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_TEMPLATE);
    }

    public String getRootPackageName() {
        return ""; //$NON-NLS-1$
    }

    public String getViewEncoding() {
        return "UTF-8"; //$NON-NLS-1$
    }

    public boolean isUseDatabase() {
        return true;
    }

    public DatabaseEntry getDatabaseEntry() {
        return databaseEntries[DEFAULT_DATABASE_INDEX];
    }

    public String getProjectName() {
        return "";
    }

    public String getGroupId() {
        return "";
    }

    public String getArtifactId() {
        return "";
    }

    public String getVersion() {
        return "0.0.1-SNAPSHOT";
    }

    public String getJREVersion() {
        return null;
    }

    public String getFieldPrefix() {
        return JdtUtils.getFieldPrefix();
    }

    public String getFieldSuffix() {
        return JdtUtils.getFieldSuffix();
    }

    public MapAdapter getYmir() {
        return ymir;
    }
}