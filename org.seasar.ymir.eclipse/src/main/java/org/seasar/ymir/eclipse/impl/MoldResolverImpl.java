package org.seasar.ymir.eclipse.impl;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.vili.Mold;
import org.seasar.ymir.vili.MoldResolver;
import org.seasar.ymir.vili.MoldType;
import org.seasar.ymir.vili.MoldTypeMismatchException;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliVersionMismatchException;
import org.seasar.ymir.vili.maven.ArtifactResolver;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.maven.ExtendedContext;
import org.seasar.ymir.vili.util.ViliUtils;

public class MoldResolverImpl implements MoldResolver {
    public Mold resolveMold(String groupId, String artifactId, String version, MoldType moldType,
            ArtifactVersion viliVersion, boolean containsSnapshot, IProject project, IProgressMonitor monitor)
            throws MoldTypeMismatchException, ViliVersionMismatchException {
        return resolveMold(Activator.getDefault().getArtifactResolver().newContext(false), groupId, artifactId,
                version, moldType, viliVersion, containsSnapshot, project, monitor);
    }

    public Mold resolveMold(ExtendedContext context, String groupId, String artifactId, String version,
            MoldType moldType, ArtifactVersion viliVersion, boolean containsSnapshot, IProject project,
            IProgressMonitor monitor) throws MoldTypeMismatchException, ViliVersionMismatchException {
        monitor.beginTask(Messages.getString("MoldResolverImpl.1"), 2); //$NON-NLS-1$
        try {
            ClassLoader projectClassLoader;
            if (project != null) {
                projectClassLoader = Activator.getDefault().getProjectRelative(project).getProjectClassLoader();
            } else {
                projectClassLoader = getClass().getClassLoader();
            }

            ArtifactResolver artifactResolver = Activator.getDefault().getArtifactResolver();
            if (version != null) {
                // バージョン指定ありの場合。
                Mold mold = Mold.newInstance(artifactResolver.resolve(context, groupId, artifactId, version),
                        projectClassLoader);
                if (mold != null) {
                    ViliBehavior behavior = mold.getBehavior();
                    if (!ViliUtils.isCompatible(viliVersion, behavior.getViliVersion())) {
                        throw new ViliVersionMismatchException(mold);
                    } else if (behavior.getMoldType() != moldType) {
                        throw new MoldTypeMismatchException(mold);
                    }
                }
                return mold;
            } else {
                // バージョン指定なしの場合。
                version = artifactResolver.getLatestVersion(context, groupId, artifactId, containsSnapshot);
                if (version == null) {
                    // バージョンが見つからなかったので終了。
                    return null;
                }
                Mold mold = Mold.newInstance(artifactResolver.resolve(context, groupId, artifactId, version),
                        projectClassLoader);
                if (mold == null) {
                    return null;
                }
                ViliBehavior behavior = mold.getBehavior();
                if (ViliUtils.isCompatible(viliVersion, behavior.getViliVersion())) {
                    // 見つかったもののViliバージョンが適合するなら終了。ただしタイプが違った場合は見つからなかったことにする。
                    if (behavior.getMoldType() != moldType) {
                        throw new MoldTypeMismatchException(mold);
                    }
                    return mold;
                }

                // Viliバージョンが適合しなかった場合は全てのバージョンから検索する。
                String[] versions = artifactResolver.getVersions(context, groupId, artifactId, containsSnapshot);
                SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
                subMonitor.beginTask(Messages.getString("MoldResolverImpl.2"), versions.length - 1); //$NON-NLS-1$
                try {
                    // 0番目はチェック済みなのでスキップする。
                    for (int i = 1; i < versions.length; i++, subMonitor.worked(1)) {
                        mold = Mold.newInstance(artifactResolver.resolve(context, groupId, artifactId, versions[i]),
                                projectClassLoader);
                        if (mold == null) {
                            continue;
                        }
                        behavior = mold.getBehavior();
                        if (ViliUtils.isCompatible(viliVersion, behavior.getViliVersion())) {
                            // 見つかったもののViliバージョンが適合するなら終了。ただしタイプが違った場合は見つからなかったことにする。
                            if (behavior.getMoldType() != moldType) {
                                throw new MoldTypeMismatchException(mold);
                            }
                            return mold;
                        }
                    }
                    return null;
                } finally {
                    subMonitor.done();
                }
            }
        } finally {
            monitor.done();
        }
    }
}
