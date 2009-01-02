package org.seasar.ymir.eclipse.wizards;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.vili.ArtifactPair;
import org.seasar.ymir.vili.ArtifactType;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ArtifactResolver;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.maven.ExtendedContext;
import org.seasar.ymir.vili.model.Fragment;
import org.seasar.ymir.vili.model.MavenArtifact;
import org.seasar.ymir.vili.model.Skeleton;
import org.seasar.ymir.vili.util.ViliUtils;

public class SkeletonArtifactResolver implements Runnable {
    private SelectArtifactPage page;

    private ViliProjectPreferences preferences;

    private Skeleton skeleton;

    private long wait;

    private ExtendedContext context;

    private Thread thread;

    private volatile boolean cancelled;

    private String errorMessage;

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
                ArtifactVersion viliVersion = preferences.getViliVersion();

                errorMessage = Messages.getString("SkeletonArtifactResolver.1"); //$NON-NLS-1$
                ArtifactPair skeletonPair = null;
                List<ArtifactPair> fragmentList = new ArrayList<ArtifactPair>();
                boolean failed = false;
                do {
                    skeletonPair = resolveArtifactPair(skeleton, ArtifactType.SKELETON, page.useSkeletonSnapshot(),
                            viliVersion);
                    if (cancelled) {
                        return;
                    }
                    if (skeletonPair == null) {
                        failed = true;
                        break;
                    }

                    for (Fragment fragment : skeleton.getAllFragments()) {
                        ArtifactPair fragmentPair = resolveArtifactPair(fragment, ArtifactType.FRAGMENT, page
                                .useFragmentSnapshot(), viliVersion);
                        if (cancelled) {
                            return;
                        }
                        if (fragmentPair == null) {
                            failed = true;
                            break;
                        }
                        fragmentList.add(fragmentPair);
                    }
                } while (false);

                if (!failed) {
                    page.setSkeletonAndFragments(skeletonPair, fragmentList.toArray(new ArtifactPair[0]));
                    errorMessage = null;
                }
                if (page.isVisible()) {
                    page.setMessage(null);
                    page.setErrorMessage(errorMessage);
                }
            }
        });
    }

    private ArtifactPair resolveArtifactPair(MavenArtifact artifact, ArtifactType artifactType,
            boolean containsSnapshot, ArtifactVersion viliVersion) {
        ArtifactResolver artifactResolver = Activator.getDefault().getArtifactResolver();
        String version = artifact.getVersion();
        if (version != null) {
            // バージョン指定ありの場合。
            ArtifactPair pair = ArtifactPair.newInstance(artifactResolver.resolve(context, artifact.getGroupId(),
                    artifact.getArtifactId(), version), page.getProjectClassLoader());
            if (pair != null) {
                if (!ViliUtils.isCompatible(viliVersion, pair.getBehavior().getViliVersion())) {
                    if (artifactType != ArtifactType.SKELETON) {
                        errorMessage = MessageFormat.format(Messages.getString("SkeletonArtifactResolver.2") //$NON-NLS-1$
                                + pair.getBehavior().getLabel(), pair.getBehavior().getViliVersion()
                                .getWithoutQualifier(), viliVersion.getWithoutQualifier());
                    } else {
                        errorMessage = MessageFormat.format(
                                Messages.getString("SkeletonArtifactResolver.3"), pair.getBehavior() //$NON-NLS-1$
                                        .getViliVersion().getWithoutQualifier(), viliVersion.getWithoutQualifier());
                    }
                    pair = null;
                } else if (pair.getBehavior().getArtifactType() != artifactType) {
                    errorMessage = Messages.getString("SkeletonArtifactResolver.0"); //$NON-NLS-1$
                    pair = null;
                }
            }
            return pair;
        } else {
            // バージョン指定なしの場合。
            version = artifactResolver.getLatestVersion(context, artifact.getGroupId(), artifact.getArtifactId(),
                    containsSnapshot);
            if (version == null) {
                // バージョンが見つからなかったので終了。
                return null;
            }
            ArtifactPair pair = ArtifactPair.newInstance(artifactResolver.resolve(context, artifact.getGroupId(),
                    artifact.getArtifactId(), version), page.getProjectClassLoader());
            if (pair == null) {
                // アーティファクト自体見つからなかったら終了。
                return null;
            } else if (ViliUtils.isCompatible(viliVersion, pair.getBehavior().getViliVersion())) {
                // 見つかったもののViliバージョンが適合するなら終了。ただしタイプが違った場合は見つからなかったことにする。
                if (pair.getBehavior().getArtifactType() != artifactType) {
                    errorMessage = Messages.getString("SkeletonArtifactResolver.0"); //$NON-NLS-1$
                    pair = null;
                }
                return pair;
            }

            // Viliバージョンが適合しなかった場合は全てのバージョンから検索する。
            String[] versions = artifactResolver.getVersions(context, artifact.getGroupId(), artifact.getArtifactId(),
                    containsSnapshot);
            // 0番目はチェック済みなのでスキップする。
            for (int i = 1; i < versions.length; i++) {
                pair = ArtifactPair.newInstance(artifactResolver.resolve(context, artifact.getGroupId(), artifact
                        .getArtifactId(), versions[i]), page.getProjectClassLoader());
                if (pair != null && ViliUtils.isCompatible(viliVersion, pair.getBehavior().getViliVersion())) {
                    // 見つかったもののViliバージョンが適合するなら終了。ただしタイプが違った場合は見つからなかったことにする。
                    if (pair.getBehavior().getArtifactType() != artifactType) {
                        errorMessage = Messages.getString("SkeletonArtifactResolver.0"); //$NON-NLS-1$
                        pair = null;
                    }
                    return pair;
                }
            }
            return null;
        }
    }
}
