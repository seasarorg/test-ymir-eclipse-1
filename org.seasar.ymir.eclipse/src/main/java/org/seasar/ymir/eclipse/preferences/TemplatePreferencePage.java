package org.seasar.ymir.eclipse.preferences;

import java.io.IOException;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ui.TemplateControl;

public class TemplatePreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
    private TemplateControl templateControl;

    public TemplatePreferencePage() {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.getString("TemplatePreferencePage.0")); //$NON-NLS-1$
    }

    @Override
    protected Control createContents(Composite parent) {
        templateControl = new TemplateControl(parent, false);
        Control control = templateControl.createControl();
        templateControl.setTemplate(getPreferenceStore().getString(PreferenceConstants.P_TEMPLATE));

        return control;
    }

    public void init(IWorkbench workbench) {
    }

    @Override
    protected void performDefaults() {
        templateControl.setTemplate(getPreferenceStore().getDefaultString(PreferenceConstants.P_TEMPLATE));
    }

    @Override
    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        String template = templateControl.getTemplate().trim();
        try {
            Activator.getDefault().createTemplateEntry(template);
        } catch (ValidationException ex) {
            MessageDialog.openInformation(getControl().getShell(), Messages.getString("TemplatePreferencePage.1"), Messages.getString("TemplatePreferencePage.2") + ex.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        } catch (IllegalSyntaxException ex) {
            MessageDialog.openInformation(getControl().getShell(), Messages.getString("TemplatePreferencePage.1"), Messages.getString("TemplatePreferencePage.2") + ex.toString()); //$NON-NLS-1$ //$NON-NLS-2$
            return false;
        }
        store.setValue(PreferenceConstants.P_TEMPLATE, template);
        try {
            ((IPersistentPreferenceStore) store).save();
        } catch (IOException ex) {
            return false;
        }

        return true;
    }
}