/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.jdt.internal;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IClasspathContainer;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;

import org.apache.maven.embedder.MavenEmbedder;

import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.core.MavenLogger;
import org.maven.ide.eclipse.jdt.BuildPathManager;
import org.maven.ide.eclipse.project.ResolverConfiguration;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;


public class JavaProjectConfigurator extends AbstractProjectConfigurator {

  // XXX make sure to configure only Java projects
  public void configure(MavenEmbedder embedder, ProjectConfigurationRequest request, IProgressMonitor monitor)
      throws CoreException {
    IProject project = request.getProject();
    ResolverConfiguration configuration = request.getResolverConfiguration();

    addNature(project, JavaCore.NATURE_ID, monitor);
    addMavenClasspathContainer(project, configuration, monitor);
  }

  private void addMavenClasspathContainer(IProject project, //
      ResolverConfiguration configuration, IProgressMonitor monitor) {
    monitor.setTaskName("Setting classpath container " + project.getName());
    try {
      IJavaProject javaProject = JavaCore.create(project);
      if(javaProject != null) {
        Set<String> containerEntrySet = new LinkedHashSet<String>();
        IClasspathContainer container = BuildPathManager.getMaven2ClasspathContainer(javaProject);
        if(container != null) {
          for(IClasspathEntry entry : container.getClasspathEntries()) {
            containerEntrySet.add(entry.getPath().toString());
          }
        }

        ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
        for(IClasspathEntry entry : javaProject.getRawClasspath()) {
          if(!BuildPathManager.isMaven2ClasspathContainer(entry.getPath())
              && !containerEntrySet.contains(entry.getPath().toString())) {
            newEntries.add(entry);
          }
        }

        newEntries.add(JavaCore.newContainerEntry(new Path(IMavenConstants.CONTAINER_ID)));

        javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), monitor);
      }
    } catch(CoreException ex) {
      String msg = "Unable to set classpaht container " + project.getName();
      console.logMessage(msg + "; " + ex.toString());
      MavenLogger.log(msg, ex);
    }
  }

  public void unconfigure(MavenEmbedder embedder, ProjectConfigurationRequest request, IProgressMonitor monitor)
      throws CoreException {
    super.unconfigure(embedder, request, monitor);
    removeMavenClasspathContainer(request.getProject());
  }

  private void removeMavenClasspathContainer(IProject project) throws JavaModelException {
    IJavaProject javaProject = JavaCore.create(project);
    if(javaProject != null) {
      // remove classpatch container from JavaProject
      ArrayList<IClasspathEntry> newEntries = new ArrayList<IClasspathEntry>();
      for(IClasspathEntry entry : javaProject.getRawClasspath()) {
        if(!BuildPathManager.isMaven2ClasspathContainer(entry.getPath())) {
          newEntries.add(entry);
        }
      }
      javaProject.setRawClasspath(newEntries.toArray(new IClasspathEntry[newEntries.size()]), null);
    }
  }
}
