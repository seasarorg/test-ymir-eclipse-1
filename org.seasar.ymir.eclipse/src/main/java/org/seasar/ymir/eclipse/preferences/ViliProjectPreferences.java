package org.seasar.ymir.eclipse.preferences;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.seasar.ymir.eclipse.DatabaseEntry;

public interface ViliProjectPreferences extends ViliProjectPreferencesProvider {
    void setProjectSpecificTemplateEnabled(boolean projectSpecificTemplateEnabled);

    void setTemplate(String template);

    void setRootPackageName(String rootPackageName);

    void setViewEncoding(String viewEncoding);

    void setUseDatabase(boolean useDatabase);

    void setDatabaseEntry(DatabaseEntry entry);

    void save(IProject project) throws IOException;
}
