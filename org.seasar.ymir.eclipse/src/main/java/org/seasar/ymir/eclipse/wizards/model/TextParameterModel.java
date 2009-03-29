package org.seasar.ymir.eclipse.wizards.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.seasar.ymir.vili.Mold;

public class TextParameterModel implements ParameterModel {
    private Mold mold;

    private String name;

    private Label label;

    private Text text;

    public TextParameterModel(Mold mold, String name, Text text, Label label) {
        this.mold = mold;
        this.name = name;
        this.text = text;
        this.label = label;
    }

    public boolean valueExists() {
        return text.getText().trim().length() > 0;
    }

    public String getLabelText() {
        return mold.getBehavior().getTemplateParameterLabel(name);
    }

    public Object getValue() {
        return text.getText().trim();
    }

    public void setValue(Object value) {
        text.setText(String.valueOf(value));
    }

    public void notifyChanged() {
        Event event = new Event();
        event.widget = text;
        text.notifyListeners(SWT.Modify, event);
    }

    public void setEnabled(boolean enabled) {
        text.setEnabled(enabled);
        label.setEnabled(enabled);
    }
}
