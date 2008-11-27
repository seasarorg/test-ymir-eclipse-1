package org.seasar.ymir.eclipse.natures;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.runtime.CoreException;
import org.seasar.ymir.eclipse.Activator;

public class ViliNature implements IProjectNature {
    public static final String ID = Activator.PLUGIN_ID + ".viliNature"; //$NON-NLS-1$

    private IProject project;

    public void configure() throws CoreException {
    }

    public void deconfigure() throws CoreException {
    }

    public IProject getProject() {
        return project;
    }

    public void setProject(IProject project) {
        this.project = project;
    }
}
