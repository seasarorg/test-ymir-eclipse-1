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
package org.seasar.ymir.vili.util;

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
