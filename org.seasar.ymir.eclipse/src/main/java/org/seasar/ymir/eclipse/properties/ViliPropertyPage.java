package org.seasar.ymir.eclipse.properties;

import java.io.IOException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.natures.ViliProjectNature;
import org.seasar.ymir.vili.util.ProjectUtils;

public class ViliPropertyPage extends PropertyPage {
    private Button enableViliProjectNatureButton_;

    /**
     * @see PreferencePage#createContents(Composite)
     */
    protected Control createContents(Composite parent) {
        Composite composite = new Composite(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        composite.setLayout(layout);
        GridData data = new GridData(GridData.FILL);
        data.grabExcessHorizontalSpace = true;
        composite.setLayoutData(data);

        try {
            createEnableNatureSection(composite);
        } catch (CoreException ex) {
            Activator.getDefault().getLog().log(ex.getStatus());
            throw new RuntimeException(ex);
        }

        return composite;
    }

    private void createEnableNatureSection(Composite parent) throws CoreException {
        enableViliProjectNatureButton_ = new Button(parent, SWT.CHECK);
        enableViliProjectNatureButton_.setText(Messages.getString("ViliPropertyPage.0")); //$NON-NLS-1$
        enableViliProjectNatureButton_.setSelection(getProject().hasNature(ViliProjectNature.ID));
    }

    protected void performDefaults() {
    }

    public boolean performOk() {
        try {
            IPreferenceStore store = getPreferenceStore();

            if (enableViliProjectNatureButton_.getSelection()) {
                ProjectUtils.addNature(getProject(), ViliProjectNature.ID, new NullProgressMonitor());
            } else {
                ProjectUtils.removeNature(getProject(), ViliProjectNature.ID, new NullProgressMonitor());
            }

            ((IPersistentPreferenceStore) store).save();
        } catch (IOException ex) {
            Activator.getDefault().log(ex);
        } catch (CoreException ex) {
            Activator.getDefault().getLog().log(ex.getStatus());
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