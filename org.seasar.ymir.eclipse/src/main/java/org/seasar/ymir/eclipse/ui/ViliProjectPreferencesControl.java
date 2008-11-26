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
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;

public class ViliProjectPreferencesControl {
    private static final String REQUIRED_TEMPLATE = "{0}を指定して下さい。";

    private Composite parent;

    private ViliProjectPreferences preferences;

    private boolean isJavaProject;

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

    public ViliProjectPreferencesControl(Composite parent, ViliProjectPreferences preferences, boolean isJavaProject) {
        this.parent = parent;
        this.preferences = preferences;
        this.isJavaProject = isJavaProject;

        entries = preferences.getDatabaseEntries();
    }

    public Control createControl() {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        if (isJavaProject) {
            createViewParametersControl(composite);
            createDatabaseParametersControl(composite);
        } else {
            new Label(composite, SWT.NULL).setText("設定可能な項目はありません。");
        }

        return composite;
    }

    void createViewParametersControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText("ビュー");

        Label encodingLabel = new Label(group, SWT.NONE);
        encodingLabel.setText("文字エンコーディング");

        viewEncodingField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        viewEncodingField.setLayoutData(data);
        viewEncodingField.addListener(SWT.Modify, validationListener);
    }

    void createDatabaseParametersControl(Composite parent) {
        useDatabaseField = new Button(parent, SWT.CHECK | SWT.LEFT);
        useDatabaseField.setText("データベースと接続する");
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

        Group databaseGroup = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        databaseGroup.setLayout(layout);
        databaseGroup.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        databaseGroup.setText("データベース");

        String[] items = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            items[i] = entries[i].getName();
        }

        databaseLabel = new Label(databaseGroup, SWT.NONE);
        databaseLabel.setText("データベースの種類");

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

                setPageComplete(validatePage());
            }
        });

        databaseDriverClassNameLabel = new Label(databaseGroup, SWT.NONE);
        databaseDriverClassNameLabel.setText("ドライバクラス");

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
                }
            }
        });

        databaseURLLabel = new Label(databaseGroup, SWT.NONE);
        databaseURLLabel.setText("接続URL");

        databaseURLField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseURLField.setLayoutData(data);
        databaseURLField.addListener(SWT.Modify, validationListener);

        databaseUserLabel = new Label(databaseGroup, SWT.NONE);
        databaseUserLabel.setText("接続ユーザ");

        databaseUserField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseUserField.setLayoutData(data);

        databasePasswordLabel = new Label(databaseGroup, SWT.NONE);
        databasePasswordLabel.setText("接続パスワード");

        databasePasswordField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databasePasswordField.setLayoutData(data);
    }

    public void setVisible(boolean visible) {
        if (viewEncodingField != null) {
            viewEncodingField.setFocus();
        }
    }

    public void setDefaultValues() {
        if (isJavaProject) {
            viewEncodingField.setText(preferences.getViewEncoding());
            useDatabaseField.setSelection(preferences.isUseDatabase());

            DatabaseEntry entry = preferences.getDatabaseEntry();
            String type = entry.getType();
            int index = entries.length - 1;
            for (int i = 0; i < entries.length; i++) {
                if (entries[i].getType().equals(type)) {
                    index = i;
                    break;
                }
            }

            databaseCombo.setText(databaseCombo.getItem(index));
            databaseDriverClassNameField.setText(entry.getDriverClassName());
            databaseURLField.setText(entry.getURL());
            databaseUserField.setText(entry.getUser());
            databasePasswordField.setText(entry.getPassword());
        }

        setPageComplete(validatePage());
    }

    public boolean validatePage() {
        if (isJavaProject) {
            if (getViewEncoding().length() == 0) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, "文字エンコーディング"));
                return false;
            }
            if (isUseDatabase()) {
                if (getDatabaseDriverClassName().length() == 0) {
                    setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, "ドライバクラス"));
                    return false;
                }
                if (getDatabaseURL().length() == 0) {
                    setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, "接続URL"));
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

    public void populateViliProjectPreferences() {
        preferences.setViewEncoding(getViewEncoding());
        preferences.setUseDatabase(isUseDatabase());
        preferences.setDatabaseEntry(getDatabaseEntry());
    }

    private String getViewEncoding() {
        if (isJavaProject) {
            return viewEncodingField.getText();
        } else {
            return "";
        }
    }

    private boolean isUseDatabase() {
        return isJavaProject && useDatabaseField.getSelection();
    }

    private DatabaseEntry getDatabaseEntry() {
        if (isJavaProject) {
            int idx = databaseCombo.getSelectionIndex();
            return new DatabaseEntry(entries[idx].getName(), entries[idx].getType(), getDatabaseDriverClassName(),
                    getDatabaseURL(), getDatabaseUser(), getDatabasePassword(), entries[idx].getDependency());
        } else {
            return new DatabaseEntry("", "", "", "", "", "", null);
        }
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

    private String getDatabaseUser() {
        if (isUseDatabase()) {
            return databaseUserField.getText();
        } else {
            return ""; //$NON-NLS-1$
        }
    }

    private String getDatabasePassword() {
        if (isUseDatabase()) {
            return databasePasswordField.getText();
        } else {
            return ""; //$NON-NLS-1$
        }
    }
}
