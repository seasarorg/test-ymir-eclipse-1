package org.seasar.ymir.vili;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.kvasir.util.collection.MapProperties;

public interface ProjectBuilder {
    void createProject(IProject project, IPath locationPath,
            IPath jreContainerPath, ArtifactPair skeleton,
            ViliProjectPreferences preferences, IProgressMonitor monitor)
            throws CoreException;

    void addFragments(IProject project, ViliProjectPreferences preferences,
            ArtifactPair[] fragments, IProgressMonitor monitor)
            throws CoreException;

    void expandArtifact(IProject project, ViliProjectPreferences preferences,
            ArtifactPair pair, Map<String, Object> parameters,
            IProgressMonitor monitor) throws IOException, CoreException;

    String evaluateTemplate(String path, Map<String, Object> parameterMap)
            throws IOException;

    void writeFile(IFile file, String body, IProgressMonitor monitor)
            throws CoreException;

    void writeFile(IFile file, InputStream is, IProgressMonitor monitor)
            throws CoreException;

    void mkdirs(IResource container, IProgressMonitor monitor)
            throws CoreException;

    void mergeProperties(IFile file, URL entry, IProgressMonitor monitor)
            throws CoreException;

    void mergeProperties(IFile file, MapProperties properties,
            IProgressMonitor monitor) throws CoreException;

    MapProperties loadApplicationProperties(IProject project)
            throws CoreException;

    void saveApplicationProperties(IProject project, MapProperties properties,
            boolean merge) throws CoreException;
}
