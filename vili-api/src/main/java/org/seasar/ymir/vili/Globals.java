package org.seasar.ymir.vili;

public interface Globals {
    String PLUGIN_ID = "org.seasar.ymir.eclipse";

    String NATURE_ID_VILIPROJECT = PLUGIN_ID + ".viliProjectNature";

    String NATURE_ID_YMIRPROJECT = PLUGIN_ID + ".ymirProjectNature";

    String BUNDLENAME_TOMCATPLUGIN = "com.sysdeo.eclipse.tomcat"; //$NON-NLS-1$

    String NATURE_ID_TOMCAT = BUNDLENAME_TOMCATPLUGIN + ".tomcatnature"; //$NON-NLS-1$

    String BUNDLENAME_M2ECLIPSE = "org.maven.ide.eclipse"; //$NON-NLS-1$

    String NATURE_ID_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".maven2Nature"; //$NON-NLS-1$

    String BUILDER_ID_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".maven2Builder"; //$NON-NLS-1$

    String BUNDLENAME_M2ECLIPSE_LIGHT = "org.maven.ide.eclipse_light"; //$NON-NLS-1$

    String NATURE_ID_M2ECLIPSE_LIGHT = BUNDLENAME_M2ECLIPSE_LIGHT
            + ".maven2Nature"; //$NON-NLS-1$

    String BUNDLENAME_MAVEN2ADDITIONAL = "net.skirnir.eclipse.maven"; //$NON-NLS-1$

    String NATURE_ID_MAVEN2ADDITIONAL = BUNDLENAME_MAVEN2ADDITIONAL
            + ".mavenAdditionalNature"; //$NON-NLS-1$

    String BUNDLENAME_RESOURCESYNCHRONIZER_SEASAR = "org.seasar.resource.synchronizer"; //$NON-NLS-1$

    String BUNDLENAME_RESOURCESYNCHRONIZER_WERKZAUGKASTEN = "werkzeugkasten.resource.synchronizer"; //$NON-NLS-1$

    String BUNDLENAME_WEBLAUNCHER = "werkzeugkasten.weblauncher"; //$NON-NLS-1$

    String NATURE_ID_WEBLAUNCHER = BUNDLENAME_WEBLAUNCHER + ".nature"; //$NON-NLS-1$

    String NATURE_ID_JAVA = "org.eclipse.jdt.core.javanature"; //$NON-NLS-1$

    String PATH_VILI_INF = "VILI-INF";

    String PATH_SRC_MAIN_JAVA = "src/main/java"; //$NON-NLS-1$

    String PATH_SRC_MAIN_RESOURCES = "src/main/resources"; //$NON-NLS-1$

    String PATH_SRC_MAIN_WEBAPP = "src/main/webapp";

    String PATH_APP_PROPERTIES = PATH_SRC_MAIN_RESOURCES + "/app.properties"; //$NON-NLS-1$

    String BEHAVIOR_PROPERTIES = "behavior.properties"; //$NON-NLS-1$

    String PATH_BEHAVIOR_PROPERTIES = PATH_VILI_INF + "/" + BEHAVIOR_PROPERTIES;

    String PATH_CLASSES = PATH_VILI_INF + "/classes"; //$NON-NLS-1$

    String PATH_LIB = PATH_VILI_INF + "/lib"; //$NON-NLS-1$

    String PATH_POM_XML = "pom.xml"; //$NON-NLS-1$

    String PATH_ACTIONS_XML = PATH_VILI_INF + "/actions.xml";
}
