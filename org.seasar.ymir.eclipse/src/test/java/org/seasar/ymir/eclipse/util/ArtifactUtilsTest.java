package org.seasar.ymir.eclipse.util;

import junit.framework.TestCase;

public class ArtifactUtilsTest extends TestCase {
    public void testCompareVersion() throws Exception {
        assertTrue(ArtifactUtils.compareVersions("1.0.0", "1.0.0") == 0);
        assertTrue(ArtifactUtils.compareVersions("1.0.2", "1.0.10") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2", "1.0.10") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2-ga", "1.1.2") > 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2-SNAPSHOT", "1.1.2") < 0);
        assertTrue(ArtifactUtils.compareVersions("1.1.2-SNAPSHOT", "1.1.2-RC1") > 0);
    }

    public void testGetArtifactId() throws Exception {
        assertEquals("kvasir", ArtifactUtils.getArtifactId("/a/b/kvasir-1.0.0.jar"));
        assertEquals("log4j", ArtifactUtils.getArtifactId("/a/b/log4j-1.0.0-SNAPSHOT.jar"));
        assertEquals("commons-beanutils", ArtifactUtils.getArtifactId("/a/b/commons-beanutils-1.0.0-SNAPSHOT.jar"));
    }
}
