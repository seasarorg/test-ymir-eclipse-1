package org.seasar.ymir.vili.model.dicon;

import java.util.ArrayList;
import java.util.List;

import net.skirnir.xom.Element;
import net.skirnir.xom.annotation.Attribute;
import net.skirnir.xom.annotation.Child;

public class Component {
    private String instance;

    private String className;

    private String name;

    private String autoBinding;

    private String externalBinding;

    private List<Element> elementList = new ArrayList<Element>();

    public Component() {
    }

    public Component(String className) {
        this(className, null);
    }

    public Component(String className, String name) {
        this(className, name, null);
    }

    public Component(String className, String name, String instance) {
        this.className = className;
        this.name = name;
        this.instance = instance;
    }

    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Component o = (Component) obj;
        if (o.name == null) {
            return false;
        } else {
            return o.name.equals(name);
        }
    }

    @Override
    public int hashCode() {
        if (name == null) {
            return super.hashCode();
        } else {
            return name.hashCode();
        }
    }

    public String getInstance() {
        return instance;
    }

    @Attribute
    public void setInstance(String instance) {
        this.instance = instance;
    }

    public String getClassName() {
        return className;
    }

    @Attribute("class")
    public void setClassName(String className) {
        this.className = className;
    }

    public String getName() {
        return name;
    }

    @Attribute
    public void setName(String name) {
        this.name = name;
    }

    public String getAutoBinding() {
        return autoBinding;
    }

    @Attribute
    public void setAutoBinding(String autoBinding) {
        this.autoBinding = autoBinding;
    }

    public String getExternalBinding() {
        return externalBinding;
    }

    @Attribute
    public void setExternalBinding(String externalBinding) {
        this.externalBinding = externalBinding;
    }

    public Element[] getElements() {
        return elementList.toArray(new Element[0]);
    }

    @Child("*")
    public void addElement(Element element) {
        elementList.add(element);
    }
}
