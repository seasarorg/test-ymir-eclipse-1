package org.seasar.ymir.eclipse.maven.impl;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.Configuration;
import werkzeugkasten.mvnhack.repository.impl.DefaultContext;

public class NonRecursiveContext extends DefaultContext {
    public NonRecursiveContext(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void resolveDependencies(Artifact a) {
        // Do nothing
    }
}
