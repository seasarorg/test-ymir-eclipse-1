package org.seasar.ymir.eclipse;

import java.util.Map;

import org.seasar.ymir.vili.Configurator;
import org.seasar.ymir.vili.ViliBehavior;

public class NullConfigurator implements Configurator {
    public Map<String, Object> createAdditionalParameters(ViliBehavior behavior) {
        return null;
    }
}
