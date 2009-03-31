package org.seasar.ymir.eclipse.ui;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.swt.SWT;
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
import org.eclipse.swt.widgets.Widget;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.wizards.model.ButtonParameterModel;
import org.seasar.ymir.eclipse.wizards.model.ComboParameterModel;
import org.seasar.ymir.eclipse.wizards.model.GroupParameterModel;
import org.seasar.ymir.eclipse.wizards.model.ParameterModel;
import org.seasar.ymir.eclipse.wizards.model.RadioParameterModel;
import org.seasar.ymir.eclipse.wizards.model.TextParameterModel;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ArtifactVersion;

public class MoldParametersControl {
    private static final String REQUIRED_TEMPLATE = Messages.getString("MoldParametersControl.0"); //$NON-NLS-1$

    private Composite parent;

    private IProject project;

    private ViliProjectPreferences preferences;

    private Mold[] molds;

    private boolean onlyModifiable;

    private Map<String, ParameterModel>[] parameterModelMaps;

    private ParameterModel[] requiredParameterModels = new ParameterModel[0];

    private boolean isPageComplete;

    private Listener validationListener = new Listener() {
        public void handleEvent(Event event) {
            setPageComplete(validatePage());
        }
    };

    public MoldParametersControl(Composite parent, IProject project, ViliProjectPreferences preferences, Mold[] molds,
            boolean onlyModifiable) {
        this.parent = parent;
        this.project = project;
        this.preferences = preferences;
        this.molds = molds;
        this.onlyModifiable = onlyModifiable;
    }

    public Control createControl() {
        Composite composite = new Composite(parent, SWT.NULL);
        composite.setFont(parent.getFont());
        composite.setLayout(new GridLayout());
        composite.setLayoutData(new GridData(GridData.FILL_BOTH));

        createMoldParametersControl(composite);

        return composite;
    }

