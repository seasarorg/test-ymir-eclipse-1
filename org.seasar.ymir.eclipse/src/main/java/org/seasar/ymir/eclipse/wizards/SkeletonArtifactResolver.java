package org.seasar.ymir.eclipse.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ArtifactNotFoundException;
import org.seasar.ymir.eclipse.MavenArtifact;
import org.seasar.ymir.eclipse.SkeletonEntry;
import org.seasar.ymir.eclipse.SkeletonFragment;

import werkzeugkasten.mvnhack.repository.Artifact;

public class SkeletonArtifactResolver implements Runnable {
    private NewProjectWizardSecondPage page;

    private SkeletonEntry entry;

    private Thread thread;

    private volatile boolean cancelled;

    public SkeletonArtifactResolver(NewProjectWizardSecondPage page, SkeletonEntry entry) {
        this.page = page;
        this.entry = entry;
    }

    public void start() {
        thread = new Thread(this);
        thread.start();
    }

    public void cancel() {
        cancelled = true;
    }

    public void run() {
        page.getShell().getDisplay().asyncExec(new Runnable() {
            public void run() {
                List<Artifact> list = new ArrayList<Artifact>();
                boolean failed = false;
                do {
                    Artifact artifact = resolveArtifact(entry);
                    if (cancelled) {
                        return;
                    }
                    if (artifact == null) {
                        failed = true;
                        break;
                    }
                    list.add(artifact);

                    for (SkeletonFragment fragment : entry.getFragments()) {
                        artifact = resolveArtifact(fragment);
                        if (cancelled) {
                            return;
                        }
                        if (artifact == null) {
                            failed = true;
                            break;
                        }
                        list.add(artifact);
                    }
                } while (false);

                String errorMessage;
                if (!failed) {
                    page.setSkeletonArtifacts(list.toArray(new Artifact[0]));
                    page.setPageComplete(page.validatePage());
                    errorMessage = null;
                } else {
                    errorMessage = Messages.getString("SkeletonArtifactResolver.1"); //$NON-NLS-1$
                }
                if (page.isVisible()) {
                    page.setMessage(null);
                    page.setErrorMessage(errorMessage);
                }
            }
        });
    }

    private Artifact resolveArtifact(MavenArtifact artifact) {
        Activator activator = Activator.getDefault();
        String version;
        if (artifact.getVersion() == null) {
            version = activator.getArtifactLatestVersion(artifact.getGroupId(), artifact.getArtifactId());
            if (version == null) {
                return null;
            }
        } else {
            version = artifact.getVersion();
        }
        try {
            return activator.resolveArtifact(artifact.getGroupId(), artifact.getArtifactId(), version,
                    new NullProgressMonitor());
        } catch (ArtifactNotFoundException ignore) {
            return null;
        }
    }
}
