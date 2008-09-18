package org.seasar.ymir.eclipse.wizards;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ArtifactNotFoundException;
import org.seasar.ymir.eclipse.MavenArtifact;
import org.seasar.ymir.eclipse.SkeletonEntry;
import org.seasar.ymir.eclipse.FragmentEntry;

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
                Artifact skeleton = null;
                List<Artifact> fragmentList = new ArrayList<Artifact>();
                boolean failed = false;
                do {
                    skeleton = resolveArtifact(entry);
                    if (cancelled) {
                        return;
                    }
                    if (skeleton == null) {
                        failed = true;
                        break;
                    }

                    for (FragmentEntry fragment : entry.getFragments()) {
                        Artifact artifact = resolveArtifact(fragment);
                        if (cancelled) {
                            return;
                        }
                        if (artifact == null) {
                            failed = true;
                            break;
                        }
                        fragmentList.add(artifact);
                    }
                } while (false);

                String errorMessage;
                if (!failed) {
                    page.setSkeletonArtifact(skeleton);
                    page.setFragmentArtifacts(fragmentList.toArray(new Artifact[0]));
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
        String version = artifact.getVersion();
        if (version == null) {
            version = activator.getArtifactLatestVersion(artifact.getGroupId(), artifact.getArtifactId());
            if (version == null) {
                return null;
            }
        }
        try {
            return activator.resolveArtifact(artifact.getGroupId(), artifact.getArtifactId(), version, false,
                    new NullProgressMonitor());
        } catch (ArtifactNotFoundException ignore) {
            return null;
        }
    }
}
