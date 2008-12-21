package org.seasar.ymir.vili;

import org.eclipse.core.resources.IProject;

public interface IAction {
    void run(IProject project, ViliProjectPreferences preferences);
}
