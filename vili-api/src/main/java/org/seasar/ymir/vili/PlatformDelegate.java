package org.seasar.ymir.vili;

import org.osgi.framework.Bundle;

public interface PlatformDelegate {
    Bundle getBundle(String symbolicName);
}
