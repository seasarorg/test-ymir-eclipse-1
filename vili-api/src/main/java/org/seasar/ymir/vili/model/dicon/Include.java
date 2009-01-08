package org.seasar.ymir.vili.model.dicon;

import java.util.ArrayList;
import java.util.List;

import net.skirnir.xom.Element;
import net.skirnir.xom.annotation.Attribute;
import net.skirnir.xom.annotation.Child;
import net.skirnir.xom.annotation.Required;

public class Include {
    private String path;

    private String condition;

    private List<Element> elementList = new ArrayList<Element>();

    public Include() {
    }

    public Include(String path) {
        this.path = path;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Include o = (Include) obj;
        if (o.getPath() == null) {
            return false;
        } else {
            return o.getPath().equals(path);
        }
    }

    @Override
    public int hashCode() {
        if (path == null) {
            return super.hashCode();
        } else {
            return path.hashCode();
        }
    }

    public String getPath() {
        return path;
    }

    @Attribute
    @Required
    public void setPath(String path) {
        this.path = path;
    }

    public String getCondition() {
        return condition;
    }

    @Attribute
    public void setCondition(String condition) {
        this.condition = condition;
    }

    public Element[] getElements() {
        return elementList.toArray(new Element[0]);
    }

    @Child("*")
    public void addElement(Element element) {
        elementList.add(element);
    }
}
