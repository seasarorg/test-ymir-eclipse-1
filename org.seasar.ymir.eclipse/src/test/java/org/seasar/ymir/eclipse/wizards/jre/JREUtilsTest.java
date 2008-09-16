package org.seasar.ymir.eclipse.wizards.jre;

import junit.framework.TestCase;

public class JREUtilsTest extends TestCase {
    public void testGetJREVersion() throws Exception {
        assertEquals("1.4", JREUtils.getJREVersion("j2re1.4.2_08"));
        assertEquals("1.4", JREUtils.getJREVersion("j2sdk1.4.2_08"));
        assertEquals("1.5", JREUtils.getJREVersion("jre1.5.0_08"));
        assertEquals("1.5", JREUtils.getJREVersion("jdk1.5.0_08"));
        assertEquals("1.6", JREUtils.getJREVersion("jre1.6.0_08"));
        assertEquals("1.6", JREUtils.getJREVersion("jdk1.6.0_08"));
        assertEquals("1.6", JREUtils.getJREVersion("???"));
    }
}
