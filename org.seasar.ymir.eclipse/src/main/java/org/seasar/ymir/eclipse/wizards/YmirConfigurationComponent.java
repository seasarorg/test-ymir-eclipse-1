package org.seasar.ymir.eclipse.wizards;

import static org.seasar.ymir.eclipse.wizards.NewProjectWizard.REQUIRED_TEMPLATE;

import java.text.MessageFormat;

import org.eclipse.core.runtime.Platform;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.seasar.ymir.eclipse.Globals;

public class YmirConfigurationComponent {
    private static final String PAGEBASE = ".ymir.PageBase"; //$NON-NLS-1$

    private ModifyListener validationListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            updatePageComplete();
        }
    };

    private NewProjectWizardThirdPage parentPage;

    private Button specifySuperclassField;

    private Label superclassLabel;

    private Text superclassField;

    private Button usingFreyjaRenderClassField;

    private Button beantableEnabledField;

    private Button formDtoCreationFeatureEnabledField;

    private Button converterCreationFeatureEnabledField;

    private Button daoCreationFeatureEnabledField;

    private Button dxoCreationFeatureEnabledField;

    private Button eclipseEnabledField;

    private Label resourceSynchronizerURLLabel;

    private Text resourceSynchronizerURLField;

    public YmirConfigurationComponent(NewProjectWizardThirdPage parentPage) {
        this.parentPage = parentPage;
    }

    public void updatePageComplete() {
        parentPage.setPageComplete(parentPage.validatePage());
    }

    public void createControl(Composite parent) {
        createAutoGenerationInformationControl(parent);
        createEclipseCooperationInformationControl(parent);

        setDefaultValues();
    }

    void createAutoGenerationInformationControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("YmirConfigurationComponent.4")); //$NON-NLS-1$

        specifySuperclassField = new Button(group, SWT.CHECK | SWT.LEFT);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        specifySuperclassField.setLayoutData(data);
        specifySuperclassField.setText(Messages.getString("YmirConfigurationComponent.5")); //$NON-NLS-1$
        specifySuperclassField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = specifySuperclassField.getSelection();
                superclassLabel.setEnabled(enabled);
                superclassField.setEnabled(enabled);
            }
        });

        superclassLabel = new Label(group, SWT.NONE);
        superclassLabel.setText(Messages.getString("YmirConfigurationComponent.6")); //$NON-NLS-1$

        superclassField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        superclassField.setLayoutData(data);

        usingFreyjaRenderClassField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        usingFreyjaRenderClassField.setLayoutData(data);
        usingFreyjaRenderClassField.setText(Messages.getString("YmirConfigurationComponent.7")); //$NON-NLS-1$

        beantableEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        beantableEnabledField.setLayoutData(data);
        beantableEnabledField.setText(Messages.getString("YmirConfigurationComponent.8")); //$NON-NLS-1$

        formDtoCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        formDtoCreationFeatureEnabledField.setLayoutData(data);
        formDtoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.9")); //$NON-NLS-1$

        daoCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        daoCreationFeatureEnabledField.setLayoutData(data);
        daoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.10")); //$NON-NLS-1$

        dxoCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        dxoCreationFeatureEnabledField.setLayoutData(data);
        dxoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.11")); //$NON-NLS-1$

        converterCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        converterCreationFeatureEnabledField.setLayoutData(data);
        converterCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.12")); //$NON-NLS-1$
    }

    void createEclipseCooperationInformationControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("YmirConfigurationComponent.13")); //$NON-NLS-1$

        eclipseEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        eclipseEnabledField.setLayoutData(data);
        eclipseEnabledField.setText(Messages.getString("YmirConfigurationComponent.14")); //$NON-NLS-1$
        eclipseEnabledField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = eclipseEnabledField.getSelection();
                resourceSynchronizerURLLabel.setEnabled(enabled);
                resourceSynchronizerURLField.setEnabled(enabled);
            }
        });

        resourceSynchronizerURLLabel = new Label(group, SWT.NONE);
        resourceSynchronizerURLLabel.setText(Messages.getString("YmirConfigurationComponent.15")); //$NON-NLS-1$

        resourceSynchronizerURLField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        resourceSynchronizerURLField.setLayoutData(data);
        resourceSynchronizerURLField.addModifyListener(validationListener);
    }

    boolean validatePage() {
        if (isEclipseEnabled() && getResourceSynchronizerURL().length() == 0) {
            parentPage.setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages
                    .getString("YmirConfigurationComponent.15"))); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    void setDefaultValues() {
        superclassLabel.setEnabled(false);
        superclassField.setText(getSuperclassDefaultValue());
        superclassField.setEnabled(false);
        usingFreyjaRenderClassField.setSelection(true);
        formDtoCreationFeatureEnabledField.setSelection(true);
        converterCreationFeatureEnabledField.setSelection(true);
        boolean eclipseEnabled = (Platform.getBundle(Globals.BUNDLENAME_RESOURCESYNCHRONIZER) != null);
        eclipseEnabledField.setSelection(eclipseEnabled);
        resourceSynchronizerURLLabel.setEnabled(eclipseEnabled);
        resourceSynchronizerURLField.setEnabled(eclipseEnabled);
        resourceSynchronizerURLField.setText("http://localhost:8386/"); //$NON-NLS-1$

        updatePageComplete();
    }

    private String getSuperclassDefaultValue() {
        return ((NewProjectWizard) parentPage.getWizard()).getRootPackageName() + PAGEBASE;
    }

    public String getSuperclass() {
        if (specifySuperclassField.getSelection()) {
            return superclassField.getText();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public boolean isUsingFreyjaRenderClass() {
        return usingFreyjaRenderClassField.getSelection();
    }

    public boolean isBeantableEnabled() {
        return beantableEnabledField.getSelection();
    }

    public boolean isFormDtoCreationFeatureEnabled() {
        return formDtoCreationFeatureEnabledField.getSelection();
    }

    public boolean isConverterCreationFeatureEnabled() {
        return converterCreationFeatureEnabledField.getSelection();
    }

    public boolean isDaoCreationFeatureEnabled() {
        return daoCreationFeatureEnabledField.getSelection();
    }

    public boolean isDxoCreationFeatureEnabled() {
        return dxoCreationFeatureEnabledField.getSelection();
    }

    public boolean isEclipseEnabled() {
        return eclipseEnabledField.getSelection();
    }

    public String getResourceSynchronizerURL() {
        return resourceSynchronizerURLField.getText();
    }

    public String getTabLabel() {
        return Messages.getString("YmirConfigurationComponent.2"); //$NON-NLS-1$
    }
}