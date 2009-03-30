package org.seasar.ymir.eclipse.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ui.MoldParametersControl;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.ViliProjectPreferences;

public class MoldParametersPropertyPage extends PropertyPage {
    private IProject project;

    private ViliProjectPreferences preferences;

    private MoldParametersControl control;

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        try {
            project = getProject();
            preferences = Activator.getDefault().getViliProjectPreferences(project);
            control = new MoldParametersControl(parent, project, preferences, Activator.getDefault()
                    .getProjectRelative(project).getMolds()) {
                @Override
                public void setErrorMessage(String message) {
                    MoldParametersPropertyPage.this.setErrorMessage(message);
                }
            };
        } catch (CoreException ex) {
            throw new RuntimeException(ex);
        }

        Control ctl = control.createControl();

        control.resumeValues();

        return ctl;
    }

    protected void performDefaults() {
        control.setDefaultValues();
    }

    public boolean performOk() {
        boolean succeed = true;
        for (Mold mold : control.getMolds()) {
            if (!mold.getBehavior().getConfigurator().saveParameters(project, mold, preferences,
                    mold.getParameterMap(),
                    Activator.getDefault().getProjectBuilder().getMoldPreferenceStore(project, mold))) {
                succeed = false;
            }
        }

        return succeed;
    }

    public IProject getProject() throws CoreException {
        IAdaptable element = getElement();
        if (element instanceof IJavaProject) {
            return ((IJavaProject) element).getProject();
        } else if (element instanceof IResource) {
            return ((IResource) element).getProject();
        } else {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, 0,
                    "Selected element is not a project", null)); //$NON-NLS-1$
        }
    }
}