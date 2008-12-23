package org.seasar.ymir.vili.model;

import net.skirnir.xom.annotation.Child;

public class Template {
    private Skeletons skeletons;

    private Fragments fragments;

    public Skeletons getSkeletons() {
        return skeletons;
    }

    @Child(order = 1)
    public void setSkeletons(Skeletons skeletons) {
        this.skeletons = skeletons;
    }

    public Skeleton[] getAllSkeletons() {
        if (skeletons != null) {
            return skeletons.getSkeletons();
        } else {
            return new Skeleton[0];
        }
    }

    public Fragments getFragments() {
        return fragments;
    }

    @Child(order = 2)
    public void setFragments(Fragments fragments) {
        this.fragments = fragments;
    }

    public Fragment[] getAllFragments() {
        if (fragments != null) {
            return fragments.getFragments();
        } else {
            return new Fragment[0];
        }
    }
}
