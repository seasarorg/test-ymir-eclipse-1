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

public class YmirConfigurationBlock {
    private static final String PAGEBASE = ".ymir.PageBase"; //$NON-NLS-1$

    private ModifyListener validationListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            updatePageComplete();
        }
    };

    private NewProjectWizardThirdPage parentPage;

    private Button autoGenerationEnabledField;

    private Button specifySuperclassField;

    private Label superclassLabel;

    private Text superclassField;

    private Button usingFreyjaRenderClassField;

    private Label fieldPrefixLabel;

    private Text fieldPrefixField;

    private Text fieldSuffixField;

    private Button inplaceEditorEnabled;

    private Button controlPanelEnabled;

    private Button formDtoCreationFeatureEnabledField;

    private Button converterCreationFeatureEnabledField;

    private Button daoCreationFeatureEnabledField;

    private Button dxoCreationFeatureEnabledField;

    private Button eclipseEnabledField;

    private Label resourceSynchronizerURLLabel;

    private Text resourceSynchronizerURLField;

    private Button beantableEnabledField;

    public YmirConfigurationBlock(NewProjectWizardThirdPage parentPage) {
        this.parentPage = parentPage;
    }

    public void updatePageComplete() {
        parentPage.setPageComplete(parentPage.validatePage());
    }

    public void createControl(Composite parent) {
        autoGenerationEnabledField = new Button(parent, SWT.CHECK | SWT.LEFT);
        autoGenerationEnabledField.setText(Messages.getString("YmirConfigurationBlock.0")); //$NON-NLS-1$
        autoGenerationEnabledField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = autoGenerationEnabledField.getSelection();

                specifySuperclassField.setEnabled(enabled);
                superclassField.setEnabled(enabled);
                usingFreyjaRenderClassField.setEnabled(enabled);
                fieldPrefixLabel.setEnabled(enabled);
                fieldPrefixField.setEnabled(enabled);
                fieldSuffixField.setEnabled(enabled);
                inplaceEditorEnabled.setEnabled(enabled);
                controlPanelEnabled.setEnabled(enabled);
                formDtoCreationFeatureEnabledField.setEnabled(enabled);
                converterCreationFeatureEnabledField.setEnabled(enabled);
                daoCreationFeatureEnabledField.setEnabled(enabled);
                dxoCreationFeatureEnabledField.setEnabled(enabled);

                eclipseEnabledField.setEnabled(enabled);
                resourceSynchronizerURLField.setEnabled(enabled);
            }
        });

        createAutoGenerationParameterControl(parent);
        createEclipseCooperationParameterControl(parent);
        createMiscParameterControl(parent);

        setDefaultValues();
    }

    void createAutoGenerationParameterControl(Composite parent) {
        Group autoGenerationParameterGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        autoGenerationParameterGroup.setLayout(layout);
        autoGenerationParameterGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        autoGenerationParameterGroup.setText(Messages.getString("YmirConfigurationComponent.4")); //$NON-NLS-1$

        specifySuperclassField = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
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

        superclassLabel = new Label(autoGenerationParameterGroup, SWT.NONE);
        superclassLabel.setText(Messages.getString("YmirConfigurationComponent.6")); //$NON-NLS-1$

        superclassField = new Text(autoGenerationParameterGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        superclassField.setLayoutData(data);

        usingFreyjaRenderClassField = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        usingFreyjaRenderClassField.setLayoutData(data);
        usingFreyjaRenderClassField.setText(Messages.getString("YmirConfigurationComponent.7")); //$NON-NLS-1$

        fieldPrefixLabel = new Label(autoGenerationParameterGroup, SWT.NONE);
        fieldPrefixLabel.setText(Messages.getString("YmirConfigurationBlock.1")); //$NON-NLS-1$

        Composite composite = new Composite(autoGenerationParameterGroup, SWT.NULL);
        composite.setLayout(new GridLayout(2, false));

        fieldPrefixField = new Text(composite, SWT.BORDER);
        data = new GridData();
        data.widthHint = 10;
        fieldPrefixField.setLayoutData(data);

        fieldSuffixField = new Text(composite, SWT.BORDER);
        data = new GridData();
        data.widthHint = 10;
        fieldSuffixField.setLayoutData(data);

        inplaceEditorEnabled = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        inplaceEditorEnabled.setLayoutData(data);
        inplaceEditorEnabled.setText(Messages.getString("YmirConfigurationBlock.2")); //$NON-NLS-1$

        controlPanelEnabled = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        controlPanelEnabled.setLayoutData(data);
        controlPanelEnabled.setText(Messages.getString("YmirConfigurationBlock.3")); //$NON-NLS-1$

        formDtoCreationFeatureEnabledField = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        formDtoCreationFeatureEnabledField.setLayoutData(data);
        formDtoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.9")); //$NON-NLS-1$

        daoCreationFeatureEnabledField = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        daoCreationFeatureEnabledField.setLayoutData(data);
        daoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.10")); //$NON-NLS-1$

        dxoCreationFeatureEnabledField = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        dxoCreationFeatureEnabledField.setLayoutData(data);
        dxoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.11")); //$NON-NLS-1$

        converterCreationFeatureEnabledField = new Button(autoGenerationParameterGroup, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        converterCreationFeatureEnabledField.setLayoutData(data);
        converterCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationComponent.12")); //$NON-NLS-1$
    }

    void createEclipseCooperationParameterControl(Composite parent) {
        Group eclipseCooperationParameterGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        eclipseCooperationParameterGroup.setLayout(layout);
        eclipseCooperationParameterGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        eclipseCooperationParameterGroup.setText(Messages.getString("YmirConfigurationComponent.13")); //$NON-NLS-1$

        eclipseEnabledField = new Button(eclipseCooperationParameterGroup, SWT.CHECK | SWT.LEFT);
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

        resourceSynchronizerURLLabel = new Label(eclipseCooperationParameterGroup, SWT.NONE);
        resourceSynchronizerURLLabel.setText(Messages.getString("YmirConfigurationComponent.15")); //$NON-NLS-1$

        resourceSynchronizerURLField = new Text(eclipseCooperationParameterGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        resourceSynchronizerURLField.setLayoutData(data);
        resourceSynchronizerURLField.addModifyListener(validationListener);
    }

    void createMiscParameterControl(Composite parent) {
        Group miscParameterGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        miscParameterGroup.setLayout(layout);
        miscParameterGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        miscParameterGroup.setText(Messages.getString("YmirConfigurationBlock.4")); //$NON-NLS-1$

        beantableEnabledField = new Button(miscParameterGroup, SWT.CHECK | SWT.LEFT);
        beantableEnabledField.setText(Messages.getString("YmirConfigurationComponent.8")); //$NON-NLS-1$
    }

    boolean validatePage() {
        if (isAutoGenerationEnabled()) {
            if (isEclipseEnabled() && getResourceSynchronizerURL().length() == 0) {
                parentPage.setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages
                        .getString("YmirConfigurationComponent.15"))); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    void setDefaultValues() {
        autoGenerationEnabledField.setSelection(true);

        superclassLabel.setEnabled(false);
        superclassField.setText(getSuperclassDefaultValue());
        superclassField.setEnabled(false);
        usingFreyjaRenderClassField.setSelection(true);
        inplaceEditorEnabled.setSelection(true);
        controlPanelEnabled.setSelection(true);
        formDtoCreationFeatureEnabledField.setSelection(true);
        converterCreationFeatureEnabledField.setSelection(true);

        boolean eclipseEnabled = (Platform.getBundle(Globals.BUNDLENAME_RESOURCESYNCHRONIZER) != null);
        eclipseEnabledField.setSelection(eclipseEnabled);
        resourceSynchronizerURLLabel.setEnabled(eclipseEnabled);
        resourceSynchronizerURLField.setEnabled(eclipseEnabled);
        resourceSynchronizerURLField.setText("http://localhost:8386/"); //$NON-NLS-1$

        updatePageComplete();
    }

    public boolean isBeantableEnabled() {
        return beantableEnabledField.getSelection();
    }

    public boolean isAutoGenerationEnabled() {
        return autoGenerationEnabledField.getSelection();
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

    public String getFieldPrefix() {
        return fieldPrefixField.getText();
    }

    public String getFieldSuffix() {
        return fieldSuffixField.getText();
    }

    public boolean isInplaceEditorEnabled() {
        return inplaceEditorEnabled.getSelection();
    }

    public boolean isControlPanelEnabled() {
        return controlPanelEnabled.getSelection();
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