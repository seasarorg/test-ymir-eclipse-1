package org.seasar.ymir.eclipse.wizards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.ArtifactPair;
import org.seasar.ymir.eclipse.maven.ArtifactResolver;
import org.seasar.ymir.eclipse.maven.ExtendedContext;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.model.Fragment;
import org.seasar.ymir.vili.model.MavenArtifact;
import org.seasar.ymir.vili.model.Skeleton;

import werkzeugkasten.mvnhack.repository.Artifact;

public class SkeletonArtifactResolver implements Runnable {
    private SelectArtifactPage page;

    private ViliProjectPreferences preferences;

    private Skeleton skeleton;

    private long wait;

    private ExtendedContext context;

    private Thread thread;

    private volatile boolean cancelled;

    public SkeletonArtifactResolver(SelectArtifactPage page, ViliProjectPreferences preferences,
            ExtendedContext context, Skeleton skeleton, long wait) {
        this.page = page;
        this.preferences = preferences;
        this.context = context;
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
                Activator activator = Activator.getDefault();
                ArtifactVersion viliVersion = preferences.getViliVersion();

                String errorMessage = Messages.getString("SkeletonArtifactResolver.1"); //$NON-NLS-1$
                Artifact skeletonArtifact = null;
                List<ArtifactPair> fragmentList = new ArrayList<ArtifactPair>();
                boolean failed = false;
                do {
                    skeletonArtifact = resolveArtifact(skeleton, page.useSkeletonSnapshot());
                    if (cancelled) {
                        return;
                    }
                    if (skeletonArtifact == null) {
                        failed = true;
                        break;
                    }

                    for (Fragment fragment : skeleton.getAllFragments()) {
                        Artifact fragmentArtifact = resolveArtifact(fragment, page.useFragmentSnapshot());
                        if (cancelled) {
                            return;
                        }
                        if (fragmentArtifact == null) {
                            failed = true;
                            break;
                        }
                        ArtifactPair pair = ArtifactPair.newInstance(fragmentArtifact, page.getProjectClassLoader());
                        if (!activator.viliVersionEquals(pair.getBehavior().getViliVersion(), viliVersion)) {
                            errorMessage = MessageFormat.format(
                                    "このスケルトンに含まれるフラグメントが対応しているViliのバージョン（{0}）がこのViliのバージョン（{1}）と一致しないため利用できません："
                                            + pair.getBehavior().getLabel(), pair.getBehavior().getViliVersion()
                                            .getWithoutQualifier(), viliVersion.getWithoutQualifier());
                            failed = true;
                            break;
                        }
                        fragmentList.add(pair);
                    }
                } while (false);

                if (!failed) {
                    ArtifactPair pair = ArtifactPair.newInstance(skeletonArtifact, page.getProjectClassLoader());
                    if (!activator.viliVersionEquals(pair.getBehavior().getViliVersion(), viliVersion)) {
                        errorMessage = MessageFormat.format(
                                "このスケルトンが対応しているViliのバージョン（{0}）がこのViliのバージョン（{1}）と一致しないため利用できません。", pair.getBehavior()
                                        .getViliVersion().getWithoutQualifier(), viliVersion.getWithoutQualifier());
                    } else if (pair.getBehavior().getArtifactType() != ArtifactType.SKELETON) {
                        errorMessage = Messages.getString("SkeletonArtifactResolver.0"); //$NON-NLS-1$
                    } else {
                        page.setSkeletonAndFragments(pair, fragmentList.toArray(new ArtifactPair[0]));
                        errorMessage = null;
                    }
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
