package org.seasar.ymir.eclipse.wizards;

import org.seasar.ymir.vili.ArtifactPair;

public interface ISelectArtifactWizard {
    void notifySkeletonCleared();

    void notifyFragmentsChanged();

    ArtifactPair getSkeletonArtifactPair();

    ArtifactPair[] getFragmentArtifactPairs();
}
