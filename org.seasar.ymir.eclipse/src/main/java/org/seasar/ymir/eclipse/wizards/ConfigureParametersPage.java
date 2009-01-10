package org.seasar.ymir.eclipse.wizards;

import static org.seasar.ymir.eclipse.wizards.NewProjectWizard.REQUIRED_TEMPLATE;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
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
import org.eclipse.swt.widgets.Widget;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.ui.ViliProjectPreferencesControl;
import org.seasar.ymir.eclipse.ui.YmirConfigurationControl;
import org.seasar.ymir.vili.ArtifactPair;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ArtifactVersion;

public class ConfigureParametersPage extends WizardPage {
    private static final int SCROLL_UNIT = 16;

    private Listener validationListener = new Listener() {
        public void handleEvent(Event event) {
            setPageComplete(validatePage());
        }
    };

    private Listener dependencyListener = new Listener() {
        public void handleEvent(Event event) {
            Widget item = event.item;
            if (item == null) {
                return;
            }
            boolean enabled;
            if (item instanceof Text) {
                enabled = ((Text) item).getText().trim().length() > 0;
            } else if (item instanceof Button) {
                enabled = ((Button) item).getSelection();
            } else if (item instanceof Combo) {
                enabled = ((Combo) item).getText().trim().length() > 0;
            } else {
                enabled = false;
            }
            ParameterModel model = (ParameterModel) item.getData();
            if (model != null) {
                model.setEnabled(enabled);
            }
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

    private ViliBehavior skeletonBehavior;

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

            preferencesControl = new ViliProjectPreferencesControl(genericTabContent, preferences, skeletonBehavior
                    .isProjectOf(ProjectType.WEB), skeletonBehavior.isProjectOf(ProjectType.DATABASE)) {
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
            if (skeletonBehavior.isProjectOf(ProjectType.YMIR)) {
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
                    Label label = new Label(group, SWT.NONE);
                    label.setText(behavior.getTemplateParameterLabel(name));
                    Text text = new Text(group, SWT.BORDER);
                    ParameterModel model = new TextParameterModel(pair, name, text, label);
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
                    String[] dependents = behavior.getTemplateParameterDependents(name);
                    if (dependents.length > 0) {
                        text.addListener(SWT.Modify, dependencyListener);
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
                    String[] dependents = behavior.getTemplateParameterDependents(name);
                    if (dependents.length > 0) {
                        button.addListener(SWT.Selection, dependencyListener);
                    }
                }
                    break;

                case SELECT: {
                    Label label = new Label(group, SWT.NONE);
                    label.setText(behavior.getTemplateParameterLabel(name));
                    Combo combo = new Combo(group, SWT.READ_ONLY);
                    ParameterModel model = new ComboParameterModel(pair, name, combo, label);
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
                    String[] dependents = behavior.getTemplateParameterDependents(name);
                    if (dependents.length > 0) {
                        combo.addListener(SWT.Modify, dependencyListener);
                    }
                }
                    break;

                case COMBOBOX: {
                    Label label = new Label(group, SWT.NONE);
                    label.setText(behavior.getTemplateParameterLabel(name));
                    Combo combo = new Combo(group, SWT.DROP_DOWN);
                    ParameterModel model = new ComboParameterModel(pair, name, combo, label);
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
                    } else {
                        combo.setText(defaultValue);
                    }
                    if (description.length() > 0) {
                        combo.setToolTipText(description);
                    }
                    if (behavior.isTemplateParameterRequired(name)) {
                        requiredList.add(model);
                        combo.addListener(SWT.Modify, validationListener);
                    }
                    String[] dependents = behavior.getTemplateParameterDependents(name);
                    if (dependents.length > 0) {
                        combo.addListener(SWT.Modify, dependencyListener);
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
                List<ViliBehavior> behaviorList = new ArrayList<ViliBehavior>();
                if (skeleton != null) {
                    skeletonBehavior = skeleton.getBehavior();
                    behaviorList.add(skeletonBehavior);
                }
                fragments = wizard.getFragmentArtifactPairs();
                for (ArtifactPair fragment : fragments) {
                    ViliBehavior fragmentBehavior = fragment.getBehavior();
                    behaviorList.add(fragmentBehavior);
                }

                startConfigurators(behaviorList);

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

    private void startConfigurators(final List<ViliBehavior> behaviorList) {
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                monitor.beginTask(Messages.getString("ConfigureParametersPage.14"), behaviorList.size()); //$NON-NLS-1$
                try {
                    for (ViliBehavior behavior : behaviorList) {
                        behavior.getConfigurator().start(project, behavior, preferences);
                        monitor.worked(1);
                    }
                } finally {
                    monitor.done();
                }
            }
        };

        try {
            getContainer().run(true, false, op);
        } catch (InvocationTargetException ex) {
            Throwable realException = ex.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage()); //$NON-NLS-1$
        } catch (InterruptedException ex) {
        }
    }

    @Override
    public boolean isPageComplete() {
        return super.isPageComplete() && (preferencesControl == null || preferencesControl.isPageComplete())
                && (ymirConfigurationControl == null || ymirConfigurationControl.isPageComplete());
    }

    public void notifySkeletonAndFragmentsCleared() {
        skeleton = null;
        skeletonBehavior = null;
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
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, model.getLabelText()));
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
        ArtifactPair[] pairs = getArtifactPairs();
        for (int i = 0; i < pairs.length; i++) {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            if (parameterModelMaps != null) {
                Map<String, ParameterModel> modelMap = parameterModelMaps[i];
                for (Iterator<Map.Entry<String, ParameterModel>> itr = modelMap.entrySet().iterator(); itr.hasNext();) {
                    Map.Entry<String, ParameterModel> entry = itr.next();
                    parameterMap.put(entry.getKey(), entry.getValue().getObject());
                }
            }

            // アーティファクト情報を追加する。
            parameterMap.put(ParameterKeys.ARTIFACT_VERSION, new ArtifactVersion(pairs[i].getArtifact().getVersion()));

            pairs[i].setParameterMap(parameterMap);
        }
    }

