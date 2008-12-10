/*
 * Copyright 2004-2006 the Seasar Foundation and the Others.
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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IProjectNature;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaModel;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.formatter.DefaultCodeFormatterConstants;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;


/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author taichi
 */
@SuppressWarnings("unchecked") //$NON-NLS-1$
public class ProjectUtil
{

    private static List getCommands(IProjectDescription desc, String[] ignore)
        throws CoreException
    {
        ICommand[] commands = desc.getBuildSpec();
        List newCommands = new ArrayList();
        for (int i = 0; i < commands.length; i++) {
            boolean flag = true;
            for (int k = 0; k < ignore.length; k++) {
                if (commands[i].getBuilderName().equals(ignore[k])) {
                    flag = false;
                    break;
                }
            }
            if (flag) {
                newCommands.add(commands[i]);
            } else {
                flag = true;
            }
        }
        return newCommands;
    }


    private static void setCommands(IProjectDescription desc, List newCommands)
    {
        desc.setBuildSpec((ICommand[])newCommands
            .toArray(new ICommand[newCommands.size()]));
    }


    public static void addBuilders(IProject project, String[] id)
        throws CoreException
    {
        IProjectDescription desc = project.getDescription();
        List newCommands = getCommands(desc, id);
        for (int i = 0; i < id.length; i++) {
            ICommand command = desc.newCommand();
            command.setBuilderName(id[i]);
            newCommands.add(command);
        }
        setCommands(desc, newCommands);
        project.setDescription(desc, null);
    }


    public static void removeBuilders(IProject project, String[] id)
        throws CoreException
    {
        IProjectDescription desc = project.getDescription();
        List newCommands = getCommands(desc, id);
        setCommands(desc, newCommands);
        project.setDescription(desc, null);
    }


    public static void addNature(IProject project, String natureID)
        throws CoreException
    {
        if ((project != null) && project.isAccessible()) {
            IProjectDescription desc = project.getDescription();
            String[] natureIDs = desc.getNatureIds();
            int length = natureIDs.length;
            String[] newIDs = new String[length + 1];
            for (int i = 0; i < length; i++) {
                if (natureIDs[i].equals(natureID)) {
                    return;
                }
                newIDs[i] = natureIDs[i];
            }
            newIDs[length] = natureID;
            desc.setNatureIds(newIDs);
            project.setDescription(desc, null);
        }
    }


    public static void removeNature(IProject project, String natureID)
        throws CoreException
    {
        if ((project != null) && project.isAccessible()) {
            IProjectDescription desc = project.getDescription();
            String[] natureIDs = desc.getNatureIds();
            int length = natureIDs.length;
            for (int i = 0; i < length; i++) {
                if (natureIDs[i].equals(natureID)) {
                    String[] newIDs = new String[length - 1];
                    System.arraycopy(natureIDs, 0, newIDs, 0, i);
                    System.arraycopy(natureIDs, i + 1, newIDs, i, length - i
                        - 1);
                    desc.setNatureIds(newIDs);
                    project.setDescription(desc, null);
                    return;
                }
            }
        }
    }


    public static IProjectNature getNature(IProject project, String natureID)
        throws CoreException
    {
        if ((project != null) && (project.isOpen())) {
            return project.getNature(natureID);
        }
        return null;
    }


    public static boolean hasNature(IProject project, String natureID)
    {
        try {
            return getNature(project, natureID) != null;
        } catch (CoreException e) {
            return false;
        }
    }


    public static String[] getNatureIds(IProject project)
    {
        try {
            return project.getDescription().getNatureIds();
        } catch (CoreException e) {
            return new String[0];
        }
    }


    public static IWorkspace getWorkspace()
    {
        return ResourcesPlugin.getWorkspace();
    }


    public static IWorkspaceRoot getWorkspaceRoot()
    {
        return getWorkspace().getRoot();
    }


    public static IProject[] getAllProjects()
    {
        return getWorkspaceRoot().getProjects();
    }


    public static IProject getProject(String projectName)
    {
        return getWorkspaceRoot().getProject(projectName);
    }


    public static IJavaProject getJavaProject(String projectName)
    {
        return JavaCore.create(getProject(projectName));
    }


    public static IJavaProject getJavaProject(IResource resource)
    {
        return JavaCore.create(resource.getProject());
    }


    public static IJavaProject[] getJavaProjects()
        throws CoreException
    {
        return getJavaModel().getJavaProjects();
    }


    public static IJavaModel getJavaModel()
    {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        return JavaCore.create(workspace.getRoot());
    }


    public static IJavaProject getJavaProject(IPath path)
        throws CoreException
    {
        IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(
            path.segment(0));
        return JavaCore.create(project);
    }


    public static String createIndentString(int indentationUnits,
        IJavaProject project)
    {
        final String tabChar = getCoreOption(project,
            DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR);
        final int tabs, spaces;
        if (JavaCore.SPACE.equals(tabChar)) {
            tabs = 0;
            spaces = indentationUnits * getIndentWidth(project);
        } else if (JavaCore.TAB.equals(tabChar)) {
            // indentWidth == tabWidth
            tabs = indentationUnits;
            spaces = 0;
        } else if (DefaultCodeFormatterConstants.MIXED.equals(tabChar)) {
            int tabWidth = getTabWidth(project);
            int spaceEquivalents = indentationUnits * getIndentWidth(project);
            if (tabWidth > 0) {
                tabs = spaceEquivalents / tabWidth;
                spaces = spaceEquivalents % tabWidth;
            } else {
                tabs = 0;
                spaces = spaceEquivalents;
            }
        } else {
            return null;
        }

        StringBuffer buffer = new StringBuffer(tabs + spaces);
        for (int i = 0; i < tabs; i++)
            buffer.append('\t');
        for (int i = 0; i < spaces; i++)
            buffer.append(' ');
        return buffer.toString();
    }


