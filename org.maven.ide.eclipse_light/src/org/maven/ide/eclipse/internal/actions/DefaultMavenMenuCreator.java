/*******************************************************************************
 * Copyright (c) 2007, 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.internal.actions;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.actions.AbstractMavenMenuCreator;
import org.maven.ide.eclipse.actions.ChangeNatureAction;
import org.maven.ide.eclipse.actions.DisableNatureAction;
import org.maven.ide.eclipse.actions.EnableNatureAction;
import org.maven.ide.eclipse.actions.RefreshMavenModelsAction;
import org.maven.ide.eclipse.actions.SelectionUtil;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.MavenProjectManager;
import org.maven.ide.eclipse.project.ResolverConfiguration;


/**
 * Default Maven menu creator
 * 
 * @author Eugene Kuleshov
 */
public class DefaultMavenMenuCreator extends AbstractMavenMenuCreator {

  public void createMenu(IMenuManager mgr) {
    int selectionType = SelectionUtil.getSelectionType(selection);
    if(selectionType == SelectionUtil.UNSUPPORTED) {
      return;
    }

    if(selectionType == SelectionUtil.PROJECT_WITHOUT_NATURE) {
      mgr.appendToGroup(NATURE, getAction(new EnableNatureAction(), //
          EnableNatureAction.ID, "Enable Dependency Management"));
    }

    if(selectionType == SelectionUtil.PROJECT_WITH_NATURE) {
      mgr.appendToGroup(UPDATE, getAction(new RefreshMavenModelsAction(), RefreshMavenModelsAction.ID,
          "Update Dependencies", "icons/update_dependencies.gif"));
      mgr.appendToGroup(UPDATE, getAction(new RefreshMavenModelsAction(true), RefreshMavenModelsAction.ID_SNAPSHOTS,
          "Update Snapshots"));

      boolean enableWorkspaceResolution = true;
      boolean enableNestedModules = true;
      if(selection.size() == 1) {
        IProject project = SelectionUtil.getType(selection.getFirstElement(), IProject.class);
        if(project != null) {
          MavenProjectManager projectManager = MavenPlugin.getDefault().getMavenProjectManager();
          IMavenProjectFacade projectFacade = projectManager.create(project, new NullProgressMonitor());
          if(projectFacade != null) {
            ResolverConfiguration configuration = projectFacade.getResolverConfiguration();
            enableWorkspaceResolution = !configuration.shouldResolveWorkspaceProjects();
            enableNestedModules = !configuration.shouldIncludeModules();
          }
        }
      }

      mgr.prependToGroup(NATURE, new Separator());
      if(enableWorkspaceResolution) {
        mgr.appendToGroup(NATURE, getAction(new ChangeNatureAction(ChangeNatureAction.ENABLE_WORKSPACE),
            ChangeNatureAction.ID_ENABLE_WORKSPACE, "Enable Workspace Resolution"));
      } else {
        mgr.appendToGroup(NATURE, getAction(new ChangeNatureAction(ChangeNatureAction.DISABLE_WORKSPACE),
            ChangeNatureAction.ID_DISABLE_WORKSPACE, "Disable Workspace Resolution"));
      }

      if(enableNestedModules) {
        mgr.appendToGroup(NATURE, getAction(new ChangeNatureAction(ChangeNatureAction.ENABLE_MODULES),
            ChangeNatureAction.ID_ENABLE_MODULES, "Enable Nested Modules"));
      } else {
        mgr.appendToGroup(NATURE, getAction(new ChangeNatureAction(ChangeNatureAction.DISABLE_MODULES),
            ChangeNatureAction.ID_DISABLE_MODULES, "Disable Nested Modules"));
      }

      mgr.appendToGroup(NATURE, getAction(new DisableNatureAction(), //
          DisableNatureAction.ID, "Disable Dependency Management"));
    }

    if(selectionType == SelectionUtil.WORKING_SET) {
      mgr.appendToGroup(UPDATE, getAction(new RefreshMavenModelsAction(), RefreshMavenModelsAction.ID,
          "Update Dependencies", "icons/update_dependencies.gif"));
      mgr.appendToGroup(UPDATE, getAction(new RefreshMavenModelsAction(true), RefreshMavenModelsAction.ID_SNAPSHOTS,
          "Update Snapshots"));
    }
  }

}
