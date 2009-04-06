package org.seasar.ymir.vili;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.model.Database;

public interface ViliProjectPreferences extends ViliProjectPreferencesProvider {
    void setProjectSpecificTemplateEnabled(
            boolean projectSpecificTemplateEnabled);

    void setTemplate(String template);

    void setRootPackageNames(String[] rootPackageNames);

    void setViewEncoding(String viewEncoding);

    void setUseDatabase(boolean useDatabase);

    void setProjectName(String projectName);

    void setGroupId(String groupId);

    void setArtifactId(String artifactId);

    void setVersion(String version);

    void setJREContainerPath(IPath jreContainerPath);

    void setApplicationProperties(MapProperties applicationProperties);

    void save(IProject project) throws CoreException;

    String getSlash();

    String getDollar();

    String getJREVersion();

    String getRootPackagePath();

    PlatformDelegate getPlatform();

    Database getDatabase();

    ArtifactVersion getViliVersion();
}