    /**
     * Gets the current tab width.
     * 
     * @param project
     *            The project where the source is used, used for project
     *            specific options or <code>null</code> if the project is
     *            unknown and the workspace default should be used
     * @return The tab width
     */
    public static int getTabWidth(IJavaProject project)
    {
        /*
         * If the tab-char is SPACE, FORMATTER_INDENTATION_SIZE is not used by
         * the core formatter. We piggy back the visual tab length setting in
         * that preference in that case.
         */
        String key;
        if (JavaCore.SPACE.equals(getCoreOption(project,
            DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)))
            key = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
        else
            key = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;

        return getCoreOption(project, key, 4);
    }


    public static int getIndentWidth(IJavaProject project)
    {
        String key;
        if (DefaultCodeFormatterConstants.MIXED.equals(getCoreOption(project,
            DefaultCodeFormatterConstants.FORMATTER_TAB_CHAR)))
            key = DefaultCodeFormatterConstants.FORMATTER_INDENTATION_SIZE;
        else
            key = DefaultCodeFormatterConstants.FORMATTER_TAB_SIZE;

        return getCoreOption(project, key, 4);
    }


    public static int getCoreOption(IJavaProject project, String key, int def)
    {
        try {
            return Integer.parseInt(getCoreOption(project, key));
        } catch (NumberFormatException e) {
            return def;
        }
    }


    public static String getCoreOption(IJavaProject project, String key)
    {
        if (project == null)
            return JavaCore.getOption(key);
        return project.getOption(key, true);
    }


    public static String getProjectLineDelimiter(IJavaProject javaProject)
    {
        IProject project = null;
        if (javaProject != null)
            project = javaProject.getProject();

        String lineDelimiter = getLineDelimiterPreference(project);
        if (lineDelimiter != null)
            return lineDelimiter;

        return System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
    }


    public static String getLineDelimiterPreference(IProject project)
    {
        IScopeContext[] scopeContext;
        if (project != null) {
            // project preference
            scopeContext = new IScopeContext[] { new ProjectScope(project) };
            String lineDelimiter = Platform.getPreferencesService().getString(
                Platform.PI_RUNTIME, Platform.PREF_LINE_SEPARATOR, null,
                scopeContext);
            if (lineDelimiter != null)
                return lineDelimiter;
        }
        // workspace preference
        scopeContext = new IScopeContext[] { new InstanceScope() };
        String platformDefault = System.getProperty("line.separator", "\n"); //$NON-NLS-1$ //$NON-NLS-2$
        return Platform.getPreferencesService().getString(Platform.PI_RUNTIME,
            Platform.PREF_LINE_SEPARATOR, platformDefault, scopeContext);
    }


    public static IPackageFragmentRoot getFirstSrcPackageFragmentRoot(
        IJavaProject javap)
        throws CoreException
    {
        IPackageFragmentRoot[] roots = javap.getPackageFragmentRoots();
        for (int i = 0; roots != null && i < roots.length; i++) {
            IPackageFragmentRoot root = roots[i];
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                return root;
            }
        }
        return null;
    }


    public static IPackageFragmentRoot[] getSrcPackageFragmentRoot(
        IJavaProject javap)
        throws CoreException
    {
        List result = new ArrayList();
        IPackageFragmentRoot[] roots = javap.getPackageFragmentRoots();
        for (int i = 0; roots != null && i < roots.length; i++) {
            IPackageFragmentRoot root = roots[i];
            if (root.getKind() == IPackageFragmentRoot.K_SOURCE) {
                result.add(root);
            }
        }
        return (IPackageFragmentRoot[])result
            .toArray(new IPackageFragmentRoot[result.size()]);
    }


    public static IPath[] getOutputLocations(IJavaProject project)
        throws CoreException
    {
        List result = new ArrayList();
        result.add(project.getOutputLocation());
        IClasspathEntry[] entries = project.getRawClasspath();
        for (int i = 0; i < entries.length; i++) {
            IClasspathEntry entry = entries[i];
            if (entry.getEntryKind() == IClasspathEntry.CPE_SOURCE) {
                result.add(entry.getOutputLocation());
            }
        }

        return (IPath[])result.toArray(new IPath[result.size()]);
    }


    public static IProject getCurrentSelectedProject()
    {
        IProject result = null;
        IWorkbenchWindow window = WorkbenchUtil.getWorkbenchWindow();
        if (window != null) {
            ISelection selection = window.getSelectionService().getSelection();
            if (selection instanceof IStructuredSelection) {
                IStructuredSelection ss = (IStructuredSelection)selection;
                Object o = ss.getFirstElement();
                result = AdaptableUtil.toProject(o);
            } else {
                IWorkbenchPage page = window.getActivePage();
                if (page != null) {
                    IEditorPart editor = page.getActiveEditor();
                    if (editor != null) {
                        result = AdaptableUtil.toProject(editor
                            .getEditorInput());
                    }
                }
            }
        }
        return result;
    }
}
