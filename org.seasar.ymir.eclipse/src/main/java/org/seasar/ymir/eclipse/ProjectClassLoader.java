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
package org.seasar.ymir.eclipse;

import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.kvasir.util.ClassUtils;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author YOKOTA Takehiko
 */
public class ProjectClassLoader extends URLClassLoader {
    private String[] errorURL;

    public ProjectClassLoader(IJavaProject project) {
        super(new URL[0]);
        getClassPaths(project);
    }

    public ProjectClassLoader(IJavaProject project, ClassLoader parent) {
        super(new URL[0], parent);
        getClassPaths(project);
    }

    private void getClassPaths(IJavaProject project) {
        if (project == null) {
            throw new NullPointerException();
        }
        try {
            IClasspathEntry[] entries = project.getResolvedClasspath(true);
            for (int i = 0; i < entries.length; i++) {
                int kind = entries[i].getEntryKind();
                URL url = null;
                if (kind == IClasspathEntry.CPE_SOURCE) {
                    try {
                        IPath outputLocation = entries[i].getOutputLocation();
                        if (outputLocation == null) {
                            outputLocation = project.getOutputLocation();
                            if (outputLocation == null) {
                                // TODO こういうケースはあるのか？
                                Activator.getDefault().getLog().log(
                                        new Status(IStatus.WARNING, Activator.PLUGIN_ID,
                                                "Can't construct URL for classLoader because default output location is null: entry=" //$NON-NLS-1$
                                                        + entries[i].getPath()));
                                continue;
                            }
                        }

                        url = ClassUtils.getURLForURLClassLoader(project.getProject().getParent().getFolder(
                                outputLocation).getLocation().toFile());
                    } catch (RuntimeException e1) {
                        Activator.getDefault().getLog().log(
                                new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                                        "Can't construct URL for classLoader: entry=" + entries[i].getPath(), e1)); //$NON-NLS-1$
                        continue;
                    }
                } else {
                    url = ClassUtils.getURLForURLClassLoader(entries[i].getPath().toFile());
                    // "jar:file:///" + entries[i].getPath().toString()
                    // + "!/";
                }
                addURL(url);
            }
        } catch (JavaModelException e) {
            throw new RuntimeException(e);
        }
    }

    public String[] getErrorPath() {
        if (errorURL == null) {
            return new String[0];
        }
        return errorURL;
    }
}
