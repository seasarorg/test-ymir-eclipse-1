package org.seasar.ymir.eclipse.maven;

import werkzeugkasten.mvnhack.repository.Artifact;

public interface ExtendedArtifact extends Artifact {
    String getActualVersion();

    long getLastUpdated();
}
