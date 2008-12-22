package org.seasar.ymir.vili.model;

import java.util.LinkedHashMap;
import java.util.Map;

import net.skirnir.xom.annotation.Child;

public class Actions {
    // LinkedHashSetを使わないのは、LinkedHashSetだと同一と判定されるキーを追加した場合に新しいものに置き換わらないから。
    private Map<Action, Action> actions = new LinkedHashMap<Action, Action>();

    public Action[] getActions() {
        return actions.values().toArray(new Action[0]);
    }

    @Child(order = 1)
    public void addAction(Action action) {
        actions.put(action, action);
    }
}
