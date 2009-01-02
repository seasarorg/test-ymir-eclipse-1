package org.seasar.ymir.vili.maven.util;

import junit.framework.TestCase;

public class ArtifactUtilsTest extends TestCase {
    public void testCompareVersion() throws Exception {
        assertTrue(ArtifactUtils.compareVersions((String) null, (String) null) == 0);
        assertTrue(ArtifactUtils.compareVersions(null, "1.0.0") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0", null) > 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-RC2-SNAPSHOT", "1.0.0") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-RC2", "1.0.0") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0", "1.0.0") == 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-1", "1.0.0") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-1-SNAPSHOT", "1.0.0") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-1-SNAPSHOT",
                "1.0.0-SNAPSHOT") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.2", "1.0.10") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2", "1.0.10") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2-ga", "1.1.2") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2-ga", "1.1.2-candidate") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2-SNAPSHOT", "1.1.2") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2-SNAPSHOT", "1.1.2-RC1") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-RC2-000-SNAPSHOT",
                "1.0.0-0") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-20080529.040130-2",
                "1.0.0-20080529.040129-5") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.0-20080529.040130-10",
                "1.0.0-20080529.040130-2") > 0);
    }

    public void testGetArtifactId() throws Exception {
        assertEquals("kvasir", ArtifactUtils
                .getArtifactId("/a/b/kvasir-1.0.0.jar"));
        assertEquals("log4j", ArtifactUtils
                .getArtifactId("/a/b/log4j-1.0.0-SNAPSHOT.jar"));
        assertEquals("commons-beanutils", ArtifactUtils
                .getArtifactId("/a/b/commons-beanutils-1.0.0-SNAPSHOT.jar"));
    }
}
