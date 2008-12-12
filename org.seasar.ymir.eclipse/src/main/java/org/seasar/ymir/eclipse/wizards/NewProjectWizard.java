package org.seasar.ymir.eclipse.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.ArtifactPair;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.HotdeployType;
import org.seasar.ymir.eclipse.maven.ExtendedContext;
import org.seasar.ymir.eclipse.maven.util.MavenUtils;
import org.seasar.ymir.eclipse.natures.ViliProjectNature;
import org.seasar.ymir.eclipse.natures.YmirProjectNature;
import org.seasar.ymir.eclipse.ui.YmirConfigurationControl;
import org.seasar.ymir.eclipse.util.BeanMap;
import org.seasar.ymir.eclipse.util.CascadeMap;
import org.seasar.ymir.eclipse.util.JdtUtils;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.Dependencies;
import org.seasar.ymir.vili.maven.Dependency;
import org.seasar.ymir.vili.maven.Project;

public class NewProjectWizard extends Wizard implements INewWizard, ISelectArtifactWizard {
    private static final char PACKAGE_DELIMITER = '.';

    private static final String CREATESUPERCLASS_KEY_PACKAGENAME = "packageName"; //$NON-NLS-1$

    private static final String CREATESUPERCLASS_KEY_CLASSSHORTNAME = "classShortName"; //$NON-NLS-1$

    private static final String TEMPLATEPATH_SUPERCLASS = "templates/Superclass.java.ftl"; //$NON-NLS-1$

    private static final String PATH_JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    private static final String PATH_JDT_CORE_PREFS = ".settings/org.eclipse.jdt.core.prefs"; //$NON-NLS-1$

    private static final String BUNDLE_PATHPREFIX_JDT_CORE_PREFS = "/prefs/compliance/org.eclipse.jdt.core.prefs-"; //$NON-NLS-1$

    static final String REQUIRED_TEMPLATE = Messages.getString("NewProjectWizard.2"); //$NON-NLS-1$

    static final String DS_SECTION = "NewProjectWizard"; //$NON-NLS-1$

    private ViliProjectPreferences preferences;

    private ExtendedContext nonTransitiveContext;

    private SelectArtifactPage firstPage;

    private ConfigureProjectPage secondPage;

    private ConfigureParametersPage thirdPage;

