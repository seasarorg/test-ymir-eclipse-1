package org.seasar.ymir.vili.model;

import java.util.LinkedHashSet;
import java.util.Set;

import net.skirnir.xom.annotation.Child;

public class Actions {
    private Set<Action> actions = new LinkedHashSet<Action>();

    public Action[] getActions() {
        return actions.toArray(new Action[0]);
    }

    @Child(order = 1)
    public void addAction(Action action) {
        actions.add(action);
    }
}
