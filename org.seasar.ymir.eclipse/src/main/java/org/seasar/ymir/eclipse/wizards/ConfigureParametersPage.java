package org.seasar.ymir.eclipse.wizards;

import static org.seasar.ymir.eclipse.wizards.NewProjectWizard.REQUIRED_TEMPLATE;

import java.lang.reflect.InvocationTargetException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
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
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.ui.ViliProjectPreferencesControl;
import org.seasar.ymir.eclipse.ui.YmirConfigurationControl;
import org.seasar.ymir.eclipse.wizards.model.ButtonParameterModel;
import org.seasar.ymir.eclipse.wizards.model.ComboParameterModel;
import org.seasar.ymir.eclipse.wizards.model.ParameterModel;
import org.seasar.ymir.eclipse.wizards.model.RadioParameterModel;
import org.seasar.ymir.eclipse.wizards.model.TextParameterModel;
import org.seasar.ymir.vili.Mold;
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

    private IProject project;

    private ViliProjectPreferences preferences;

    private Composite tabFolderParent;

    private CTabFolder tabFolder;

    private Label tabLabel;

    private ViliProjectPreferencesControl preferencesControl;

    private Composite skeletonTabContent;

    private Composite ymirConfigurationTabContent;

    private boolean tabPrepared;

    private Mold skeleton;

    private ViliBehavior skeletonBehavior;

    private Mold[] fragments;

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
        for (Mold mold : getMolds()) {
            count += mold.getBehavior().getTemplateParameters().length;
        }
        return count > 0;
    }

    private Mold[] getMolds() {
        List<Mold> moldList = new ArrayList<Mold>();
        if (skeleton != null) {
            moldList.add(skeleton);
        }
        if (fragments != null) {
            moldList.addAll(Arrays.asList(fragments));
        }
        return moldList.toArray(new Mold[0]);
    }

    @SuppressWarnings("unchecked")//$NON-NLS-1$
    void createSkeletonParametersControl(Composite parent) {
        Mold[] molds = getMolds();
        parameterModelMaps = new Map[molds.length];
        java.util.List<ParameterModel> requiredList = new ArrayList<ParameterModel>();

        for (int i = 0; i < molds.length; i++) {
            Mold mold = molds[i];
            parameterModelMaps[i] = new LinkedHashMap<String, ParameterModel>();
            ViliBehavior behavior = mold.getBehavior();
            String[] names = behavior.getTemplateParameters();

            if (names.length == 0) {
                continue;
            }

            createGroupControl(parent, mold, behavior, parameterModelMaps[i], requiredList, null, names);
        }
        requiredParameterModels = requiredList.toArray(new ParameterModel[0]);
    }

    void createGroupControl(Composite parent, Mold mold, ViliBehavior behavior, Map<String, ParameterModel> modelMap,
            List<ParameterModel> requiredList, String groupName, String[] childNames) {

        String prefix;
        String groupLabel;
        if (groupName == null) {
            prefix = "";
            groupLabel = behavior.getLabel();
        } else {
            prefix = groupName + ".";
            groupLabel = behavior.getTemplateParameterLabel(groupName);
        }

        Group group = new Group(parent, SWT.NONE);
        GridLayout layout = new GridLayout();
        layout.numColumns = 2;
        group.setLayout(layout);
        group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
        group.setText(groupLabel);

        for (int i = 0; i < childNames.length; i++) {
            String name = prefix + childNames[i];
            String description = behavior.getTemplateParameterDescription(name);
            switch (behavior.getTemplateParameterType(name)) {
            case TEXT: {
                Label label = new Label(group, SWT.NONE);
                label.setText(behavior.getTemplateParameterLabel(name));
                Text text = new Text(group, SWT.BORDER);
                ParameterModel model = new TextParameterModel(mold, name, text, label);
                modelMap.put(name, model);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.widthHint = 250;
                text.setLayoutData(data);
                if (description.length() > 0) {
                    text.setToolTipText(description);
                }
                if (behavior.isTemplateParameterRequired(name)) {
                    requiredList.add(model);
                    text.addListener(SWT.Modify, validationListener);
                }
                String[] dependents = behavior.getTemplateParameterDependents(name);
                if (dependents.length > 0) {
                    text.addListener(SWT.Modify, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case CHECKBOX: {
                Button button = new Button(group, SWT.CHECK | SWT.LEFT);
                ParameterModel model = new ButtonParameterModel(mold, name, button);
                modelMap.put(name, model);
                GridData data = new GridData();
                data.horizontalSpan = 2;
                button.setLayoutData(data);
                button.setText(behavior.getTemplateParameterLabel(name));
                if (description.length() > 0) {
                    button.setToolTipText(description);
                }
                String[] dependents = behavior.getTemplateParameterDependents(name);
                if (dependents.length > 0) {
                    button.addListener(SWT.Selection, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case SELECT: {
                Label label = new Label(group, SWT.NONE);
                label.setText(behavior.getTemplateParameterLabel(name));
                Combo combo = new Combo(group, SWT.READ_ONLY);
                ParameterModel model = new ComboParameterModel(mold, name, combo, label);
                modelMap.put(name, model);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.widthHint = 250;
                combo.setLayoutData(data);
                String[] candidates = behavior.getTemplateParameterCandidates(name);
                for (int k = 0; k < candidates.length; k++) {
                    combo.add(candidates[k]);
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
                    combo.addListener(SWT.Modify, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case COMBOBOX: {
                Label label = new Label(group, SWT.NONE);
                label.setText(behavior.getTemplateParameterLabel(name));
                Combo combo = new Combo(group, SWT.DROP_DOWN);
                ParameterModel model = new ComboParameterModel(mold, name, combo, label);
                modelMap.put(name, model);
                GridData data = new GridData(GridData.FILL_HORIZONTAL);
                data.widthHint = 250;
                combo.setLayoutData(data);
                String[] candidates = behavior.getTemplateParameterCandidates(name);
                for (int k = 0; k < candidates.length; k++) {
                    combo.add(candidates[k]);
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
                    combo.addListener(SWT.Modify, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case GROUP:
                createGroupControl(parent, mold, behavior, modelMap, requiredList, name, behavior
                        .getTemplateParameterMembers(name));
                break;

            case RADIO: {
                Group radio = new Group(group, SWT.NONE);
                GridLayout radioLayout = new GridLayout();
                radioLayout.numColumns = 2;
                radio.setLayout(radioLayout);
                radio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                radio.setText(behavior.getTemplateParameterLabel(name));

                String[] candidates = behavior.getTemplateParameterCandidates(name);
                Button[] buttons = new Button[candidates.length];
                for (int j = 0; j < candidates.length; j++) {
                    String key = name + "." + candidates[j];
                    Button button = new Button(group, SWT.RADIO | SWT.LEFT);
                    GridData data = new GridData();
                    data.horizontalSpan = 2;
                    button.setLayoutData(data);
                    button.setText(behavior.getTemplateParameterLabel(key));
                    String buttonDescription = behavior.getTemplateParameterDescription(key);
                    if (buttonDescription.length() > 0) {
                        button.setToolTipText(buttonDescription);
                    }
                    buttons[j] = button;
                }

                ParameterModel model = new RadioParameterModel(mold, name, radio, candidates, buttons);
                modelMap.put(name, model);

                String[] dependents = behavior.getTemplateParameterDependents(name);
                if (dependents.length > 0) {
                    button.addListener(SWT.Selection, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            default:
            }
        }
    }

    @Override
    public void setVisible(boolean visible) {
        super.setVisible(visible);
        if (visible) {
            if (!tabPrepared) {
                ISelectArtifactWizard wizard = (ISelectArtifactWizard) getWizard();
                skeleton = wizard.getSkeletonMold();
                List<ViliBehavior> behaviorList = new ArrayList<ViliBehavior>();
                if (skeleton != null) {
                    skeletonBehavior = skeleton.getBehavior();
                    behaviorList.add(skeletonBehavior);
                }
                fragments = wizard.getFragmentMolds();
                for (Mold fragment : fragments) {
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
                        behavior.notifyPropertiesChanged();
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
        if (parameterModelMaps != null) {
            Mold[] molds = getMolds();
            for (int i = 0; i < parameterModelMaps.length; i++) {
                Map<String, ParameterModel> modelMap = parameterModelMaps[i];
                ViliBehavior behavior = molds[i].getBehavior();
                for (String name : modelMap.keySet()) {
                    String defaultValue = behavior.getTemplateParameterDefault(name);
                    if (defaultValue != null) {
                        ParameterModel model = modelMap.get(name);
                        model.setValue(defaultValue);
                        model.notifyChanged();
                    }
                }
            }
        }
        if (ymirConfigurationControl != null) {
            ymirConfigurationControl.setDefaultValues();
        }
        if (tabFolder != null) {
            tabFolder.setSelection(0);
        }
    }

    public void populateSkeletonParameters() {
        Mold[] molds = getMolds();
        for (int i = 0; i < molds.length; i++) {
            Map<String, Object> parameterMap = new HashMap<String, Object>();
            if (parameterModelMaps != null) {
                Map<String, ParameterModel> modelMap = parameterModelMaps[i];
                for (Iterator<Map.Entry<String, ParameterModel>> itr = modelMap.entrySet().iterator(); itr.hasNext();) {
                    Map.Entry<String, ParameterModel> entry = itr.next();
                    parameterMap.put(entry.getKey(), entry.getValue().getValue());
                }
            }

            // アーティファクト情報を追加する。
            parameterMap.put(ParameterKeys.ARTIFACT_VERSION, new ArtifactVersion(molds[i].getArtifact().getVersion()));

            molds[i].setParameterMap(parameterMap);
        }
    }

    public YmirConfigurationControl getYmirConfigurationControl() {
        return ymirConfigurationControl;
    }

    static class DependencyListener implements Listener {
        private Map<String, ParameterModel> modelMap;

        private String[] dependents;

        DependencyListener(Map<String, ParameterModel> modelMap, String[] dependents) {
            this.modelMap = modelMap;
            this.dependents = dependents;
        }

        public void handleEvent(Event event) {
            Widget widget = event.widget;
            boolean enabled;
            if (widget instanceof Text) {
                enabled = ((Text) widget).getText().trim().length() > 0;
            } else if (widget instanceof Button) {
                enabled = ((Button) widget).getSelection();
            } else if (widget instanceof Combo) {
                enabled = ((Combo) widget).getText().trim().length() > 0;
            } else {
                enabled = false;
            }

            for (String dependent : dependents) {
                ParameterModel model = modelMap.get(dependent);
                if (model != null) {
                    model.setEnabled(enabled);
                }
            }
        }
    }
}