    public YmirConfigurationControl getYmirConfigurationControl() {
        return ymirConfigurationControl;
    }

    static interface ParameterModel {
        boolean valueExists();

        String getLabelText();

        Object getObject();

        void setEnabled(boolean enabled);
    }

    static class TextParameterModel implements ParameterModel {
        private ArtifactPair pair;

        private String name;

        private Label label;

        private Text text;

        TextParameterModel(ArtifactPair pair, String name, Text text, Label label) {
            this.pair = pair;
            this.name = name;
            this.text = text;
            this.label = label;
        }

        public boolean valueExists() {
            return text.getText().trim().length() > 0;
        }

        public String getLabelText() {
            return pair.getBehavior().getTemplateParameterLabel(name);
        }

        public Object getObject() {
            return text.getText().trim();
        }

        public void setEnabled(boolean enabled) {
            text.setEnabled(enabled);
            label.setEnabled(enabled);
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

        public String getLabelText() {
            return pair.getBehavior().getTemplateParameterLabel(name);
        }

        public Object getObject() {
            return Boolean.valueOf(button.getSelection());
        }

        public void setEnabled(boolean enabled) {
            button.setEnabled(enabled);
        }
    }

    static class ComboParameterModel implements ParameterModel {
        private ArtifactPair pair;

        private String name;

        private Combo combo;

        private Label label;

        ComboParameterModel(ArtifactPair pair, String name, Combo combo, Label label) {
            this.pair = pair;
            this.name = name;
            this.combo = combo;
            this.label = label;
        }

        public boolean valueExists() {
            return combo.getText().trim().length() > 0;
        }

        public String getLabelText() {
            return pair.getBehavior().getTemplateParameterLabel(name);
        }

        public Object getObject() {
            return combo.getText().trim();
        }

        public void setEnabled(boolean enabled) {
            combo.setEnabled(enabled);
            label.setEnabled(enabled);
        }
    }
}