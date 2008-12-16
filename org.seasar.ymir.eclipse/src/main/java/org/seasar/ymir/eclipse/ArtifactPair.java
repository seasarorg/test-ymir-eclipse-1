package org.seasar.ymir.eclipse;

import java.io.IOException;
import java.util.Map;

import org.seasar.ymir.vili.ViliBehavior;

import werkzeugkasten.mvnhack.repository.Artifact;

public class ArtifactPair {
    private Artifact artifact;

    private ClassLoader projectClassLoader;

    private ViliBehavior behavior;

    private Map<String, Object> parameterMap;

    public static ArtifactPair newInstance(Artifact artifact, ClassLoader projectClassLoader) {
        if (artifact == null) {
            return null;
        } else {
            return new ArtifactPair(artifact, projectClassLoader);
        }
    }

    private ArtifactPair(Artifact artifact, ClassLoader projectClassLoader) {
        this.artifact = artifact;
        this.projectClassLoader = projectClassLoader;
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
                behavior = new ViliBehaviorImpl(artifact, projectClassLoader);
            } catch (IOException ex) {
                throw new RuntimeException("Can't load vili-behavior: " + artifact, ex); //$NON-NLS-1$
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
