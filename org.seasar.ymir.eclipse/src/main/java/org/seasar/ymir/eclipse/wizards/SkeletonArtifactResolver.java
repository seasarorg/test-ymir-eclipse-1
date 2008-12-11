package org.seasar.ymir.eclipse.wizards;

import java.util.ArrayList;
import java.util.List;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ArtifactPair;
import org.seasar.ymir.eclipse.FragmentEntry;
import org.seasar.ymir.eclipse.MavenArtifact;
import org.seasar.ymir.eclipse.SkeletonEntry;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.maven.ExtendedContext;
import org.seasar.ymir.vili.ArtifactType;

import werkzeugkasten.mvnhack.repository.Artifact;

public class SkeletonArtifactResolver implements Runnable {
    private SelectArtifactPage page;

    private SkeletonEntry entry;

    private long wait;

    private ExtendedContext context;

    private Thread thread;

    private volatile boolean cancelled;

    public SkeletonArtifactResolver(SelectArtifactPage page, ExtendedContext context, SkeletonEntry entry, long wait) {
        this.page = page;
        this.context = context;
        this.entry = entry;
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
                Artifact skeleton = null;
                List<ArtifactPair> fragmentList = new ArrayList<ArtifactPair>();
                boolean failed = false;
                do {
                    skeleton = resolveArtifact(entry, page.useSkeletonSnapshot());
                    if (cancelled) {
                        return;
                    }
                    if (skeleton == null) {
                        failed = true;
                        break;
                    }

                    for (FragmentEntry fragment : entry.getAllFragments()) {
                        Artifact artifact = resolveArtifact(fragment, page.useFragmentSnapshot());
                        if (cancelled) {
                            return;
                        }
                        if (artifact == null) {
                            failed = true;
                            break;
                        }
                        fragmentList.add(ArtifactPair.newInstance(artifact));
                    }
                } while (false);

                String errorMessage;
                if (!failed) {
                    ArtifactPair pair = ArtifactPair.newInstance(skeleton);
                    if (pair.getBehavior().getArtifactType() == ArtifactType.SKELETON) {
                        page.setSkeletonAndFragments(pair, fragmentList.toArray(new ArtifactPair[0]));
                        errorMessage = null;
                    } else {
                        errorMessage = Messages.getString("SkeletonArtifactResolver.0"); //$NON-NLS-1$
                    }
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

    private Artifact resolveArtifact(MavenArtifact artifact, boolean containsSnapshot) {
        ArtifactResolver artifactResolver = Activator.getDefault().getArtifactResolver();
        String version = artifact.getVersion();
        if (version == null) {
            version = artifactResolver.getLatestVersion(context, artifact.getGroupId(), artifact.getArtifactId(),
                    containsSnapshot);
            if (version == null) {
                return null;
            }
        }
        return artifactResolver.resolve(context, artifact.getGroupId(), artifact.getArtifactId(), version);
    }
}
