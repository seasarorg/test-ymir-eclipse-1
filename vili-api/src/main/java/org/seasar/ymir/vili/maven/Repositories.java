package org.seasar.ymir.vili.maven;

import java.util.LinkedHashSet;
import java.util.Set;

import net.skirnir.xom.annotation.Child;

public class Repositories {
    private Set<Repository> repositories = new LinkedHashSet<Repository>();

    public Repositories() {
    }

    public Repositories(Repository... repositories) {
        for (Repository repository : repositories) {
            addRepository(repository);
        }
    }

    public Repository[] getRepositories() {
        return repositories.toArray(new Repository[0]);
    }

    @Child
    public void addRepository(Repository repository) {
        repositories.add(repository);
    }
}
