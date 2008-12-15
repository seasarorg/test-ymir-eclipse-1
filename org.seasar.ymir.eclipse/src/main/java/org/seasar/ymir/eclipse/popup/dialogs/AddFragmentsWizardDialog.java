package org.seasar.ymir.eclipse.popup.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Shell;
import org.seasar.ymir.eclipse.wizards.AddFragmentsWizard;

public class AddFragmentsWizardDialog extends WizardDialog {
    public AddFragmentsWizardDialog(Shell parentShell, IProject project) {
        super(parentShell, new AddFragmentsWizard(project));
    }

    @Override
    protected Point getInitialSize() {
        return new Point(600, 558);
    }
}
