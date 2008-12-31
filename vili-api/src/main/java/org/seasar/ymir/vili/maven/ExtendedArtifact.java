package org.seasar.ymir.vili.maven;

import werkzeugkasten.mvnhack.repository.Artifact;

public interface ExtendedArtifact extends Artifact {
    String getActualVersion();

    long getLastUpdated();
}
