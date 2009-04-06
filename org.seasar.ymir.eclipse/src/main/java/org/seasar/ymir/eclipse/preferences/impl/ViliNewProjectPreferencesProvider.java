package org.seasar.ymir.eclipse.preferences.impl;

import java.util.TreeMap;

import org.eclipse.core.runtime.IPath;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.vili.model.Database;
import org.seasar.ymir.vili.util.JdtUtils;

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

    public String[] getRootPackageNames() {
        return new String[0];
    }

    public String getViewEncoding() {
        return ""; //$NON-NLS-1$
    }

    public boolean isUseDatabase() {
        return true;
    }

    public Database getDatabase() {
        return new Database("", "", "", "", "", "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
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