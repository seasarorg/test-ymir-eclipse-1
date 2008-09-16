package org.seasar.ymir.eclipse.wizards;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ArtifactNotFoundException;

import werkzeugkasten.mvnhack.repository.Artifact;

public class SkeletonArtifactResolver implements Runnable {
    private NewProjectWizardSecondPage page;

    private String groupId;

    private String artifactId;

    private String version;

    private Thread thread;

    private volatile boolean cancelled;

    public SkeletonArtifactResolver(NewProjectWizardSecondPage page, String groupId, String artifactId, String version) {
        this.page = page;
        this.groupId = groupId;
        this.artifactId = artifactId;
        this.version = version;
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
                if (page.isVisible()) {
                    page.setMessage("スケルトンアーカイブをレポジトリから取得しています。（この処理は時間がかかることがあります）");
                    page.setErrorMessage(null);
                }

                Artifact artifact = resolveSkeletonArtifact();
                if (cancelled) {
                    return;
                }

                String errorMessage;
                if (artifact != null) {
                    page.setSkeletonArtifact(artifact);
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

    private Artifact resolveSkeletonArtifact() {
        Activator activator = Activator.getDefault();
        String v;
        if (version.length() == 0) {
            v = activator.getArtifactLatestVersion(groupId, artifactId);
            if (v == null) {
                return null;
            }
        } else {
            v = version;
        }
        try {
            return activator.resolveArtifact(groupId, artifactId, v, new NullProgressMonitor());
        } catch (ArtifactNotFoundException ignore) {
            return null;
        }
    }
}