    /**
     * Constructor for NewProjectWizard.
     */
    public NewProjectWizard() {
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.getString("NewProjectWizard.11")); //$NON-NLS-1$
        setDefaultPageImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Globals.IMAGE_YMIR));

        preferences = Activator.getDefault().newViliProjectPreferences();
        nonTransitiveContext = Activator.getDefault().getArtifactResolver().newContext(false);

        IDialogSettings settings = Activator.getDefault().getDialogSettings();
        setDialogSettings(settings);
        IDialogSettings section = settings.getSection(DS_SECTION);
        if (section == null) {
            section = settings.addNewSection(DS_SECTION);
        }
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        firstPage = new SelectArtifactPage(nonTransitiveContext, true);
        firstPage.setTitle(Messages.getString("NewProjectWizard.26")); //$NON-NLS-1$
        firstPage.setDescription(Messages.getString("NewProjectWizard.27")); //$NON-NLS-1$
        addPage(firstPage);
        secondPage = new ConfigureProjectPage(preferences);
        addPage(secondPage);
        thirdPage = new ConfigureParametersPage(preferences);
        addPage(thirdPage);
    }

    @Override
    public void setContainer(IWizardContainer wizardContainer) {
        super.setContainer(wizardContainer);
        Shell shell = getShell();
        if (shell != null) {
            shell.setSize(600, shell.getSize().y);
        }
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    public boolean performFinish() {
        thirdPage.populateSkeletonParameters();
        try {
            final ArtifactPair skeleton = firstPage.getSkeleton();
            final ArtifactPair[] fragments = firstPage.getFragments();
            final IProject project = secondPage.getProjectHandle();
            final IPath locationPath = secondPage.getLocationPath();
            final IPath jreContainerPath = preferences.getJREContainerPath();
            preferences.setApplicationProperties(createApplicationProperties());
            IRunnableWithProgress op = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    monitor.beginTask(Messages.getString("NewProjectWizard.1"), 2); //$NON-NLS-1$
                    try {
                        createProject(project, locationPath, jreContainerPath, skeleton, new SubProgressMonitor(
                                monitor, 1));
                        Activator.getDefault().addFragments(project, preferences, fragments,
                                new SubProgressMonitor(monitor, 1));
                    } catch (CoreException e) {
                        Activator.getDefault().getLog().log(e.getStatus());
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            };

            getContainer().run(true, true, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException ex) {
            Throwable realException = ex.getTargetException();

            String message = realException.getMessage();
            if (message == null || message.length() == 0) {
                message = realException.getClass().getName();
            }

            ILog log = Activator.getDefault().getLog();
            IStatus status;
            if (realException instanceof CoreException) {
                status = ((CoreException) realException).getStatus();
            } else {
                status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, realException);
            }
            log.log(status);

            MessageDialog.openError(getShell(), "Error", message); //$NON-NLS-1$
            return false;
        } catch (CoreException ex) {
            ILog log = Activator.getDefault().getLog();
            log.log(ex.getStatus());

            String message = ex.getMessage();
            if (message == null || message.length() == 0) {
                message = ex.getClass().getName();
            }
            MessageDialog.openError(getShell(), "Error", message); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private MapProperties createApplicationProperties() throws CoreException {
        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        YmirConfigurationControl ymirConfig = thirdPage.getYmirConfigurationControl();
        if (ymirConfig != null) {
            prop.setProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME, preferences.getRootPackageName());
            String value = ymirConfig.getSuperclass();
            if (value.length() > 0) {
                prop.setProperty(ApplicationPropertiesKeys.SUPERCLASS, value);
            }
            prop.setProperty(ApplicationPropertiesKeys.SOURCECREATOR_ENABLE, String.valueOf(ymirConfig
                    .isAutoGenerationEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.FIELDPREFIX, JdtUtils.getFieldPrefix());
            prop.setProperty(ApplicationPropertiesKeys.FIELDSUFFIX, JdtUtils.getFieldSuffix());
            prop.setProperty(ApplicationPropertiesKeys.FIELDSPECIALPREFIX, JdtUtils.getFieldSpecialPrefix());
            prop.setProperty(ApplicationPropertiesKeys.ENABLEINPLACEEDITOR, String.valueOf(ymirConfig
                    .isInplaceEditorEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.ENABLECONTROLPANEL, String.valueOf(ymirConfig
                    .isControlPanelEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.USING_FREYJA_RENDER_CLASS, String.valueOf(ymirConfig
                    .isUsingFreyjaRenderClass()));
            prop.setProperty(ApplicationPropertiesKeys.BEANTABLE_ENABLED, String.valueOf(ymirConfig
                    .isBeantableEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.FORM_DTO_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isFormDtoCreationFeatureEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.CONVERTER_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isConverterCreationFeatureEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.DAO_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isDaoCreationFeatureEnabled()));
            prop.setProperty(ApplicationPropertiesKeys.DXO_CREATION_FEATURE_ENABLED, String.valueOf(ymirConfig
                    .isDxoCreationFeatureEnabled()));

            boolean eclipseEnabled = ymirConfig.isEclipseEnabled();
            prop.setProperty(ApplicationPropertiesKeys.ECLIPSE_ENABLED, String.valueOf(eclipseEnabled));
            if (eclipseEnabled) {
                value = ymirConfig.getResourceSynchronizerURL();
                if (value.length() > 0) {
                    prop.setProperty(ApplicationPropertiesKeys.RESOURCE_SYNCHRONIZER_URL, value);
                }
                prop.setProperty(ApplicationPropertiesKeys.ECLIPSE_PROJECTNAME, secondPage.getProjectName());
            }

            prop.setProperty(ApplicationPropertiesKeys.S2CONTAINER_CLASSLOADING_DISABLEHOTDEPLOY, String
                    .valueOf(ymirConfig.getHotdeployType() != HotdeployType.S2));
            prop.setProperty(ApplicationPropertiesKeys.S2CONTAINER_COMPONENTREGISTRATION_DISABLEDYNAMIC, String
                    .valueOf(ymirConfig.getHotdeployType() == HotdeployType.VOID));
            prop.setProperty(ApplicationPropertiesKeys.HOTDEPLOY_TYPE, ymirConfig.getHotdeployType().getName());
        }

        return prop;
    }

    private void createProject(IProject project, IPath locationPath, IPath jreContainerPath, ArtifactPair skeleton,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.12"), 11); //$NON-NLS-1$
        try {
            if (!project.exists()) {
                IProjectDescription description = project.getWorkspace().newProjectDescription(project.getName());
                if (Platform.getLocation().equals(locationPath)) {
                    locationPath = null;
                }
                description.setLocation(locationPath);
                project.create(description, new SubProgressMonitor(monitor, 1));
            } else {
                monitor.worked(1);
            }
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            project.open(new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            ViliBehavior behavior = skeleton.getBehavior();
            try {
                @SuppressWarnings("unchecked")
                Map<String, Object> parameters = new CascadeMap<String, Object>(skeleton.getParameterMap(),
                        new BeanMap(preferences));
                Activator.getDefault()
                        .expandArtifact(project, skeleton, parameters, new SubProgressMonitor(monitor, 1));
            } catch (IOException ex) {
                throwCoreException(Messages.getString("NewProjectWizard.13"), ex); //$NON-NLS-1$
                return;
            }
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            addDatabaseDependenciesToPom(project, new SubProgressMonitor(monitor, 1));

            if (behavior.isProjectOf(ProjectType.JAVA)) {
                IJavaProject javaProject = JavaCore.create(project);

                setUpProjectDescription(project, behavior, new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                setUpJRECompliance(project, preferences.getJREVersion(), new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                setUpClasspath(javaProject, jreContainerPath, new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            } else {
                monitor.worked(3);
            }

            if (behavior.isProjectOf(ProjectType.YMIR)) {
                MapProperties applicationProperties = preferences.getApplicationProperties();

                updateApplicationProperties(project, applicationProperties, new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                createSuperclass(project, applicationProperties.getProperty(ApplicationPropertiesKeys.SUPERCLASS),
                        new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            } else {
                monitor.worked(2);
            }

            try {
                preferences.save(project);
            } catch (IOException ex) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID,
                        "Can't save project preferences", ex)); //$NON-NLS-1$
            }
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    private void addDatabaseDependenciesToPom(IProject project, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.28"), 1); //$NON-NLS-1$
        try {
            Dependency databaseDependency = preferences.getDatabaseEntry().getDependency();
            if (databaseDependency != null) {
                Project pom = new Project();
                Dependencies dependencies = new Dependencies();
                dependencies.addDependency(databaseDependency);
                pom.setDependencies(dependencies);
                MavenUtils.addToPom(project.getFile(Globals.PATH_POM_XML), pom, monitor);
            }
        } finally {
            monitor.done();
        }
    }

    public ExtendedContext getNonTransitiveContext() {
        return nonTransitiveContext;
    }

    private void setUpJRECompliance(IProject project, String jreVersion, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.25"), 1); //$NON-NLS-1$
        try {
            if (jreVersion.length() == 0) {
                return;
            }
            URL entry = Activator.getDefault().getBundle().getEntry(BUNDLE_PATHPREFIX_JDT_CORE_PREFS + jreVersion);
            if (entry != null) {
                Activator.getDefault().mergeProperties(project.getFile(PATH_JDT_CORE_PREFS), entry,
                        new SubProgressMonitor(monitor, 1));
            }
        } finally {
            monitor.done();
        }
    }

    private void createSuperclass(IProject project, String superclass, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.14"), 2); //$NON-NLS-1$
        try {
            if (superclass == null) {
                return;
            }
            IFile file = project.getFile(Globals.PATH_SRC_MAIN_JAVA + "/" //$NON-NLS-1$
                    + superclass.replace('.', '/').concat(".java")); //$NON-NLS-1$
            if (file.exists()) {
                return;
            }

            String packageName;
            String classShortName;
            int dot = superclass.lastIndexOf(PACKAGE_DELIMITER);
            if (dot < 0) {
                packageName = ""; //$NON-NLS-1$
                classShortName = superclass;
            } else {
                packageName = superclass.substring(0, dot);
                classShortName = superclass.substring(dot + 1);
            }
            Map<String, Object> map = new HashMap<String, Object>();
            map.put(CREATESUPERCLASS_KEY_PACKAGENAME, packageName);
            map.put(CREATESUPERCLASS_KEY_CLASSSHORTNAME, classShortName);
            String body;
            try {
                body = Activator.getDefault().evaluateTemplate(TEMPLATEPATH_SUPERCLASS, map);
            } catch (IOException ex) {
                throwCoreException(Messages.getString("NewProjectWizard.18") + TEMPLATEPATH_SUPERCLASS, ex); //$NON-NLS-1$
                return;
            }
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            Activator.getDefault().writeFile(file, body, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void setUpClasspath(IJavaProject javaProject, IPath jreContainerPath, IProgressMonitor monitor)
            throws CoreException {
        if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null) {
            setUpClasspathForM2Eclipse(javaProject, jreContainerPath, Globals.CLASSPATH_CONTAINER_M2ECLIPSE_LIGHT,
                    monitor);
        } else if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
            setUpClasspathForM2Eclipse(javaProject, jreContainerPath, Globals.CLASSPATH_CONTAINER_M2ECLIPSE, monitor);
        } else {
            setUpClasspathForNonM2Eclipse(javaProject, jreContainerPath, monitor);
        }
    }

    private void setUpClasspathForM2Eclipse(IJavaProject javaProject, IPath jreContainerPath,
            String m2EclipseContainerPath, IProgressMonitor monitor) throws JavaModelException {
        monitor.beginTask(Messages.getString("NewProjectWizard.23"), 1); //$NON-NLS-1$

        List<IClasspathEntry> newEntryList = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry entry : javaProject.getRawClasspath()) {
            int kind = entry.getEntryKind();
            if (kind == IClasspathEntry.CPE_LIBRARY || kind == IClasspathEntry.CPE_PROJECT
                    || kind == IClasspathEntry.CPE_VARIABLE || kind == IClasspathEntry.CPE_CONTAINER) {
                continue;
            }
            newEntryList.add(entry);
        }
        newEntryList.add(JavaCore.newContainerEntry(jreContainerPath));
        newEntryList.add(JavaCore.newContainerEntry(new Path(m2EclipseContainerPath)));

        javaProject.setRawClasspath(newEntryList.toArray(new IClasspathEntry[0]), new SubProgressMonitor(monitor, 1));
    }

    private void setUpClasspathForNonM2Eclipse(IJavaProject javaProject, IPath jreContainerPath,
            IProgressMonitor monitor) throws JavaModelException {
        monitor.beginTask(Messages.getString("NewProjectWizard.23"), 1); //$NON-NLS-1$
        try {
            List<IClasspathEntry> newEntryList = new ArrayList<IClasspathEntry>();
            for (IClasspathEntry entry : javaProject.getRawClasspath()) {
                int kind = entry.getEntryKind();
                IPath path = entry.getPath();
                if (kind == IClasspathEntry.CPE_CONTAINER) {
                    if (Globals.CLASSPATH_CONTAINER_M2ECLIPSE_LIGHT.equals(path.toPortableString())
                            || Globals.CLASSPATH_CONTAINER_M2ECLIPSE.equals(path.toPortableString())
                            || PATH_JRE_CONTAINER.equals(path.segment(0))) {
                        continue;
                    }
                }
                newEntryList.add(entry);
            }
            newEntryList.add(JavaCore.newContainerEntry(jreContainerPath));

            javaProject.setRawClasspath(newEntryList.toArray(new IClasspathEntry[0]),
                    new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void setUpProjectDescription(IProject project, ViliBehavior behavior, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.19"), 1); //$NON-NLS-1$
        try {
            IProjectDescription description = project.getDescription();

            List<String> newNatureList = new ArrayList<String>();
            List<ICommand> newBuilderList = new ArrayList<ICommand>();

            newNatureList.add(JavaCore.NATURE_ID);
            ICommand command = description.newCommand();
            command.setBuilderName(JavaCore.BUILDER_ID);
            newBuilderList.add(command);
            if (behavior.isProjectOf(ProjectType.WEB)) {
                if (Platform.getBundle(Globals.BUNDLENAME_TOMCATPLUGIN) != null) {
                    newNatureList.add(Globals.NATURE_ID_TOMCAT);
                }
                if (Platform.getBundle(Globals.BUNDLENAME_WEBLAUNCHER) != null) {
                    newNatureList.add(Globals.NATURE_ID_WEBLAUNCHER);
                }
                if (Platform.getBundle(Globals.BUNDLENAME_MAVEN2ADDITIONAL) != null) {
                    newNatureList.add(Globals.NATURE_ID_MAVEN2ADDITIONAL);
                }
            }
            if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null) {
                newNatureList.add(Globals.NATURE_ID_M2ECLIPSE_LIGHT);
            } else if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
                newNatureList.add(Globals.NATURE_ID_M2ECLIPSE);
                command = description.newCommand();
                command.setBuilderName(Globals.BUILDER_ID_M2ECLIPSE);
                newBuilderList.add(command);
            }

            newNatureList.add(ViliProjectNature.ID);
            if (behavior.isProjectOf(ProjectType.YMIR)) {
                newNatureList.add(YmirProjectNature.ID);
            }

            addNatures(description, newNatureList);
            addBuilders(description, newBuilderList);

            project.setDescription(description, new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }
        } finally {
            monitor.done();
        }
    }

    private void updateApplicationProperties(IProject project, MapProperties applicationProperties,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.20"), 1); //$NON-NLS-1$
        try {
            Activator.getDefault().mergeProperties(project.getFile(Globals.PATH_APP_PROPERTIES), applicationProperties,
                    new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void addBuilders(IProjectDescription description, List<ICommand> newBuilderList) {
        Map<String, ICommand> map = new LinkedHashMap<String, ICommand>();
        for (ICommand builder : description.getBuildSpec()) {
            map.put(builder.getBuilderName(), builder);
        }
        for (ICommand builder : newBuilderList) {
            map.put(builder.getBuilderName(), builder);
        }
        description.setBuildSpec(map.values().toArray(new ICommand[0]));
    }

    private void addNatures(IProjectDescription description, List<String> newNatureList) {
        Set<String> set = new LinkedHashSet<String>();
        set.addAll(Arrays.asList(description.getNatureIds()));
        set.addAll(newNatureList);
        description.setNatureIds(set.toArray(new String[0]));
    }

    private void throwCoreException(String message, Throwable cause) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, IStatus.OK, message, cause);
        throw new CoreException(status);
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    public void notifySkeletonCleared() {
        secondPage.notifySkeletonAndFragmentsCleared();
        thirdPage.notifySkeletonAndFragmentsCleared();
    }

    public ArtifactPair getSkeleton() {
        return firstPage.getSkeleton();
    }

    public ArtifactPair[] getFragments() {
        return firstPage.getFragments();
    }

    public void notifyFragmentsChanged() {
        thirdPage.notifyFragmentsChanged();
    }
}