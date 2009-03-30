package org.seasar.ymir.vili;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.ymir.vili.maven.ArtifactResolver;

import werkzeugkasten.mvnhack.repository.Artifact;

public class Activator {
    private static final String CLASS_ACTIVATOR = "org.seasar.ymir.eclipse.Activator";

    private static final String METHOD_GETDEFAULT = "getDefault";

    private static final String METHOD_GETLOG = "getLog";

    private static final String METHOD_GETARTIFACTRESOLVER = "getArtifactResolver";

    private static final String METHOD_GETMOLDRESOLVER = "getMoldResolver";

    private static final String METHOD_GETPROJECTBUILDER = "getProjectBuilder";

    private static final String METHOD_NEWVILIBEHAVIOR = "newViliBehavior";

    private static final String METHOD_GETPREFERENCESTORE = "getPreferenceStore";

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

    public static void throwCoreException(String message, Throwable t)
            throws CoreException {
        throw new CoreException(new Status(IStatus.ERROR, Globals.PLUGIN_ID,
                IStatus.OK, message, t));
    }

    public static ViliBehavior newViliBehavior(Artifact artifact,
            ClassLoader projectClassLoader, ProcessContext context)
            throws CoreException {
        try {
            Class<?> activatorClass = Class.forName(CLASS_ACTIVATOR);
            Object activator = activatorClass.getMethod(METHOD_GETDEFAULT)
                    .invoke(null);
            return (ViliBehavior) activatorClass.getMethod(
                    METHOD_NEWVILIBEHAVIOR, Artifact.class, ClassLoader.class,
                    ProcessContext.class).invoke(activator, artifact,
                    projectClassLoader, context);
        } catch (Throwable t) {
            throwCoreException("Can't construct ViliBehavior instance", t);
            return null;
        }
    }

    public static ArtifactResolver getArtifactResolver() {
        try {
            Class<?> activatorClass = Class.forName(CLASS_ACTIVATOR);
            Object activator = activatorClass.getMethod(METHOD_GETDEFAULT)
                    .invoke(null);
            return (ArtifactResolver) activatorClass.getMethod(
                    METHOD_GETARTIFACTRESOLVER).invoke(activator);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static MoldResolver getMoldResolver() {
        try {
            Class<?> activatorClass = Class.forName(CLASS_ACTIVATOR);
            Object activator = activatorClass.getMethod(METHOD_GETDEFAULT)
                    .invoke(null);
            return (MoldResolver) activatorClass.getMethod(
                    METHOD_GETMOLDRESOLVER).invoke(activator);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static ProjectBuilder getProjectBuilder() {
        try {
            Class<?> activatorClass = Class.forName(CLASS_ACTIVATOR);
            Object activator = activatorClass.getMethod(METHOD_GETDEFAULT)
                    .invoke(null);
            return (ProjectBuilder) activatorClass.getMethod(
                    METHOD_GETPROJECTBUILDER).invoke(activator);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }

    public static IPreferenceStore getPreferenceStore() {
        try {
            Class<?> activatorClass = Class.forName(CLASS_ACTIVATOR);
            Object activator = activatorClass.getMethod(METHOD_GETDEFAULT)
                    .invoke(null);
            return (IPreferenceStore) activatorClass.getMethod(
                    METHOD_GETPREFERENCESTORE).invoke(activator);
        } catch (Throwable t) {
            throw new RuntimeException(t);
        }
    }
}
