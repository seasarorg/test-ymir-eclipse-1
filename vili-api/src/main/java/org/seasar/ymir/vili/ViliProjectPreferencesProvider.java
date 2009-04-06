package org.seasar.ymir.vili;

import org.eclipse.core.runtime.IPath;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.vili.model.Database;

public interface ViliProjectPreferencesProvider {
    String DEFAULT_VIEWENCODING = "UTF-8";

    boolean isProjectSpecificTemplateEnabled();

    String getTemplate();

    String getRootPackageName();

    String[] getRootPackageNames();

    String getViewEncoding();

    Database[] getDatabases();

    boolean isUseDatabase();

    Database getDatabase();

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
