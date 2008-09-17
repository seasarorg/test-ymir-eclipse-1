package org.seasar.ymir.eclipse.maven;

import java.util.List;

import net.skirnir.xom.annotation.Child;

public class Dependencies {
    private List<Dependency> dependencies;

    public Dependency[] getDependencies() {
        return dependencies.toArray(new Dependency[0]);
    }

    @Child
    public void addDependency(Dependency dependency) {
        dependencies.add(dependency);
    }
}
