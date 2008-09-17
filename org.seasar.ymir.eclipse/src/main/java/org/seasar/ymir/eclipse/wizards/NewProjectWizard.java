package org.seasar.ymir.eclipse.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
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
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.ParameterKeys;

import werkzeugkasten.mvnhack.repository.Artifact;

/**
 * This is a sample new wizard. Its role is to create a new file 
 * resource in the provided container. If the container resource
 * (a folder or a project) is selected in the workspace 
 * when the wizard is opened, it will accept it as the target
 * container. The wizard creates one file with the extension
 * "mpe". If a sample multi-page editor (also available
 * as a template) is registered for the same extension, it will
 * be able to open it.
 */

public class NewProjectWizard extends Wizard implements INewWizard {
    private static final String PATH_APP_PROPERTIES = Globals.PATH_SRC_MAIN_RESOURCES + "/app.properties"; //$NON-NLS-1$

    private static final String PLACEHOLDER_WEBAPP = "%WEBAPP%"; //$NON-NLS-1$

    private static final char PACKAGE_DELIMITER = '.';

    private static final String CREATESUPERCLASS_KEY_PACKAGENAME = "packageName"; //$NON-NLS-1$

    private static final String CREATESUPERCLASS_KEY_CLASSSHORTNAME = "classShortName"; //$NON-NLS-1$

    private static final String TEMPLATEPATH_SUPERCLASS = "templates/Superclass.java.ftl"; //$NON-NLS-1$

    private static final String PATH_DELIMITER = "/"; //$NON-NLS-1$

    private static final String PATH_JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    private static final Map<String, String> JRE_VERSION_MAP;

    private static final String PATH_JDT_CORE_PREFS = ".settings/org.eclipse.jdt.core.prefs"; //$NON-NLS-1$

    private static final String DEFAULT_COMPLIANCE = "1.3"; //$NON-NLS-1$

    private static final String BUNDLE_PATHPREFIX_JDT_CORE_PREFS = "/prefs/compliance/org.eclipse.jdt.core.prefs-"; //$NON-NLS-1$

    private NewProjectWizardFirstPage firstPage;

    private NewProjectWizardSecondPage secondPage;

    private NewProjectWizardThirdPage thirdPage;

