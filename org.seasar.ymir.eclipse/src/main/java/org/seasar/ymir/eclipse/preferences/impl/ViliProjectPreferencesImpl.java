package org.seasar.ymir.eclipse.preferences.impl;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.Globals;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.impl.PlatformDelegateImpl;
import org.seasar.ymir.eclipse.natures.YmirProjectNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.util.MapAdapter;
import org.seasar.ymir.vili.PlatformDelegate;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.ViliProjectPreferencesProvider;
import org.seasar.ymir.vili.model.Database;

public class ViliProjectPreferencesImpl implements ViliProjectPreferences {
    private static final Map<String, String> JRE_VERSION_MAP;

    private static final String DEFAULT_JREVERSION = "1.6"; //$NON-NLS-1$

    private ViliProjectPreferencesProvider provider;

    private boolean projectSpecificTemplateEnabled;

    private String template;

    private String rootPackageName;

    private String rootPackagePath;

    private String viewEncoding;

    private boolean useDatabase;

    private Database database;

    private Database emptyDatabase;

    private String projectName;

    private String groupId;

    private String artifactId;

    private String version;

    private IPath jreContainerPath;

    private String jreVersion;

    private PlatformDelegate platformDelegate = new PlatformDelegateImpl();

    private MapProperties applicationProperties;

    private MapAdapter ymir;

    private String fieldPrefix;

    private String fieldSuffix;

    private String fieldSpecialPrefix;

    private String viliVersion;

