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
package org.seasar.ymir.vili.util;

import java.net.URL;
import java.net.URLClassLoader;

import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.seasar.kvasir.util.ClassUtils;
import org.seasar.ymir.vili.Activator;

/**
 * @author Masataka Kurihara (Gluegent, Inc.)
 * @author YOKOTA Takehiko
 */
public class ProjectClassLoader extends URLClassLoader {
    private String[] errorURL;

    public ProjectClassLoader(IJavaProject project) {
        super(new URL[0]);
        addClasspathURLsOf(project, true);
    }

    public ProjectClassLoader(IJavaProject project, ClassLoader parent) {
        super(new URL[0], parent);
        addClasspathURLsOf(project, true);
    }

    private void addClasspathURLsOf(IJavaProject project, boolean recursive) {
        if (project == null) {
            throw new NullPointerException();
        }
        try {
            IClasspathEntry[] entries = project.getResolvedClasspath(true);
            for (int i = 0; i < entries.length; i++) {
                int kind = entries[i].getEntryKind();
                if (kind == IClasspathEntry.CPE_SOURCE) {
                    IPath outputLocation = entries[i].getOutputLocation();
                    if (outputLocation == null) {
                        outputLocation = project.getOutputLocation();
                        if (outputLocation == null) {
                            // TODO こういうケースはあるのか？
                            Activator
                                    .log("ProjectClassLoader: Can't construct URL for classLoader because default output location is null: entry=" //$NON-NLS-1$
                                            + entries[i].getPath());
                            continue;
                        }
                    }

                    URL url = ClassUtils.getURLForURLClassLoader(project
                            .getProject().getParent().getFolder(outputLocation)
                            .getLocation().toFile());
                    if (url == null) {
                        Activator
                                .log("ProjectClassLoader: Can't construct URL for classLoader: entry=" //$NON-NLS-1$
                                        + entries[i].getPath());
                        continue;
                    }
                    addURL(url);
                } else {
                    if (recursive) {
                        if (kind == IClasspathEntry.CPE_PROJECT) {
                            addClasspathURLsOf(JavaCore.create(ResourcesPlugin
                                    .getWorkspace().getRoot().getProject(
                                            entries[i].getPath().toFile()
                                                    .getName())), false);
                        } else {
                            URL url = ClassUtils
                                    .getURLForURLClassLoader(entries[i]
                                            .getPath().toFile());
                            if (url == null) {
                                url = ClassUtils
                                        .getURLForURLClassLoader(project
                                                .getProject().getParent()
                                                .getFile(entries[i].getPath())
                                                .getLocation().toFile());
                            }
                            if (url == null) {
                                Activator
                                        .log("ProjectClassLoader: Can't construct URL for classLoader: entry=" //$NON-NLS-1$
                                                + entries[i].getPath());
                                continue;
                            }
                            addURL(url);
                        }
                    }
                }
            }
        } catch (JavaModelException ex) {
            Activator
                    .log("ProjectClassLoader: Can't construct ClassLoader", ex);
            throw new RuntimeException(ex);
        }
    }

    public String[] getErrorPath() {
        if (errorURL == null) {
            return new String[0];
        }
        return errorURL;
    }
}
