package org.seasar.ymir.eclipse.impl;

import static org.seasar.ymir.vili.Globals.PATH_POM_XML;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jdt.core.IClasspathEntry;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.osgi.framework.Bundle;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.Messages;
import org.seasar.ymir.eclipse.maven.util.ArtifactUtils;
import org.seasar.ymir.eclipse.maven.util.MavenUtils;
import org.seasar.ymir.eclipse.natures.ViliProjectNature;
import org.seasar.ymir.eclipse.natures.YmirProjectNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.util.BeanMap;
import org.seasar.ymir.eclipse.util.CascadeMap;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.eclipse.util.XOMUtils;
import org.seasar.ymir.vili.ArtifactPair;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.ProjectBuilder;
import org.seasar.ymir.vili.ProjectType;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.model.Action;
import org.seasar.ymir.vili.model.Actions;
import org.seasar.ymir.vili.model.maven.Dependencies;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.PluginRepositories;
import org.seasar.ymir.vili.model.maven.PluginRepository;
import org.seasar.ymir.vili.model.maven.Profile;
import org.seasar.ymir.vili.model.maven.Profiles;
import org.seasar.ymir.vili.model.maven.Project;
import org.seasar.ymir.vili.model.maven.Repositories;
import org.seasar.ymir.vili.model.maven.Repository;

import werkzeugkasten.mvnhack.repository.Artifact;
import freemarker.cache.TemplateLoader;
import freemarker.cache.URLTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.DefaultObjectWrapper;
import freemarker.template.TemplateException;

public class ProjectBuilderImpl implements ProjectBuilder {
    private static final char PACKAGE_DELIMITER = '.';

    private static final String BUNDLE_PATHPREFIX_JDT_CORE_PREFS = "/prefs/compliance/org.eclipse.jdt.core.prefs-"; //$NON-NLS-1$

    private static final String PATH_JRE_CONTAINER = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    private static final String PATH_JDT_CORE_PREFS = ".settings/org.eclipse.jdt.core.prefs"; //$NON-NLS-1$

    private static final String CREATESUPERCLASS_KEY_PACKAGENAME = "packageName"; //$NON-NLS-1$

    private static final String CREATESUPERCLASS_KEY_CLASSSHORTNAME = "classShortName"; //$NON-NLS-1$

    private static final String TEMPLATEPATH_SUPERCLASS = "templates/Superclass.java.ftl"; //$NON-NLS-1$

    private static final String EXTENSION_PROPERTIES = "properties"; //$NON-NLS-1$

    private static final String EXTENSION_XPROPERTIES = "xproperties"; //$NON-NLS-1$

    private static final String EXTENSION_PREFS = "prefs";

    private Bundle bundle;

    private ViliBehavior systemBehavior;

    private Configuration cfg;

    public ProjectBuilderImpl(Bundle bundle) throws CoreException {
        this.bundle = bundle;
        try {
            readSystemBehavior();
        } catch (IOException ex) {
            Activator.getDefault().throwCoreException("Can't read system behavior", ex);
            return;
        }
        setUpTemplateEngine();
    }

    private void readSystemBehavior() throws IOException {
        systemBehavior = new ViliBehaviorImpl(getClass().getResource(Globals.BEHAVIOR_PROPERTIES));
    }

    private void setUpTemplateEngine() {
        cfg = new Configuration();
        cfg.setLocalizedLookup(false);
        cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
        cfg.setTemplateLoader(new URLTemplateLoader() {
            protected URL getURL(String path) {
                return bundle.getEntry(path);
            }
        });
    }

