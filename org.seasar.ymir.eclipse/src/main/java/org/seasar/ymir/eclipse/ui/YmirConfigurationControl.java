package org.seasar.ymir.eclipse.ui;

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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.HotdeployType;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;

public class YmirConfigurationControl {
    private static final String REQUIRED_TEMPLATE = Messages.getString("YmirConfigurationControl.21"); //$NON-NLS-1$

    private static final String PAGEBASE = ".web.PageBase"; //$NON-NLS-1$

    private ModifyListener validationListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            setPageComplete(validatePage());
        }
    };

    private Composite parent;

    private ViliProjectPreferences preferences;

    private boolean isPageComplete;

    private Button autoGenerationEnabledField;

    private Button specifySuperclassField;

    private Label superclassLabel;

    private Text superclassField;

    private Button usingFreyjaRenderClassField;

    private Button inplaceEditorEnabled;

    private Button controlPanelEnabled;

    private Button formDtoCreationFeatureEnabledField;

    private Button converterCreationFeatureEnabledField;

    private Button daoCreationFeatureEnabledField;

    private Button dxoCreationFeatureEnabledField;

    private Button eclipseEnabledField;

    private Label resourceSynchronizerURLLabel;

    private Text resourceSynchronizerURLField;

    private Button useS2HotdeployField;

    private Button useJavaRebelHotdeployField;

    private Button useVoidHotdeployField;

    private Button beantableEnabledField;

    public YmirConfigurationControl(Composite parent, ViliProjectPreferences preferences) {
        this.parent = parent;
        this.preferences = preferences;
    }

    public Control createControl() {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        autoGenerationEnabledField = new Button(composite, SWT.CHECK | SWT.LEFT);
        autoGenerationEnabledField.setText(Messages.getString("YmirConfigurationControl.0")); //$NON-NLS-1$
        autoGenerationEnabledField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = autoGenerationEnabledField.getSelection();

                specifySuperclassField.setEnabled(enabled);
                superclassField.setEnabled(enabled);
                usingFreyjaRenderClassField.setEnabled(enabled);
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

        createAutoGenerationParameterControl(composite);
        createEclipseCooperationParameterControl(composite);
        createHotdeployParameterControl(composite);
        createMiscParameterControl(composite);

        return composite;
    }

    void createAutoGenerationParameterControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("YmirConfigurationControl.9")); //$NON-NLS-1$

        specifySuperclassField = new Button(group, SWT.CHECK | SWT.LEFT);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        specifySuperclassField.setLayoutData(data);
        specifySuperclassField.setText(Messages.getString("YmirConfigurationControl.10")); //$NON-NLS-1$
        specifySuperclassField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = specifySuperclassField.getSelection();
                superclassLabel.setEnabled(enabled);
                superclassField.setEnabled(enabled);
            }
        });

        superclassLabel = new Label(group, SWT.NONE);
        superclassLabel.setText(Messages.getString("YmirConfigurationControl.11")); //$NON-NLS-1$

        superclassField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        superclassField.setLayoutData(data);

        usingFreyjaRenderClassField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        usingFreyjaRenderClassField.setLayoutData(data);
        usingFreyjaRenderClassField.setText(Messages.getString("YmirConfigurationControl.12")); //$NON-NLS-1$

        inplaceEditorEnabled = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        inplaceEditorEnabled.setLayoutData(data);
        inplaceEditorEnabled.setText(Messages.getString("YmirConfigurationControl.2")); //$NON-NLS-1$

        controlPanelEnabled = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        controlPanelEnabled.setLayoutData(data);
        controlPanelEnabled.setText(Messages.getString("YmirConfigurationControl.3")); //$NON-NLS-1$

        formDtoCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        formDtoCreationFeatureEnabledField.setLayoutData(data);
        formDtoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationControl.14")); //$NON-NLS-1$

        daoCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        daoCreationFeatureEnabledField.setLayoutData(data);
        daoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationControl.15")); //$NON-NLS-1$

        dxoCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        dxoCreationFeatureEnabledField.setLayoutData(data);
        dxoCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationControl.16")); //$NON-NLS-1$

        converterCreationFeatureEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        converterCreationFeatureEnabledField.setLayoutData(data);
        converterCreationFeatureEnabledField.setText(Messages.getString("YmirConfigurationControl.17")); //$NON-NLS-1$
    }

    void createEclipseCooperationParameterControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("YmirConfigurationControl.18")); //$NON-NLS-1$

        eclipseEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.horizontalSpan = 2;
        eclipseEnabledField.setLayoutData(data);
        eclipseEnabledField.setText(Messages.getString("YmirConfigurationControl.19")); //$NON-NLS-1$
        eclipseEnabledField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = eclipseEnabledField.getSelection();
                resourceSynchronizerURLLabel.setEnabled(enabled);
                resourceSynchronizerURLField.setEnabled(enabled);
            }
        });

        resourceSynchronizerURLLabel = new Label(group, SWT.NONE);
        resourceSynchronizerURLLabel.setText(Messages.getString("YmirConfigurationControl.20")); //$NON-NLS-1$

        resourceSynchronizerURLField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        resourceSynchronizerURLField.setLayoutData(data);
        resourceSynchronizerURLField.addModifyListener(validationListener);
    }

    void createHotdeployParameterControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("YmirConfigurationControl.5")); //$NON-NLS-1$

        useS2HotdeployField = new Button(group, SWT.RADIO);
        useS2HotdeployField.setText(Messages.getString("YmirConfigurationControl.6")); //$NON-NLS-1$

        useJavaRebelHotdeployField = new Button(group, SWT.RADIO);
        useJavaRebelHotdeployField.setText(Messages.getString("YmirConfigurationControl.7")); //$NON-NLS-1$

        useVoidHotdeployField = new Button(group, SWT.RADIO);
        useVoidHotdeployField.setText(Messages.getString("YmirConfigurationControl.8")); //$NON-NLS-1$
    }

    void createMiscParameterControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("YmirConfigurationControl.4")); //$NON-NLS-1$

        beantableEnabledField = new Button(group, SWT.CHECK | SWT.LEFT);
        beantableEnabledField.setText(Messages.getString("YmirConfigurationControl.13")); //$NON-NLS-1$
    }

    public boolean validatePage() {
        if (isAutoGenerationEnabled()) {
            if (isEclipseEnabled() && getResourceSynchronizerURL().length() == 0) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages.getString("YmirConfigurationControl.20"))); //$NON-NLS-1$
                return false;
            }
        }
        return true;
    }

    public void setErrorMessage(String message) {
    }

    public boolean isPageComplete() {
        return isPageComplete;
    }

    public void setPageComplete(boolean isPageComplete) {
        this.isPageComplete = isPageComplete;
    }

    public void setDefaultValues() {
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

        useS2HotdeployField.setSelection(true);

        setPageComplete(validatePage());
    }

    public boolean isBeantableEnabled() {
        return beantableEnabledField.getSelection();
    }

    public boolean isAutoGenerationEnabled() {
        return autoGenerationEnabledField.getSelection();
    }

    private String getSuperclassDefaultValue() {
        return preferences.getRootPackageName() + PAGEBASE;
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
        if (isEclipseEnabled()) {
            return resourceSynchronizerURLField.getText();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public HotdeployType getHotdeployType() {
        if (useVoidHotdeployField.getSelection()) {
            return HotdeployType.VOID;
        } else if (useJavaRebelHotdeployField.getSelection()) {
            return HotdeployType.JAVAREBEL;
        } else {
            return HotdeployType.S2;
        }
    }
}