package org.seasar.ymir.eclipse.wizards;

import static org.seasar.ymir.eclipse.wizards.NewProjectWizard.REQUIRED_TEMPLATE;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
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
import org.seasar.ymir.eclipse.ArtifactPair;
import org.seasar.ymir.eclipse.ui.ViliProjectPreferencesControl;
import org.seasar.ymir.eclipse.ui.YmirConfigurationControl;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;

public class ConfigureParametersPage extends WizardPage {
    private static final int SCROLL_UNIT = 16;

    private Listener validationListener = new Listener() {
        public void handleEvent(Event event) {
            setPageComplete(validatePage());
        }
    };

    private IProject project;

    private ViliProjectPreferences preferences;

    private Composite tabFolderParent;

    private CTabFolder tabFolder;

    private Label tabLabel;

    private ViliProjectPreferencesControl preferencesControl;

    private Composite skeletonTabContent;

    private Composite ymirConfigurationTabContent;

    private boolean tabPrepared;

    private ArtifactPair skeleton;

    private ViliBehavior SkeletonBehavior;

    private ArtifactPair[] fragments;

    private Map<String, ParameterModel>[] parameterModelMaps;

    private ParameterModel[] requiredParameterModels = new ParameterModel[0];

    private YmirConfigurationControl ymirConfigurationControl;

    public ConfigureParametersPage(IProject project, ViliProjectPreferences preferences) {
        super("ConfigureParametersPage"); //$NON-NLS-1$

        this.project = project;
        this.preferences = preferences;

        setTitle(Messages.getString("ConfigureParametersPage.1")); //$NON-NLS-1$
        setDescription(Messages.getString("ConfigureParametersPage.2")); //$NON-NLS-1$

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
        boolean skeletonParameterExists = skeletonParameterExists();

        if (skeleton != null || skeletonParameterExists) {
            tabFolder = new CTabFolder(tabFolderParent, SWT.NULL);
            tabFolder.setLayout(new FillLayout());
            tabFolder.setSimple(false);
            tabFolder.setTabHeight(tabFolder.getTabHeight() + 2);
        } else {
            tabLabel = new Label(tabFolderParent, SWT.NULL);
            tabLabel.setText(Messages.getString("ConfigureParametersPage.13")); //$NON-NLS-1$
        }

        if (skeleton != null) {
            CTabItem genericTabItem = new CTabItem(tabFolder, SWT.NONE);
            genericTabItem.setText(Messages.getString("ConfigureParametersPage.11")); //$NON-NLS-1$

            Composite genericTabContent = new Composite(tabFolder, SWT.NULL);
            genericTabContent.setLayout(new GridLayout());
            genericTabItem.setControl(genericTabContent);

            preferencesControl = new ViliProjectPreferencesControl(genericTabContent, preferences, SkeletonBehavior
                    .isProjectOf(ProjectType.WEB), SkeletonBehavior.isProjectOf(ProjectType.DATABASE)) {
                @Override
                public void setErrorMessage(String message) {
                    ConfigureParametersPage.this.setErrorMessage(message);
                }
            };
            preferencesControl.createControl();
        }

        if (skeletonParameterExists) {
            CTabItem skeletonTabItem = new CTabItem(tabFolder, SWT.NONE);
            skeletonTabItem.setText(Messages.getString("ConfigureParametersPage.12")); //$NON-NLS-1$

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

        if (skeleton != null) {
            if (SkeletonBehavior.isProjectOf(ProjectType.YMIR)) {
                CTabItem ymirConfigurationTabItem = new CTabItem(tabFolder, SWT.NONE);
                ymirConfigurationTabItem.setText(Messages.getString("ConfigureParametersPage.0")); //$NON-NLS-1$

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
                        ConfigureParametersPage.this.setErrorMessage(message);
                    }
                };
                ymirConfigurationControl.createControl();
                scroll.setMinHeight(ymirConfigurationTabContent.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
            }
        }

        tabFolderParent.layout();

        setDefaultValues();
    }

    private boolean skeletonParameterExists() {
        int count = 0;
        for (ArtifactPair pair : getArtifactPairs()) {
            count += pair.getBehavior().getTemplateParameters().length;
        }
        return count > 0;
    }

