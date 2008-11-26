package org.seasar.ymir.eclipse.preferences;

import org.seasar.ymir.eclipse.DatabaseEntry;

public interface ViliProjectPreferencesProvider {
    boolean isProjectSpecificTemplateEnabled();

    String getTemplate();

    String getRootPackageName();

    String getViewEncoding();

    DatabaseEntry[] getDatabaseEntries();

    boolean isUseDatabase();

    DatabaseEntry getDatabaseEntry();
}
