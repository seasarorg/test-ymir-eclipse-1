package org.seasar.ymir.vili;

import java.io.InputStream;
import java.net.URL;
import java.util.Map;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.Project;

public interface ProjectBuilder {
    void createProject(IProject project, IPath locationPath,
            IPath jreContainerPath, Mold skeleton,
            ViliProjectPreferences preferences, IProgressMonitor monitor)
            throws CoreException;

    void addFragments(IProject project, ViliProjectPreferences preferences,
            Mold[] fragments, IProgressMonitor monitor) throws CoreException;

    void expandMold(IProject project, ViliProjectPreferences preferences,
            Mold mold, Map<String, Object> parameters, IProgressMonitor monitor)
            throws CoreException;

    String evaluateTemplate(String path, Map<String, Object> parameterMap)
            throws CoreException;

    String evaluate(String content, Map<String, Object> parameterMap)
            throws CoreException;

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

    void updatePom(IProject project, Project pom, IProgressMonitor monitor)
            throws CoreException;

    Dependency getDependency(IProject project, String groupId, String artifactId)
            throws CoreException;

    WizardDialog createAddFragmentsWizardDialog(Shell parentShell,
            IProject project, Mold... fragmentMolds);
}