    private NewProjectWizardFourthPage fourthPage;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("J2SE-1.3", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("J2SE-1.4", "1.4"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put(Messages.getString("NewProjectWizard.2"), "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("JavaSE-1.6", "1.6"); //$NON-NLS-1$ //$NON-NLS-2$
        JRE_VERSION_MAP = Collections.unmodifiableMap(map);
    }

    /**
     * Constructor for NewProjectWizard.
     */
    public NewProjectWizard() {
        super();
        setNeedsProgressMonitor(true);
        setWindowTitle(Messages.getString("NewProjectWizard.11")); //$NON-NLS-1$
    }

    /**
     * Adding the page to the wizard.
     */

    public void addPages() {
        firstPage = new NewProjectWizardFirstPage();
        addPage(firstPage);
        secondPage = new NewProjectWizardSecondPage();
        addPage(secondPage);
        thirdPage = new NewProjectWizardThirdPage();
        addPage(thirdPage);
        fourthPage = new NewProjectWizardFourthPage();
        addPage(fourthPage);
    }

    /**
     * This method is called when 'Finish' button is pressed in
     * the wizard. We will create an operation and run it
     * using wizard as execution context.
     */
    public boolean performFinish() {
        final IProject project = firstPage.getProjectHandle();
        final IPath locationPath = firstPage.getLocationPath();
        final IPath jreContainerPath = firstPage.getJREContainerPath();
        final Artifact skeletonArtifact = secondPage.getSkeletonArtifact();
        final Map<String, Object> parameterMap = createParameterMap();
        final MapProperties applicationProperties = createApplicationProperties();
        IRunnableWithProgress op = new IRunnableWithProgress() {
            public void run(IProgressMonitor monitor) throws InvocationTargetException {
                try {
                    createProject(project, locationPath, jreContainerPath, skeletonArtifact, parameterMap,
                            applicationProperties, monitor);
                } catch (CoreException e) {
                    throw new InvocationTargetException(e);
                } finally {
                    monitor.done();
                }
            }
        };
        try {
            getContainer().run(true, false, op);
        } catch (InterruptedException e) {
            return false;
        } catch (InvocationTargetException e) {
            Throwable realException = e.getTargetException();
            MessageDialog.openError(getShell(), "Error", realException.getMessage()); //$NON-NLS-1$
            return false;
        }
        return true;
    }

    private Map<String, Object> createParameterMap() {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ParameterKeys.SLASH, PATH_DELIMITER);
        map.put(ParameterKeys.PROJECT_NAME, firstPage.getProjectName());
        map.put(ParameterKeys.ROOT_PACKAGE_NAME, firstPage.getRootPackageName());
        map.put(ParameterKeys.ROOT_PACKAGE_PATH, firstPage.getRootPackageName().replace('.', '/'));
        map.put(ParameterKeys.GROUP_ID, firstPage.getProjectGroupId());
        map.put(ParameterKeys.ARTIFACT_ID, firstPage.getProjectArtifactId());
        map.put(ParameterKeys.VERSION, firstPage.getProjectVersion());
        map.put(ParameterKeys.JRE_VERSION, getJREVersion(firstPage.getJREContainerPath()));
        map.put(ParameterKeys.VIEW_ENCODING, thirdPage.getViewEncoding());
        map.put(ParameterKeys.USE_DATABASE, thirdPage.isUseDatabase());
        map.put(ParameterKeys.DATABASE_DRIVER_CLASS_NAME, thirdPage.getDatabaseDriverClassName());
        map.put(ParameterKeys.DATABASE_URL, resolveDatabaseURL(thirdPage.getDatabaseURL()));
        map.put(ParameterKeys.DATABASE_URL_FOR_YMIR, resolveDatabaseURLForYmir(thirdPage.getDatabaseURL()));
        map.put(ParameterKeys.DATABASE_USER, thirdPage.getDatabaseUser());
        map.put(ParameterKeys.DATABASE_PASSWORD, thirdPage.getDatabasePassword());

        return map;
    }

    private String resolveDatabaseURL(String databaseURL) {
        return databaseURL.replace(PLACEHOLDER_WEBAPP, "../src/main/webapp"); //$NON-NLS-1$
    }

    private String resolveDatabaseURLForYmir(String databaseURL) {
        int placeHolder = databaseURL.indexOf(PLACEHOLDER_WEBAPP);
        if (placeHolder < 0) {
            return "\"" + databaseURL + "\""; //$NON-NLS-1$ //$NON-NLS-2$
        } else {
            return "\"" + databaseURL.substring(0, placeHolder) + "\" + application.getRealPath(\"\") + \"" //$NON-NLS-1$ //$NON-NLS-2$
                    + databaseURL.substring(placeHolder + PLACEHOLDER_WEBAPP.length()) + "\""; //$NON-NLS-1$
        }
    }

    private MapProperties createApplicationProperties() {
        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        prop.setProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME, firstPage.getRootPackageName());
        String value = fourthPage.getSuperclass();
        if (value != null && value.length() > 0) {
            prop.setProperty(ApplicationPropertiesKeys.SUPERCLASS, value);
        }
        prop.setProperty(ApplicationPropertiesKeys.USING_FREYJA_RENDER_CLASS, String.valueOf(fourthPage
                .isUsingFreyjaRenderClass()));
        prop.setProperty(ApplicationPropertiesKeys.BEANTABLE_ENABLED, String.valueOf(fourthPage.isBeantableEnabled()));
        prop.setProperty(ApplicationPropertiesKeys.FORM_DTO_CREATION_FEATURE_ENABLED, String.valueOf(fourthPage
                .isFormDtoCreationFeatureEnabled()));
        prop.setProperty(ApplicationPropertiesKeys.CONVERTER_CREATION_FEATURE_ENABLED, String.valueOf(fourthPage
                .isConverterCreationFeatureEnabled()));
        prop.setProperty(ApplicationPropertiesKeys.DAO_CREATION_FEATURE_ENABLED, String.valueOf(fourthPage
                .isDaoCreationFeatureEnabled()));
        prop.setProperty(ApplicationPropertiesKeys.DXO_CREATION_FEATURE_ENABLED, String.valueOf(fourthPage
                .isDxoCreationFeatureEnabled()));
        boolean eclipseEnabled = fourthPage.isEclipseEnabled();
        prop.setProperty(ApplicationPropertiesKeys.ECLIPSE_ENABLED, String.valueOf(eclipseEnabled));
        if (eclipseEnabled) {
            value = fourthPage.getResourceSynchronizerURL();
            if (value != null && value.length() > 0) {
                prop.setProperty(ApplicationPropertiesKeys.RESOURCE_SYNCHRONIZER_URL, value);
            }
        }

        return prop;
    }

