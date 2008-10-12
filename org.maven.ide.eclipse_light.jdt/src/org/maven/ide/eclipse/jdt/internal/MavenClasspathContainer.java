/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.jdt.internal;

import java.io.Serializable;

import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;

/**
 * Maven classpath container
 */
public class MavenClasspathContainer implements IClasspathContainer, Serializable {
  private static final long serialVersionUID = -5976726121300869771L;
  
  private final IClasspathEntry[] entries;
  private final IPath path;

  public MavenClasspathContainer(IPath path, IClasspathEntry[] entries) {
    this.path = path;
    this.entries = entries;
  }
  
  public String getDescription() {
    return "Maven Dependencies";  // TODO move to properties
  }
  
  public int getKind() {
    return IClasspathContainer.K_APPLICATION;
  }
  
  public synchronized IClasspathEntry[] getClasspathEntries() {
    return entries;
  }

  public IPath getPath() {
    return path; 
  }
  
}
