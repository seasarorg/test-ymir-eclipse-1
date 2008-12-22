package org.seasar.ymir.vili;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

abstract public class AbstractConfigurator implements IConfigurator {
    public void start(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences) {
    }

    public Map<String, Object> createAdditionalParameters(IProject project,
            ViliBehavior behavior, ViliProjectPreferences preferences,
            Map<String, Object> artifactParameters) {
        return null;
    }

    public void processBeforeExpanding(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor) {
    }

    public InclusionType shouldExpand(String path, String resolvedPath,
            IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        return InclusionType.UNDEFINED;
    }

    public void processAfterExpanded(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor) {
    }
}
