package org.seasar.ymir.vili;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.maven.ExtendedContext;

public interface MoldResolver {
    Mold resolveMold(String groupId, String artifactId, String version,
            MoldType moldType, ArtifactVersion viliVersion,
            boolean containsSnapshot, IProject project,
            ProcessContext processCtx, IProgressMonitor monitor)
            throws MoldTypeMismatchException, ViliVersionMismatchException;

    Mold resolveMold(ExtendedContext ctx, String groupId, String artifactId,
            String version, MoldType moldType, ArtifactVersion viliVersion,
            boolean containsSnapshot, IProject project,
            ProcessContext processCtx, IProgressMonitor monitor)
            throws MoldTypeMismatchException, ViliVersionMismatchException;
}
