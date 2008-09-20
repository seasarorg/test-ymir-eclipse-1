package org.seasar.ymir.eclipse.maven.impl;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.Configuration;

public class NonTransitiveContext extends ExtendedContext {
    public NonTransitiveContext(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void resolveDependencies(Artifact a) {
        // Do nothing
    }
}
