/*******************************************************************************
 * Copyright (c) 2008 Sonatype, Inc.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/

package org.maven.ide.eclipse.internal.project;

import java.io.File;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.Properties;
import java.util.Set;
import java.util.TreeSet;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.ui.IWorkingSet;

import org.apache.maven.archetype.ArchetypeGenerationRequest;
import org.apache.maven.archetype.ArchetypeGenerationResult;
import org.apache.maven.archetype.catalog.Archetype;
import org.apache.maven.embedder.MavenEmbedder;
import org.apache.maven.embedder.MavenEmbedderException;
import org.apache.maven.model.Model;
import org.apache.maven.project.MavenProject;

import org.maven.ide.eclipse.core.IMavenConstants;
import org.maven.ide.eclipse.core.MavenConsole;
import org.maven.ide.eclipse.core.MavenLogger;
import org.maven.ide.eclipse.embedder.EmbedderFactory;
import org.maven.ide.eclipse.embedder.MavenEmbedderManager;
import org.maven.ide.eclipse.embedder.MavenModelManager;
import org.maven.ide.eclipse.embedder.MavenRuntimeManager;
import org.maven.ide.eclipse.index.IndexManager;
import org.maven.ide.eclipse.internal.ExtensionReader;
import org.maven.ide.eclipse.internal.embedder.TransferListenerAdapter;
import org.maven.ide.eclipse.project.IMavenProjectChangedListener;
import org.maven.ide.eclipse.project.IMavenProjectFacade;
import org.maven.ide.eclipse.project.IProjectConfigurationManager;
import org.maven.ide.eclipse.project.LocalProjectScanner;
import org.maven.ide.eclipse.project.MavenProjectChangedEvent;
import org.maven.ide.eclipse.project.MavenProjectInfo;
import org.maven.ide.eclipse.project.MavenProjectManager;
import org.maven.ide.eclipse.project.MavenUpdateRequest;
import org.maven.ide.eclipse.project.ProjectImportConfiguration;
import org.maven.ide.eclipse.project.ResolverConfiguration;
import org.maven.ide.eclipse.project.configurator.AbstractProjectConfigurator;
import org.maven.ide.eclipse.project.configurator.ProjectConfigurationRequest;
import org.maven.ide.eclipse.util.Util;


/**
 * ProjectConfiguration Manager
 * 
 * @author igor
 */
public class ProjectConfigurationManager implements IProjectConfigurationManager, IMavenProjectChangedListener {

  static final QualifiedName QNAME = new QualifiedName(IMavenConstants.PLUGIN_ID, "ProjectImportManager");

  final MavenModelManager modelManager;

  final MavenConsole console;

  final MavenRuntimeManager runtimeManager;

  final MavenProjectManager projectManager;

  final MavenProjectManagerImpl projectManagerImpl;

  final IndexManager indexManager;

  final MavenEmbedderManager embedderManager;

  final MavenModelManager mavenModelManager;

  private Set<AbstractProjectConfigurator> configurators;

  public ProjectConfigurationManager(MavenModelManager modelManager, MavenConsole console,
      MavenRuntimeManager runtimeManager, MavenProjectManager projectManager,
      MavenProjectManagerImpl projectManagerImpl, IndexManager indexManager, MavenEmbedderManager embedderManager,
      MavenModelManager mavenModelManager) {
    this.modelManager = modelManager;
    this.console = console;
    this.runtimeManager = runtimeManager;
    this.projectManager = projectManager;
    this.projectManagerImpl = projectManagerImpl;
    this.indexManager = indexManager;
    this.embedderManager = embedderManager;
    this.mavenModelManager = mavenModelManager;
  }