    static {
        Map<String, String> map = new HashMap<String, String>();
        map.put("J2SE-1.3", "1.3"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("J2SE-1.4", "1.4"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("J2SE-1.5", "1.5"); //$NON-NLS-1$ //$NON-NLS-2$
        map.put("JavaSE-1.6", "1.6"); //$NON-NLS-1$ //$NON-NLS-2$
        JRE_VERSION_MAP = Collections.unmodifiableMap(map);
    }

    public ViliProjectPreferencesImpl(ViliProjectPreferencesProvider provider) {
        this.provider = provider;

        projectSpecificTemplateEnabled = provider.isProjectSpecificTemplateEnabled();
        template = provider.getTemplate();
        setRootPackageName(provider.getRootPackageName());
        viewEncoding = provider.getViewEncoding();
        useDatabase = provider.isUseDatabase();
        database = provider.getDatabase();
        emptyDatabase = new Database("", "", "", "", "", "", null); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
        projectName = provider.getProjectName();
        groupId = provider.getGroupId();
        artifactId = provider.getArtifactId();
        version = provider.getVersion();
        setJREContainerPath(provider.getJREContainerPath());
        fieldPrefix = provider.getFieldPrefix();
        fieldSuffix = provider.getFieldSuffix();
        fieldSpecialPrefix = provider.getFieldSpecialPrefix();
        setApplicationProperties(provider.getApplicationProperties());
        viliVersion = readViliVersion();
    }

    String readViliVersion() {
        InputStream is = getClass().getClassLoader().getResourceAsStream(Globals.PATH_VILI_API_POM_PROPERTIES);
        if (is != null) {
            try {
                Properties prop = new Properties();
                prop.load(is);
                return prop.getProperty(Globals.KEY_VERSION);
            } catch (IOException ex) {
                Activator.getDefault().getLog().log(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't read " //$NON-NLS-1$
                        + Globals.PATH_VILI_API_POM_PROPERTIES, ex));
            } finally {
                IOUtils.closeQuietly(is);
            }
        }
        return ""; //$NON-NLS-1$
    }

    public boolean isProjectSpecificTemplateEnabled() {
        return projectSpecificTemplateEnabled;
    }

    public String getTemplate() {
        return template;
    }

    public String getRootPackageName() {
        return rootPackageName;
    }

    public String getRootPackagePath() {
        return rootPackagePath;
    }

    public String getViewEncoding() {
        return viewEncoding;
    }

    public Database[] getDatabases() {
        return provider.getDatabases();
    }

    public boolean isUseDatabase() {
        return useDatabase;
    }

    public Database getDatabase() {
        if (isUseDatabase()) {
            return database;
        } else {
            return emptyDatabase;
        }
    }

    public void setProjectSpecificTemplateEnabled(boolean projectSpecificTemplateEnabled) {
        this.projectSpecificTemplateEnabled = projectSpecificTemplateEnabled;
    }

    public void setTemplate(String template) {
        this.template = template;
    }

    public void setRootPackageName(String rootPackageName) {
        this.rootPackageName = rootPackageName;
        if (this.rootPackageName == null) {
            rootPackagePath = null;
        } else {
            rootPackagePath = this.rootPackageName.replace('.', '/');
        }
    }

    public void setViewEncoding(String viewEncoding) {
        this.viewEncoding = viewEncoding;
    }

    public void setUseDatabase(boolean useDatabase) {
        this.useDatabase = useDatabase;
    }

    public void save(IProject project) throws IOException {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore(project);
        boolean isYmirProject;
        try {
            isYmirProject = project.hasNature(YmirProjectNature.ID);
        } catch (CoreException ex) {
            isYmirProject = false;
        }

        Database database = getDatabase();
        store.putValue(ParameterKeys.DATABASE_DRIVER_CLASS_NAME, database.getDriverClassName());
        store.putValue(ParameterKeys.DATABASE_PASSWORD, database.getPassword());
        store.putValue(ParameterKeys.DATABASE_NAME, database.getName());
        store.putValue(ParameterKeys.DATABASE_TYPE, database.getType());
        store.putValue(ParameterKeys.DATABASE_URL, database.getURL());
        store.putValue(ParameterKeys.USE_DATABASE, String.valueOf(useDatabase));
        store.putValue(ParameterKeys.DATABASE_USER, database.getUser());
        if (isYmirProject) {
            applicationProperties.setProperty(ApplicationPropertiesKeys.ROOT_PACKAGE_NAME, rootPackageName);
        } else {
            store.putValue(ParameterKeys.ROOT_PACKAGE_NAME, rootPackageName);
        }
        store.putValue(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED, String
                .valueOf(projectSpecificTemplateEnabled));
        if (isProjectSpecificTemplateEnabled()) {
            store.putValue(PreferenceConstants.P_TEMPLATE, template);
        } else {
            store.setToDefault(PreferenceConstants.P_TEMPLATE);
        }
        store.putValue(ParameterKeys.VIEW_ENCODING, viewEncoding);

        ((IPersistentPreferenceStore) store).save();
        if (isYmirProject) {
            Activator.getDefault().saveApplicationProperties(project, applicationProperties, true);
        }
    }

    public String getSlash() {
        return "/"; //$NON-NLS-1$
    }

    public String getDollar() {
        return "$"; //$NON-NLS-1$
    }

    public String getProjectName() {
        return projectName;
    }

    public void setProjectName(String projectName) {
        this.projectName = projectName;
    }

    public String getGroupId() {
        return groupId;
    }

    public void setGroupId(String groupId) {
        this.groupId = groupId;
    }

    public String getArtifactId() {
        return artifactId;
    }

    public void setArtifactId(String artifactId) {
        this.artifactId = artifactId;
    }

    public String getVersion() {
        return version;
    }

    public void setVersion(String version) {
        this.version = version;
    }

    public String getJREVersion() {
        return jreVersion;
    }

    public IPath getJREContainerPath() {
        return jreContainerPath;
    }

    public void setJREContainerPath(IPath jreContainerPath) {
        this.jreContainerPath = jreContainerPath;
        if (this.jreContainerPath == null) {
            jreVersion = DEFAULT_JREVERSION;
        } else {
            jreVersion = JRE_VERSION_MAP.get(this.jreContainerPath.lastSegment());
            if (jreVersion == null) {
                jreVersion = DEFAULT_JREVERSION;
            }
        }
    }

    public PlatformDelegate getPlatform() {
        return platformDelegate;
    }

    public String getFieldPrefix() {
        return fieldPrefix;
    }

    public String getFieldSuffix() {
        return fieldSuffix;
    }

    public String getFieldSpecialPrefix() {
        return fieldSpecialPrefix;
    }

    public MapAdapter getYmir() {
        return ymir;
    }

    public MapProperties getApplicationProperties() {
        return applicationProperties;
    }

    public void setApplicationProperties(MapProperties applicationProperties) {
        this.applicationProperties = applicationProperties;
        ymir = new MapAdapter(this.applicationProperties);
    }

    public String getViliVersion() {
        return viliVersion;
    }
}
