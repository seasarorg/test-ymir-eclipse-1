/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.project;

import java.io.Serializable;


/**
 * Resolver configuration holder
 * 
 * @author Eugene Kuleshov
 */
public class ResolverConfiguration implements Serializable {
  private static final long serialVersionUID = 1258510761534886581L;

  private boolean includeModules = false;

  private boolean resolveWorkspaceProjects = true;

  public boolean shouldIncludeModules() {
    return this.includeModules;
  }

  public boolean shouldResolveWorkspaceProjects() {
    return this.resolveWorkspaceProjects;
  }

  public void setResolveWorkspaceProjects(boolean resolveWorkspaceProjects) {
    this.resolveWorkspaceProjects = resolveWorkspaceProjects;
  }

  public void setIncludeModules(boolean includeModules) {
    this.includeModules = includeModules;
  }
}
