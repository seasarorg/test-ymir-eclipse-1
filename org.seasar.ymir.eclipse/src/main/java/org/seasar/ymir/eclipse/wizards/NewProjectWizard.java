package org.seasar.ymir.eclipse.wizards;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XMLParser;
import net.skirnir.xom.XOMapper;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
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
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.HotdeployType;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.PlatformDelegate;
import org.seasar.ymir.eclipse.ViliBehavior;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.maven.Dependencies;
import org.seasar.ymir.eclipse.maven.Dependency;
import org.seasar.ymir.eclipse.maven.ExtendedContext;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;
import org.seasar.ymir.eclipse.util.StreamUtils;

import werkzeugkasten.mvnhack.repository.Artifact;

public class NewProjectWizard extends Wizard implements INewWizard {
    private static final String PATH_APP_PROPERTIES = Globals.PATH_SRC_MAIN_RESOURCES + "/app.properties"; //$NON-NLS-1$

    private static final String PLACEHOLDER_WEBAPP = "%WEBAPP%"; //$NON-NLS-1$

    private static final char PACKAGE_DELIMITER = '.';

    private static final String CREATESUPERCLASS_KEY_PACKAGENAME = "packageName"; //$NON-NLS-1$

    private static final String CREATESUPERCLASS_KEY_CLASSSHORTNAME = "classShortName"; //$NON-NLS-1$

    private static final String TEMPLATEPATH_SUPERCLASS = "templates/Superclass.java.ftl"; //$NON-NLS-1$

    private static final String PATH_JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    private static final Map<String, String> JRE_VERSION_MAP;

    private static final String PATH_JDT_CORE_PREFS = ".settings/org.eclipse.jdt.core.prefs"; //$NON-NLS-1$

    private static final String DEFAULT_COMPLIANCE = "1.3"; //$NON-NLS-1$

    private static final String BUNDLE_PATHPREFIX_JDT_CORE_PREFS = "/prefs/compliance/org.eclipse.jdt.core.prefs-"; //$NON-NLS-1$

    static final String REQUIRED_TEMPLATE = Messages.getString("NewProjectWizard.2"); //$NON-NLS-1$

    static final String DS_SECTION = "NewProjectWizard"; //$NON-NLS-1$

    private NewProjectWizardFirstPage firstPage;

    private NewProjectWizardSecondPage secondPage;

    private NewProjectWizardThirdPage thirdPage;

