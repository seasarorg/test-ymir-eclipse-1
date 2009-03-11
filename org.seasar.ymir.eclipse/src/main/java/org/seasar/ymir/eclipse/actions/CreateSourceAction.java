package org.seasar.ymir.eclipse.actions;

import org.eclipse.core.runtime.CoreException;

public class CreateSourceAction extends AbstractOpenURLAction {
    @Override
    protected void openUrl(String url) throws CoreException {
        super.openUrl(url + "?__ymir__task=updateClasses&__ymir__method=GET");
    }
}
