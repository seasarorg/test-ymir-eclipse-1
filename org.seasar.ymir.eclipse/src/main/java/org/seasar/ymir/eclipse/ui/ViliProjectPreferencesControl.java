package org.seasar.ymir.eclipse.ui;

import java.text.MessageFormat;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.seasar.ymir.vili.DatabaseEntry;
import org.seasar.ymir.vili.ViliProjectPreferences;

public class ViliProjectPreferencesControl {
    private static final String REQUIRED_TEMPLATE = Messages.getString("ViliProjectPreferencesControl.0"); //$NON-NLS-1$

    private static final int DEFAULT_DATABASEENTRY_INDEX = 0;

    private Composite parent;

    private ViliProjectPreferences preferences;

    private boolean isWebProject;

    private boolean isDatabaseProject;

    private DatabaseEntry[] entries;

    private boolean isPageComplete;

    private Listener validationListener = new Listener() {
        public void handleEvent(Event event) {
            setPageComplete(validatePage());
        }
    };

    private Text viewEncodingField;

    private Button useDatabaseField;

    private Label databaseLabel;

    private Combo databaseCombo;

    private Label databaseDriverClassNameLabel;

    private Text databaseDriverClassNameField;

    private Label databaseURLLabel;

    private Text databaseURLField;

    private Label databaseUserLabel;

    private Text databaseUserField;

    private Label databasePasswordLabel;

    private Text databasePasswordField;

    public ViliProjectPreferencesControl(Composite parent, ViliProjectPreferences preferences, boolean isWebProject,
            boolean isDatabaseProject) {
        this.parent = parent;
        this.preferences = preferences;
        this.isWebProject = isWebProject;
        this.isDatabaseProject = isDatabaseProject;

        entries = preferences.getDatabaseEntries();
    }

    public Control createControl() {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (isWebProject) {
            createViewParametersControl(composite);
        }
        if (isDatabaseProject) {
            createDatabaseParametersControl(composite);
        }
        if (!isWebProject && !isDatabaseProject) {
            new Label(composite, SWT.NULL).setText(Messages.getString("ViliProjectPreferencesControl.1")); //$NON-NLS-1$
        }

        return composite;
    }

