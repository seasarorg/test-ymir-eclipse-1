/*
 * Copyright 2004-2008 the Seasar Foundation and the Others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, 
 * either express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.seasar.eclipse.common.util;

import org.eclipse.core.resources.IResource;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;

/**
 * @author taichi
 * 
 */
public class ResouceUtil {

    public static IResource getCurrentSelectedResouce() {
        IResource result = null;
        IWorkbenchWindow window = WorkbenchUtil.getWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                // getActiveEditorで取れる参照は、フォーカスがどこにあってもアクティブなエディタの参照が取れてしまう為。
                IWorkbenchPart part = page.getActivePart();
                if (part instanceof IEditorPart) {
                    IEditorPart editor = (IEditorPart) part;
                    result = AdaptableUtil.toResource(editor.getEditorInput());
                }
            }
            if (result == null) {
                ISelection selection = window.getSelectionService()
                        .getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    Object o = ss.getFirstElement();
                    result = AdaptableUtil.toResource(o);
                }
            }
        }
        return result;
    }

}
