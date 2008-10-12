/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.project;

import java.util.Collection;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;


public interface IProjectConfigurationManager {

  ISchedulingRule getRule();

  Set<MavenProjectInfo> collectProjects(Collection<MavenProjectInfo> projects, boolean includeModules);

  void updateProjectConfiguration(IProject project, ResolverConfiguration configuration, IProgressMonitor monitor)
      throws CoreException;

  void enableMavenNature(IProject project, ResolverConfiguration configuration, IProgressMonitor monitor)
      throws CoreException;

  void disableMavenNature(IProject project, IProgressMonitor monitor) throws CoreException;

}
