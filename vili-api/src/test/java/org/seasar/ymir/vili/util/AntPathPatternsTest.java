package org.seasar.ymir.vili.util;

import org.seasar.ymir.vili.util.AntPathPatterns;

import junit.framework.TestCase;

public class AntPathPatternsTest extends TestCase {
    public void testMatches() throws Exception {
        AntPathPatterns target = AntPathPatterns.newInstance("a.txt");
        assertTrue(target.matches("a.txt"));
        assertFalse(target.matches("a.txt2"));
    }

    public void testMatches2() throws Exception {
        AntPathPatterns target = AntPathPatterns.newInstance("a/");
        assertTrue(target.matches("a"));
        assertTrue(target.matches("a/"));
        assertTrue(target.matches("a/b"));
        assertFalse(target.matches("ab"));
    }

    public void testMatches3() throws Exception {
        AntPathPatterns target = AntPathPatterns.newInstance("*.java");
        assertTrue(target.matches("a.java"));
        assertFalse(target.matches("a/a.java"));
    }

    public void testMatches4() throws Exception {
        AntPathPatterns target = AntPathPatterns.newInstance("**/*.java");
        assertTrue(target.matches("a.java"));
        assertTrue(target.matches("a/a.java"));
    }

    public void testMatches5() throws Exception {
        AntPathPatterns target = AntPathPatterns.newInstance("**/.*");
        assertTrue(target.matches(".rc"));
        assertFalse(target.matches("rc"));
        assertTrue(target.matches("a/.rc"));
        assertFalse(target.matches("a/rc"));
    }

    public void testMatches6() throws Exception {
        AntPathPatterns target = AntPathPatterns.newInstance("**/");
        assertTrue(target.matches("rc"));
        assertTrue(target.matches("a/"));
        assertTrue(target.matches("a/rc"));
        assertTrue(target.matches("a/b/"));
        assertTrue(target.matches("a/b/rc"));
    }

    public void testMatches7() throws Exception {
        AntPathPatterns target = AntPathPatterns.newInstance("vili-*/");
        assertTrue(target.matches("vili-behavior.properties"));
        assertTrue(target.matches("vili-lib/"));
        assertTrue(target.matches("vili-lib/lib.jar"));
        assertTrue(target.matches("vili-lib/lib/lib.jar"));
    }

    public void testMatches8() throws Exception {
        AntPathPatterns target = AntPathPatterns
                .newInstance("dbflute_${projectName}/");
        assertTrue(target.matches("dbflute_${projectName}"));
        assertTrue(target.matches("dbflute_${projectName}/hoehoe"));
    }

    public void testMatches9() throws Exception {
        AntPathPatterns target = AntPathPatterns
                .newInstance("messages*.xproperties");
        assertTrue(target.matches("messages.xproperties"));
        assertTrue(target.matches("messages_ja.xproperties"));
    }
}
