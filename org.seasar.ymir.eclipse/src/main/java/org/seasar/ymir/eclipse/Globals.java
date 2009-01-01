package org.seasar.ymir.eclipse;

public interface Globals extends org.seasar.ymir.vili.Globals {
    String CLASSPATH_CONTAINER_M2ECLIPSE = BUNDLENAME_M2ECLIPSE + ".MAVEN2_CLASSPATH_CONTAINER"; //$NON-NLS-1$

    String CLASSPATH_CONTAINER_M2ECLIPSE_LIGHT = BUNDLENAME_M2ECLIPSE_LIGHT + ".MAVEN2_CLASSPATH_CONTAINER"; //$NON-NLS-1$

    String CLASSPATH_CONTAEINR_JRE = "org.eclipse.jdt.launching.JRE_CONTAINER"; //$NON-NLS-1$

    String ENCODING = "UTF-8"; //$NON-NLS-1$

    String HEAD_BEHAVIOR_PROPERTIES = PATH_VILI_INF + "/behavior_"; //$NON-NLS-1$

    String TAIL_BEHAVIOR_PROPERTIES = ".properties"; //$NON-NLS-1$

    String IMAGE_YMIR = "icons/ymir.gif"; //$NON-NLS-1$

    String PATH_VILI_API_POM_PROPERTIES = "META-INF/maven/org.seasar.ymir.vili/vili-api/pom.properties"; //$NON-NLS-1$

    String KEY_VERSION = "version"; //$NON-NLS-1$

    String PATH_M2ECLIPSE_PREFS = ".settings/org.maven.ide.eclipse.prefs"; //$NON-NLS-1$

    String PATH_M2ECLIPSE_LIGHT_PREFS = ".settings/org.maven.ide.eclipse_light.prefs"; //$NON-NLS-1$

    String PATH_MAVEN2ADDITIONAL_PREFS = ".settings/net.skirnir.eclipse.maven.prefs"; //$NON-NLS-1$

    String QUALIFIER_MAPPING = "org.seasar.ymir.extension.mapping"; //$NON-NLS-1$
}