    void createViewParametersControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("ViliProjectPreferencesControl.2")); //$NON-NLS-1$

        Label encodingLabel = new Label(group, SWT.NONE);
        encodingLabel.setText(Messages.getString("ViliProjectPreferencesControl.3")); //$NON-NLS-1$

        viewEncodingField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        viewEncodingField.setLayoutData(data);
        viewEncodingField.addListener(SWT.Modify, validationListener);
        viewEncodingField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                preferences.setViewEncoding(viewEncodingField.getText().trim());
            }
        });
    }

    void createDatabaseParametersControl(Composite parent) {
        useDatabaseField = new Button(parent, SWT.CHECK | SWT.LEFT);
        useDatabaseField.setText(Messages.getString("ViliProjectPreferencesControl.4")); //$NON-NLS-1$
        useDatabaseField.addListener(SWT.Selection, validationListener);
        useDatabaseField.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                boolean enabled = useDatabaseField.getSelection();
                databaseLabel.setEnabled(enabled);
                databaseCombo.setEnabled(enabled);
                databaseDriverClassNameLabel.setEnabled(enabled);
                databaseDriverClassNameField.setEnabled(enabled);
                databaseURLLabel.setEnabled(enabled);
                databaseURLField.setEnabled(enabled);
                databaseUserLabel.setEnabled(enabled);
                databaseUserField.setEnabled(enabled);
                databasePasswordLabel.setEnabled(enabled);
                databasePasswordField.setEnabled(enabled);
            }
        });
        useDatabaseField.addListener(SWT.Selection, new Listener() {
            public void handleEvent(Event event) {
                preferences.setUseDatabase(useDatabaseField.getSelection());
            }
        });

        Group databaseGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        databaseGroup.setLayout(layout);
        databaseGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        databaseGroup.setText(Messages.getString("ViliProjectPreferencesControl.5")); //$NON-NLS-1$

        String[] items = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            items[i] = entries[i].getName();
        }

        databaseLabel = new Label(databaseGroup, SWT.NONE);
        databaseLabel.setText(Messages.getString("ViliProjectPreferencesControl.6")); //$NON-NLS-1$

        databaseCombo = new Combo(databaseGroup, SWT.READ_ONLY);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseCombo.setLayoutData(data);
        databaseCombo.setItems(items);
        databaseCombo.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
                int idx = databaseCombo.getSelectionIndex();
                databaseDriverClassNameField.setText(entries[idx].getDriverClassName());
                databaseURLField.setText(entries[idx].getURL());
                databaseUserField.setText(entries[idx].getUser());
                databasePasswordField.setText(entries[idx].getPassword());
                DatabaseEntry entry = preferences.getDatabaseEntry();
                entry.setType(entries[idx].getType());
                entry.setName(entries[idx].getName());
                entry.setDependency(entries[idx].getDependency());

                setPageComplete(validatePage());
            }
        });

        databaseDriverClassNameLabel = new Label(databaseGroup, SWT.NONE);
        databaseDriverClassNameLabel.setText(Messages.getString("ViliProjectPreferencesControl.7")); //$NON-NLS-1$

        databaseDriverClassNameField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseDriverClassNameField.setLayoutData(data);
        databaseDriverClassNameField.addListener(SWT.Modify, validationListener);
        databaseDriverClassNameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event e) {
                if (!databaseDriverClassNameField.getText().equals(
                        entries[databaseCombo.getSelectionIndex()].getDriverClassName())) {
                    databaseCombo.setText(databaseCombo.getItem(entries.length - 1));
                    DatabaseEntry entry = preferences.getDatabaseEntry();
                    entry.setName(entries[entries.length - 1].getName());
                    entry.setType(entries[entries.length - 1].getType());
                }
            }
        });
        databaseDriverClassNameField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                preferences.getDatabaseEntry().setDriverClassName(databaseDriverClassNameField.getText().trim());
            }
        });

        databaseURLLabel = new Label(databaseGroup, SWT.NONE);
        databaseURLLabel.setText(Messages.getString("ViliProjectPreferencesControl.8")); //$NON-NLS-1$

        databaseURLField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseURLField.setLayoutData(data);
        databaseURLField.addListener(SWT.Modify, validationListener);
        databaseURLField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                preferences.getDatabaseEntry().setURL(databaseURLField.getText().trim());
            }
        });

        databaseUserLabel = new Label(databaseGroup, SWT.NONE);
        databaseUserLabel.setText(Messages.getString("ViliProjectPreferencesControl.9")); //$NON-NLS-1$

        databaseUserField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseUserField.setLayoutData(data);
        databaseUserField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                preferences.getDatabaseEntry().setUser(databaseUserField.getText().trim());
            }
        });

        databasePasswordLabel = new Label(databaseGroup, SWT.NONE);
        databasePasswordLabel.setText(Messages.getString("ViliProjectPreferencesControl.10")); //$NON-NLS-1$

        databasePasswordField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databasePasswordField.setLayoutData(data);
        databasePasswordField.addListener(SWT.Modify, new Listener() {
            public void handleEvent(Event event) {
                preferences.getDatabaseEntry().setPassword(databasePasswordField.getText().trim());
            }
        });
    }

    public void setVisible(boolean visible) {
        if (viewEncodingField != null) {
            viewEncodingField.setFocus();
        }
    }

    public void setDefaultValues() {
        if (isWebProject) {
            viewEncodingField.setText("UTF-8");
        }
        if (isDatabaseProject) {
            useDatabaseField.setSelection(true);
            databaseCombo.setText(databaseCombo.getItem(DEFAULT_DATABASEENTRY_INDEX));
            databaseDriverClassNameField.setText(entries[DEFAULT_DATABASEENTRY_INDEX].getDriverClassName());
            databaseURLField.setText(entries[DEFAULT_DATABASEENTRY_INDEX].getURL());
            databaseUserField.setText(entries[DEFAULT_DATABASEENTRY_INDEX].getUser());
            databasePasswordField.setText(entries[DEFAULT_DATABASEENTRY_INDEX].getPassword());
            DatabaseEntry entry = preferences.getDatabaseEntry();
            entry.setName(entries[DEFAULT_DATABASEENTRY_INDEX].getName());
            entry.setType(entries[DEFAULT_DATABASEENTRY_INDEX].getType());
            entry.setDependency(entries[DEFAULT_DATABASEENTRY_INDEX].getDependency());
        }

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (isWebProject) {
            if (getViewEncoding().length() == 0) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages
                        .getString("ViliProjectPreferencesControl.3"))); //$NON-NLS-1$
                return false;
            }
        }
        if (isDatabaseProject) {
            if (isUseDatabase()) {
                if (getDatabaseDriverClassName().length() == 0) {
                    setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages
                            .getString("ViliProjectPreferencesControl.7"))); //$NON-NLS-1$
                    return false;
                }
                if (getDatabaseURL().length() == 0) {
                    setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages
                            .getString("ViliProjectPreferencesControl.8"))); //$NON-NLS-1$
                    return false;
                }
            }
        }

        setErrorMessage(null);
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

    private String getViewEncoding() {
        if (isWebProject) {
            return viewEncodingField.getText();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    private boolean isUseDatabase() {
        return isDatabaseProject && useDatabaseField.getSelection();
    }

    private String getDatabaseDriverClassName() {
        if (isUseDatabase()) {
            return databaseDriverClassNameField.getText();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    private String getDatabaseURL() {
        if (isUseDatabase()) {
            return databaseURLField.getText();
        } else {
            return ""; //$NON-NLS-1$
        }
    }
}
