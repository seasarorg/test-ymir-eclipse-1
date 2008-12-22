package org.seasar.ymir.eclipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IActionDelegate;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.vili.model.Action;

public class ActionActionDelegate implements IActionDelegate {
    private Action model;

    private IProject project;

    public ActionActionDelegate(Action model, IProject project) {
        this.model = model;
        this.project = project;
    }

    public void run(IAction action) {
        try {
            model.newAction().run(project, Activator.getDefault().getViliProjectPreferences(project));
        } catch (Throwable t) {
            Activator.getDefault().log(t);
        }
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }
}
