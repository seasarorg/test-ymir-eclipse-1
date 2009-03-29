package org.seasar.ymir.eclipse.wizards.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.seasar.ymir.vili.Mold;

public class ComboParameterModel implements ParameterModel {
    private Mold mold;

    private String name;

    private Combo combo;

    private Label label;

    public ComboParameterModel(Mold mold, String name, Combo combo, Label label) {
        this.mold = mold;
        this.name = name;
        this.combo = combo;
        this.label = label;
    }

    public boolean valueExists() {
        return combo.getText().trim().length() > 0;
    }

    public String getLabelText() {
        return mold.getBehavior().getTemplateParameterLabel(name);
    }

    public Object getValue() {
        return combo.getText().trim();
    }

    public void setValue(Object value) {
        String text = String.valueOf(value);

        int selectedIndex = -1;
        int n = combo.getItemCount();
        for (int i = 0; i < n; i++) {
            if (combo.getItem(i).equals(text)) {
                selectedIndex = i;
                break;
            }
        }
        if (selectedIndex >= 0) {
            combo.select(selectedIndex);
        } else {
            combo.setText(text);
        }
    }

    public void notifyChanged() {
        Event event = new Event();
        event.widget = combo;
        combo.notifyListeners(SWT.Modify, event);
    }

    public void setEnabled(boolean enabled) {
        combo.setEnabled(enabled);
        label.setEnabled(enabled);
    }
}