    private ArtifactPair[] getArtifactPairs() {
        List<ArtifactPair> pairList = new ArrayList<ArtifactPair>();
        if (skeleton != null) {
            pairList.add(skeleton);
        }
        if (fragments != null) {
            pairList.addAll(Arrays.asList(fragments));
        }
        return pairList.toArray(new ArtifactPair[0]);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    void createSkeletonParametersControl(Composite parent) {
        ArtifactPair[] pairs = getArtifactPairs();
        parameterModelMaps = new Map[pairs.length];
        java.util.List<ParameterModel> requiredList = new ArrayList<ParameterModel>();

        int count = 0;
        for (int i = 0; i < pairs.length; i++) {
            Map<String, ParameterModel> modelMap = new HashMap<String, ParameterModel>();
            parameterModelMaps[i] = modelMap;

            ArtifactPair pair = pairs[i];
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
                case TEXT: {
                    new Label(group, SWT.NONE).setText(behavior.getTemplateParameterLabel(name));
                    Text text = new Text(group, SWT.BORDER);
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

                case CHECKBOX: {
                    Button button = new Button(group, SWT.CHECK | SWT.LEFT);
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

                case SELECT: {
                    new Label(group, SWT.NONE).setText(behavior.getTemplateParameterLabel(name));
                    Combo combo = new Combo(group, SWT.READ_ONLY);
                    ParameterModel model = new ComboParameterModel(pair, name, combo);
                    modelMap.put(name, model);
                    GridData data = new GridData(GridData.FILL_HORIZONTAL);
                    data.widthHint = 250;
                    combo.setLayoutData(data);
                    String defaultValue = behavior.getTemplateParameterDefault(name);
                    String[] candidates = behavior.getTemplateParameterCandidates(name);
                    int selectedIndex = -1;
                    for (int k = 0; k < candidates.length; k++) {
                        combo.add(candidates[k]);
                        if (candidates[k].equals(defaultValue)) {
                            selectedIndex = k;
                        }
                    }
                    if (selectedIndex >= 0) {
                        combo.select(selectedIndex);
                    }
                    if (description.length() > 0) {
                        combo.setToolTipText(description);
                    }
                    if (behavior.isTemplateParameterRequired(name)) {
                        requiredList.add(model);
                        combo.addListener(SWT.Modify, validationListener);
                    }
                }
                    break;

                case COMBOBOX: {
                    new Label(group, SWT.NONE).setText(behavior.getTemplateParameterLabel(name));
                    Combo combo = new Combo(group, SWT.DROP_DOWN);
                    ParameterModel model = new ComboParameterModel(pair, name, combo);
                    modelMap.put(name, model);
                    GridData data = new GridData(GridData.FILL_HORIZONTAL);
                    data.widthHint = 250;
                    combo.setLayoutData(data);
                    String defaultValue = behavior.getTemplateParameterDefault(name);
                    String[] candidates = behavior.getTemplateParameterCandidates(name);
                    int selectedIndex = -1;
                    for (int k = 0; k < candidates.length; k++) {
                        combo.add(candidates[k]);
                        if (candidates[k].equals(defaultValue)) {
                            selectedIndex = k;
                        }
                    }
                    if (selectedIndex >= 0) {
                        combo.select(selectedIndex);
                    } else if (defaultValue != null) {
                        combo.setText(defaultValue);
                    }
                    if (description.length() > 0) {
                        combo.setToolTipText(description);
                    }
                    if (behavior.isTemplateParameterRequired(name)) {
                        requiredList.add(model);
                        combo.addListener(SWT.Modify, validationListener);
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
                ISelectArtifactWizard wizard = (ISelectArtifactWizard) getWizard();
                skeleton = wizard.getSkeletonArtifactPair();
                if (skeleton != null) {
                    SkeletonBehavior = skeleton.getBehavior();
                }
                fragments = wizard.getFragmentArtifactPairs();
                for (ArtifactPair fragment : fragments) {
                    ViliBehavior fragmentBehavior = fragment.getBehavior();
                    fragmentBehavior.getConfigurator().start(project, fragmentBehavior, preferences);
                }
                createTabFolder();
                tabPrepared = true;

                if (tabFolder != null) {
                    tabFolder.setSelection(0);
                }
                if (preferencesControl != null) {
                    preferencesControl.setVisible(true);
                }

                setPageComplete(validatePage());
            }
        }
    }

    @Override
    public boolean isPageComplete() {
        return super.isPageComplete() && (preferencesControl == null || preferencesControl.isPageComplete())
                && (ymirConfigurationControl == null || ymirConfigurationControl.isPageComplete());
    }

    public void notifySkeletonAndFragmentsCleared() {
        skeleton = null;
        SkeletonBehavior = null;
        fragments = null;

        if (tabFolder != null) {
            tabFolder.dispose();
            tabFolder = null;
        }
        if (tabLabel != null) {
            tabLabel.dispose();
            tabLabel = null;
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

    public void notifyFragmentsChanged() {
        fragments = null;

        if (tabFolder != null) {
            tabFolder.dispose();
            tabFolder = null;
        }
        if (tabLabel != null) {
            tabLabel.dispose();
            tabLabel = null;
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
        if (preferencesControl != null && !preferencesControl.validatePage()) {
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
        if (preferencesControl != null) {
            preferencesControl.setDefaultValues();
        }
        if (ymirConfigurationControl != null) {
            ymirConfigurationControl.setDefaultValues();
        }
        if (tabFolder != null) {
            tabFolder.setSelection(0);
        }
    }

    public void populateSkeletonParameters() {
        if (parameterModelMaps == null) {
            return;
        }

        ArtifactPair[] pairs = getArtifactPairs();
        for (int i = 0; i < parameterModelMaps.length; i++) {
            Map<String, ParameterModel> modelMap = parameterModelMaps[i];
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            for (Iterator<Map.Entry<String, ParameterModel>> itr = modelMap.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<String, ParameterModel> entry = itr.next();
                parameterMap.put(entry.getKey(), entry.getValue().getObject());
            }
            pairs[i].setParameterMap(parameterMap);
        }
    }

    public YmirConfigurationControl getYmirConfigurationControl() {
        return ymirConfigurationControl;
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
            return text.getText().trim().length() > 0;
        }

        public String getLabel() {
            return pair.getBehavior().getTemplateParameterLabel(name);
        }

        public Object getObject() {
            return text.getText().trim();
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

    static class ComboParameterModel implements ParameterModel {
        private ArtifactPair pair;

        private String name;

        private Combo combo;

        ComboParameterModel(ArtifactPair pair, String name, Combo combo) {
            this.pair = pair;
            this.name = name;
            this.combo = combo;
        }

        public boolean valueExists() {
            return combo.getText().trim().length() > 0;
        }

        public String getLabel() {
            return pair.getBehavior().getTemplateParameterLabel(name);
        }

        public Object getObject() {
            return combo.getText().trim();
        }
    }
}