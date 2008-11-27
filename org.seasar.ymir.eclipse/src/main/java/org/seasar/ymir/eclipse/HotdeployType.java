package org.seasar.ymir.eclipse;

public enum HotdeployType {
    S2("S2"), JAVAREBEL("JavaRebel"), VOID("void"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

    private String name;

    private HotdeployType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
