/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.util;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.variables.VariablesPlugin;

import org.maven.ide.eclipse.core.MavenLogger;


/**
 * Utility methods
 *
 * @author Eugene Kuleshov
 */
public class Util {

  /**
   * Helper method which creates a folder and, recursively, all its parent
   * folders.
   *
   * @param folder  The folder to create.
   * @param derived true if folder should be marked as derived
   *
   * @throws CoreException if creating the given <code>folder</code> or any of
   *                       its parents fails.
   */
  public static void createFolder(IFolder folder, boolean derived) throws CoreException {
    // Recurse until we find a parent folder which already exists.
    if(!folder.exists()) {
      IContainer parent = folder.getParent();
      // First, make sure that all parent folders exist.
      if(parent != null && !parent.exists()) {
        createFolder((IFolder) parent, false);
      }
      folder.create(false, true, null);
    }
    
    if(folder.isAccessible() && derived) {
      folder.setDerived(true);
    }
  }
  
  public static void setDerived(IFolder folder, boolean derived) throws CoreException {
    if(folder.isAccessible()) {
      folder.setDerived(derived);
    }
  }

  /**
   * Substitute any variable
   */
  public static String substituteVar(String s) {
    if(s == null) {
      return s;
    }
    try {
      return VariablesPlugin.getDefault().getStringVariableManager().performStringSubstitution(s);
    } catch(CoreException e) {
      MavenLogger.log(e);
      return null;
    }
  }

}

