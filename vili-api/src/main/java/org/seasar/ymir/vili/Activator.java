package org.seasar.ymir.vili;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import werkzeugkasten.mvnhack.repository.Artifact;

public class Activator {
    private static final String CLASS_ACTIVATOR = "org.seasar.ymir.eclipse.Activator";

    private static final String METHOD_GETDEFAULT = "getDefault";

    private static final String METHOD_GETLOG = "getLog";

    private Activator() {
    }

    public static void log(Throwable t) {
        log("Exception has occured", t);
    }

    public static void log(String message) {
        log(message, null);
    }

    public static void log(String message, Throwable t) {
        getLog().log(new Status(IStatus.ERROR, Globals.PLUGIN_ID, message, t));
    }

    public static ILog getLog() {
        try {
            Class<?> activatorClass = Class.forName(CLASS_ACTIVATOR);
            Object activator = activatorClass.getMethod(METHOD_GETDEFAULT)
                    .invoke(null);
            return (ILog) activatorClass.getMethod(METHOD_GETLOG).invoke(
                    activator);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static ViliBehavior newViliBehavior(Artifact artifact,
            ClassLoader projectClassLoader) throws CoreException {
        // TODO 
        return null;
    }
}
