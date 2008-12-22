package org.seasar.ymir.vili;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;

abstract public class AbstractConfigurator implements Configurator {
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

    public void processAfterExpanded(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor) {
    }
}
