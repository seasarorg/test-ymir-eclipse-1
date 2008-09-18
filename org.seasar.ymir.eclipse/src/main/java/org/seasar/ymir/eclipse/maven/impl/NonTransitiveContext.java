package org.seasar.ymir.eclipse.maven.impl;

import werkzeugkasten.mvnhack.repository.Artifact;
import werkzeugkasten.mvnhack.repository.Configuration;
import werkzeugkasten.mvnhack.repository.impl.DefaultContext;

public class NonTransitiveContext extends DefaultContext {
    public NonTransitiveContext(Configuration configuration) {
        super(configuration);
    }

    @Override
    protected void resolveDependencies(Artifact a) {
        // Do nothing
    }
}
