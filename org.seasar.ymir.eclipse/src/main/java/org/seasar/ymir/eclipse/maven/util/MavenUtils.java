package org.seasar.ymir.eclipse.maven.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import net.skirnir.freyja.Element;
import net.skirnir.freyja.FreyjaRuntimeException;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.impl.TemplateEvaluatorImpl;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.vili.model.maven.Project;

public class MavenUtils {
    private static final String POM_ENCODING = "UTF-8"; //$NON-NLS-1$

    private static TemplateEvaluator evaluator = new TemplateEvaluatorImpl(new PomTagEvaluator(),
            new PomExpressionEvaluator());

    protected MavenUtils() {
    }

    /*
     * for test
     */
    static void setTemplateEvaluator(TemplateEvaluator evaluator) {
        MavenUtils.evaluator = evaluator;
    }

    public static void addToPom(IFile pomFile, Project project, IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("MavenUtils.1"), 2); //$NON-NLS-1$
        try {
            if (!pomFile.exists()) {
                return;
            } else if (isEmpty(project)) {
                return;
            }

            String evaluated;
            InputStream is = null;
            try {
                is = pomFile.getContents();
                evaluated = addToPom(new InputStreamReader(is, POM_ENCODING), project);
            } catch (UnsupportedEncodingException ex) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't happen!", ex)); //$NON-NLS-1$
            } finally {
                IOUtils.closeQuietly(is);
            }
            monitor.worked(1);

            try {
                pomFile.setContents(new ByteArrayInputStream(evaluated.getBytes(POM_ENCODING)), true, true, monitor);
            } catch (UnsupportedEncodingException ex) {
                throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Can't happen!", ex)); //$NON-NLS-1$
            }
            monitor.worked(1);
        } finally {
            monitor.done();
        }
    }

    static boolean isEmpty(Project project) {
        if (project == null) {
            return true;
        }
        if ((project.getRepositories() == null || project.getRepositories().getRepositories().length == 0)
                && (project.getDependencies() == null || project.getDependencies().getDependencies().length == 0)) {
            return true;
        }
        return false;
    }

    static String addToPom(Reader reader, Project project) throws CoreException {
        Element[] elems;
        try {
            elems = evaluator.parse(reader);
        } catch (FreyjaRuntimeException ex) {
            throw new CoreException(new Status(IStatus.ERROR, Activator.PLUGIN_ID, "Illegal syntax", ex)); //$NON-NLS-1$
        }

        PomTemplateContext ctx = (PomTemplateContext) evaluator.newContext();
        ctx.setMetadataToAdd(project);
        return evaluator.evaluate(ctx, elems);
    }
}
