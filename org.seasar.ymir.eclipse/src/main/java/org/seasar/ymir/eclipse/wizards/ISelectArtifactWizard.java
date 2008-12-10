package org.seasar.ymir.eclipse.wizards;

import org.seasar.ymir.eclipse.ArtifactPair;

public interface ISelectArtifactWizard {
    void notifySkeletonCleared();

    void notifyFragmentsChanged();

    ArtifactPair getSkeleton();

    ArtifactPair[] getFragments();
}