    @SuppressWarnings("unchecked")
    void createMoldParametersControl(Composite parent) {
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

            Group group = new Group(parent, SWT.NONE);
            GridLayout layout = new GridLayout();
            layout.numColumns = 2;
            group.setLayout(layout);
            group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
            group.setText(behavior.getLabel());

            createParameterModels(parent, null, mold, parameterModelMaps[i], requiredList, null);
        }
        requiredParameterModels = requiredList.toArray(new ParameterModel[0]);
    }

    ParameterModel[] createParameterModels(Composite parent, String groupName, Mold mold,
            Map<String, ParameterModel> modelMap, List<ParameterModel> requiredList, String[] parentDependents) {
        ViliBehavior behavior = mold.getBehavior();
        List<ParameterModel> modelList = new ArrayList<ParameterModel>();
        for (String name : behavior.getTemplateParameters(groupName)) {
            if (onlyModifiable && !behavior.isTemplateParameterModifiable(name)) {
                continue;
            }
            String description = behavior.getTemplateParameterDescription(name);
            String[] dependents = add(parentDependents, behavior.getTemplateParameterDependents(name));
            ParameterModel model;
            switch (behavior.getTemplateParameterType(name)) {
            case TEXT: {
                Label label = new Label(parent, SWT.NONE);
                label.setText(behavior.getTemplateParameterLabel(name));
                Text text = new Text(parent, SWT.BORDER);
                model = new TextParameterModel(mold, name, text, label);
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
                if (dependents.length > 0) {
                    text.addListener(SWT.Modify, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case CHECKBOX: {
                Button button = new Button(parent, SWT.CHECK | SWT.LEFT);
                model = new ButtonParameterModel(mold, name, button);
                GridData data = new GridData();
                data.horizontalSpan = 2;
                button.setLayoutData(data);
                button.setText(behavior.getTemplateParameterLabel(name));
                if (description.length() > 0) {
                    button.setToolTipText(description);
                }
                if (dependents.length > 0) {
                    button.addListener(SWT.Selection, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case SELECT: {
                Label label = new Label(parent, SWT.NONE);
                label.setText(behavior.getTemplateParameterLabel(name));
                Combo combo = new Combo(parent, SWT.READ_ONLY);
                model = new ComboParameterModel(mold, name, combo, label);
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
                if (dependents.length > 0) {
                    combo.addListener(SWT.Modify, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case COMBOBOX: {
                Label label = new Label(parent, SWT.NONE);
                label.setText(behavior.getTemplateParameterLabel(name));
                Combo combo = new Combo(parent, SWT.DROP_DOWN);
                model = new ComboParameterModel(mold, name, combo, label);
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
                if (dependents.length > 0) {
                    combo.addListener(SWT.Modify, new DependencyListener(modelMap, dependents));
                }
            }
                break;

            case GROUP:
                Group group = new Group(parent, SWT.NONE);
                GridLayout layout = new GridLayout();
                layout.numColumns = 2;
                group.setLayout(layout);
                group.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                group.setText(behavior.getTemplateParameterLabel(name));
                if (description.length() > 0) {
                    group.setToolTipText(description);
                }

                model = new GroupParameterModel(mold, name, group, createParameterModels(group, name, mold, modelMap,
                        requiredList, dependents));

                break;

            case RADIO: {
                Group radio = new Group(parent, SWT.NONE);
                GridLayout radioLayout = new GridLayout();
                radioLayout.numColumns = 2;
                radio.setLayout(radioLayout);
                radio.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
                radio.setText(behavior.getTemplateParameterLabel(name));
                if (description.length() > 0) {
                    radio.setToolTipText(description);
                }

                String[] candidates = behavior.getTemplateParameterCandidates(name);
                Button[] buttons = new Button[candidates.length];
                for (int j = 0; j < candidates.length; j++) {
                    String key = name + "." + candidates[j];
                    Button button = new Button(parent, SWT.RADIO | SWT.LEFT);
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

                model = new RadioParameterModel(mold, name, radio, candidates, buttons);

                if (behavior.isTemplateParameterRequired(name)) {
                    requiredList.add(model);
                    for (Button button : buttons) {
                        button.addListener(SWT.Selection, validationListener);
                    }
                }
                if (dependents.length > 0) {
                    for (Button button : buttons) {
                        button.addListener(SWT.Selection, new DependencyListener(modelMap, dependents));
                    }
                }
            }
                break;

            default:
                throw new RuntimeException("Unknown model type: " + behavior.getTemplateParameterType(name));
            }

            modelList.add(model);
            modelMap.put(name, model);
        }

        return modelList.toArray(new ParameterModel[0]);
    }

    String[] add(String[] a1, String[] a2) {
        if (a1 == null) {
            if (a2 == null) {
                return new String[0];
            } else {
                return a2;
            }
        } else {
            if (a2 == null) {
                return a1;
            } else {
                String[] a = new String[a1.length + a2.length];
                System.arraycopy(a1, 0, a, 0, a1.length);
                System.arraycopy(a2, 0, a, a1.length, a2.length);
                return a;
            }
        }
    }

    public boolean validatePage() {
        for (ParameterModel model : requiredParameterModels) {
            if (!model.valueExists()) {
                setErrorMessage(MessageFormat.format(REQUIRED_TEMPLATE, model.getLabelText()));
                return false;
            }
        }

        setErrorMessage(null);
        return true;
    }

    public void setErrorMessage(String message) {
    }

    public void resumeValues() {
        for (int i = 0; i < parameterModelMaps.length; i++) {
            Map<String, ParameterModel> modelMap = parameterModelMaps[i];
            ViliBehavior behavior = molds[i].getBehavior();
            Map<String, Object> loaded = behavior.getConfigurator().loadParameters(project, molds[i], preferences);
            molds[i].setParameterMap(loaded);
            for (String name : modelMap.keySet()) {
                String defaultValue = toString(loaded.get(name));
                if (defaultValue != null) {
                    ParameterModel model = modelMap.get(name);
                    model.setValue(defaultValue);
                    model.notifyChanged();
                }
            }
        }
    }

    public void setDefaultValues() {
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

    private String toString(Object obj) {
        if (obj == null) {
            return null;
        } else {
            return obj.toString();
        }
    }

    public boolean isPageComplete() {
        return isPageComplete;
    }

    public void setPageComplete(boolean isPageComplete) {
        this.isPageComplete = isPageComplete;
    }

    public void populateMoldParameters() {
        for (int i = 0; i < molds.length; i++) {
            if (molds[i].getParameterMap() == null) {
                molds[i].setParameterMap(new HashMap<String, Object>());
            }
            Map<String, Object> parameterMap = molds[i].getParameterMap();
            Map<String, ParameterModel> modelMap = parameterModelMaps[i];
            for (Iterator<Map.Entry<String, ParameterModel>> itr = modelMap.entrySet().iterator(); itr.hasNext();) {
                Map.Entry<String, ParameterModel> entry = itr.next();
                parameterMap.put(entry.getKey(), entry.getValue().getValue());
            }

            // アーティファクト情報を追加する。
            parameterMap.put(ParameterKeys.ARTIFACT_VERSION, new ArtifactVersion(molds[i].getArtifact().getVersion()));

            molds[i].setParameterMap(parameterMap);
        }
    }

    public Mold[] getMolds() {
        populateMoldParameters();
        return molds;
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
