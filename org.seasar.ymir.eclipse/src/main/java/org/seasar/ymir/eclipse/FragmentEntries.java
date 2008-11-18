package org.seasar.ymir.eclipse;

import net.skirnir.xom.annotation.Bean;
import net.skirnir.xom.annotation.Child;

@Bean("fragments")
public class FragmentEntries {
    private FragmentEntry[] fragments = new FragmentEntry[0];

    public FragmentEntries() {
    }

    public FragmentEntries(FragmentEntry... fragments) {
        this.fragments = fragments;
    }

    public FragmentEntry[] getFragments() {
        return fragments;
    }

    @Child
    public void addFragment(FragmentEntry fragment) {
        FragmentEntry[] newFragments = new FragmentEntry[fragments.length + 1];
        System.arraycopy(fragments, 0, newFragments, 0, fragments.length);
        newFragments[fragments.length] = fragment;
        fragments = newFragments;
    }
}
