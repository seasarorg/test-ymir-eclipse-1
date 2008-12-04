package org.seasar.ymir.eclipse.preferences;

import org.eclipse.core.runtime.IPath;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.DatabaseEntry;

public interface ViliProjectPreferencesProvider {
    boolean isProjectSpecificTemplateEnabled();

    String getTemplate();

    String getRootPackageName();

    String getViewEncoding();

    DatabaseEntry[] getDatabaseEntries();

    boolean isUseDatabase();

    DatabaseEntry getDatabaseEntry();

    String getProjectName();

    String getGroupId();

    String getArtifactId();

    String getVersion();

    IPath getJREContainerPath();

    String getFieldPrefix();

    String getFieldSuffix();

    String getFieldSpecialPrefix();

    MapProperties getApplicationProperties();
}
