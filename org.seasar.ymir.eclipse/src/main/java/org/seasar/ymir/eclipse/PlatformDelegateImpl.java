package org.seasar.ymir.eclipse;

import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;
import org.seasar.ymir.vili.PlatformDelegate;

public class PlatformDelegateImpl implements PlatformDelegate {
    public Bundle getBundle(String symbolicName) {
        return Platform.getBundle(symbolicName);
    }
}
