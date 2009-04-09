package org.seasar.ymir.eclipse.actions;

import java.text.MessageFormat;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.util.WorkbenchUtils;

/**
 * <p>このクラスはDoltengのソースコードを基にしています。</p>
 */
public class TogglePageAndHtmlAction extends AbstractWorkbenchWindowActionDelegate {
    private static final char PATH_DELIMITER_CHAR = '%';

    private static final String PATH_DELIMITER = String.valueOf(PATH_DELIMITER_CHAR);

    private static final String SUFFIX_BASE = "Base"; //$NON-NLS-1$

    @Override
    protected void processJava(IProject project, IJavaElement element) throws Exception {
        if (element instanceof ICompilationUnit) {
            IType type = ((ICompilationUnit) element).findPrimaryType();
            String pkg = type.getPackageFragment().getElementName();
            IFile file = findHtmlFromPage(project, pkg + "." + type.getElementName()); //$NON-NLS-1$
            if (file != null) {
                WorkbenchUtils.openResource(file);
            } else {
                WorkbenchUtils.showMessage(MessageFormat.format(
                        Messages.getString("TogglePageAndHtmlAction.2"), pkg + "." //$NON-NLS-1$ //$NON-NLS-2$
                                + type.getElementName()));
            }
        }
    }

    IFile findHtmlFromPage(IProject project, String className) {
        if (className.endsWith(SUFFIX_BASE)) {
            className = className.substring(0, className.length() - SUFFIX_BASE.length());
        }
        String path = getMappingPreferenceStore(project).getString(className);
        if (path == null || path.length() == 0) {
            return null;
        }

        return project.getFile(new Path(Globals.PATH_SRC_MAIN_WEBAPP + path));
    }

    @Override
    protected void processResource(IProject project, IResource resource) {
        try {
            if (resource instanceof IFile) {
                String path = ((IFile) resource).getFullPath()
                        .removeFirstSegments(4/* プロジェクト名/src/main/webappを取り除く */).toPortableString();
                String className = findPageFromHtml(project, path);
                IType type = JavaCore.create(project).findType(className);
                if (type != null && type.exists()) {
                    JavaUI.openInEditor(type);
                } else {
                    WorkbenchUtils.showMessage(MessageFormat.format(
                            Messages.getString("TogglePageAndHtmlAction.4"), path)); //$NON-NLS-1$
                }
            }
        } catch (Exception e) {
            Activator.getDefault().log(e);
        }
    }

    String findPageFromHtml(IProject project, String path) {
        return getMappingPreferenceStore(project).getString(PATH_DELIMITER + path.replace('/', PATH_DELIMITER_CHAR));
    }

    public IPreferenceStore getMappingPreferenceStore(IProject project) {
        return Activator.getDefault().getPreferenceStore(project, Globals.QUALIFIER_MAPPING);
    }
}
