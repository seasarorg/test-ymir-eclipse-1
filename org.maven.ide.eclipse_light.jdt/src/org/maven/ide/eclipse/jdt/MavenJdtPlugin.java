/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.jdt;

import java.io.File;

import org.osgi.framework.BundleActivator;
import org.osgi.framework.BundleContext;

import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.ResourcesPlugin;

import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.MavenConsole;
import org.maven.ide.eclipse.embedder.AbstractMavenEmbedderListener;
import org.maven.ide.eclipse.embedder.MavenEmbedderManager;
import org.maven.ide.eclipse.embedder.MavenModelManager;
import org.maven.ide.eclipse.embedder.MavenRuntimeManager;
import org.maven.ide.eclipse.index.IndexManager;
import org.maven.ide.eclipse.project.MavenProjectManager;


public class MavenJdtPlugin implements BundleActivator {

  public static String PLUGIN_ID = "org.maven.ide.eclipse_light.jdt";

  private static MavenJdtPlugin instance;

  BuildPathManager buildpathManager;

  public MavenJdtPlugin() {
    instance = this;
  }

  public void start(BundleContext bundleContext) throws Exception {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    MavenPlugin mavenPlugin = MavenPlugin.getDefault();

    MavenProjectManager projectManager = mavenPlugin.getMavenProjectManager();
    MavenEmbedderManager embedderManager = mavenPlugin.getMavenEmbedderManager();
    MavenConsole console = mavenPlugin.getConsole();
    IndexManager indexManager = mavenPlugin.getIndexManager();
    MavenModelManager modelManager = mavenPlugin.getMavenModelManager();
    MavenRuntimeManager runtimeManager = mavenPlugin.getMavenRuntimeManager();

    File stateLocationDir = mavenPlugin.getStateLocation().toFile(); // TODO migrate JDT settings to this plugin's store

    this.buildpathManager = new BuildPathManager(embedderManager, console, projectManager, indexManager, modelManager,
        runtimeManager, bundleContext, stateLocationDir);
    workspace.addResourceChangeListener(buildpathManager, IResourceChangeEvent.PRE_DELETE);

    projectManager.addMavenProjectChangedListener(this.buildpathManager);
    projectManager.addDownloadSourceListener(this.buildpathManager);

    embedderManager.addListener(new AbstractMavenEmbedderListener() {
      public void workspaceEmbedderInvalidated() {
        buildpathManager.setupVariables();
      }
    });
  }

  public void stop(BundleContext context) throws Exception {
    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    MavenPlugin mavenPlugin = MavenPlugin.getDefault();

    MavenProjectManager projectManager = mavenPlugin.getMavenProjectManager();
    projectManager.removeMavenProjectChangedListener(buildpathManager);
    projectManager.removeDownloadSourceListener(this.buildpathManager);

    workspace.removeResourceChangeListener(this.buildpathManager);

    this.buildpathManager = null;
  }

  public static MavenJdtPlugin getDefault() {
    return instance;
  }

  public BuildPathManager getBuildpathManager() {
    return buildpathManager;
  }
}
