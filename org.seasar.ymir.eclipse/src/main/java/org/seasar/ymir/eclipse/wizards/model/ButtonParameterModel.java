package org.seasar.ymir.eclipse.wizards.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.ymir.vili.Mold;

public class ButtonParameterModel implements ParameterModel {
    private Mold mold;

    private String name;

    private Button button;

    public ButtonParameterModel(Mold mold, String name, Button button) {
        this.mold = mold;
        this.name = name;
        this.button = button;
    }

    public boolean valueExists() {
        return true;
    }

    public String getLabelText() {
        return mold.getBehavior().getTemplateParameterLabel(name);
    }

    public Object getValue() {
        return Boolean.valueOf(button.getSelection());
    }

    public void setValue(Object value) {
        button.setSelection(PropertyUtils.valueOf(value, false));
    }

    public void notifyChanged() {
        Event event = new Event();
        event.widget = button;
        button.notifyListeners(SWT.Selection, event);
    }

    public void setEnabled(boolean enabled) {
        button.setEnabled(enabled);
    }
}
