package org.seasar.ymir.eclipse;

import java.util.Map;

import org.seasar.ymir.vili.Configurator;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;

public class NullConfigurator implements Configurator {
    public Map<String, Object> createAdditionalParameters(ViliBehavior behavior, ViliProjectPreferences preferences) {
        return null;
    }
}
