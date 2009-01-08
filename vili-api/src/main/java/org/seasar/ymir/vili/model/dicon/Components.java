package org.seasar.ymir.vili.model.dicon;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import net.skirnir.xom.annotation.Child;

public class Components {
    private List<Include> includeList = new ArrayList<Include>();

    private List<Component> componentList = new ArrayList<Component>();

    private List<Meta> metaList = new ArrayList<Meta>();

    public Include[] getIncludes() {
        return includeList.toArray(new Include[0]);
    }

    @Child(order = 1)
    public void addInclude(Include include) {
        includeList.add(include);
    }

    public void setIncludes(Include... includes) {
        includeList.clear();
        includeList.addAll(Arrays.asList(includes));
    }

    public Component[] getComponents() {
        return componentList.toArray(new Component[0]);
    }

    @Child(order = 2)
    public void addComponent(Component component) {
        componentList.add(component);
    }

    public void setComponents(Component... components) {
        componentList.clear();
        componentList.addAll(Arrays.asList(components));
    }

    public Meta[] getMetas() {
        return metaList.toArray(new Meta[0]);
    }

    @Child(order = 3)
    public void addMeta(Meta meta) {
        metaList.add(meta);
    }

    public void setMetas(Meta... metas) {
        metaList.clear();
        metaList.addAll(Arrays.asList(metas));
    }
}
