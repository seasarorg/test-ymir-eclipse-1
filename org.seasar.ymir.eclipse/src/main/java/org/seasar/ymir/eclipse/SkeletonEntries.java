package org.seasar.ymir.eclipse;

import net.skirnir.xom.annotation.Bean;
import net.skirnir.xom.annotation.Child;

@Bean("skeletons")
public class SkeletonEntries {
    private SkeletonEntry[] skeletons = new SkeletonEntry[0];

    public SkeletonEntries() {
    }

    public SkeletonEntries(SkeletonEntry... skeletons) {
        this.skeletons = skeletons;
    }

    public SkeletonEntry[] getSkeletons() {
        return skeletons;
    }

    @Child
    public void addSkeleton(SkeletonEntry skeleton) {
        SkeletonEntry[] newSkeletons = new SkeletonEntry[skeletons.length + 1];
        System.arraycopy(skeletons, 0, newSkeletons, 0, skeletons.length);
        newSkeletons[skeletons.length] = skeleton;
        skeletons = newSkeletons;
    }
}
