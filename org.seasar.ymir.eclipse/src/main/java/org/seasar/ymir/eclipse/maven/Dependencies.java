package org.seasar.ymir.eclipse.maven;

import java.util.LinkedHashSet;
import java.util.Set;

import net.skirnir.xom.annotation.Child;

public class Dependencies {
    private Set<Dependency> dependencies = new LinkedHashSet<Dependency>();

    public Dependencies() {
    }

    public Dependencies(Dependency... dependencies) {
        for (Dependency dependency : dependencies) {
            addDependency(dependency);
        }
    }

    public Dependency[] getDependencies() {
        return dependencies.toArray(new Dependency[0]);
    }

    @Child
    public void addDependency(Dependency dependency) {
        dependencies.add(dependency);
    }
}
