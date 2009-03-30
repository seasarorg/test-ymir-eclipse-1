package org.seasar.ymir.eclipse.wizards.model;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.seasar.ymir.vili.Mold;

public class GroupParameterModel implements ParameterModel {
    private Mold mold;

    private String name;

    private Group group;

    private ParameterModel[] members;

    public GroupParameterModel(Mold mold, String name, Group group, ParameterModel[] members) {
        this.mold = mold;
        this.name = name;
        this.group = group;
        this.members = members;
    }

    public boolean valueExists() {
        for (Button button : buttons) {
            if (button.getSelection()) {
                return true;
            }
        }
        return false;
    }

    public String getLabelText() {
        return mold.getBehavior().getTemplateParameterLabel(name);
    }

    public Object getValue() {
        for (int i = 0; i < buttons.length; i++) {
            if (buttons[i].getSelection()) {
                return candidates[i];
            }
        }
        return null;
    }

    public void setValue(Object value) {
        for (int i = 0; i < candidates.length; i++) {
            if (value.equals(candidates[i])) {
                buttons[i].setSelection(true);
                break;
            }
        }
    }

    public void notifyChanged() {
        for (Button button : buttons) {
            Event event = new Event();
            event.widget = button;
            button.notifyListeners(SWT.Selection, event);
        }
    }

    public void setEnabled(boolean enabled) {
        group.setEnabled(enabled);
        for (Button button : buttons) {
            button.setEnabled(enabled);
        }
    }
}
