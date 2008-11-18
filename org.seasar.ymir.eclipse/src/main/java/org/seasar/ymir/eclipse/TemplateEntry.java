package org.seasar.ymir.eclipse;

import net.skirnir.xom.annotation.Bean;
import net.skirnir.xom.annotation.Child;

@Bean("template")
public class TemplateEntry {
    private SkeletonEntries skeletons;

    private FragmentEntries fragments;

    public SkeletonEntries getSkeletons() {
        return skeletons;
    }

    @Child(order = 1)
    public void setSkeletons(SkeletonEntries skeletons) {
        this.skeletons = skeletons;
    }

    public SkeletonEntry[] getAllSkeletons() {
        if (skeletons != null) {
            return skeletons.getSkeletons();
        } else {
            return new SkeletonEntry[0];
        }
    }

    public FragmentEntries getFragments() {
        return fragments;
    }

    @Child(order = 2)
    public void setFragments(FragmentEntries fragments) {
        this.fragments = fragments;
    }

    public FragmentEntry[] getAllFragments() {
        if (fragments != null) {
            return fragments.getFragments();
        } else {
            return new FragmentEntry[0];
        }
    }
}
