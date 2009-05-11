package org.seasar.ymir.eclipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;

public class ViewSystemConsoleAction extends AbstractOpenURLAction {
    @Override
    protected void processResource(IProject project, IResource resource) throws Exception {
        openUrl(getBaseURL(getPreferenceStore(project)).toString() + "?__ymir__task=systemConsole");
    }
}
