package org.seasar.ymir.vili.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import java.util.jar.JarEntry;
import java.util.jar.JarInputStream;

import org.seasar.kvasir.util.io.IOUtils;

public class JarClassLoader extends ClassLoader {
    private static final String SUFFIX_JAR = ".jar";

    private static final String DEFAULT_CLASSESPATH = "WEB-INF/classes/";

    private static final String DEFAULT_LIBPATH = "WEB-INF/lib/";

    private static final String PATH_DELIMITER = "$";

    private URL jarURL;

    private String classesPath = DEFAULT_CLASSESPATH;

    private String libPath = DEFAULT_LIBPATH;;

    private Set<String> notExistPaths = Collections.synchronizedSet(new HashSet<String>());

    private File tempDir;

    public JarClassLoader(URL jarURL) {
        this.jarURL = jarURL;

        initialize();
    }

    public JarClassLoader(URL jarURL, ClassLoader parent) {
        super(parent);
        this.jarURL = jarURL;

        initialize();
    }

    void initialize() {
        try {
            tempDir = File.createTempFile("JarClassLoader", ".tmp");
            tempDir.delete();
            tempDir.mkdirs();
            tempDir.deleteOnExit();
        } catch (IOException ex) {
            tempDir = null;
        }
    }

    public void setClassesPath(String classesPath) {
        this.classesPath = normalizePath(classesPath);
    }

    public void setLibPath(String libPath) {
        this.libPath = normalizePath(libPath);
    }

    String normalizePath(String path) {
        if (path == null) {
            return null;
        } else if (!path.endsWith("/")) {
            return path + "/";
        } else {
            return path;
        }
    }

    @Override
    protected Class<?> findClass(String name) throws ClassNotFoundException {
        String path = name.replace('.', '/').concat(".class");
        if (notExistPaths.contains(path)) {
            throw new ClassNotFoundException(name);
        }

        byte[] bytes = getResourceBytes(path);
        if (bytes == null) {
            notExistPaths.add(path);
            throw new ClassNotFoundException(name);
        }
        return defineClass(name, bytes, 0, bytes.length);
    }

    byte[] getResourceBytes(String path) {
        InputStream is = null;
        try {
            is = jarURL.openStream();
            JarInputStream jis = new JarInputStream(is);
            for (JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
                String name = entry.getName();
                if (name.equals(classesPath + path)) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    IOUtils.pipe(jis, baos, false, false);
                    return baos.toByteArray();
                } else if (name.startsWith(libPath) && name.toLowerCase().endsWith(SUFFIX_JAR)) {
                    JarInputStream jjis = new JarInputStream(jis);
                    for (JarEntry e = jjis.getNextJarEntry(); e != null; e = jjis.getNextJarEntry()) {
                        String n = e.getName();
                        if (n.equals(path)) {
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            IOUtils.pipe(jjis, baos, false, false);
                            return baos.toByteArray();
                        }
                    }
                }
            }
            return null;
        } catch (IOException ex) {
            return null;
        } finally {
            IOUtils.closeQuietly(is);
        }
    }

    @Override
    protected URL findResource(String path) {
        if (notExistPaths.contains(path)) {
            return null;
        }
        if (tempDir == null) {
            return null;
        }

        InputStream is = null;
        try {
            is = jarURL.openStream();
            JarInputStream jis = new JarInputStream(is);
            for (JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
                String name = entry.getName();
                if (name.equals(classesPath + path)) {
                    File file = new File(tempDir, classesPath + PATH_DELIMITER + path);
                    IOUtils.pipe(jis, new FileOutputStream(file), false, true);
                    file.deleteOnExit();
                    return file.toURI().toURL();
                } else if (name.startsWith(libPath) && name.toLowerCase().endsWith(SUFFIX_JAR)) {
                    JarInputStream jjis = new JarInputStream(jis);
                    for (JarEntry e = jjis.getNextJarEntry(); e != null; e = jjis.getNextJarEntry()) {
                        String n = e.getName();
                        if (n.equals(path)) {
                            File file = new File(tempDir, name + PATH_DELIMITER + path);
                            IOUtils.pipe(jis, new FileOutputStream(file), false, true);
                            file.deleteOnExit();
                            return file.toURI().toURL();
                        }
                    }
                }
            }
        } catch (IOException ignore) {
        } finally {
            IOUtils.closeQuietly(is);
        }

        notExistPaths.add(path);
        return null;
    }

    @Override
    protected Enumeration<URL> findResources(String path) throws IOException {
        Vector<URL> urlList = new Vector<URL>();

        if (!notExistPaths.contains(path) && tempDir != null) {
            InputStream is = null;
            try {
                is = jarURL.openStream();
                JarInputStream jis = new JarInputStream(is);
                for (JarEntry entry = jis.getNextJarEntry(); entry != null; entry = jis.getNextJarEntry()) {
                    String name = entry.getName();
                    if (name.equals(classesPath + path)) {
                        File file = new File(tempDir, classesPath + PATH_DELIMITER + path);
                        IOUtils.pipe(jis, new FileOutputStream(file), false, true);
                        file.deleteOnExit();
                        urlList.add(file.toURI().toURL());
                    } else if (name.startsWith(libPath) && name.toLowerCase().endsWith(SUFFIX_JAR)) {
                        JarInputStream jjis = new JarInputStream(jis);
                        for (JarEntry e = jjis.getNextJarEntry(); e != null; e = jjis.getNextJarEntry()) {
                            String n = e.getName();
                            if (n.equals(path)) {
                                File file = new File(tempDir, name + PATH_DELIMITER + path);
                                IOUtils.pipe(jis, new FileOutputStream(file), false, true);
                                file.deleteOnExit();
                                urlList.add(file.toURI().toURL());
                                break;
                            }
                        }
                    }
                }
                return null;
            } finally {
                IOUtils.closeQuietly(is);
            }
        }

        if (urlList.isEmpty()) {
            notExistPaths.add(path);
        }

        return urlList.elements();
    }
}
