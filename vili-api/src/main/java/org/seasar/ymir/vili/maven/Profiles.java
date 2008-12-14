package org.seasar.ymir.vili.maven;

import java.util.LinkedHashSet;
import java.util.Set;

import net.skirnir.xom.annotation.Child;

public class Profiles {
    private Set<Profile> profiles = new LinkedHashSet<Profile>();

    public Profiles() {
    }

    public Profiles(Profile... profiles) {
        for (Profile profile : profiles) {
            addProfile(profile);
        }
    }

    public Profile[] getProfiles() {
        return profiles.toArray(new Profile[0]);
    }

    @Child
    public void addProfile(Profile profile) {
        profiles.add(profile);
    }
}