    private void createProject(IProject project, IPath locationPath, IPath jreContainerPath, Artifact skeletonArtifact,
            Map<String, Object> parameterMap, MapProperties applicationProperties, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.12"), 8); //$NON-NLS-1$
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

            try {
                Activator.getDefault().expandSkeleton(project, skeletonArtifact, parameterMap,
                        new SubProgressMonitor(monitor, 1));
            } catch (IOException ex) {
                throwCoreException(Messages.getString("NewProjectWizard.13"), ex); //$NON-NLS-1$
                return;
            }
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            setUpCompliance(project, (String) parameterMap.get(ParameterKeys.JRE_VERSION), new SubProgressMonitor(
                    monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            updateApplicationProperties(project, applicationProperties, new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            createSuperclass(project, applicationProperties.getProperty(ApplicationPropertiesKeys.SUPERCLASS),
                    new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            setUpProjectDescription(project, new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            IJavaProject javaProject = JavaCore.create(project);

            setUpClasspath(javaProject, jreContainerPath, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void setUpCompliance(IProject project, String jreVersion, IProgressMonitor monitor) throws CoreException {
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

    private String getJREVersion(IPath jreContainerPath) {
        String version = JRE_VERSION_MAP.get(jreContainerPath.lastSegment());
        if (version != null) {
            return version;
        } else {
            return ""; //$NON-NLS-1$
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
        if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
            setUpClasspathForM2Eclipse(javaProject, jreContainerPath, monitor);
        } else {
            setUpClasspathForNonM2Eclipse(javaProject, jreContainerPath, monitor);
        }
    }

    private void setUpClasspathForM2Eclipse(IJavaProject javaProject, IPath jreContainerPath, IProgressMonitor monitor)
            throws JavaModelException {
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
        newEntryList.add(JavaCore.newContainerEntry(new Path(Globals.CLASSPATH_CONTAINER_M2ECLIPSE)));

        javaProject.setRawClasspath(newEntryList.toArray(new IClasspathEntry[0]), new SubProgressMonitor(monitor, 1));
    }

    private void setUpClasspathForNonM2Eclipse(IJavaProject javaProject, IPath jreContainerPath,
            IProgressMonitor monitor) throws JavaModelException {
        monitor.beginTask(Messages.getString("NewProjectWizard.23"), 1); //$NON-NLS-1$

        List<IClasspathEntry> newEntryList = new ArrayList<IClasspathEntry>();
        for (IClasspathEntry entry : javaProject.getRawClasspath()) {
            int kind = entry.getEntryKind();
            IPath path = entry.getPath();
            if (kind == IClasspathEntry.CPE_CONTAINER
                    && (Globals.CLASSPATH_CONTAINER_M2ECLIPSE.equals(path.toPortableString()) || PATH_JRE_CONTAINER
                            .equals(path.segment(0)))) {
                continue;
            }
            newEntryList.add(entry);
        }
        newEntryList.add(JavaCore.newContainerEntry(jreContainerPath));

        javaProject.setRawClasspath(newEntryList.toArray(new IClasspathEntry[0]), new SubProgressMonitor(monitor, 1));
    }

    private void setUpProjectDescription(IProject project, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.19"), 1); //$NON-NLS-1$
        try {
            IProjectDescription description = project.getDescription();

            List<String> newNatureList = new ArrayList<String>();
            List<ICommand> newBuilderList = new ArrayList<ICommand>();

            newNatureList.add(JavaCore.NATURE_ID);
            ICommand command = description.newCommand();
            command.setBuilderName(JavaCore.BUILDER_ID);
            newBuilderList.add(command);
            if (Platform.getBundle(Globals.BUNDLENAME_TOMCATPLUGIN) != null) {
                newNatureList.add(Globals.NATURE_ID_TOMCAT);
            }
            if (Platform.getBundle(Globals.BUNDLENAME_WEBLAUNCHER) != null) {
                newNatureList.add(Globals.NATURE_ID_WEBLAUNCHER);
            }
            if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
                newNatureList.add(Globals.NATURE_ID_M2ECLIPSE);
                command = description.newCommand();
                command.setBuilderName(Globals.BUILDER_ID_M2ECLIPSE);
                newBuilderList.add(command);
            }
            if (Platform.getBundle(Globals.BUNDLENAME_MAVEN2ADDITIONAL) != null) {
                newNatureList.add(Globals.NATURE_ID_MAVEN2ADDITIONAL);
                command = description.newCommand();
                command.setBuilderName(Globals.BUILDER_ID_WEBINFLIB);
                newBuilderList.add(command);
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
            IFile file = project.getFile(PATH_APP_PROPERTIES);
            if (!file.exists()) {
                return;
            }

            Activator.getDefault().mergeProperties(file, applicationProperties, new SubProgressMonitor(monitor, 1));
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

    private void throwCoreException(String message) throws CoreException {
        throwCoreException(message, null);
    }

    private void throwCoreException(String message, Throwable cause) throws CoreException {
        IStatus status = new Status(IStatus.ERROR, "org.seasar.ymir.eclipse", IStatus.OK, message, cause); //$NON-NLS-1$
        throw new CoreException(status);
    }

    /**
     * We will accept the selection in the workbench to see if
     * we can initialize from it.
     * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
     */
    public void init(IWorkbench workbench, IStructuredSelection selection) {
    }

    public String getRootPackageName() {
        return firstPage.getRootPackageName();
    }
}