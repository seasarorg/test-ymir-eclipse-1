package org.seasar.ymir.eclipse;

public interface Globals {
    String BUNDLENAME_TOMCATPLUGIN = "com.sysdeo.eclipse.tomcat";

    String NATURE_ID_TOMCAT = BUNDLENAME_TOMCATPLUGIN + ".tomcatnature";

    String BUNDLENAME_M2ECLIPSE = "org.maven.ide.eclipse";

    String NATURE_ID_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".maven2Nature";

    String BUILDER_ID_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".maven2Builder";

    String BUNDLENAME_MAVEN2ADDITIONAL = "net.skirnir.eclipse.maven";

    String NATURE_ID_MAVEN2ADDITIONAL = BUNDLENAME_MAVEN2ADDITIONAL + ".mavenAdditionalNature";

    String BUILDER_ID_WEBINFLIB = BUNDLENAME_MAVEN2ADDITIONAL + ".webinfLibBuilder";

    String CLASSPATH_CONTAINER_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".MAVEN2_CLASSPATH_CONTAINER";

    String CLASSPATH_CONTAEINR_JRE = "org.eclipse.jdt.launching.JRE_CONTAINER";

    String ENCODING = "UTF-8";

    String BUNDLENAME_RESOURCESYNCHRONIZER = "org.seasar.resource.synchronizer";

    String PATH_SRC_MAIN_JAVA = "src/main/java";

    String PATH_SRC_MAIN_RESOURCES = "src/main/resources";

    String BUNDLENAME_WEBLAUNCHER = "werkzeugkasten.weblauncher";

    String NATURE_ID_WEBLAUNCHER = BUNDLENAME_WEBLAUNCHER + ".nature";
}
