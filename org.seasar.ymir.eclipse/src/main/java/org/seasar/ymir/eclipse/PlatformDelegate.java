package org.seasar.ymir.eclipse;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

public class PlatformDelegate {
    public Bundle getBundle(String symbolicName) {
        return Platform.getBundle(symbolicName);
    }
}
