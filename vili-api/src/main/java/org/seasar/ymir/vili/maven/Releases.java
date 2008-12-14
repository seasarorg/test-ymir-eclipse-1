package org.seasar.ymir.vili.maven;

import net.skirnir.xom.annotation.Child;

public class Releases {
    private String enabled;

    public Releases() {
    }

    public Releases(boolean enabled) {
        this.enabled = String.valueOf(enabled);
    }

    public String getEnabled() {
        return enabled;
    }

    @Child
    public void setEnabled(String enabled) {
        this.enabled = enabled;
    }
}
