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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Text;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.ymir.eclipse.ArtifactPair;
import org.seasar.ymir.eclipse.ViliBehavior;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;
import org.seasar.ymir.eclipse.ui.ViliProjectPreferencesControl;
import org.seasar.ymir.eclipse.ui.YmirConfigurationControl;

/**
 * The "New" wizard page allows setting the container for the new file as well
 * as the file name. The page will only accept file name without the extension
 * OR with the extension that matches the expected one (mpe).
 */

public class NewProjectWizardThirdPage extends WizardPage {
    private static final int SCROLL_UNIT = 16;

    private Listener validationListener = new Listener() {
        public void handleEvent(Event event) {
            setPageComplete(validatePage());
        }
    };

    private ViliProjectPreferences preferences;

    private Composite tabFolderParent;

    private CTabFolder tabFolder;

    private ViliProjectPreferencesControl preferencesControl;

    private Composite skeletonTabContent;

    private Composite ymirConfigurationTabContent;

    private boolean tabPrepared;

    private ArtifactPair[] skeletonAndFragments;

    private ViliBehavior behavior;

    private Map<String, ParameterModel>[] parameterModelMaps;

    private ParameterModel[] requiredParameterModels = new ParameterModel[0];

    private YmirConfigurationControl ymirConfigurationControl;

    public NewProjectWizardThirdPage(ViliProjectPreferences preferences) {
        super("NewProjectWizardThirdPage"); //$NON-NLS-1$

        this.preferences = preferences;

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

        preferencesControl = new ViliProjectPreferencesControl(genericTabContent, preferences, behavior.isJavaProject()) {
            @Override
            public void setErrorMessage(String message) {
                NewProjectWizardThirdPage.this.setErrorMessage(message);
            }
        };
        preferencesControl.createControl();

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

        if (behavior.isYmirProject()) {
            CTabItem ymirConfigurationTabItem = new CTabItem(tabFolder, SWT.NONE);
            ymirConfigurationTabItem.setText("Ymirプロジェクト設定");

            final ScrolledComposite scroll = new ScrolledComposite(tabFolder, SWT.V_SCROLL);
            scroll.setLayout(new FillLayout());
            scroll.setExpandHorizontal(true); // ←君の意味を勘違いしていたせいで8時間を無駄にしたよ... orz 2008-09-20
            scroll.setExpandVertical(true);
            scroll.getVerticalBar().addSelectionListener(new SelectionAdapter() {
                @Override
                public void widgetSelected(SelectionEvent e) {
                    if (e.detail == SWT.ARROW_UP) {
                        scroll.getVerticalBar().setIncrement(-SCROLL_UNIT);
                    } else if (e.detail == SWT.ARROW_DOWN) {
                        scroll.getVerticalBar().setIncrement(SCROLL_UNIT);
                    }
                }
            });
            ymirConfigurationTabItem.setControl(scroll);

            ymirConfigurationTabContent = new Composite(scroll, SWT.NONE);
            ymirConfigurationTabContent.setLayout(new GridLayout());
            scroll.setContent(ymirConfigurationTabContent);

            ymirConfigurationControl = new YmirConfigurationControl(ymirConfigurationTabContent, preferences) {
                @Override
                public void setErrorMessage(String message) {
                    NewProjectWizardThirdPage.this.setErrorMessage(message);
                }
            };
            ymirConfigurationControl.createControl();
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

    @SuppressWarnings("unchecked")//$NON-NLS-1$
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
                behavior = skeletonAndFragments[0].getBehavior();
                createTabFolder();
                tabPrepared = true;

                tabFolder.setSelection(0);
                preferencesControl.setVisible(true);

                setPageComplete(validatePage());
            }
        }
    }

    @Override
    public boolean isPageComplete() {
        return super.isPageComplete() && preferencesControl != null && preferencesControl.isPageComplete()
                && (ymirConfigurationControl == null || ymirConfigurationControl.isPageComplete());
    }

    public void notifySkeletonAndFragmentsCleared() {
        skeletonAndFragments = null;
        behavior = null;

        if (tabFolder != null) {
            tabFolder.dispose();
            tabFolder = null;
        }
        tabPrepared = false;

        skeletonTabContent = null;
        parameterModelMaps = null;
        requiredParameterModels = new ParameterModel[0];

        preferencesControl = null;

        ymirConfigurationTabContent = null;
        ymirConfigurationControl = null;

        setPageComplete(false);
    }

    boolean validatePage() {
        if (!preferencesControl.validatePage()) {
            return false;
        }

        for (ParameterModel model : requiredParameterModels) {
            if (!model.valueExists()) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, model.getLabel()));
                return false;
            }
        }

        if (ymirConfigurationControl != null && !ymirConfigurationControl.validatePage()) {
            return false;
        }

        setErrorMessage(null);
        return true;
    }

    void setDefaultValues() {
        preferencesControl.setDefaultValues();
        if (ymirConfigurationControl != null) {
            ymirConfigurationControl.setDefaultValues();
        }
        tabFolder.setSelection(0);
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

    public YmirConfigurationControl getYmirConfigurationControl() {
        return ymirConfigurationControl;
    }

    public void populateViliProjectPreferences() {
        preferencesControl.populateViliProjectPreferences();
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