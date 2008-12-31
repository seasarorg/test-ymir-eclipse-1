package org.seasar.ymir.vili.maven.util;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.UnsupportedEncodingException;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.vili.Activator;
import org.seasar.ymir.vili.Globals;
import org.seasar.ymir.vili.model.maven.Project;

import net.skirnir.freyja.Element;
import net.skirnir.freyja.FreyjaRuntimeException;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.impl.TemplateEvaluatorImpl;

public class MavenUtils {
    private static final String POM_ENCODING = "UTF-8"; //$NON-NLS-1$

    private static TemplateEvaluator evaluator = new TemplateEvaluatorImpl(
            new PomTagEvaluator(), new PomExpressionEvaluator());

    protected MavenUtils() {
    }

    /*
     * for test
     */
    static void setTemplateEvaluator(TemplateEvaluator evaluator) {
        MavenUtils.evaluator = evaluator;
    }

    public static void updatePom(IProject project, Project pom,
            IProgressMonitor monitor) throws CoreException {
        monitor.beginTask(Messages.getString("MavenUtils.1"), 2); //$NON-NLS-1$
        try {
            if (project == null) {
                return;
            }

            IFile pomFile = project.getFile(Globals.PATH_POM_XML);
            if (!pomFile.exists()) {
                return;
            } else if (isEmpty(pom)) {
                return;
            }

            String evaluated;
            InputStream is = null;
            try {
                is = pomFile.getContents();
                evaluated = addToPom(new InputStreamReader(is, POM_ENCODING),
                        pom);
            } catch (UnsupportedEncodingException ex) {
                Activator.throwCoreException("Can't happen!", ex); //$NON-NLS-1$
                return;
            } finally {
                IOUtils.closeQuietly(is);
            }
            monitor.worked(1);

            try {
                pomFile.setContents(new ByteArrayInputStream(evaluated
                        .getBytes(POM_ENCODING)), true, true, monitor);
            } catch (UnsupportedEncodingException ex) {
                Activator.throwCoreException("Can't happen!", ex); //$NON-NLS-1$
                return;
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
        if ((project.getRepositories() == null || project.getRepositories()
                .getRepositories().length == 0)
                && (project.getDependencies() == null || project
                        .getDependencies().getDependencies().length == 0)) {
            return true;
        }
        return false;
    }

    static String addToPom(Reader reader, Project project) throws CoreException {
        Element[] elems;
        try {
            elems = evaluator.parse(reader);
        } catch (FreyjaRuntimeException ex) {
            Activator.throwCoreException("Illegal syntax", ex); //$NON-NLS-1$
            return null;
        }

        PomTemplateContext ctx = (PomTemplateContext) evaluator.newContext();
        ctx.setMetadataToAdd(project);
        return evaluator.evaluate(ctx, elems);
    }
}
