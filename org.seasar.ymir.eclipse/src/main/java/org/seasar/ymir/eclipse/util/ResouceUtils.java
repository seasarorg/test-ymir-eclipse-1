package org.seasar.ymir.eclipse.util;

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
public class ResouceUtils {
    public static IResource getCurrentSelectedResouce() {
        IResource result = null;
        IWorkbenchWindow window = WorkbenchUtils.getWorkbenchWindow();
        if (window != null) {
            IWorkbenchPage page = window.getActivePage();
            if (page != null) {
                // getActiveEditorで取れる参照は、フォーカスがどこにあってもアクティブなエディタの参照が取れてしまう為。
                IWorkbenchPart part = page.getActivePart();
                if (part instanceof IEditorPart) {
                    IEditorPart editor = (IEditorPart) part;
                    result = AdaptableUtils.toResource(editor.getEditorInput());
                }
            }
            if (result == null) {
                ISelection selection = window.getSelectionService().getSelection();
                if (selection instanceof IStructuredSelection) {
                    IStructuredSelection ss = (IStructuredSelection) selection;
                    Object o = ss.getFirstElement();
                    result = AdaptableUtils.toResource(o);
                }
            }
        }
        return result;
    }

}
