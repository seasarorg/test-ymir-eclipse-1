package org.seasar.ymir.eclipse;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.seasar.ymir.vili.Configurator;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;

public class NullConfigurator implements Configurator {
    public void start(IProject project, ViliBehavior behavior, ViliProjectPreferences preferences) {
    }

    public Map<String, Object> createAdditionalParameters(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> artifactParameters) {
        return null;
    }
}
