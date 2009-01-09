package org.seasar.ymir.vili.model.dicon;

import java.util.ArrayList;
import java.util.List;

import net.skirnir.xom.Element;
import net.skirnir.xom.annotation.Attribute;
import net.skirnir.xom.annotation.Child;
import net.skirnir.xom.annotation.Content;

public class Meta {
    private String name;

    private List<Element> elementList = new ArrayList<Element>();

    private String content;

    public Meta() {
    }

    public Meta(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) {
            return true;
        } else if (obj == null || obj.getClass() != getClass()) {
            return false;
        }

        Meta o = (Meta) obj;
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

    public String getName() {
        return name;
    }

    @Attribute
    public void setName(String name) {
        this.name = name;
    }

    public Element[] getElements() {
        return elementList.toArray(new Element[0]);
    }

    @Child("*")
    public void addElement(Element element) {
        elementList.add(element);
    }

    public String getContent() {
        return content;
    }

    @Content
    public void setContent(String content) {
        this.content = content;
    }
}
