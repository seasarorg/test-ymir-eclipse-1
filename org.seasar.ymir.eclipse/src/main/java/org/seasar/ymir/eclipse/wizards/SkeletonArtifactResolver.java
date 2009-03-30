package org.seasar.ymir.eclipse.wizards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.MoldResolver;
import org.seasar.ymir.vili.MoldType;
import org.seasar.ymir.vili.MoldTypeMismatchException;
import org.seasar.ymir.vili.ProcessContext;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.ViliVersionMismatchException;
import org.seasar.ymir.vili.maven.ExtendedContext;
import org.seasar.ymir.vili.model.Fragment;
import org.seasar.ymir.vili.model.Skeleton;

public class SkeletonArtifactResolver implements Runnable {
    private SelectArtifactPage page;

    private IProject project;

    private ViliProjectPreferences preferences;

    private ProcessContext processCtx;

    private Skeleton skeleton;

    private long wait;

    private ExtendedContext ctx;

    private Thread thread;

    private volatile boolean cancelled;

    private String errorMessage;

    public SkeletonArtifactResolver(SelectArtifactPage page, IProject project, ViliProjectPreferences preferences,
            ProcessContext processCtx, ExtendedContext ctx, Skeleton skeleton, long wait) {
        this.page = page;
        this.project = project;
        this.preferences = preferences;
        this.processCtx = processCtx;
        this.ctx = ctx;
        this.skeleton = skeleton;
        this.wait = wait;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void cancel() {
        cancelled = true;
    }

    public void run() {
        try {
            Thread.sleep(wait);
        } catch (InterruptedException ignore) {
        }
        if (cancelled) {
            return;
        }

        page.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                MoldResolver moldResolver = Activator.getDefault().getMoldResolver();

                errorMessage = Messages.getString("SkeletonArtifactResolver.1"); //$NON-NLS-1$
                Mold skeletonMold = null;
                List<Mold> fragmentList = new ArrayList<Mold>();
                boolean failed = false;
                do {
                    try {
                        skeletonMold = moldResolver.resolveMold(ctx, skeleton.getGroupId(), skeleton.getArtifactId(),
                                skeleton.getVersion(), MoldType.SKELETON, preferences.getViliVersion(), page
                                        .useSkeletonSnapshot(), project, processCtx, new NullProgressMonitor());
                    } catch (MoldTypeMismatchException ex) {
                        errorMessage = Messages.getString("SkeletonArtifactResolver.0"); //$NON-NLS-1$
                    } catch (ViliVersionMismatchException ex) {
                        errorMessage = MessageFormat.format(
                                Messages.getString("SkeletonArtifactResolver.3"), ex.getMold().getBehavior() //$NON-NLS-1$
                                        .getViliVersion().getWithoutQualifier(), preferences.getViliVersion()
                                        .getWithoutQualifier());
                    }
                    if (cancelled) {
                        return;
                    }
                    if (skeletonMold == null) {
                        failed = true;
                        break;
                    }

                    for (Fragment fragment : skeleton.getAllFragments()) {
                        Mold fragmentMold = null;
                        try {
                            fragmentMold = moldResolver.resolveMold(ctx, fragment.getGroupId(), fragment
                                    .getArtifactId(), fragment.getVersion(), MoldType.FRAGMENT, preferences
                                    .getViliVersion(), page.useFragmentSnapshot(), project, processCtx,
                                    new NullProgressMonitor());
                        } catch (MoldTypeMismatchException ex) {
                            errorMessage = MessageFormat.format(
                                    Messages.getString("SkeletonArtifactResolver.4"), fragment.toString()); //$NON-NLS-1$
                        } catch (ViliVersionMismatchException ex) {
                            errorMessage = MessageFormat.format(Messages.getString("SkeletonArtifactResolver.2"), //$NON-NLS-1$
                                    fragment.toString(), ex.getMold().getBehavior().getViliVersion()
                                            .getWithoutQualifier(), preferences.getViliVersion().getWithoutQualifier());
                        }
                        if (cancelled) {
                            return;
                        }
                        if (fragmentMold == null) {
                            failed = true;
                            break;
                        }
                        fragmentList.add(fragmentMold);
                    }
                } while (false);

                if (!failed) {
                    page.setSkeletonAndFragments(skeletonMold, fragmentList.toArray(new Mold[0]));
                    errorMessage = null;
                }
                if (page.isVisible()) {
                    page.setMessage(null);
                    page.setErrorMessage(errorMessage);
                }
            }
        });
    }
}
