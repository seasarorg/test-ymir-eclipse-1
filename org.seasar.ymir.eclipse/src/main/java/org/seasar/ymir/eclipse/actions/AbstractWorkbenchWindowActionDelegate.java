package org.seasar.ymir.eclipse.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.IWorkbenchWindowActionDelegate;
import org.eclipse.ui.texteditor.ITextEditor;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.util.AdaptableUtils;
import org.seasar.ymir.eclipse.util.ResouceUtils;
import org.seasar.ymir.eclipse.util.TextEditorUtils;
import org.seasar.ymir.eclipse.util.WorkbenchUtils;

/**
 * <p>このクラスはDoltengのソースコードを基にしています。</p>
 */
public abstract class AbstractWorkbenchWindowActionDelegate implements IWorkbenchWindowActionDelegate {
    public AbstractWorkbenchWindowActionDelegate() {
    }

    public void selectionChanged(IAction action, ISelection selection) {
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IActionDelegate#run(org.eclipse.jface.action.IAction)
     */
    public void run(IAction action) {
        IResource resource = ResouceUtils.getCurrentSelectedResouce();
        if (resource == null) {
            return;
        }
        IProject project = resource.getProject();

        try {
            if (JavaCore.isJavaLikeFileName(resource.getName())) {
                processJava(project, JavaCore.create(resource));
            } else {
                processResource(project, resource);
            }
        } catch (Exception e) {
            Activator.getDefault().log(e);
        }
    }

    protected IJavaElement getSelectionElement() throws JavaModelException {
        IJavaElement result = null;
        ITextEditor txtEditor = TextEditorUtils.toTextEditor(WorkbenchUtils.getActiveEditor());
        if (txtEditor != null) {
            IResource resource = AdaptableUtils.toResource(txtEditor.getEditorInput());
            if (resource != null) {
                IJavaElement javaElement = JavaCore.create(resource);
                if (javaElement instanceof ICompilationUnit) {
                    ICompilationUnit unit = (ICompilationUnit) javaElement;
                    ISelectionProvider provider = txtEditor.getSelectionProvider();
                    if (provider != null) {
                        ISelection selection = provider.getSelection();
                        if (selection instanceof ITextSelection) {
                            ITextSelection ts = (ITextSelection) selection;
                            result = unit.getElementAt(ts.getOffset());
                        }
                    }
                }
            }
        }
        return result;
    }

    protected void processJava(IProject project, IJavaElement element) throws Exception {
    }

    protected void processResource(IProject project, IResource resource) throws Exception {
    }

    public void dispose() {

    }

    public void init(IWorkbenchWindow window) {

    }
}
