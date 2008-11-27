package org.seasar.ymir.eclipse.preferences;

import java.io.IOException;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
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
        String template = templateControl.getTemplate();
        try {
            Activator.getDefault().createTemplateEntry(template);
        } catch (ValidationException ex) {
            Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.toString(), ex));
            return false;
        } catch (IllegalSyntaxException ex) {
            Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, ex.toString(), ex));
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