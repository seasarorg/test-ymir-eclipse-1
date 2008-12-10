package org.seasar.ymir.eclipse.popup.dialogs;

import org.eclipse.core.resources.IProject;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.swt.widgets.Shell;
import org.seasar.ymir.eclipse.wizards.AddFeaturesWizard;

public class AddFeaturesWizardDialog extends WizardDialog {
    public AddFeaturesWizardDialog(Shell parentShell, IProject project) {
        super(parentShell, new AddFeaturesWizard(project));
    }
}