    private ExtendedContext nonTransitiveContext;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("J2SE-1.3", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("J2SE-1.4", "1.4"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("J2SE-1.5", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
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
        setDefaultPageImageDescriptor(Activator.getDefault().getImageRegistry().getDescriptor(Globals.IMAGE_YMIR));

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
        firstPage = new NewProjectWizardFirstPage();
        addPage(firstPage);
        secondPage = new NewProjectWizardSecondPage();
        addPage(secondPage);
        thirdPage = new NewProjectWizardThirdPage();
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
        firstPage.putDialogSettings();
        try {
            final ArtifactPair[] skeletonAndFragments = firstPage.getSkeletonAndFragments();
            final IProject project = secondPage.getProjectHandle();
            final IPath locationPath = secondPage.getLocationPath();
            final IPath jreContainerPath = secondPage.getJREContainerPath();
            thirdPage.populateSkeletonParameters();
            final Dependency[] dependencies = createDependencies(thirdPage.getDatabaseEntry().getDependency(),
                    skeletonAndFragments);
            final Map<String, Object> parameterMap = createParameterMap(dependencies);
            final MapProperties applicationProperties = createApplicationProperties();
            IRunnableWithProgress op = new IRunnableWithProgress() {
                public void run(IProgressMonitor monitor) throws InvocationTargetException {
                    try {
                        createProject(project, locationPath, jreContainerPath, skeletonAndFragments, parameterMap,
                                dependencies, applicationProperties, monitor);
                    } catch (CoreException e) {
                        throw new InvocationTargetException(e);
                    } finally {
                        monitor.done();
                    }
                }
            };

            getContainer().run(true, false, op);
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

    private Dependency[] createDependencies(Dependency databaseDependency, ArtifactPair[] skeletonAndFragments)
            throws CoreException {
        Activator activator = Activator.getDefault();
        XOMapper mapper = activator.getXOMapper();
        XMLParser parser = activator.getXMLParser();

        Map<Dependency, Dependency> map = new LinkedHashMap<Dependency, Dependency>();

        if (databaseDependency != null) {
            add(map, databaseDependency);
        }

        // フラグメントだけを対象にするため1から始めている。
        for (int i = 1; i < skeletonAndFragments.length; i++) {
            String text = activator.getResourceAsString(skeletonAndFragments[i].getArtifact(),
                    Globals.VILI_DEPENDENCIES_XML, Globals.ENCODING, new NullProgressMonitor());
            if (text != null) {
                Dependencies dependencies;
                try {
                    dependencies = mapper.toBean(parser.parse(new StringReader(text)).getRootElement(),
                            Dependencies.class);
                } catch (Throwable t) {
                    throwCoreException(
                            "Can't read " + Globals.VILI_DEPENDENCIES_XML + " in " + skeletonAndFragments[i], t); //$NON-NLS-1$ //$NON-NLS-2$
                    return null;
                }
                for (Dependency dep : dependencies.getDependencies()) {
                    add(map, dep);
                }
            }
        }

        return map.values().toArray(new Dependency[0]);
    }

    private void add(Map<Dependency, Dependency> map, Dependency dep) {
        Dependency d = map.get(dep);
        if (d == null || ArtifactUtils.compareVersions(d.getVersion(), dep.getVersion()) < 0) {
            map.put(dep, dep);
        }
    }

    private Map<String, Object> createParameterMap(Dependency[] dependencies) {
        Map<String, Object> map = new HashMap<String, Object>();
        map.put(ParameterKeys.SLASH, "/"); //$NON-NLS-1$
        map.put(ParameterKeys.DOLLAR, "$"); //$NON-NLS-1$
        map.put(ParameterKeys.PROJECT_NAME, secondPage.getProjectName());
        map.put(ParameterKeys.ROOT_PACKAGE_NAME, secondPage.getRootPackageName());
        map.put(ParameterKeys.ROOT_PACKAGE_PATH, secondPage.getRootPackageName().replace('.', '/'));
        map.put(ParameterKeys.GROUP_ID, secondPage.getProjectGroupId());
        map.put(ParameterKeys.ARTIFACT_ID, secondPage.getProjectArtifactId());
        map.put(ParameterKeys.VERSION, secondPage.getProjectVersion());
        map.put(ParameterKeys.JRE_VERSION, getJREVersion(secondPage.getJREContainerPath()));
        map.put(ParameterKeys.VIEW_ENCODING, thirdPage.getViewEncoding());
        map.put(ParameterKeys.USE_DATABASE, thirdPage.isUseDatabase());
        DatabaseEntry entry = thirdPage.getDatabaseEntry();
        map.put(ParameterKeys.DATABASE_TYPE, entry.getType());
        map.put(ParameterKeys.DATABASE_DRIVER_CLASS_NAME, entry.getDriverClassName());
        map.put(ParameterKeys.DATABASE_URL, resolveDatabaseURL(entry.getURL()));
        map.put(ParameterKeys.DATABASE_URL_FOR_YMIR, resolveDatabaseURLForYmir(entry.getURL()));
        map.put(ParameterKeys.DATABASE_USER, entry.getUser());
        map.put(ParameterKeys.DATABASE_PASSWORD, entry.getPassword());
        map.put(ParameterKeys.DEPENDENCIES, getDependenciesString(dependencies));
        map.put(ParameterKeys.PLATFORM, new PlatformDelegate());

        YmirConfigurationBlock ymirConfig = thirdPage.getYmirConfigurationBlock();
        if (ymirConfig != null) {
            map.put(ParameterKeys.USE_RESOURCE_SYNCHRONIZER, ymirConfig.isEclipseEnabled());
            map.put(ParameterKeys.RESOURCE_SYNCHRONIZER_URL, ymirConfig.getResourceSynchronizerURL());
            map.put(ParameterKeys.HOTDEPLOY_TYPE, ymirConfig.getHotdeployType().getName());
            String fieldPrefix = ymirConfig.getFieldPrefix();
            map.put(ParameterKeys.FIELD_PREFIX, fieldPrefix);
            String fieldSuffix = ymirConfig.getFieldSuffix();
            map.put(ParameterKeys.FIELD_SUFFIX, fieldSuffix);
            map.put(ParameterKeys.FIELD_SPECIAL_PREFIX,
                    fieldPrefix.length() == 0 && fieldSuffix.length() == 0 ? "this." : ""); //$NON-NLS-1$ //$NON-NLS-2$
        }

        return map;
    }

    private String getDependenciesString(Dependency[] dependencies) {
        XOMapper mapper = Activator.getDefault().getXOMapper();
        StringBuilder sb = new StringBuilder();
        for (Dependency dependency : dependencies) {
            StringWriter sw = new StringWriter();
            try {
                mapper.toXML(dependency, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException("May logic error", ex); //$NON-NLS-1$
            } catch (IOException ex) {
                throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
            }
            sb.append(sw.toString());
        }
        return sb.toString();
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

    private MapProperties createApplicationProperties() throws CoreException {
        YmirConfigurationBlock ymirConfig = thirdPage.getYmirConfigurationBlock();
        if (ymirConfig == null) {
            return null;
        }

        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        prop.setProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME, secondPage.getRootPackageName());
        String value = ymirConfig.getSuperclass();
        if (value.length() > 0) {
            prop.setProperty(ApplicationPropertiesKeys.SUPERCLASS, value);
        }
        prop.setProperty(ApplicationPropertiesKeys.SOURCECREATOR_ENABLE, String.valueOf(ymirConfig
                .isAutoGenerationEnabled()));
        String fieldPrefix = ymirConfig.getFieldPrefix();
        prop.setProperty(ApplicationPropertiesKeys.FIELDPREFIX, fieldPrefix);
        String fieldSuffix = ymirConfig.getFieldSuffix();
        prop.setProperty(ApplicationPropertiesKeys.FIELDSUFFIX, fieldSuffix);
        prop.setProperty(ApplicationPropertiesKeys.FIELDSPECIALPREFIX, fieldPrefix.length() == 0
                && fieldSuffix.length() == 0 ? "this." : ""); //$NON-NLS-1$ //$NON-NLS-2$
        prop.setProperty(ApplicationPropertiesKeys.ENABLEINPLACEEDITOR, String.valueOf(ymirConfig
                .isInplaceEditorEnabled()));
        prop.setProperty(ApplicationPropertiesKeys.ENABLECONTROLPANEL, String.valueOf(ymirConfig
                .isControlPanelEnabled()));
        prop.setProperty(ApplicationPropertiesKeys.USING_FREYJA_RENDER_CLASS, String.valueOf(ymirConfig
                .isUsingFreyjaRenderClass()));
        prop.setProperty(ApplicationPropertiesKeys.BEANTABLE_ENABLED, String.valueOf(ymirConfig.isBeantableEnabled()));
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

        prop.setProperty(ApplicationPropertiesKeys.S2CONTAINER_CLASSLOADING_DISABLEHOTDEPLOY, String.valueOf(ymirConfig
                .getHotdeployType() != HotdeployType.S2));
        prop.setProperty(ApplicationPropertiesKeys.S2CONTAINER_COMPONENTREGISTRATION_DISABLEDYNAMIC, String
                .valueOf(ymirConfig.getHotdeployType() == HotdeployType.VOID));

        return prop;
    }

    private void createProject(IProject project, IPath locationPath, IPath jreContainerPath,
            ArtifactPair[] skeletonAndFragments, Map<String, Object> parameterMap, Dependency[] dependencies,
            MapProperties applicationProperties, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("NewProjectWizard.12"), 8 + skeletonAndFragments.length); //$NON-NLS-1$
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

            ViliBehavior behavior = skeletonAndFragments[0].getBehavior();

            for (ArtifactPair pair : skeletonAndFragments) {
                try {
                    Activator.getDefault().expandSkeleton(project, pair, parameterMap,
                            new SubProgressMonitor(monitor, 1));
                } catch (IOException ex) {
                    throwCoreException(Messages.getString("NewProjectWizard.13"), ex); //$NON-NLS-1$
                    return;
                }
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            }

            IPath[] dependencyPaths = copyDependencies(project, dependencies, new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            if (behavior.isJavaProject()) {
                setUpJRECompliance(project, (String) parameterMap.get(ParameterKeys.JRE_VERSION),
                        new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            } else {
                monitor.worked(1);
            }

            if (applicationProperties != null) {
                updateApplicationProperties(project, applicationProperties, new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                // createSuperclass(project, applicationProperties.getProperty(ApplicationPropertiesKeys.SUPERCLASS),
                // new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            } else {
                monitor.worked(2);
            }

            if (behavior.isJavaProject()) {
                IJavaProject javaProject = JavaCore.create(project);

                setUpClasspath(javaProject, jreContainerPath, dependencyPaths, new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                setUpProjectDescription(project, new SubProgressMonitor(monitor, 1));
            } else {
                monitor.worked(2);
            }
        } finally {
            monitor.done();
        }
    }

    private IPath[] copyDependencies(IProject project, Dependency[] dependencies, IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("NewProjectWizard.1"), dependencies.length); //$NON-NLS-1$
        try {
            if (Activator.getDefault().libsAreManagedAutomatically()) {
                return new IPath[0];
            }

            List<IPath> list = new ArrayList<IPath>();
            for (Dependency dependency : dependencies) {
                IPath dependencyPath = copyDependency(project, dependency, new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
                if (dependencyPath != null) {
                    list.add(dependencyPath);
                }
            }
            return list.toArray(new IPath[0]);
        } finally {
            monitor.done();
        }
    }

    private IPath copyDependency(IProject project, Dependency dependency, IProgressMonitor monitor) {
        monitor.beginTask(Messages.getString("NewProjectWizard.1"), 2); //$NON-NLS-1$
        try {
            Activator activator = Activator.getDefault();
            ArtifactResolver artifactResolver = activator.getArtifactResolver();

            IPath path = null;
            try {
                Artifact artifact = artifactResolver.resolve(getNonTransitiveContext(), dependency.getGroupId(),
                        dependency.getArtifactId(), dependency.getVersion());
                monitor.worked(1);
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                URL url = artifactResolver.getURL(artifact);
                IFile file = project.getFile(Globals.PATH_SRC_MAIN_WEBAPP_WEBINF_LIB
                        + "/" + ArtifactUtils.getFileName(artifact)); //$NON-NLS-1$ 
                InputStream is = null;
                try {
                    activator.writeFile(file, url.openStream(), new SubProgressMonitor(monitor, 1));
                } finally {
                    StreamUtils.close(is);
                }
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                path = file.getFullPath();
            } catch (Throwable ignore) {
            }

            return path;
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

    private String getJREVersion(IPath jreContainerPath) {
        String version = JRE_VERSION_MAP.get(jreContainerPath.lastSegment());
        if (version != null) {
            return version;
        } else {
            return "1.6"; //$NON-NLS-1$
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

    private void setUpClasspath(IJavaProject javaProject, IPath jreContainerPath, IPath[] dependencyPaths,
            IProgressMonitor monitor) throws CoreException {
        if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null) {
            setUpClasspathForM2Eclipse(javaProject, jreContainerPath, Globals.CLASSPATH_CONTAINER_M2ECLIPSE_LIGHT,
                    monitor);
        } else if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
            setUpClasspathForM2Eclipse(javaProject, jreContainerPath, Globals.CLASSPATH_CONTAINER_M2ECLIPSE, monitor);
        } else {
            setUpClasspathForNonM2Eclipse(javaProject, jreContainerPath, dependencyPaths, monitor);
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
            IPath[] dependencyPaths, IProgressMonitor monitor) throws JavaModelException {
        monitor.beginTask(Messages.getString("NewProjectWizard.23"), 1); //$NON-NLS-1$
        try {
            Set<String> existsSet = new HashSet<String>();
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
                } else if (kind == IClasspathEntry.CPE_LIBRARY) {
                    existsSet.add(ArtifactUtils.getArtifactId(path.toPortableString()));
                }
                newEntryList.add(entry);
            }
            newEntryList.add(JavaCore.newContainerEntry(jreContainerPath));

            for (IPath dependencyPath : dependencyPaths) {
                if (!existsSet.contains(ArtifactUtils.getArtifactId(dependencyPath.toPortableString()))) {
                    newEntryList.add(JavaCore.newLibraryEntry(dependencyPath, null, null));
                }
            }

            javaProject.setRawClasspath(newEntryList.toArray(new IClasspathEntry[0]),
                    new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
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
            if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null) {
                newNatureList.add(Globals.NATURE_ID_M2ECLIPSE_LIGHT);
            } else if (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
                newNatureList.add(Globals.NATURE_ID_M2ECLIPSE);
                command = description.newCommand();
                command.setBuilderName(Globals.BUILDER_ID_M2ECLIPSE);
                newBuilderList.add(command);
            }
            if (Platform.getBundle(Globals.BUNDLENAME_MAVEN2ADDITIONAL) != null) {
                newNatureList.add(Globals.NATURE_ID_MAVEN2ADDITIONAL);
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
            Activator.getDefault().mergeProperties(project.getFile(PATH_APP_PROPERTIES), applicationProperties,
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

    private void throwCoreException(String message) throws CoreException {
        throwCoreException(message, null);
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

    public String getRootPackageName() {
        return secondPage.getRootPackageName();
    }

    public void notifySkeletonCleared() {
        secondPage.notifySkeletonAndFragmentsCleared();
        thirdPage.notifySkeletonAndFragmentsCleared();
    }

    public ArtifactPair[] getSkeletonAndFragments() {
        return firstPage.getSkeletonAndFragments();
    }
}