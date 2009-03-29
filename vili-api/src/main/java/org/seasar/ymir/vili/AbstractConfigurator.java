package org.seasar.ymir.vili;

import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.ymir.vili.model.maven.Dependency;

abstract public class AbstractConfigurator implements IConfigurator {
    public void start(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences) {
    }

    public void processBeforeExpanding(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor) {
        monitor.done();
    }

    public String adjustPath(String path, IProject project,
            ViliBehavior behavior, ViliProjectPreferences preferences,
            Map<String, Object> parameters) {
        return path;
    }

    public InclusionType shouldExpand(String path, String resolvedPath,
            IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        return InclusionType.UNDEFINED;
    }

    public Dependency[] mergePomDependencies(
            Map<Dependency, Dependency> dependencyMap,
            Map<Dependency, Dependency> fragmentDependencyMap,
            IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        return null;
    }

    public void processAfterExpanded(IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IProgressMonitor monitor) {
        monitor.done();
    }
}
