package org.seasar.ymir.eclipse.preferences;

import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.PlatformDelegate;
import org.seasar.ymir.eclipse.util.MapAdapter;

public interface ViliProjectPreferencesProvider {
    boolean isProjectSpecificTemplateEnabled();

    String getTemplate();

    String getRootPackageName();

    String getViewEncoding();

    DatabaseEntry[] getDatabaseEntries();

    boolean isUseDatabase();

    DatabaseEntry getDatabaseEntry();

    String getSlash();

    String getDollar();

    String getProjectName();

    String getRootPackagePath();

    String getGroupId();

    String getArtifactId();

    String getVersion();

    String getJREVersion();

    PlatformDelegate getPlatform();

    String getFieldPrefix();

    String getFieldSuffix();

    String getFieldSpecialPrefix();

    MapAdapter getYmir();
}