    public void addFragments(IProject project, ViliProjectPreferences preferences, ArtifactPair[] fragments,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.8"), fragments.length + 1); //$NON-NLS-1$
        try {
            Project pom = new Project();
            Dependencies dependencies = new Dependencies();
            pom.setDependencies(dependencies);
            Repositories repositories = new Repositories();
            pom.setRepositories(repositories);
            PluginRepositories pluginRepositories = new PluginRepositories();
            pom.setPluginRepositories(pluginRepositories);
            Profiles profiles = new Profiles();
            pom.setProfiles(profiles);

            IPreferenceStore store = Activator.getDefault().getPreferenceStore(project);
            Actions actions = XOMUtils.getAsBean(store.getString(PreferenceConstants.P_ACTIONS), Actions.class);
            if (actions == null) {
                actions = new Actions();
            }

            for (ArtifactPair fragment : fragments) {
                Artifact artifact = fragment.getArtifact();
                ViliBehavior behavior = fragment.getBehavior();
                @SuppressWarnings("unchecked")//$NON-NLS-1$
                Map<String, Object> parameters = new CascadeMap<String, Object>(new HashMap<String, Object>(), fragment
                        .getParameterMap(), new BeanMap(preferences));

                try {
                    expandArtifact(project, preferences, fragment, parameters, new SubProgressMonitor(monitor, 1));
                } catch (IOException ex) {
                    Activator.getDefault().throwCoreException("Failed to add fragments", ex); //$NON-NLS-1$
                    return;
                }
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }

                Project fPom = fragment.getBehavior().getPom();
                if (fPom.getDependencies() != null) {
                    for (Dependency fDependency : fPom.getDependencies().getDependencies()) {
                        dependencies.addDependency(fDependency);
                    }
                }
                if (fPom.getRepositories() != null) {
                    for (Repository fRepository : fPom.getRepositories().getRepositories()) {
                        repositories.addRepository(fRepository);
                    }
                }
                if (fPom.getPluginRepositories() != null) {
                    for (PluginRepository fPluignRepository : fPom.getPluginRepositories().getPluginRepositories()) {
                        pluginRepositories.addPluginRepository(fPluignRepository);
                    }
                }
                if (fPom.getProfiles() != null) {
                    for (Profile fProfile : fPom.getProfiles().getProfiles()) {
                        profiles.addProfile(fProfile);
                    }
                }

                Actions fragmentActions = behavior.getActions();
                if (fragmentActions != null) {
                    List<Action> actionList = new ArrayList<Action>();
                    for (Action action : actions.getActions()) {
                        if (action.getGroupId().equals(artifact.getGroupId())
                                && action.getArtifactId().equals(artifact.getArtifactId())) {
                            continue;
                        }
                        actionList.add(action);
                    }
                    for (Action action : fragmentActions.getActions()) {
                        actionList.add(action);
                    }
                    actions.setActions(actionList.toArray(new Action[0]));
                }
            }

            MavenUtils.updatePom(project, pom, new SubProgressMonitor(monitor, 1));
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            try {
                StringWriter sw = new StringWriter();
                XOMUtils.getXOMapper().toXML(actions, sw);
                store.putValue(PreferenceConstants.P_ACTIONS, sw.toString());
                ((IPersistentPreferenceStore) store).save();
            } catch (Throwable t) {
                Activator.getDefault().log(t);
            }
        } finally {
            monitor.done();
        }
    }

    public void createProject(IProject project, IPath locationPath, IPath jreContainerPath, ArtifactPair skeleton,
            ViliProjectPreferences preferences, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.12"), 12); //$NON-NLS-1$
        try {
            Activator activator = Activator.getDefault();

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
                @SuppressWarnings("unchecked")//$NON-NLS-1$
                Map<String, Object> parameters = new CascadeMap<String, Object>(skeleton.getParameterMap(),
                        new BeanMap(preferences));
                expandArtifact(project, preferences, skeleton, parameters, new SubProgressMonitor(monitor, 1));
            } catch (IOException ex) {
                Activator.getDefault().throwCoreException(Messages.getString("ProjectBuilderImpl.13"), ex); //$NON-NLS-1$
                return;
            }
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            addDatabaseDependenciesToPom(project, preferences, new SubProgressMonitor(monitor, 1));

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

                setUpClasspath(javaProject, behavior, jreContainerPath, new SubProgressMonitor(monitor, 1));
                if (monitor.isCanceled()) {
                    throw new OperationCanceledException();
                }
            } else {
                monitor.worked(3);
            }

            Actions actions = behavior.getActions();
            if (actions != null) {
                IPreferenceStore store = activator.getPreferenceStore(project);
                try {
                    StringWriter sw = new StringWriter();
                    XOMUtils.getXOMapper().toXML(actions, sw);
                    store.putValue(org.seasar.ymir.eclipse.preferences.PreferenceConstants.P_ACTIONS, sw.toString());
                    ((IPersistentPreferenceStore) store).save();
                } catch (Throwable t) {
                    activator.log(t);
                }
            } else {
                monitor.worked(1);
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

            preferences.save(project);
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    private void addDatabaseDependenciesToPom(IProject project, ViliProjectPreferences preferences,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.28"), 1); //$NON-NLS-1$
        try {
            Dependency databaseDependency = preferences.getDatabase().getDependency();
            if (databaseDependency != null) {
                Project pom = new Project();
                Dependencies dependencies = new Dependencies();
                dependencies.addDependency(databaseDependency);
                pom.setDependencies(dependencies);
                MavenUtils.updatePom(project, pom, monitor);
            }
        } finally {
            monitor.done();
        }
    }

    private void setUpJRECompliance(IProject project, String jreVersion, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.25"), 1); //$NON-NLS-1$
        try {
            if (jreVersion.length() == 0) {
                return;
            }
            URL entry = Activator.getDefault().getBundle().getEntry(BUNDLE_PATHPREFIX_JDT_CORE_PREFS + jreVersion);
            if (entry != null) {
                mergeProperties(project.getFile(PATH_JDT_CORE_PREFS), entry, new SubProgressMonitor(monitor, 1));
            }
        } finally {
            monitor.done();
        }
    }

    private void setUpProjectDescription(IProject project, ViliBehavior behavior, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.19"), 1); //$NON-NLS-1$
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
                if (behavior.isTieUpWithBundle(Globals.BUNDLENAME_MAVEN2ADDITIONAL)
                        && Platform.getBundle(Globals.BUNDLENAME_MAVEN2ADDITIONAL) != null) {
                    newNatureList.add(Globals.NATURE_ID_MAVEN2ADDITIONAL);
                }
            }
            if (behavior.isTieUpWithBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT)
                    && Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null) {
                newNatureList.add(Globals.NATURE_ID_M2ECLIPSE_LIGHT);
            } else if (behavior.isTieUpWithBundle(Globals.BUNDLENAME_M2ECLIPSE)
                    && Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
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

    private void setUpClasspath(IJavaProject javaProject, ViliBehavior behavior, IPath jreContainerPath,
            IProgressMonitor monitor) throws CoreException {
        if (behavior.isTieUpWithBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT)
                && Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null) {
            setUpClasspathForM2Eclipse(javaProject, jreContainerPath, Globals.CLASSPATH_CONTAINER_M2ECLIPSE_LIGHT,
                    monitor);
        } else if (behavior.isTieUpWithBundle(Globals.BUNDLENAME_M2ECLIPSE)
                && Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE) != null) {
            setUpClasspathForM2Eclipse(javaProject, jreContainerPath, Globals.CLASSPATH_CONTAINER_M2ECLIPSE, monitor);
        } else {
            setUpClasspathForNonM2Eclipse(javaProject, jreContainerPath, monitor);
        }
    }

    private void setUpClasspathForM2Eclipse(IJavaProject javaProject, IPath jreContainerPath,
            String m2EclipseContainerPath, IProgressMonitor monitor) throws JavaModelException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.23"), 1); //$NON-NLS-1$

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
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.23"), 1); //$NON-NLS-1$
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

    private void updateApplicationProperties(IProject project, MapProperties applicationProperties,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.20"), 1); //$NON-NLS-1$
        try {
            mergeProperties(project.getFile(Globals.PATH_APP_PROPERTIES), applicationProperties,
                    new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void createSuperclass(IProject project, String superclass, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("ProjectBuilderImpl.14"), 2); //$NON-NLS-1$
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
                body = evaluateTemplate(TEMPLATEPATH_SUPERCLASS, map);
            } catch (IOException ex) {
                Activator.getDefault().throwCoreException(
                        Messages.getString("ProjectBuilderImpl.18") + TEMPLATEPATH_SUPERCLASS, ex); //$NON-NLS-1$
                return;
            }
            monitor.worked(1);
            if (monitor.isCanceled()) {
                throw new OperationCanceledException();
            }

            writeFile(file, body, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    private void addNatures(IProjectDescription description, List<String> newNatureList) {
        Set<String> set = new LinkedHashSet<String>();
        set.addAll(Arrays.asList(description.getNatureIds()));
        set.addAll(newNatureList);
        description.setNatureIds(set.toArray(new String[0]));
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

    public String evaluateTemplate(String path, Map<String, Object> parameterMap) throws IOException {
        StringWriter sw = new StringWriter();
        try {
            cfg.getTemplate(path).process(parameterMap, sw);
        } catch (TemplateException ex) {
            IOException ioex = new IOException();
            ioex.initCause(ex);
            throw ioex;
        }
        return sw.toString();
    }

    public void expandArtifact(IProject project, ViliProjectPreferences preferences, ArtifactPair pair,
            Map<String, Object> parameters, IProgressMonitor monitor) throws IOException, CoreException {
        monitor.beginTask(Messages.getString("Activator.15"), IProgressMonitor.UNKNOWN); //$NON-NLS-1$
        try {
            final JarFile jarFile = ArtifactUtils.getJarFile(pair.getArtifact());
            try {
                Configuration cfg = new Configuration();
                cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
                cfg.setLocalizedLookup(false);
                cfg.setTemplateLoader(new TemplateLoader() {
                    public void closeTemplateSource(Object name) throws IOException {
                    }

                    public Object findTemplateSource(String name) throws IOException {
                        return jarFile.getJarEntry(name);
                    }

                    public long getLastModified(Object name) {
                        return 0;
                    }

                    public Reader getReader(Object templateSource, String encoding) throws IOException {
                        return new InputStreamReader(jarFile.getInputStream((JarEntry) templateSource), encoding);
                    }
                });
                cfg.setObjectWrapper(new DefaultObjectWrapper());

                ViliBehavior behavior = pair.getBehavior();
                behavior.getConfigurator().processBeforeExpanding(project, behavior, preferences, parameters,
                        new SubProgressMonitor(monitor, 1));

                for (Enumeration<JarEntry> enm = jarFile.entries(); enm.hasMoreElements();) {
                    JarEntry entry = enm.nextElement();
                    String name = entry.getName();
                    expand(name, jarFile, project, behavior, preferences, parameters, cfg, new SubProgressMonitor(
                            monitor, 1));
                }

                behavior.getConfigurator().processAfterExpanded(project, behavior, preferences, parameters,
                        new SubProgressMonitor(monitor, 1));
            } finally {
                jarFile.close();
            }
        } finally {
            monitor.done();
        }
    }

    private void expand(String path, JarFile jarFile, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters, Configuration cfg,
            IProgressMonitor monitor) throws IOException, CoreException {
        String resolvedPath = resolvePath(path, cfg, parameters);
        if (!shouldExpand(path, resolvedPath, project, behavior, preferences, parameters)) {
            return;
        }

        if (path.endsWith("/")) { //$NON-NLS-1$
            mkdirs(project.getFolder(resolvedPath), new SubProgressMonitor(monitor, 1));
        } else {
            InputStream in;
            boolean evaluateAsTemplate = shouldEvaluateAsTemplate(path, behavior);
            boolean viewTemplate = isViewTemplate(path, behavior);
            if (evaluateAsTemplate || viewTemplate) {
                String templateEncoding = getTemplateEncoding(path, behavior);

                String evaluatedString;
                if (evaluateAsTemplate) {
                    try {
                        StringWriter sw = new StringWriter();
                        cfg.setEncoding(Locale.getDefault(), templateEncoding);
                        cfg.getTemplate(path).process(parameters, sw);
                        evaluatedString = sw.toString();
                    } catch (TemplateException ex) {
                        IOException ioex = new IOException();
                        ioex.initCause(ex);
                        throw ioex;
                    } finally {
                        cfg.setEncoding(Locale.getDefault(), Globals.ENCODING);
                    }
                } else {
                    evaluatedString = IOUtils.readString(jarFile.getInputStream(jarFile.getJarEntry(path)),
                            templateEncoding, false);
                }

                byte[] evaluated = evaluatedString.getBytes(viewTemplate ? getValidEncoding(preferences
                        .getViewEncoding(), templateEncoding) : templateEncoding);
                in = new ByteArrayInputStream(evaluated);
            } else {
                in = jarFile.getInputStream(jarFile.getJarEntry(path));
            }
            try {
                IFile outputFile = project.getFile(resolvedPath);
                if (outputFile.exists()) {
                    if (evaluateAsTemplate && shouldMergeWhenExpanding(path, behavior)) {
                        in = mergeFile(outputFile, in);
                    }
                    outputFile.setContents(in, false, false, new SubProgressMonitor(monitor, 1));
                } else {
                    mkdirs(outputFile.getParent(), new SubProgressMonitor(monitor, 1));
                    outputFile.create(in, false, new SubProgressMonitor(monitor, 1));
                }
            } finally {
                StreamUtils.close(in);
            }
        }
    }

    private InputStream mergeFile(IFile file, InputStream in) throws CoreException {
        String extension = file.getLocation().getFileExtension();
        if (EXTENSION_PROPERTIES.equals(extension) || EXTENSION_PREFS.equals(extension)) {
            return mergeFileAsProperties(file, in, false);
        } else if (EXTENSION_XPROPERTIES.equals(extension)) {
            return mergeFileAsProperties(file, in, true);
        } else {
            // TODO 警告などを出すようにする？
            return in;
        }
    }

    private InputStream mergeFileAsProperties(IFile file, InputStream in, boolean xproeprties) throws CoreException {
        String encoding = xproeprties ? "UTF-8" : "ISO-8859-1"; //$NON-NLS-1$ //$NON-NLS-2$

        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        InputStream is = file.getContents();
        try {
            prop.load(is, encoding);
        } catch (IOException ex) {
            Activator.getDefault().throwCoreException("Can't load " + file, ex); //$NON-NLS-1$
            return null;
        } finally {
            StreamUtils.close(is);
        }

        MapProperties fragment = new MapProperties(new TreeMap<String, String>());
        try {
            fragment.load(in, encoding);
        } catch (IOException ex) {
            Activator.getDefault().throwCoreException("Can't load fragment for " + file, ex); //$NON-NLS-1$
            return null;
        } finally {
            StreamUtils.close(in);
        }

        for (Enumeration<?> enm = fragment.propertyNames(); enm.hasMoreElements();) {
            String name = (String) enm.nextElement();
            prop.setProperty(name, fragment.getProperty(name));
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            prop.store(baos, encoding);
        } catch (IOException ex) {
            Activator.getDefault().throwCoreException("Can't store " + file, ex); //$NON-NLS-1$
            return null;
        }

        return new ByteArrayInputStream(baos.toByteArray());
    }

    private boolean shouldMergeWhenExpanding(String path, ViliBehavior behavior) {
        switch (behavior.shouldMergeWhenExpanding(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldMergeWhenExpanding(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return false;
    }

    private String getValidEncoding(String viewEncoding, String defaultEncoding) {
        if (viewEncoding == null || viewEncoding.trim().length() == 0) {
            return defaultEncoding;
        }
        return viewEncoding;
    }

    private String getTemplateEncoding(String path, ViliBehavior behavior) {
        String encoding = behavior.getTemplateEncoding(path);
        if (encoding != null) {
            return encoding;
        }
        encoding = systemBehavior.getTemplateEncoding(path);
        if (encoding != null) {
            return encoding;
        }

        return Globals.ENCODING;
    }

    private boolean isViewTemplate(String path, ViliBehavior behavior) {
        switch (behavior.shouldTreatAsViewTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldTreatAsViewTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return false;
    }

    private boolean shouldEvaluateAsTemplate(String path, ViliBehavior behavior) {
        switch (behavior.shouldEvaluateAsTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldEvaluateAsTemplate(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return false;
    }

    private boolean shouldExpand(String path, String resolvedPath, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        if (behavior.getArtifactType() == ArtifactType.SKELETON) {
            if (path.equals(Globals.PATH_M2ECLIPSE_LIGHT_PREFS)
                    && Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) == null) {
                return false;
            } else if (path.equals(Globals.PATH_M2ECLIPSE_PREFS)
                    && (Platform.getBundle(Globals.BUNDLENAME_M2ECLIPSE_LIGHT) != null || Platform
                            .getBundle(Globals.BUNDLENAME_M2ECLIPSE) == null)) {
                return false;
            }
        }

        if (behavior.getArtifactType() == ArtifactType.FRAGMENT) {
            if (path.equals(PATH_POM_XML)) {
                return false;
            }
        }

        switch (behavior.getConfigurator().shouldExpand(path, resolvedPath, project, behavior, preferences, parameters)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (behavior.shouldExpand(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }
        switch (systemBehavior.shouldExpand(path)) {
        case INCLUDED:
            return true;
        case EXCLUDED:
            return false;
        }

        return true;
    }

    private String resolvePath(String path, Configuration cfg, Map<String, Object> parameterMap) throws IOException {
        try {
            StringWriter sw = new StringWriter();
            new freemarker.template.Template("pathName", new StringReader(path), cfg).process(parameterMap, sw); //$NON-NLS-1$
            return sw.toString();
        } catch (TemplateException ex) {
            IOException ioex = new IOException();
            ioex.initCause(ex);
            throw ioex;
        }
    }

    public void mergeProperties(IFile file, URL entry, IProgressMonitor monitor) throws CoreException {
        MapProperties prop = new MapProperties(new TreeMap<String, String>());
        InputStream is = null;
        try {
            is = entry.openStream();
            prop.load(is);
        } catch (IOException ex) {
            Activator.getDefault().throwCoreException("Can't load: " + entry, ex); //$NON-NLS-1$
        } finally {
            IOUtils.closeQuietly(is);
        }

        mergeProperties(file, prop, monitor);
    }

    public void mergeProperties(IFile file, MapProperties properties, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask("Merge properties", 1); //$NON-NLS-1$
        try {
            MapProperties prop = new MapProperties(new TreeMap<String, String>());
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = file.getContents();
                    prop.load(is);
                } catch (IOException ex) {
                    Activator.getDefault().throwCoreException("Can't load: " + file, ex); //$NON-NLS-1$
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }

            for (Enumeration<?> enm = properties.propertyNames(); enm.hasMoreElements();) {
                String name = (String) enm.nextElement();
                prop.setProperty(name, properties.getProperty(name));
            }

            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            try {
                prop.store(baos);
            } catch (IOException ex) {
                Activator.getDefault().throwCoreException("Can't happen!", ex); //$NON-NLS-1$
            }

            ByteArrayInputStream bais = new ByteArrayInputStream(baos.toByteArray());
            writeFile(file, bais, new SubProgressMonitor(monitor, 1));
        } finally {
            monitor.done();
        }
    }

    public void writeFile(IFile file, String body, IProgressMonitor monitor) throws CoreException {
        try {
            writeFile(file, new ByteArrayInputStream(body.getBytes(Globals.ENCODING)), monitor);
        } catch (UnsupportedEncodingException ex) {
            throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
        }
    }

    public void writeFile(IFile file, InputStream is, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("Activator.21"), 2); //$NON-NLS-1$
        if (file.exists()) {
            file.setContents(is, false, false, new SubProgressMonitor(monitor, 2));
        } else {
            mkdirs(file.getParent(), new SubProgressMonitor(monitor, 1));
            file.create(is, false, new SubProgressMonitor(monitor, 1));
        }
        monitor.done();
    }

    public void mkdirs(IResource container, IProgressMonitor monitor) throws CoreException {
        if (container.getType() != IResource.FOLDER) {
            return;
        }
        IFolder folder = (IFolder) container;
        if (!folder.exists()) {
            mkdirs(folder.getParent(), monitor);
            folder.create(false, true, new SubProgressMonitor(monitor, 1));
        }
    }

    public MapProperties loadApplicationProperties(IProject project) throws CoreException {
        MapProperties properties = new MapProperties(new TreeMap<String, String>());
        boolean isYmirProject = project.hasNature(YmirProjectNature.ID);
        if (isYmirProject) {
            IFile file = project.getFile(Globals.PATH_APP_PROPERTIES);
            if (file.exists()) {
                InputStream is = null;
                try {
                    is = file.getContents();
                    properties.load(is);
                } catch (IOException ex) {
                    Activator.getDefault().throwCoreException("Can't load: " + file, ex); //$NON-NLS-1$
                } finally {
                    IOUtils.closeQuietly(is);
                }
            }
        }
        return properties;
    }

    public void saveApplicationProperties(IProject project, MapProperties properties, boolean merge)
            throws CoreException {
        boolean isYmirProject = project.hasNature(YmirProjectNature.ID);
        if (!isYmirProject) {
            return;
        }

        if (merge) {
            MapProperties base = loadApplicationProperties(project);
            for (Enumeration<?> enm = properties.propertyNames(); enm.hasMoreElements();) {
                String name = (String) enm.nextElement();
                base.setProperty(name, properties.getProperty(name));
            }
            properties = base;
        }

        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try {
            properties.store(baos);
        } catch (IOException ex) {
            Activator.getDefault().throwCoreException("Can't happen!", ex); //$NON-NLS-1$
            return;
        }

        writeFile(project.getFile(Globals.PATH_APP_PROPERTIES), new ByteArrayInputStream(baos.toByteArray()),
                new NullProgressMonitor());
    }
}
