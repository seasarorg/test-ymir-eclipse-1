package org.seasar.ymir.eclipse.maven.impl;

import org.seasar.ymir.vili.maven.ExtendedConfiguration;

import werkzeugkasten.mvnhack.repository.Artifact;

public class NonTransitiveContext extends DefaultExtendedContext {
    public NonTransitiveContext(ExtendedConfiguration configuration) {
        super(configuration);
    }

    @Override
    protected void resolveDependencies(Artifact a) {
        // Do nothing
    }
}
