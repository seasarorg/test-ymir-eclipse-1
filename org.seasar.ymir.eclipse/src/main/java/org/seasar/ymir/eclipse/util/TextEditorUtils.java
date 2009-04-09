package org.seasar.ymir.eclipse.util;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IMember;
import org.eclipse.jdt.core.ISourceRange;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

/**
 * @author taichi
 * 
 */
public class TextEditorUtils {
    public static ITextEditor toTextEditor(IEditorPart editor) {
        ITextEditor result = null;
        if (editor instanceof ITextEditor) {
            result = (ITextEditor) editor;
        } else if (editor != null) {
            result = (ITextEditor) editor.getAdapter(ITextEditor.class);
        }
        return result;
    }

    public static ITextEditor selectAndReveal(IMember member)
            throws CoreException {
        IEditorPart part = JavaUI.openInEditor(member);
        ITextEditor editor = TextEditorUtils.toTextEditor(part);
        ISourceRange range = member.getNameRange();
        editor.selectAndReveal(range.getOffset(), 0);
        return editor;
    }
}
