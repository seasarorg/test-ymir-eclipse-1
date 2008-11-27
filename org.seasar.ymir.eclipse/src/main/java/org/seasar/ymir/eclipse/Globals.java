package org.seasar.ymir.eclipse;

public interface Globals {
    String BUNDLENAME_TOMCATPLUGIN = "com.sysdeo.eclipse.tomcat"; //$NON-NLS-1$

    String NATURE_ID_TOMCAT = BUNDLENAME_TOMCATPLUGIN + ".tomcatnature"; //$NON-NLS-1$

    String BUNDLENAME_M2ECLIPSE = "org.maven.ide.eclipse"; //$NON-NLS-1$

    String NATURE_ID_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".maven2Nature"; //$NON-NLS-1$

    String BUILDER_ID_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".maven2Builder"; //$NON-NLS-1$

    String BUNDLENAME_M2ECLIPSE_LIGHT = "org.maven.ide.eclipse_light"; //$NON-NLS-1$

    String NATURE_ID_M2ECLIPSE_LIGHT = BUNDLENAME_M2ECLIPSE_LIGHT + ".maven2Nature"; //$NON-NLS-1$

    String BUNDLENAME_MAVEN2ADDITIONAL = "net.skirnir.eclipse.maven"; //$NON-NLS-1$

    String NATURE_ID_MAVEN2ADDITIONAL = BUNDLENAME_MAVEN2ADDITIONAL + ".mavenAdditionalNature"; //$NON-NLS-1$

    String CLASSPATH_CONTAINER_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".MAVEN2_CLASSPATH_CONTAINER"; //$NON-NLS-1$

    String CLASSPATH_CONTAINER_M2ECLIPSE_LIGHT = BUNDLENAME_M2ECLIPSE_LIGHT + ".MAVEN2_CLASSPATH_CONTAINER"; //$NON-NLS-1$

    String CLASSPATH_CONTAEINR_JRE = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    String ENCODING = "UTF-8"; //$NON-NLS-1$

    String BUNDLENAME_RESOURCESYNCHRONIZER = "org.seasar.resource.synchronizer"; //$NON-NLS-1$

    String PATH_SRC_MAIN_JAVA = "src/main/java"; //$NON-NLS-1$

    String PATH_SRC_MAIN_RESOURCES = "src/main/resources"; //$NON-NLS-1$

    String PATH_SRC_MAIN_WEBAPP_WEBINF_LIB = "src/main/webapp/WEB-INF/lib"; //$NON-NLS-1$

    String PATH_APP_PROPERTIES = PATH_SRC_MAIN_RESOURCES + "/app.properties"; //$NON-NLS-1$

    String BUNDLENAME_WEBLAUNCHER = "werkzeugkasten.weblauncher"; //$NON-NLS-1$

    String NATURE_ID_WEBLAUNCHER = BUNDLENAME_WEBLAUNCHER + ".nature"; //$NON-NLS-1$

    String PREFIX_VILI = "vili-"; //$NON-NLS-1$

    String VILI_DEPENDENCIES_XML = PREFIX_VILI + "dependencies.xml"; //$NON-NLS-1$

    String VILI_BEHAVIOR_PROPERTIES = PREFIX_VILI + "behavior.properties"; //$NON-NLS-1$

    String HEAD_VILI_BEHAVIOR_PROPERTIES = PREFIX_VILI + "behavior_"; //$NON-NLS-1$

    String TAIL_VILI_BEHAVIOR_PROPERTIES = ".properties"; //$NON-NLS-1$

    String IMAGE_YMIR = "icons/ymir.gif"; //$NON-NLS-1$
}
