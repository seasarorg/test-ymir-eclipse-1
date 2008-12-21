package org.seasar.ymir.eclipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IActionDelegate;
import org.eclipse.ui.IObjectActionDelegate;
import org.eclipse.ui.IWorkbenchPart;
import org.seasar.eclipse.common.util.AdaptableUtil;
import org.seasar.ymir.eclipse.popup.dialogs.AddFragmentsWizardDialog;

public class AddFragmentsAction implements IObjectActionDelegate {
    private IProject project;

    /**
     * @see IObjectActionDelegate#setActivePart(IAction, IWorkbenchPart)
     */
    public void setActivePart(IAction action, IWorkbenchPart targetPart) {
    }

    /**
     * @see IActionDelegate#run(IAction)
     */
    public void run(IAction action) {
        Shell shell = new Shell();
        AddFragmentsWizardDialog dialog = new AddFragmentsWizardDialog(shell, project);
        dialog.open();
    }

    /**
     * @see IActionDelegate#selectionChanged(IAction, ISelection)
     */
    public void selectionChanged(IAction action, ISelection selection) {
        if (selection instanceof IStructuredSelection) {
            Object o = ((IStructuredSelection) selection).getFirstElement();
            project = AdaptableUtil.toProject(o);
            if (project == null) {
                IResource r = AdaptableUtil.toResource(o);
                if (r != null) {
                    project = r.getProject();
                }
            }
        }
    }
}
