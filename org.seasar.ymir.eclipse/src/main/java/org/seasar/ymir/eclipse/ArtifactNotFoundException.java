package org.seasar.ymir.eclipse;

public class ArtifactNotFoundException extends Exception {
    private static final long serialVersionUID = 1L;

    public ArtifactNotFoundException() {
    }

    public ArtifactNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }

    public ArtifactNotFoundException(String message) {
        super(message);
    }

    public ArtifactNotFoundException(Throwable cause) {
        super(cause);
    }
}
