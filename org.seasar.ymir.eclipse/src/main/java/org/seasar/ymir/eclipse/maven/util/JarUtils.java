package org.seasar.ymir.eclipse.maven.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.TreeMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.seasar.kvasir.util.collection.MapProperties;
import org.seasar.ymir.eclipse.Messages;
import org.seasar.ymir.eclipse.util.StreamUtils;
import org.seasar.ymir.vili.Globals;

public class JarUtils {
    private JarUtils() {
    }

    public static MapProperties getPropertiesResource(JarFile jarFile, String path, IProgressMonitor monitor)
            throws CoreException {
        monitor.beginTask(Messages.getString("Activator.58"), 1); //$NON-NLS-1$

        InputStream is = null;
        try {
            JarEntry entry = jarFile.getJarEntry(path);
            if (entry == null) {
                return null;
            }

            is = jarFile.getInputStream(entry);

            MapProperties prop = new MapProperties(new TreeMap<String, String>());
            prop.load(is);
            return prop;
        } catch (IOException ex) {
            throw new CoreException(new Status(IStatus.ERROR, Globals.PLUGIN_ID,
                    "Can't read resource: jarFile=" + jarFile.getName() + ", path=" + path, ex)); //$NON-NLS-1$ //$NON-NLS-2$
        } finally {
            StreamUtils.close(is);
            monitor.done();
        }
    }
}
