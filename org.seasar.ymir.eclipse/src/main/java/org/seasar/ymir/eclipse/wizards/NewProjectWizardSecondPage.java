package org.seasar.ymir.eclipse.wizards;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.SkeletonEntry;

import werkzeugkasten.mvnhack.repository.Artifact;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewProjectWizardSecondPage extends WizardPage {
    private boolean initialized;

    private volatile boolean visible;

    private SkeletonEntry[] entries;

    private Label skeletonListLabel;

    private List skeletonListField;

    private Text skeletonDescriptionText;

    private Button specifySkeletonIdField;

    private Label skeletonGroupIdLabel;

    private Text skeletonGroupIdField;

    private Label skeletonArtifactIdLabel;

    private Text skeletonArtifactIdField;

    private Label skeletonVersionLabel;

    private Text skeletonVersionField;

    private Button useLatestVersionField;

    private volatile Artifact skeletonArtifact;

    private SkeletonArtifactResolver skeletonArtifactResolver;

    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param pageName
     */
    public NewProjectWizardSecondPage() {
        super("NewProjectWizardSecondPage"); //$NON-NLS-1$

        setTitle(Messages.getString("NewProjectWizardSecondPage.1")); //$NON-NLS-1$
        setDescription(Messages.getString("NewProjectWizardSecondPage.2")); //$NON-NLS-1$
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));
        setControl(composite);

        createSkeletonInformationControl(composite);

        setPageComplete(false);
        setErrorMessage(null);
        setMessage(null);
    }

    void createSkeletonInformationControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        group.setLayout(new GridLayout());
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("NewProjectWizardSecondPage.4")); //$NON-NLS-1$

        skeletonListLabel = new Label(group, SWT.NONE);
        skeletonListLabel.setText(Messages.getString("NewProjectWizardSecondPage.5")); //$NON-NLS-1$

        Composite composite = new Composite(group, SWT.NULL);
        composite.setFont(group.getFont());
        composite.setLayout(new GridLayout(2, true));
        GridData data = new GridData(GridData.FILL_BOTH);
        data.widthHint = 250;
        data.heightHint = 150;
        composite.setLayoutData(data);

        skeletonListField = new List(composite, SWT.SINGLE | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
        skeletonListField.setFont(group.getFont());
        skeletonListField.setLayoutData(data);
        entries = Activator.getDefault().getSkeletonEntries();
        for (SkeletonEntry entry : entries) {
            skeletonListField.add(entry.getName());
        }
        skeletonListField.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                String description;
                int selectionIndex = skeletonListField.getSelectionIndex();
                if (selectionIndex != -1) {
                    description = entries[selectionIndex].getDescription();
                } else {
                    description = ""; //$NON-NLS-1$
                }
                skeletonDescriptionText.setText(description);

                resolveSkeletonArtifact();
            }
        });

        skeletonDescriptionText = new Text(composite, SWT.MULTI | SWT.BORDER | SWT.V_SCROLL | SWT.WRAP);
        skeletonDescriptionText.setLayoutData(data);

        composite = new Composite(group, SWT.NULL);
        composite.setFont(group.getFont());
        composite.setLayout(new GridLayout(2, false));
        composite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

        specifySkeletonIdField = new Button(composite, SWT.CHECK | SWT.LEFT);
        data = new GridData();
        data.horizontalSpan = 2;
        specifySkeletonIdField.setLayoutData(data);
        specifySkeletonIdField.setText(Messages.getString("NewProjectWizardSecondPage.6")); //$NON-NLS-1$
        specifySkeletonIdField.addSelectionListener(new SelectionAdapter() {
            public void widgetSelected(SelectionEvent e) {
                boolean enabled = specifySkeletonIdField.getSelection();
                skeletonListLabel.setEnabled(!enabled);
                skeletonListField.setEnabled(!enabled);
                skeletonDescriptionText.setEnabled(!enabled);
                skeletonGroupIdLabel.setEnabled(enabled);
                skeletonGroupIdField.setEnabled(enabled);
                skeletonArtifactIdLabel.setEnabled(enabled);
                skeletonArtifactIdField.setEnabled(enabled);
                useLatestVersionField.setEnabled(enabled);
                boolean versionEnabled = enabled && !useLatestVersionField.getSelection();
                skeletonVersionLabel.setEnabled(versionEnabled);
                skeletonVersionField.setEnabled(versionEnabled);

                resolveSkeletonArtifact();
            }
        });

        skeletonGroupIdLabel = new Label(composite, SWT.NONE);
        skeletonGroupIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.7")); //$NON-NLS-1$

        skeletonGroupIdField = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        skeletonGroupIdField.setLayoutData(data);
        skeletonGroupIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact();
            }
        });

        skeletonArtifactIdLabel = new Label(composite, SWT.NONE);
        skeletonArtifactIdLabel.setText(Messages.getString("NewProjectWizardSecondPage.8")); //$NON-NLS-1$

        skeletonArtifactIdField = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        skeletonArtifactIdField.setLayoutData(data);
        skeletonArtifactIdField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact();
            }
        });

        skeletonVersionLabel = new Label(composite, SWT.NONE);
        skeletonVersionLabel.setText(Messages.getString("NewProjectWizardSecondPage.9")); //$NON-NLS-1$

        skeletonVersionField = new Text(composite, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        skeletonVersionField.setLayoutData(data);
        skeletonVersionField.addModifyListener(new ModifyListener() {
            public void modifyText(ModifyEvent e) {
                resolveSkeletonArtifact();
            }
        });

        new Label(composite, SWT.NONE);

        useLatestVersionField = new Button(composite, SWT.CHECK | SWT.LEFT);
        useLatestVersionField.setText(Messages.getString("NewProjectWizardSecondPage.10")); //$NON-NLS-1$
        useLatestVersionField.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                boolean enabled = useLatestVersionField.getSelection();
                skeletonVersionLabel.setEnabled(!enabled);
                skeletonVersionField.setEnabled(!enabled);

                resolveSkeletonArtifact();
            }
        });
    }

    boolean validatePage() {
        if (skeletonArtifact == null) {
            return false;
        }
        return true;
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        this.visible = visible;
        if (!initialized) {
            setDefaultValues();
            initialized = true;
        }
    }

    boolean isVisible() {
        return visible;
    }

    void setDefaultValues() {
        skeletonListField.setSelection(0, 0);
        skeletonDescriptionText.setText(entries[0].getDescription());
        skeletonGroupIdLabel.setEnabled(false);
        skeletonGroupIdField.setEnabled(false);
        skeletonArtifactIdLabel.setEnabled(false);
        skeletonArtifactIdField.setEnabled(false);
        skeletonVersionLabel.setEnabled(false);
        skeletonVersionField.setEnabled(false);
        useLatestVersionField.setEnabled(false);
        useLatestVersionField.setSelection(true);

        resolveSkeletonArtifact();
    }

    private boolean validateToResolveSkeletonArtifact() {
        if (getSkeletonGroupId().length() == 0) {
            return false;
        }
        if (getSkeletonArtifactId().length() == 0) {
            return false;
        }
        if (specifySkeletonIdField.getSelection() && !useLatestVersionField.getSelection()
                && skeletonVersionField.getText().length() == 0) {
            return false;
        }
        return true;
    }

    private void resolveSkeletonArtifact() {
        skeletonArtifact = null;
        setPageComplete(false);

        if (isVisible()) {
            setMessage(Messages.getString("NewProjectWizardSecondPage.0"), IMessageProvider.INFORMATION); //$NON-NLS-1$
            setErrorMessage(null);
        }

        if (!validateToResolveSkeletonArtifact()) {
            return;
        }

        if (skeletonArtifactResolver != null) {
            skeletonArtifactResolver.cancel();
        }
        skeletonArtifactResolver = new SkeletonArtifactResolver(this, getSkeletonGroupId(), getSkeletonArtifactId(),
                getSkeletonVersion());
        skeletonArtifactResolver.start();
    }

    private String getSkeletonGroupId() {
        if (specifySkeletonIdField.getSelection()) {
            return skeletonGroupIdField.getText();
        } else {
            int index = skeletonListField.getSelectionIndex();
            if (index == -1) {
                return ""; //$NON-NLS-1$
            } else {
                return entries[index].getGroupId();
            }
        }
    }

    private String getSkeletonArtifactId() {
        if (specifySkeletonIdField.getSelection()) {
            return skeletonArtifactIdField.getText();
        } else {
            int index = skeletonListField.getSelectionIndex();
            if (index == -1) {
                return ""; //$NON-NLS-1$
            } else {
                return entries[index].getArtifactId();
            }
        }
    }

    private String getSkeletonVersion() {
        if (specifySkeletonIdField.getSelection()) {
            if (useLatestVersionField.getSelection()) {
                return ""; //$NON-NLS-1$
            } else {
                return skeletonVersionField.getText();
            }
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    public Artifact getSkeletonArtifact() {
        return skeletonArtifact;
    }

    public void setSkeletonArtifact(Artifact skeletonArtifact) {
        this.skeletonArtifact = skeletonArtifact;
    }
}