/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.ui.internal.preferences;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.PropertyPage;

import org.maven.ide.eclipse.MavenPlugin;
import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.core.MavenLogger;
import org.maven.ide.eclipse.project.MavenProjectManager;
import org.maven.ide.eclipse.project.ResolverConfiguration;


/**
 * Maven project preference page
 * 
 * @author Eugene Kuleshov
 */
public class MavenProjectPreferencePage extends PropertyPage {

  private Button resolveWorspaceProjectsButton;

  private Button includeModulesButton;

  public MavenProjectPreferencePage() {
    setTitle("Maven");
    setDescription("Maven project configuration:");
  }

  protected Control createContents(Composite parent) {
    Composite composite = new Composite(parent, SWT.NONE);
    composite.setLayout(new GridLayout(2, false));
    composite.setLayoutData(new GridData(GridData.FILL));

    resolveWorspaceProjectsButton = new Button(composite, SWT.CHECK);
    GridData resolveWorspaceProjectsButtonData = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
    resolveWorspaceProjectsButtonData.verticalIndent = 7;
    resolveWorspaceProjectsButton.setLayoutData(resolveWorspaceProjectsButtonData);
    resolveWorspaceProjectsButton.setText("Resolve dependencies from &Workspace projects");

    includeModulesButton = new Button(composite, SWT.CHECK);
    includeModulesButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 2, 1));
    includeModulesButton.setText("Include &Modules");

    Text includeModulesText = new Text(composite, SWT.WRAP | SWT.READ_ONLY | SWT.MULTI);
    includeModulesText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_DARK_BLUE));
    GridData gd_includeModulesText = new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1);
    gd_includeModulesText.horizontalIndent = 12;
    gd_includeModulesText.widthHint = 300;
    includeModulesText.setLayoutData(gd_includeModulesText);
    includeModulesText.setText("When enabled, dependencies from all nested modules "
        + "are added to the \"Maven Dependencies\" container and "
        + "source folders from nested modules are added to the current "
        + "project build path (use \"Update Sources\" action)");

    init(getResolverConfiguration());

    return composite;
  }

  protected void performDefaults() {
    init(new ResolverConfiguration());
  }

  private void init(ResolverConfiguration configuration) {
    resolveWorspaceProjectsButton.setSelection(configuration.shouldResolveWorkspaceProjects());
    includeModulesButton.setSelection(configuration.shouldIncludeModules());
  }

  public boolean performOk() {
    final IProject project = getProject();
    try {
      if(!project.isAccessible() || !project.hasNature(IMavenConstants.NATURE_ID)) {
        return true;
      }
    } catch(CoreException ex) {
      MavenLogger.log(ex);
      return false;
    }

    final ResolverConfiguration configuration = getResolverConfiguration();
    if(configuration.shouldIncludeModules() == includeModulesButton.getSelection()
        && configuration.shouldResolveWorkspaceProjects() == resolveWorspaceProjectsButton.getSelection()) {
      return true;
    }

    configuration.setResolveWorkspaceProjects(resolveWorspaceProjectsButton.getSelection());
    configuration.setIncludeModules(includeModulesButton.getSelection());

    MavenProjectManager projectManager = MavenPlugin.getDefault().getMavenProjectManager();
    boolean isSet = projectManager.setResolverConfiguration(getProject(), configuration);
    if(isSet) {

      // XXX this hack need to be replaced with a listener listening on java model or resource changes
//      Display.getCurrent().asyncExec(new Runnable() {
//        public void run() {
      boolean res = MessageDialog.openQuestion(getShell(), "Maven Settings", //
          "Maven settings has changed. Do you want to update project configuration?");
      if(res) {
        final MavenPlugin plugin = MavenPlugin.getDefault();
        WorkspaceJob job = new WorkspaceJob("Updating " + project.getName() + " Sources") {
          public IStatus runInWorkspace(IProgressMonitor monitor) {
            try {
              plugin.getProjectConfigurationManager().updateProjectConfiguration(project, configuration, monitor);
            } catch(CoreException ex) {
              return ex.getStatus();
            }
            return Status.OK_STATUS;
          }
        };
        job.setRule(plugin.getProjectConfigurationManager().getRule());
        job.schedule();
      }
//        }
//      });
    }

    return isSet;
  }

  private ResolverConfiguration getResolverConfiguration() {
    MavenProjectManager projectManager = MavenPlugin.getDefault().getMavenProjectManager();
    return projectManager.getResolverConfiguration(getProject());
  }

  private IProject getProject() {
    return (IProject) getElement();
  }

}
