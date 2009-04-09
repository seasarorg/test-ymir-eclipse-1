package org.seasar.ymir.eclipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.util.WorkbenchUtils;

abstract public class AbstractOpenURLAction extends AbstractWorkbenchWindowActionDelegate {
    private static final String KEY_BASEDIR = "baseDir"; //$NON-NLS-1$

    private static final String KEY_CONTEXTNAME = "contextName"; //$NON-NLS-1$

    private static final String KEY_HTTPPORT = "httpPort"; //$NON-NLS-1$

    @Override
    protected void processResource(IProject project, IResource resource) throws Exception {
        IPreferenceStore store = getPreferenceStore(project);
        IPath p = resource.getFullPath();
        IPath baseDir = new Path(getBaseDir(store));
        if (baseDir.isPrefixOf(p)) {
            openUrl(getBaseURL(store).append(p.removeFirstSegments(baseDir.segmentCount())).toString());
        }
    }

    protected void openUrl(String url) throws CoreException {
        WorkbenchUtils.openUrl(url);
    }

    final protected IPath getBaseURL(IPreferenceStore store) {
        String contextName = getContextName(store);
        String httpPort = getHttpPort(store);
        return new Path("http://localhost:" + httpPort).append(contextName); //$NON-NLS-1$
    }

    final protected String getHttpPort(IPreferenceStore store) {
        return store.getString(KEY_HTTPPORT);
    }

    final protected String getContextName(IPreferenceStore store) {
        return store.getString(KEY_CONTEXTNAME);
    }

    final protected String getBaseDir(IPreferenceStore store) {
        return store.getString(KEY_BASEDIR);
    }

    final protected IPreferenceStore getPreferenceStore(IProject project) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(new ProjectScope(project),
                Globals.BUNDLENAME_WEBLAUNCHER);
        store.setSearchContexts(new IScopeContext[] { new ProjectScope(project), new InstanceScope() });
        return store;
    }
}
