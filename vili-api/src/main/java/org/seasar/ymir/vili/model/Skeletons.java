package org.seasar.ymir.vili.model;

import net.skirnir.xom.annotation.Child;

public class Skeletons {
    private Skeleton[] skeletons = new Skeleton[0];

    public Skeletons() {
    }

    public Skeletons(Skeleton... skeletons) {
        this.skeletons = skeletons;
    }

    public Skeleton[] getSkeletons() {
        return skeletons;
    }

    @Child
    public void addSkeleton(Skeleton skeleton) {
        Skeleton[] newSkeletons = new Skeleton[skeletons.length + 1];
        System.arraycopy(skeletons, 0, newSkeletons, 0, skeletons.length);
        newSkeletons[skeletons.length] = skeleton;
        skeletons = newSkeletons;
    }
}
