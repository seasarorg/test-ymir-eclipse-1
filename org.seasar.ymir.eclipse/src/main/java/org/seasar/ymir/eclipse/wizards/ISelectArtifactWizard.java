package org.seasar.ymir.eclipse.wizards;

import org.seasar.ymir.vili.Mold;

public interface ISelectArtifactWizard {
    void notifySkeletonCleared();

    void notifyFragmentsChanged();

    Mold getSkeletonMold();

    Mold[] getFragmentMolds();
}
