package org.seasar.ymir.vili;

import java.util.Map;

import org.eclipse.core.resources.IProject;

abstract public class AbstractConfigurator implements Configurator {
    public void start(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences) {
    }

    public Map<String, Object> createAdditionalParameters(IProject project,
            ViliBehavior behavior, ViliProjectPreferences preferences,
            Map<String, Object> artifactParameters) {
        return null;
    }
}
