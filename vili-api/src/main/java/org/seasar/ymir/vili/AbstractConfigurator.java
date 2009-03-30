package org.seasar.ymir.vili;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
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

    public boolean saveParameters(IProject project, Mold mold,
            ViliProjectPreferences preferences, Map<String, Object> parameters,
            IPersistentPreferenceStore store) {
        for (Map.Entry<String, Object> entry : parameters.entrySet()) {
            store.setValue(entry.getKey(), entry.getValue().toString());
        }
        try {
            store.save();
            return true;
        } catch (IOException ex) {
            Activator.log("Can't save parameters for mold " + mold
                    + ": project=" + project, ex);
            return false;
        }
    }

    public Map<String, Object> resumeParameters(IProject project, Mold mold,
            ViliProjectPreferences preferences) {
        return new HashMap<String, Object>();
    }
}
