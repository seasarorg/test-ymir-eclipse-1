package org.seasar.ymir.vili.model.maven;

import net.skirnir.xom.annotation.Child;
import net.skirnir.xom.annotation.Default;

public class Snapshot {
    private String timestamp;

    private Integer buildNumber;

    private boolean localCopy;

    public String getTimestamp() {
        return timestamp;
    }

    @Child(order = 1)
    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    public Integer getBuildNumber() {
        return buildNumber;
    }

    @Child(order = 2)
    public void setBuildNumber(Integer buildNumber) {
        this.buildNumber = buildNumber;
    }

    public boolean isLocalCopy() {
        return localCopy;
    }

    @Child(order = 3)
    @Default("false") //$NON-NLS-1$
    public void setLocalCopy(boolean localCopy) {
        this.localCopy = localCopy;
    }
}
