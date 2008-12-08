package org.seasar.ymir.eclipse.preferences.impl;

import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jface.preference.IPersistentPreferenceStore;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ApplicationPropertiesKeys;
import org.seasar.ymir.eclipse.DatabaseEntry;
import org.seasar.ymir.eclipse.ParameterKeys;
import org.seasar.ymir.eclipse.PlatformDelegate;
import org.seasar.ymir.eclipse.natures.ViliNature;
import org.seasar.ymir.eclipse.preferences.PreferenceConstants;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferences;
import org.seasar.ymir.eclipse.preferences.ViliProjectPreferencesProvider;
import org.seasar.ymir.eclipse.util.MapAdapter;

public class ViliProjectPreferencesImpl implements ViliProjectPreferences {
    private static final Map<String, String> JRE_VERSION_MAP;

    private static final String DEFAULT_JREVERSION = "1.6";

    private ViliProjectPreferencesProvider provider;

    private boolean projectSpecificTemplateEnabled;

    private String template;

    private String rootPackageName;

    private String rootPackagePath;

    private String viewEncoding;

    private boolean useDatabase;

    private DatabaseEntry databaseEntry;

    private String projectName;

    private String groupId;

    private String artifactId;

    private String version;

    private IPath jreContainerPath;

    private String jreVersion;

    private PlatformDelegate platformDelegate = new PlatformDelegate();

    private MapProperties applicationProperties;

    private MapAdapter ymir;

    private String fieldPrefix;

    private String fieldSuffix;

    private String fieldSpecialPrefix;

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
        databaseEntry = provider.getDatabaseEntry();
        projectName = provider.getProjectName();
        groupId = provider.getGroupId();
        artifactId = provider.getArtifactId();
        version = provider.getVersion();
        setJREContainerPath(provider.getJREContainerPath());
        fieldPrefix = provider.getFieldPrefix();
        fieldSuffix = provider.getFieldSuffix();
        fieldSpecialPrefix = provider.getFieldSpecialPrefix();
        applicationProperties = provider.getApplicationProperties();
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

    public DatabaseEntry[] getDatabaseEntries() {
        return provider.getDatabaseEntries();
    }

    public boolean isUseDatabase() {
        return useDatabase;
    }

    public DatabaseEntry getDatabaseEntry() {
        return databaseEntry;
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
            isYmirProject = project.hasNature(ViliNature.ID);
        } catch (CoreException ex) {
            isYmirProject = false;
        }

        store.putValue(ParameterKeys.DATABASE_DRIVER_CLASS_NAME, databaseEntry.getDriverClassName());
        store.putValue(ParameterKeys.DATABASE_PASSWORD, databaseEntry.getPassword());
        store.putValue(ParameterKeys.DATABASE_NAME, databaseEntry.getName());
        store.putValue(ParameterKeys.DATABASE_TYPE, databaseEntry.getType());
        store.putValue(ParameterKeys.DATABASE_URL, databaseEntry.getURL());
        store.putValue(ParameterKeys.USE_DATABASE, String.valueOf(useDatabase));
        store.putValue(ParameterKeys.DATABASE_USER, databaseEntry.getUser());
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
        return "/";
    }

    public String getDollar() {
        return "$";
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

    public DatabaseEntry getDatabase() {
        return getDatabaseEntry();
    }
}
