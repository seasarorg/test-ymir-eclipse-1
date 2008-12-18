package org.seasar.ymir.eclipse.util;

import junit.framework.TestCase;

public class JarClassLoaderTest extends TestCase {
    public void testLoadClass_存在しないクラス() throws Exception {
        JarClassLoader target = new JarClassLoader(getClass().getResource("sample.jar"));
        try {
            target.loadClass("com.example.Hoe");
            fail();
        } catch (ClassNotFoundException expected) {
        }
    }

    public void testLoadClass_classesの中() throws Exception {
        JarClassLoader target = new JarClassLoader(getClass().getResource("sample.jar"));

        Class<?> actual = null;
        try {
            actual = target.loadClass("com.example.Sample");
        } catch (ClassNotFoundException ex) {
            fail();
        }

        assertNotNull(actual);
        assertEquals("com.example.Sample", actual.getName());
    }

    public void testLoadClass_libの中() throws Exception {
        JarClassLoader target = new JarClassLoader(getClass().getResource("sample.jar"));

        Class<?> actual = null;
        try {
            actual = target.loadClass("com.example.Sample2");
        } catch (ClassNotFoundException ex) {
            fail();
        }

        assertNotNull(actual);
        assertEquals("com.example.Sample2", actual.getName());
    }
}
