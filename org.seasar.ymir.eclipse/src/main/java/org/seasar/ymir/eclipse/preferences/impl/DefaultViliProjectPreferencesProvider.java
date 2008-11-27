package org.seasar.ymir.eclipse.preferences.impl;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;

public class DefaultViliProjectPreferencesProvider extends ViliProjectPreferencesProviderBase {
    private static final int DEFAULT_DATABASE_INDEX = 0;

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
}