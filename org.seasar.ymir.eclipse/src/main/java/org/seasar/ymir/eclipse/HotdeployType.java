package org.seasar.ymir.eclipse;

public enum HotdeployType {
    S2("S2"), JAVAREBEL("JavaRebel"), VOID("void");

    private String name;

    private HotdeployType(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
