package org.seasar.ymir.eclipse.util;

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
}