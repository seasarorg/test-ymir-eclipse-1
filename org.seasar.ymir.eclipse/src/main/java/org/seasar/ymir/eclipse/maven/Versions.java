package org.seasar.ymir.eclipse.maven;

import java.util.ArrayList;
import java.util.List;

import net.skirnir.xom.annotation.Child;

public class Versions {
    private List<String> versionList = new ArrayList<String>();

    public String[] getVersions() {
        return versionList.toArray(new String[0]);
    }

    @Child
    public void addVersion(String version) {
        versionList.add(version);
    }
}
