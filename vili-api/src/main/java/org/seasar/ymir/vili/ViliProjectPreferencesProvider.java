package org.seasar.ymir.vili;

import org.eclipse.core.runtime.IPath;
import org.seasar.kvasir.util.collection.MapProperties;

public interface ViliProjectPreferencesProvider {
    boolean isProjectSpecificTemplateEnabled();

    String getTemplate();

    String getRootPackageName();

    String getViewEncoding();

    Database[] getDatabaseEntries();

    boolean isUseDatabase();

    Database getDatabaseEntry();

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
