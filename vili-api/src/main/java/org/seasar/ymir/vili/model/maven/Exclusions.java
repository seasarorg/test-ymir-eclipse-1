package org.seasar.ymir.vili.model.maven;

import java.util.LinkedHashSet;
import java.util.Set;

import net.skirnir.xom.annotation.Child;

public class Exclusions {
    private Set<Exclusion> exclusions = new LinkedHashSet<Exclusion>();

    public Exclusions() {
    }

    public Exclusions(Exclusion... exclusions) {
        for (Exclusion exclusion : exclusions) {
            addExclusion(exclusion);
        }
    }

    public Exclusion[] getExclusions() {
        return exclusions.toArray(new Exclusion[0]);
    }

    @Child
    public void addExclusion(Exclusion exclusion) {
        exclusions.add(exclusion);
    }
}
