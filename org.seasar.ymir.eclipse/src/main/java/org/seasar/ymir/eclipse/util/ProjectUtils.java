package org.seasar.ymir.eclipse.util;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;

public class ProjectUtils {
    private ProjectUtils() {
    }

    public static void addNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        Set<String> set = new LinkedHashSet<String>(Arrays.asList(description.getNatureIds()));
        set.add(natureId);
        description.setNatureIds(set.toArray(new String[0]));
        project.setDescription(description, monitor);
    }

    public static void removeNature(IProject project, String natureId, IProgressMonitor monitor) throws CoreException {
        IProjectDescription description = project.getDescription();
        Set<String> set = new LinkedHashSet<String>(Arrays.asList(description.getNatureIds()));
        set.remove(natureId);
        description.setNatureIds(set.toArray(new String[0]));
        project.setDescription(description, monitor);
    }
}
