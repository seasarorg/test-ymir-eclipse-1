package org.seasar.ymir.eclipse;

import java.io.IOException;
import java.util.Map;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ArtifactPair {
    private Artifact artifact;

    private ViliBehavior behavior;

    private Map<String, Object> parameterMap;

    public static ArtifactPair newInstance(Artifact artifact) {
        if (artifact == null) {
            return null;
        } else {
            return new ArtifactPair(artifact);
        }
    }

    private ArtifactPair(Artifact artifact) {
        this.artifact = artifact;
    }

    @Override
    public String toString() {
        return artifact.toString();
    }

    public Artifact getArtifact() {
        return artifact;
    }

    public ViliBehavior getBehavior() {
        if (behavior == null) {
            try {
                behavior = new ViliBehavior(artifact);
            } catch (IOException ex) {
                throw new RuntimeException("Can't load vili-behavior: " + artifact, ex);
            }
        }
        return behavior;
    }

    public Map<String, Object> getParameterMap() {
        return parameterMap;
    }

    public void setParameterMap(Map<String, Object> parameterMap) {
        this.parameterMap = parameterMap;
    }
}