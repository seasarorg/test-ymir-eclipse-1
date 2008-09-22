package org.seasar.ymir.eclipse.wizards;

import static org.seasar.ymir.eclipse.wizards.NewProjectWizard.REQUIRED_TEMPLATE;

import java.io.File;
import java.text.MessageFormat;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.jdt.launching.JavaRuntime;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.util.IPropertyChangeListener;
import org.eclipse.jface.util.PropertyChangeEvent;
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
import org.eclipse.ui.dialogs.WizardNewProjectCreationPage;
import org.seasar.ymir.eclipse.wizards.jre.BuildJREDescriptor;
import org.seasar.ymir.eclipse.wizards.jre.JREsComboBlock;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewProjectWizardSecondPage extends WizardNewProjectCreationPage {
    private ModifyListener validationListener = new ModifyListener() {
        public void modifyText(ModifyEvent e) {
            setPageComplete(validatePage());
        }
    };

    private boolean initialized;

    private Text projectNameField;

    private Text locationPathField;

    private String initialLocationPath;

    private JREsComboBlock jreBlock;

    private Text rootPackageNameField;

    private Label projectGroupIdLabel;

    private Text projectGroupIdField;

    private Button useRootPackageNameAsProjectGroupIdField;

    private Label projectArtifactIdLabel;

    private Text projectArtifactIdField;

    private Button useProjectNameAsProjectArtifactIdField;

    private Text projectVersionField;

    public NewProjectWizardSecondPage() {
        super("NewProjectWizardSecondPage"); //$NON-NLS-1$

        setTitle(Messages.getString("NewProjectWizardSecondPage.1")); //$NON-NLS-1$
        setDescription(Messages.getString("NewProjectWizardSecondPage.2")); //$NON-NLS-1$
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        super.createControl(parent);

        Composite composite = (Composite) getControl();
        createJREControl(composite);
        createProjectInformationControl(composite);

        projectNameField = null;
        locationPathField = null;
        findProjectNameFieldAndLocationPathField(composite);
        projectNameField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (!locationPathField.isEnabled()) {
                    String projectName = projectNameField.getText().trim();
                    String locationPath;
                    if (projectName.length() == 0) {
                        locationPath = initialLocationPath;
                    } else {
                        locationPath = initialLocationPath + File.separator + projectName;
                    }
                    locationPathField.setText(locationPath);
                }

                if (useProjectNameAsProjectArtifactIdField.getSelection()) {
                    projectArtifactIdField.setText(projectNameField.getText().trim());
                }
            }
        });

        setPageComplete(false);
        setErrorMessage(null);
        setMessage(null);
    }

    private void createJREControl(Composite parent) {
        jreBlock = new JREsComboBlock();
        jreBlock.setDefaultJREDescriptor(new BuildJREDescriptor());
        jreBlock.setTitle(Messages.getString("NewProjectWizardSecondPage.0")); //$NON-NLS-1$
        jreBlock.createControl(parent);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        jreBlock.getControl().setLayoutData(data);
        jreBlock.addPropertyChangeListener(new IPropertyChangeListener() {
            public void propertyChange(PropertyChangeEvent event) {
                IStatus status = jreBlock.getStatus();
                if (status.isOK()) {
                    setErrorMessage(null);
                } else {
                    setErrorMessage(status.getMessage());
                }
            }
        });

        jreBlock.setPath(JavaRuntime.newDefaultJREContainerPath());
    }

    private void createProjectInformationControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("NewProjectWizardSecondPage.3")); //$NON-NLS-1$

        Label rootPackageNameLabel = new Label(group, SWT.NONE);
        rootPackageNameLabel.setText(Messages.getString("NewProjectWizardSecondPage.4")); //$NON-NLS-1$

        rootPackageNameField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        rootPackageNameField.setLayoutData(data);
        rootPackageNameField.addModifyListener(validationListener);
        rootPackageNameField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                if (useRootPackageNameAsProjectGroupIdField.getSelection()) {
                    projectGroupIdField.setText(rootPackageNameField.getText().trim());
                }
            }
        });

        projectGroupIdLabel = new Label(group, SWT.NONE);
        projectGroupIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.5")); //$NON-NLS-1$

        projectGroupIdField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        projectGroupIdField.setLayoutData(data);
        projectGroupIdField.addModifyListener(validationListener);

        new Label(group, SWT.NONE);
        useRootPackageNameAsProjectGroupIdField = new Button(group, SWT.CHECK | SWT.LEFT);
        useRootPackageNameAsProjectGroupIdField.setText(Messages.getString("NewProjectWizardSecondPage.6")); //$NON-NLS-1$
        useRootPackageNameAsProjectGroupIdField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = !useRootPackageNameAsProjectGroupIdField.getSelection();
                projectGroupIdLabel.setEnabled(enabled);
                projectGroupIdField.setEnabled(enabled);
            }
        });

        projectArtifactIdLabel = new Label(group, SWT.NONE);
        projectArtifactIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.7")); //$NON-NLS-1$

        projectArtifactIdField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        projectArtifactIdField.setLayoutData(data);
        projectArtifactIdField.addModifyListener(validationListener);

        new Label(group, SWT.NONE);
        useProjectNameAsProjectArtifactIdField = new Button(group, SWT.CHECK | SWT.LEFT);
        useProjectNameAsProjectArtifactIdField.setText(Messages.getString("NewProjectWizardSecondPage.8")); //$NON-NLS-1$
        useProjectNameAsProjectArtifactIdField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = !useProjectNameAsProjectArtifactIdField.getSelection();
                projectArtifactIdLabel.setEnabled(enabled);
                projectArtifactIdField.setEnabled(enabled);
            }
        });

        Label projectVersionLabel = new Label(group, SWT.NONE);
        projectVersionLabel.setText(Messages.getString("NewProjectWizardSecondPage.9")); //$NON-NLS-1$

        projectVersionField = new Text(group, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        projectVersionField.setLayoutData(data);
        projectVersionField.addModifyListener(validationListener);
    }

    private boolean findProjectNameFieldAndLocationPathField(Composite composite) {
        Control[] children = composite.getChildren();
        for (int i = 0; i < children.length; i++) {
            if (children[i] instanceof Composite) {
                if (findProjectNameFieldAndLocationPathField((Composite) children[i])) {
                    return true;
                }
            } else if (children[i] instanceof Text) {
                if (projectNameField == null) {
                    projectNameField = (Text) children[i];
                } else if (locationPathField == null) {
                    locationPathField = (Text) children[i];
                    initialLocationPath = locationPathField.getText();
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    protected boolean validatePage() {
        boolean validated = super.validatePage();
        if (!validated) {
            return false;
        }

        if (getRootPackageName().length() == 0) {
            setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages.getString("NewProjectWizardSecondPage.4"))); //$NON-NLS-1$
            return false;
        }
        if (getProjectGroupId().length() == 0) {
            setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages.getString("NewProjectWizardSecondPage.5"))); //$NON-NLS-1$
            return false;
        }
        if (getProjectArtifactId().length() == 0) {
            setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages.getString("NewProjectWizardSecondPage.7"))); //$NON-NLS-1$
            return false;
        }
        if (getProjectVersion().length() == 0) {
            setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages.getString("NewProjectWizardSecondPage.9"))); //$NON-NLS-1$
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (!initialized) {
            setDefaultValues();
            initialized = true;
        }
    }

    void setDefaultValues() {
        useRootPackageNameAsProjectGroupIdField.setSelection(true);
        projectGroupIdLabel.setEnabled(false);
        projectGroupIdField.setEnabled(false);
        useProjectNameAsProjectArtifactIdField.setSelection(true);
        projectArtifactIdLabel.setEnabled(false);
        projectArtifactIdField.setEnabled(false);
        projectVersionField.setText("0.0.1-SNAPSHOT"); //$NON-NLS-1$

        setPageComplete(validatePage());
    }

    public IPath getJREContainerPath() {
        return jreBlock.getPath();
    }

    public String getRootPackageName() {
        return rootPackageNameField.getText();
    }

    public String getProjectGroupId() {
        return projectGroupIdField.getText();
    }

    public String getProjectArtifactId() {
        return projectArtifactIdField.getText();
    }

    public String getProjectVersion() {
        return projectVersionField.getText();
    }
}