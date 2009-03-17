package org.seasar.ymir.eclipse.properties;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.ui.TemplateControl;

public class TemplatePropertyPage extends PropertyPage {
    private TemplateControl control;

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        IPreferenceStore store = getPreferenceStore();

        control = new TemplateControl(parent, true);
        Control ctl = control.createControl();

        control.setProjectSpecificSettingsEnabled(store
                .getBoolean(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED));
        control.setTemplate(store.getString(PreferenceConstants.P_TEMPLATE));

        return ctl;
    }

    protected void performDefaults() {
        control.setProjectSpecificSettingsEnabled(false);
        control.setTemplate(Activator.getDefault().getPreferenceStore().getString(PreferenceConstants.P_TEMPLATE));
    }

    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();

        boolean templateProjectSpecificSettingsEnabled = control.isProjectSpecificSettingsEnabled();
        if (templateProjectSpecificSettingsEnabled) {
            String template = control.getTemplate().trim();
            try {
                Activator.getDefault().createTemplate(template);
            } catch (CoreException ex) {
                MessageDialog
                        .openInformation(
                                getControl().getShell(),
                                Messages.getString("TemplatePropertyPage.1"), Messages.getString("TemplatePropertyPage.2") + ex.getMessage()); //$NON-NLS-1$ //$NON-NLS-2$
                return false;
            }
            store.putValue(PreferenceConstants.P_TEMPLATE, template);
        } else {
            store.setToDefault(PreferenceConstants.P_TEMPLATE);
        }
        store.setValue(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED,
                templateProjectSpecificSettingsEnabled);

        try {
            ((IPersistentPreferenceStore) store).save();
        } catch (IOException ex) {
            return false;
        }

        return true;
    }

    @Override
    public IPreferenceStore getPreferenceStore() {
        try {
            return Activator.getDefault().getPreferenceStore(getProject());
        } catch (CoreException ex) {
            Activator.getDefault().getLog().log(ex.getStatus());
            throw new RuntimeException(ex);
        }
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