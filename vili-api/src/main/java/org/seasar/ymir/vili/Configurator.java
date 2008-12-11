package org.seasar.ymir.vili;

import java.util.Map;

public interface Configurator {
    Map<String, Object> createAdditionalParameters(ViliBehavior behavior);
}
