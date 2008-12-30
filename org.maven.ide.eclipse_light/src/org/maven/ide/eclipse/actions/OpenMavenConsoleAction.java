/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.actions;

import org.eclipse.jface.action.Action;

import org.maven.ide.eclipse.MavenPlugin;

/**
 * Open Maven Console Action
 *
 * @author Eugene Kuleshov
 */
public class OpenMavenConsoleAction extends Action {
  
  public void run() {
    MavenPlugin.getDefault().getConsole().showConsole();
  }

}
