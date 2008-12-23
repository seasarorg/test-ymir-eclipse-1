package org.seasar.ymir.vili.model;

import net.skirnir.xom.annotation.Child;

public class Fragments {
    private Fragment[] fragments = new Fragment[0];

    public Fragments() {
    }

    public Fragments(Fragment... fragments) {
        this.fragments = fragments;
    }

    public Fragment[] getFragments() {
        return fragments;
    }

    @Child
    public void addFragment(Fragment fragment) {
        Fragment[] newFragments = new Fragment[fragments.length + 1];
        System.arraycopy(fragments, 0, newFragments, 0, fragments.length);
        newFragments[fragments.length] = fragment;
        fragments = newFragments;
    }
}
