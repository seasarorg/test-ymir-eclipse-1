package org.seasar.ymir.eclipse.wizards;

import static org.seasar.ymir.eclipse.wizards.NewProjectWizard.REQUIRED_TEMPLATE;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ArtifactPair;
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.ViliBehavior;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewProjectWizardThirdPage extends WizardPage {
    private static final int DEFAULT_DATABASE_INDEX = 0;

    private Listener validationListener = new Listener() {
        public void handleEvent(Event event) {
            setPageComplete(validatePage());
        }
    };

    private DatabaseEntry[] entries;

    private boolean initialized;

    private Composite tabFolderParent;

    private CTabFolder tabFolder;

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

    private Composite skeletonTabContent;

    private Composite ymirConfigurationTabContent;

    private boolean tabPrepared;

    private ArtifactPair[] skeletonAndFragments;

    private Map<String, ParameterModel>[] parameterModelMaps;

    private ParameterModel[] requiredParameterModels = new ParameterModel[0];

    private YmirConfigurationBlock ymirConfigurationBlock;

    /**
     * Constructor for SampleNewWizardPage.
     * 
     * @param pageName
     */
    public NewProjectWizardThirdPage() {
        super("NewProjectWizardThirdPage"); //$NON-NLS-1$

        setTitle(Messages.getString("NewProjectWizardThirdPage.1")); //$NON-NLS-1$
        setDescription(Messages.getString("NewProjectWizardThirdPage.2")); //$NON-NLS-1$

        // このページが表示されるまでスケルトンパラメータの取得を遅延させているため、
        // このページに必ず遷移するようこうしている。
        setPageComplete(false);
    }

    /**
     * @see IDialogPage#createControl(Composite)
     */
    public void createControl(Composite parent) {
        tabFolderParent = new Composite(parent, SWT.NULL);
        tabFolderParent.setLayout(new FillLayout());
        setControl(tabFolderParent);

        setErrorMessage(null);
        setMessage(null);
    }

    void createViewParametersControl(Composite parent) {
        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(Messages.getString("NewProjectWizardThirdPage.3")); //$NON-NLS-1$

        Label encodingLabel = new Label(group, SWT.NONE);
        encodingLabel.setText(Messages.getString("NewProjectWizardThirdPage.4")); //$NON-NLS-1$

        viewEncodingField = new Text(group, SWT.BORDER);
        GridData data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        viewEncodingField.setLayoutData(data);
        viewEncodingField.addListener(SWT.Modify, validationListener);
    }

    void createDatabaseParametersControl(Composite parent) {
        useDatabaseField = new Button(parent, SWT.CHECK | SWT.LEFT);
        useDatabaseField.setText(Messages.getString("NewProjectWizardThirdPage.5")); //$NON-NLS-1$
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
        databaseGroup.setText(Messages.getString("NewProjectWizardThirdPage.6")); //$NON-NLS-1$

        entries = Activator.getDefault().getDatabaseEntries();
        String[] items = new String[entries.length];
        for (int i = 0; i < entries.length; i++) {
            items[i] = entries[i].getName();
        }

        databaseLabel = new Label(databaseGroup, SWT.NONE);
        databaseLabel.setText(Messages.getString("NewProjectWizardThirdPage.0")); //$NON-NLS-1$

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
        databaseDriverClassNameLabel.setText(Messages.getString("NewProjectWizardThirdPage.7")); //$NON-NLS-1$

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
        databaseURLLabel.setText(Messages.getString("NewProjectWizardThirdPage.8")); //$NON-NLS-1$

        databaseURLField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseURLField.setLayoutData(data);
        databaseURLField.addListener(SWT.Modify, validationListener);

        databaseUserLabel = new Label(databaseGroup, SWT.NONE);
        databaseUserLabel.setText(Messages.getString("NewProjectWizardThirdPage.9")); //$NON-NLS-1$

        databaseUserField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databaseUserField.setLayoutData(data);

        databasePasswordLabel = new Label(databaseGroup, SWT.NONE);
        databasePasswordLabel.setText(Messages.getString("NewProjectWizardThirdPage.10")); //$NON-NLS-1$

        databasePasswordField = new Text(databaseGroup, SWT.BORDER);
        data = new GridData(GridData.FILL_HORIZONTAL);
        data.widthHint = 250;
        databasePasswordField.setLayoutData(data);
    }

    void createTabFolder() {
        tabFolder = new CTabFolder(tabFolderParent, SWT.NULL);
        tabFolder.setLayout(new FillLayout());
        tabFolder.setSimple(false);
        tabFolder.setTabHeight(tabFolder.getTabHeight() + 2);

        CTabItem genericTabItem = new CTabItem(tabFolder, SWT.NONE);
        genericTabItem.setText(Messages.getString("NewProjectWizardThirdPage.11")); //$NON-NLS-1$

        Composite genericTabContent = new Composite(tabFolder, SWT.NULL);
        genericTabContent.setLayout(new GridLayout());
        genericTabItem.setControl(genericTabContent);
        createViewParametersControl(genericTabContent);
        createDatabaseParametersControl(genericTabContent);

        if (skeletonParameterExists()) {
            CTabItem skeletonTabItem = new CTabItem(tabFolder, SWT.NONE);
            skeletonTabItem.setText(Messages.getString("NewProjectWizardThirdPage.12")); //$NON-NLS-1$

            ScrolledComposite scroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL);
            scroll.setLayout(new FillLayout());
            scroll.setExpandHorizontal(true);
            scroll.setExpandVertical(true);
            skeletonTabItem.setControl(scroll);

            skeletonTabContent = new Composite(scroll, SWT.NULL);
            skeletonTabContent.setLayout(new GridLayout());
            scroll.setContent(skeletonTabContent);

            createSkeletonParametersControl(skeletonTabContent);
            scroll.setMinHeight(skeletonTabContent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        }

        if (skeletonAndFragments[0].getBehavior().isYmirProject()) {
            ymirConfigurationBlock = new YmirConfigurationBlock(this);

            CTabItem ymirConfigurationTabItem = new CTabItem(tabFolder, SWT.NONE);
            ymirConfigurationTabItem.setText(ymirConfigurationBlock.getTabLabel());

            ScrolledComposite scroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL);
            scroll.setLayout(new FillLayout());
            scroll.setExpandHorizontal(true); // ←君の意味を勘違いしていたせいで8時間を無駄にしたよ... orz 2008-09-20
            scroll.setExpandVertical(true);
            ymirConfigurationTabItem.setControl(scroll);

            ymirConfigurationTabContent = new Composite(scroll, SWT.NONE);
            ymirConfigurationTabContent.setLayout(new GridLayout());
            scroll.setContent(ymirConfigurationTabContent);

            ymirConfigurationBlock.createControl(ymirConfigurationTabContent);
            scroll.setMinHeight(ymirConfigurationTabContent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
        }

        tabFolderParent.layout();

        setDefaultValues();
    }

    private boolean skeletonParameterExists() {
        int count = 0;
        for (ArtifactPair pair : skeletonAndFragments) {
            count += pair.getBehavior().getTemplateParameters().length;
        }
        return count > 0;
    }

    @SuppressWarnings("unchecked") //$NON-NLS-1$
    void createSkeletonParametersControl(Composite parent) {
        parameterModelMaps = new Map[skeletonAndFragments.length];
        java.util.List<ParameterModel> requiredList = new ArrayList<ParameterModel>();

        int count = 0;
        for (int i = 0; i < skeletonAndFragments.length; i++) {
            Map<String, ParameterModel> modelMap = new HashMap<String, ParameterModel>();
            parameterModelMaps[i] = modelMap;

            ArtifactPair pair = skeletonAndFragments[i];
            ViliBehavior behavior = pair.getBehavior();
            String[] names = behavior.getTemplateParameters();
            count += names.length;
            if (names.length == 0) {
                continue;
            }

            Group group = new Group(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            group.setLayout(layout);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            group.setText(behavior.getLabel());

            for (int j = 0; j < names.length; j++) {
                String name = names[j];
                String description = behavior.getTemplateParameterDescription(name);
                switch (behavior.getTemplateParameterType(name)) {
                case TEXT:
                    new Label(group, SWT.NONE).setText(behavior.getTemplateParameterLabel(name));
                    Text text = new Text(group, SWT.BORDER);
                    {
                        ParameterModel model = new TextParameterModel(pair, name, text);
                        modelMap.put(name, model);
                        GridData data = new GridData(GridData.FILL_HORIZONTAL);
                        data.widthHint = 250;
                        text.setLayoutData(data);
                        text.setText(behavior.getTemplateParameterDefault(name));
                        if (description.length() > 0) {
                            text.setToolTipText(description);
                        }
                        if (behavior.isTemplateParameterRequired(name)) {
                            requiredList.add(model);
                            text.addListener(SWT.Modify, validationListener);
                        }
                    }
                    break;

                case CHECKBOX:
                    Button button = new Button(group, SWT.CHECK | SWT.LEFT);
                    {
                        ParameterModel model = new ButtonParameterModel(pair, name, button);
                        modelMap.put(name, model);
                        GridData data = new GridData();
                        data.horizontalSpan = 2;
                        button.setLayoutData(data);
                        button.setSelection(PropertyUtils.valueOf(behavior.getTemplateParameterDefault(name), false));
                        button.setText(behavior.getTemplateParameterLabel(name));
                        if (description.length() > 0) {
                            button.setToolTipText(description);
                        }
                    }
                    break;

                default:
                }
            }
        }
        requiredParameterModels = requiredList.toArray(new ParameterModel[0]);
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (!tabPrepared) {
                skeletonAndFragments = ((NewProjectWizard) getWizard()).getSkeletonAndFragments();
                createTabFolder();
                tabPrepared = true;

                tabFolder.setSelection(0);
                viewEncodingField.setFocus();

                setPageComplete(validatePage());
            }
        }
    }

    public void clearSkeletonParameters() {
        skeletonAndFragments = null;

        if (tabFolder != null) {
            tabFolder.dispose();
            tabFolder = null;
        }
        tabPrepared = false;

        skeletonTabContent = null;
        parameterModelMaps = null;
        requiredParameterModels = new ParameterModel[0];

        ymirConfigurationTabContent = null;
        ymirConfigurationBlock = null;

        setPageComplete(false);
    }

    boolean validatePage() {
        if (getViewEncoding().length() == 0) {
            setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages.getString("NewProjectWizardThirdPage.4"))); //$NON-NLS-1$
            return false;
        }
        if (isUseDatabase()) {
            if (getDatabaseDriverClassName().length() == 0) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages
                        .getString("NewProjectWizardThirdPage.7"))); //$NON-NLS-1$
                return false;
            }
            if (getDatabaseURL().length() == 0) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, Messages
                        .getString("NewProjectWizardThirdPage.8"))); //$NON-NLS-1$
                return false;
            }
        }

        for (ParameterModel model : requiredParameterModels) {
            if (!model.valueExists()) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, model.getLabel()));
                return false;
            }
        }

        if (ymirConfigurationBlock != null && !ymirConfigurationBlock.validatePage()) {
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    void setDefaultValues() {
        tabFolder.setSelection(0);

        viewEncodingField.setText("UTF-8"); //$NON-NLS-1$
        useDatabaseField.setSelection(true);
        databaseCombo.setText(databaseCombo.getItem(DEFAULT_DATABASE_INDEX));
        databaseDriverClassNameField.setText(entries[DEFAULT_DATABASE_INDEX].getDriverClassName());
        databaseURLField.setText(entries[DEFAULT_DATABASE_INDEX].getURL());
        databaseUserField.setText(entries[DEFAULT_DATABASE_INDEX].getUser());
        databasePasswordField.setText(entries[DEFAULT_DATABASE_INDEX].getPassword());
    }

    public String getViewEncoding() {
        return viewEncodingField.getText();
    }

    public boolean isUseDatabase() {
        return useDatabaseField.getSelection();
    }

    public DatabaseEntry getDatabaseEntry() {
        int idx = databaseCombo.getSelectionIndex();
        return new DatabaseEntry(entries[idx].getName(), entries[idx].getType(), getDatabaseDriverClassName(),
                getDatabaseURL(), getDatabaseUser(), getDatabasePassword(), entries[idx].getDependency());
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

    public void populateSkeletonParameters() {
        if (parameterModelMaps == null) {
            return;
        }

        for (int i = 0; i < parameterModelMaps.length; i++) {
            Map<String, ParameterModel> modelMap = parameterModelMaps[i];
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            for (Iterator<Map.Entry<String, ParameterModel>> itr = modelMap.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<String, ParameterModel> entry = itr.next();
                parameterMap.put(entry.getKey(), entry.getValue().getObject());
            }
            skeletonAndFragments[i].setParameterMap(parameterMap);
        }
    }

    public YmirConfigurationBlock getYmirConfigurationBlock() {
        return ymirConfigurationBlock;
    }

    static interface ParameterModel {
        boolean valueExists();

        String getLabel();

        Object getObject();
    }

    static class TextParameterModel implements ParameterModel {
        private ArtifactPair pair;

        private String name;

        private Text text;

        TextParameterModel(ArtifactPair pair, String name, Text text) {
            this.pair = pair;
            this.name = name;
            this.text = text;
        }

        public boolean valueExists() {
            return text.getText().length() > 0;
        }

        public String getLabel() {
            return pair.getBehavior().getTemplateParameterLabel(name);
        }

        public Object getObject() {
            return text.getText();
        }
    }

    static class ButtonParameterModel implements ParameterModel {
        private ArtifactPair pair;

        private String name;

        private Button button;

        ButtonParameterModel(ArtifactPair pair, String name, Button button) {
            this.pair = pair;
            this.name = name;
            this.button = button;
        }

        public boolean valueExists() {
            return true;
        }

        public String getLabel() {
            return pair.getBehavior().getTemplateParameterLabel(name);
        }

        public Object getObject() {
            return Boolean.valueOf(button.getSelection());
        }
    }
}