  public void importProjects(Collection<MavenProjectInfo> projectInfos, ProjectImportConfiguration configuration,
      IProgressMonitor monitor) throws CoreException {
    try {
      MavenEmbedder embedder = projectManagerImpl.createWorkspaceEmbedder();
      try {
        ArrayList<IProject> projects = new ArrayList<IProject>();

        // first, create all projects with basic configuration
        for(MavenProjectInfo projectInfo : projectInfos) {
          if(monitor.isCanceled()) {
            throw new OperationCanceledException();
          }

          IProject project = create(projectInfo, configuration, monitor);
          if(project != null) {
            projects.add(project);

            IWorkingSet workingSet = configuration.getWorkingSet();
            if(workingSet != null) {
              addToWorkingSet(project, workingSet);
            }
          }
        }

        // then configure maven for all projects
        configureNewMavenProject(embedder, projects, monitor);
      } finally {
        embedder.stop();
      }

    } catch(MavenEmbedderException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, ex.getMessage(), ex));
    }
  }

  private void configureNewMavenProject(MavenEmbedder embedder, ArrayList<IProject> projects, IProgressMonitor monitor)
      throws CoreException {

    try {
      // first, resolve maven dependencies for all projects
      MavenUpdateRequest updateRequest = new MavenUpdateRequest(false, false);
      for(IProject project : projects) {
        updateRequest.addPomFile(project);
      }

      DependencyResolutionContext resolutionContext = new DependencyResolutionContext(updateRequest);
      while(!resolutionContext.isEmpty()) {
        if(monitor.isCanceled()) {
          throw new OperationCanceledException();
        }

        IFile pom = resolutionContext.pop();
        monitor.subTask(pom.getFullPath().toString());

        projectManagerImpl.refresh(embedder, pom, resolutionContext, monitor);
        monitor.worked(1);
      }

      // then, perform detailed project configuration
      for(IProject project : projects) {
        if(monitor.isCanceled()) {
          throw new OperationCanceledException();
        }

        IMavenProjectFacade facade = projectManagerImpl.create(project, monitor);
        if(facade != null) {
          ProjectConfigurationRequest request = new ProjectConfigurationRequest(facade.getProject(), facade.getPom(),
              facade.getMavenProject(monitor), facade.getResolverConfiguration(), false /*updateSources*/);
          updateProjectConfiguration(embedder, request, monitor);
        }
      }
    } finally {
      projectManagerImpl.notifyProjectChangeListeners(monitor);
    }
  }

  // PlatformUI.getWorkbench().getWorkingSetManager().addToWorkingSets(project, new IWorkingSet[] {workingSet});
  private void addToWorkingSet(IProject project, IWorkingSet workingSet) {
    // IAdaptable[] elements = workingSet.adaptElements(new IAdaptable[] {project});
    // if(elements.length == 1) {
    IAdaptable[] oldElements = workingSet.getElements();
    IAdaptable[] newElements = new IAdaptable[oldElements.length + 1];
    System.arraycopy(oldElements, 0, newElements, 0, oldElements.length);
    newElements[oldElements.length] = project;
    workingSet.setElements(newElements);
    // }
  }

  public void updateProjectConfiguration(IProject project, ResolverConfiguration configuration, IProgressMonitor monitor)
      throws CoreException {
    try {
      MavenEmbedder embedder = embedderManager.createEmbedder(EmbedderFactory.createExecutionCustomizer());
      try {
        IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
        if(pom.isAccessible()) {
          IMavenProjectFacade facade = projectManagerImpl.create(pom, false, monitor);
          if(facade != null) { // facade is null if pom.xml cannot be read
            ProjectConfigurationRequest request = new ProjectConfigurationRequest(project, facade.getPom(), facade
                .getMavenProject(monitor), configuration, true);
            updateProjectConfiguration(embedder, request, monitor);
          }
        }
      } finally {
        embedder.stop();
      }
    } catch(MavenEmbedderException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, ex.getMessage(), ex));
    }
  }

  private void updateProjectConfiguration(MavenEmbedder embedder, ProjectConfigurationRequest request,
      IProgressMonitor monitor) throws CoreException {
    for(AbstractProjectConfigurator configurator : getConfigurators()) {
      if(monitor.isCanceled()) {
        throw new OperationCanceledException();
      }
      configurator.configure(embedder, request, monitor);
    }

    IProject project = request.getProject();
    addMavenNature(project, monitor);
  }

  public void enableMavenNature(IProject project, ResolverConfiguration configuration, IProgressMonitor monitor)
      throws CoreException {
    monitor.subTask("Enable Maven nature");
    try {
      MavenEmbedder embedder = projectManagerImpl.createWorkspaceEmbedder();
      try {
        enableBasicMavenNature(project, configuration, monitor);

        ArrayList<IProject> projects = new ArrayList<IProject>();
        projects.add(project);
        configureNewMavenProject(embedder, projects, monitor);

      } finally {
        embedder.stop();
      }
    } catch(MavenEmbedderException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, ex.getMessage(), ex));
    }
  }

  private void enableBasicMavenNature(IProject project, ResolverConfiguration configuration, IProgressMonitor monitor)
      throws CoreException {
    projectManagerImpl.setResolverConfiguration(project, configuration);

    // add maven nature even for projects without valid pom.xml file
    addMavenNature(project, monitor);
  }

  private void addMavenNature(IProject project, IProgressMonitor monitor) throws CoreException {
    if(!project.hasNature(IMavenConstants.NATURE_ID)) {
      IProjectDescription description = project.getDescription();
      String[] prevNatures = description.getNatureIds();
      String[] newNatures = new String[prevNatures.length + 1];
      System.arraycopy(prevNatures, 0, newNatures, 1, prevNatures.length);
      newNatures[0] = IMavenConstants.NATURE_ID;
      description.setNatureIds(newNatures);
      project.setDescription(description, monitor);
    }
  }

  public void disableMavenNature(IProject project, IProgressMonitor monitor) throws CoreException {
    monitor.subTask("Disable Maven nature");

    try {
      MavenEmbedder embedder = projectManagerImpl.createWorkspaceEmbedder();
      try {
        IFile pom = project.getFile(IMavenConstants.POM_FILE_NAME);
        IMavenProjectFacade facade = projectManager.create(project, monitor);
        MavenProject mavenProject = facade != null ? facade.getMavenProject(monitor) : null;
        ResolverConfiguration resolverConfiguration = facade != null ? facade.getResolverConfiguration() : null;
        ProjectConfigurationRequest request = new ProjectConfigurationRequest(project, pom, mavenProject,
            resolverConfiguration, false);
        for(AbstractProjectConfigurator configurator : getConfigurators()) {
          if(monitor.isCanceled()) {
            throw new OperationCanceledException();
          }
          configurator.unconfigure(embedder, request, monitor);
        }

        project.deleteMarkers(IMavenConstants.MARKER_ID, true, IResource.DEPTH_INFINITE);

        IProjectDescription description = project.getDescription();
        ArrayList<String> newNatures = new ArrayList<String>();
        for(String natureId : description.getNatureIds()) {
          if(!IMavenConstants.NATURE_ID.equals(natureId)) {
            newNatures.add(natureId);
          }
        }
        description.setNatureIds(newNatures.toArray(new String[newNatures.size()]));
        project.setDescription(description, null);

      } finally {
        embedder.stop();
      }
    } catch(MavenEmbedderException ex) {
      throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, ex.getMessage(), ex));
    }
  }

  // project creation

  /**
   * Creates simple Maven project
   * <p>
   * The following steps are executed in the given order:
   * <ul>
   * <li>Creates the workspace project</li>
   * <li>Adds project to working set</li>
   * <li>Creates the required folders</li>
   * <li>Creates the POM</li>
   * <li>Configures project</li>
   * </ul>
   * </p>
   */
  // XXX should use Maven plugin configurations instead of manually specifying folders
  public void createSimpleProject(IProject project, IPath location, Model model, String[] directories,
      ProjectImportConfiguration configuration, IProgressMonitor monitor) throws CoreException {
    String projectName = project.getName();
    monitor.beginTask("Creating project " + projectName, 5);

    monitor.subTask("Creating workspace project...");
    IProjectDescription description = ResourcesPlugin.getWorkspace().newProjectDescription(projectName);
    description.setLocation(location);
    project.create(description, monitor);
    project.open(monitor);
    monitor.worked(1);

    IWorkingSet workingSet = configuration.getWorkingSet();
    if(workingSet != null) {
      addToWorkingSet(project, workingSet);
    }
    monitor.worked(1);

    monitor.subTask("Creating the POM file...");
    IFile pomFile = project.getFile(IMavenConstants.POM_FILE_NAME);
    modelManager.createMavenModel(pomFile, model);
    monitor.worked(1);

    monitor.subTask("Creating project folders...");
    for(int i = 0; i < directories.length; i++ ) {
      Util.createFolder(project.getFolder(directories[i]), false);
    }
    monitor.worked(1);

    monitor.subTask("Configuring project...");
    enableMavenNature(project, configuration.getResolverConfiguration(), monitor);
    monitor.worked(1);
  }

  /**
   * Creates project structure using Archetype and then imports created project
   */
  public void createArchetypeProject(IProject project, IPath location, Archetype archetype, String groupId,
      String artifactId, String version, String javaPackage, Properties properties,
      ProjectImportConfiguration configuration, IProgressMonitor monitor) throws CoreException {
    monitor.beginTask("Creating project " + project.getName(), 2);

    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();

    monitor.subTask("Executing Archetype " + archetype.getGroupId() + ":" + archetype.getArtifactId());
    if(location == null) {
      // if the project should be created in the workspace, figure out the path
      location = workspaceRoot.getLocation();
    }

    try {
      ArchetypeGenerationRequest request = new ArchetypeGenerationRequest() //
          .setTransferListener(new TransferListenerAdapter(new NullProgressMonitor(), console, indexManager)) //
          .setArchetypeGroupId(archetype.getGroupId()) //
          .setArchetypeArtifactId(archetype.getArtifactId()) //
          .setArchetypeVersion(archetype.getVersion()) //
          .setArchetypeRepository(archetype.getRepository()) //
          .setGroupId(groupId) //
          .setArtifactId(artifactId) //
          .setVersion(version) //
          .setPackage(javaPackage) // the model does not have a package field
          .setLocalRepository(embedderManager.getWorkspaceEmbedder().getLocalRepository()) //
          .setProperties(properties).setOutputDirectory(location.toPortableString());

      ArchetypeGenerationResult result = getArchetyper().generateProjectFromArchetype(request);
      Exception cause = result.getCause();
      if(cause != null) {
        String msg = "Unable to create project from archetype " + archetype.toString();
        MavenLogger.log(msg, cause);
        throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, msg, cause));
      }
      monitor.worked(1);

      // XXX Archetyper don't allow to specify project folder
      String projectFolder = location.append(artifactId).toFile().getAbsolutePath();

      LocalProjectScanner scanner = new LocalProjectScanner(workspaceRoot.getLocation().toFile(), //
          projectFolder, true, mavenModelManager, console);
      scanner.run(monitor);

      Set<MavenProjectInfo> projectSet = collectProjects(scanner.getProjects(), //
          configuration.getResolverConfiguration().shouldIncludeModules());

      importProjects(projectSet, configuration, monitor);

      monitor.worked(1);
    } catch(InterruptedException e) {
      throw new CoreException(Status.CANCEL_STATUS);
    }
  }

  private org.apache.maven.archetype.Archetype getArchetyper() throws CoreException {
    return embedderManager.getComponent(org.apache.maven.archetype.Archetype.class, null);
  }

  /**
   * Flatten hierarchical projects
   * 
   * @param projects a collection of {@link MavenProjectInfo}
   * @param includeModules of true
   * @return flattened collection of {@link MavenProjectInfo}
   */
  public Set<MavenProjectInfo> collectProjects(Collection<MavenProjectInfo> projects, boolean includeModules) {
    Set<MavenProjectInfo> projectSet = collectProjects(projects);
    if(!includeModules) {
      return projectSet;
    }

    Set<MavenProjectInfo> parentProjects = new HashSet<MavenProjectInfo>();
    for(MavenProjectInfo projectInfo : projectSet) {
      MavenProjectInfo parent = projectInfo.getParent();
      if(parent == null || !projectSet.contains(parent)) {
        parentProjects.add(projectInfo);
      }
    }
    return parentProjects;
  }

  private Set<MavenProjectInfo> collectProjects(Collection<MavenProjectInfo> projects) {
    return new LinkedHashSet<MavenProjectInfo>() {
      private static final long serialVersionUID = 1L;

      public Set<MavenProjectInfo> collectProjects(Collection<MavenProjectInfo> projects) {
        for(MavenProjectInfo projectInfo : projects) {
          add(projectInfo);
          collectProjects(projectInfo.getProjects());
        }
        return this;
      }
    }.collectProjects(projects);
  }

  public ISchedulingRule getRule() {
    return new SchedulingRule(false);
  }

  private IProject create(MavenProjectInfo projectInfo, ProjectImportConfiguration configuration,
      IProgressMonitor monitor) throws CoreException {
    File pomFile = projectInfo.getPomFile();
    Model model = projectInfo.getModel();
    if(model == null) {
      model = modelManager.readMavenModel(pomFile);
      projectInfo.setModel(model);
    }

    String projectName = configuration.getProjectName(model);

    monitor.subTask("Importing project " + projectName);

    IWorkspace workspace = ResourcesPlugin.getWorkspace();
    IWorkspaceRoot root = workspace.getRoot();
    IProject project = configuration.getProject(root, model);
    if(project.exists()) {
      console.logError("Project " + projectName + " already exists");
      return null;
    }

    File projectDir = pomFile.getParentFile();

    if(projectDir.equals(root.getLocation().toFile())) {
      console.logError("Can't create project " + projectName + " at Workspace folder");
      return null;
    }

    if(projectInfo.isNeedsRename()) {
      File newProject = new File(projectDir.getParent(), projectName);
      boolean renamed = projectDir.renameTo(newProject);
      if(!renamed) {
        throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, "Can't rename "
            + projectDir.getAbsolutePath(), null));
      }
      projectInfo.setPomFile(new File(newProject, IMavenConstants.POM_FILE_NAME));
      projectDir = newProject;
    }

    String projectParent = projectDir.getParentFile().getAbsolutePath();
    if(projectParent.equals(root.getLocation().toFile().getAbsolutePath())) {
      // rename dir in workspace to match expected project name
      if(!projectDir.equals(root.getLocation().append(project.getName()).toFile())) {
        File newProject = new File(projectDir.getParent(), projectName);
        boolean renamed = projectDir.renameTo(newProject);
        if(!renamed) {
          throw new CoreException(new Status(IStatus.ERROR, IMavenConstants.PLUGIN_ID, -1, "Can't rename "
              + projectDir.getAbsolutePath(), null));
        }
        projectInfo.setPomFile(new File(newProject, IMavenConstants.POM_FILE_NAME));
      }
      project.create(monitor);
    } else {
      IProjectDescription description = workspace.newProjectDescription(projectName);
      description.setLocation(new Path(projectDir.getAbsolutePath()));
      project.create(description, monitor);
    }

    if(!project.isOpen()) {
      project.open(monitor);
    }

    ResolverConfiguration resolverConfiguration = configuration.getResolverConfiguration();
    enableBasicMavenNature(project, resolverConfiguration, monitor);

    return project;
  }

  public void mavenProjectChanged(MavenProjectChangedEvent[] events, IProgressMonitor monitor) {
    for(AbstractProjectConfigurator configurator : getConfigurators()) {
      configurator.mavenProjectChanged(events, monitor);
    }
  }

  public synchronized Set<AbstractProjectConfigurator> getConfigurators() {
    if(configurators == null) {
      configurators = new TreeSet<AbstractProjectConfigurator>(new ProjectConfiguratorComparator());
      configurators.addAll(ExtensionReader.readProjectConfiguratorExtensions(projectManager, runtimeManager, console));
    }
    return Collections.unmodifiableSet(configurators);
  }

  /**
   * ProjectConfigurator comparator
   */
  static class ProjectConfiguratorComparator implements Comparator<AbstractProjectConfigurator>, Serializable {
    private static final long serialVersionUID = 1L;

    public int compare(AbstractProjectConfigurator c1, AbstractProjectConfigurator c2) {
      int res = c1.getPriority() - c2.getPriority();
      return res == 0 ? c1.getId().compareTo(c2.getId()) : res;
    }
  }
}
