package org.seasar.ymir.eclipse.properties;

import java.io.IOException;

import net.skirnir.xom.IllegalSyntaxException;
import net.skirnir.xom.ValidationException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.ui.TemplateControl;

public class ViliPropertyPage extends PropertyPage {
    private Composite tabFolderParent;

    private CTabFolder tabFolder;

    private TemplateControl templateControl;

    /**
     * Constructor for ViliPropertyPage.
     */
    public ViliPropertyPage() {
        super();
    }

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        tabFolderParent = new Composite(parent, SWT.NULL);
        tabFolderParent.setLayout(new FillLayout());
        createTabFolder();
        return tabFolderParent;
    }

    void createTabFolder() {
        IPreferenceStore store = getPreferenceStore();
        if (store == null) {
            return;
        }

        tabFolder = new CTabFolder(tabFolderParent, SWT.NULL);
        tabFolder.setLayout(new FillLayout());
        tabFolder.setSimple(false);
        tabFolder.setTabHeight(tabFolder.getTabHeight() + 2);

        CTabItem templateTabItem = new CTabItem(tabFolder, SWT.NONE);
        templateTabItem.setText(Messages.getString("ViliPropertyPage.0")); //$NON-NLS-1$

        Composite templateTabContent = new Composite(tabFolder, SWT.NULL);
        templateTabContent.setLayout(new GridLayout());
        templateTabContent.setLayoutData(new GridData(GridData.FILL_BOTH));
        templateTabItem.setControl(templateTabContent);

        templateControl = new TemplateControl(templateTabContent, true);
        templateControl.createControl();

        templateControl.setProjectSpecificSettingsEnabled(store
                .getBoolean(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED));
        templateControl.setTemplate(store.getString(PreferenceConstants.P_TEMPLATE));

        tabFolderParent.layout();
        tabFolder.setSelection(0);
    }

    protected void performDefaults() {
        templateControl.setProjectSpecificSettingsEnabled(false);
        templateControl.setTemplate(Activator.getDefault().getPreferenceStore().getString(
                PreferenceConstants.P_TEMPLATE));
    }

    public boolean performOk() {
        IPreferenceStore store = getPreferenceStore();
        if (store == null) {
            return false;
        }

        boolean templateProjectSpecificSettingsEnabled = templateControl.isProjectSpecificSettingsEnabled();
        if (templateProjectSpecificSettingsEnabled) {
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
            return null;
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