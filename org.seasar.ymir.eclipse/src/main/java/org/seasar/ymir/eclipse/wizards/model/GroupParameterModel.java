package org.seasar.ymir.eclipse.wizards.model;

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
        return false;
    }

    public String getLabelText() {
        return mold.getBehavior().getTemplateParameterLabel(name);
    }

    public Object getValue() {
        return null;
    }

    public void setValue(Object value) {
    }

    public void notifyChanged() {
        for (ParameterModel member : members) {
            member.notifyChanged();
        }
    }

    public void setEnabled(boolean enabled) {
        group.setEnabled(enabled);
        for (ParameterModel member : members) {
            member.setEnabled(enabled);
        }
    }
}
