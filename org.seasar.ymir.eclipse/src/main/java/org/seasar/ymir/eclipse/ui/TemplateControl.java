package org.seasar.ymir.eclipse.ui;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;

public class TemplateControl {
    private Composite parent;

    private boolean forProject;

    private Button projectSpecificSettingsEnabledButton;

    private Label templateContentLabel;

    private Text templateContentText;

    public TemplateControl(Composite parent, boolean forProject) {
        this.parent = parent;
        this.forProject = forProject;
    }

    public Control createControl() {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (forProject) {
            projectSpecificSettingsEnabledButton = new Button(composite, SWT.CHECK);
            projectSpecificSettingsEnabledButton.setText(Messages.getString("TemplateControl.0")); //$NON-NLS-1$
        }

        templateContentLabel = new Label(composite, SWT.NONE);
        templateContentLabel.setLayoutData(new GridData());
        templateContentLabel.setText(Messages.getString("TemplateControl.1")); //$NON-NLS-1$

        templateContentText = new Text(composite, SWT.BORDER | SWT.V_SCROLL | SWT.H_SCROLL);
        GridData gridData = new GridData();
        gridData.heightHint = 320;
        gridData.widthHint = 480;
        templateContentText.setLayoutData(gridData);

        if (forProject) {
            projectSpecificSettingsEnabledButton.addListener(SWT.Selection, new Listener() {
                public void handleEvent(Event event) {
                    boolean enabled = projectSpecificSettingsEnabledButton.getSelection();
                    templateContentLabel.setEnabled(enabled);
                    templateContentText.setEnabled(enabled);
                }
            });
        }

        return composite;
    }

    public boolean isProjectSpecificSettingsEnabled() {
        if (projectSpecificSettingsEnabledButton != null) {
            return projectSpecificSettingsEnabledButton.getSelection();
        } else {
            return false;
        }
    }

    public void setProjectSpecificSettingsEnabled(boolean enabled) {
        if (projectSpecificSettingsEnabledButton != null) {
            projectSpecificSettingsEnabledButton.setSelection(enabled);
            templateContentLabel.setEnabled(enabled);
            templateContentText.setEnabled(enabled);

        }
    }

    public String getTemplate() {
        return templateContentText.getText();
    }

    public void setTemplate(String text) {
        templateContentText.setText(text);
    }
}
