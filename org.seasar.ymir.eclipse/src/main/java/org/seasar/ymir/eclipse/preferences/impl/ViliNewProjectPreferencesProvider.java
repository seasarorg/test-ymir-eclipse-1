package org.seasar.ymir.eclipse.preferences.impl;

import java.util.TreeMap;

import org.eclipse.core.runtime.IPath;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.util.JdtUtils;

public class ViliNewProjectPreferencesProvider extends ViliProjectPreferencesProviderBase {
    @SuppressWarnings("unchecked")//$NON-NLS-1$
    private MapProperties applicationProperties = new MapProperties(new TreeMap());

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
        return ""; //$NON-NLS-1$
    }

    public boolean isUseDatabase() {
        return true;
    }

    public DatabaseEntry getDatabaseEntry() {
        return new DatabaseEntry("", "", "", "", "", "", null);
    }

    public String getProjectName() {
        return ""; //$NON-NLS-1$
    }

    public String getGroupId() {
        return ""; //$NON-NLS-1$
    }

    public String getArtifactId() {
        return ""; //$NON-NLS-1$
    }

    public String getVersion() {
        return "0.0.1-SNAPSHOT"; //$NON-NLS-1$
    }

    public IPath getJREContainerPath() {
        return null;
    }

    public String getFieldPrefix() {
        return JdtUtils.getFieldPrefix();
    }

    public String getFieldSuffix() {
        return JdtUtils.getFieldSuffix();
    }

    public MapProperties getApplicationProperties() {
        return applicationProperties;
    }
}