package org.seasar.ymir.vili.model.maven;

import java.util.ArrayList;
import java.util.List;

import net.skirnir.xom.Element;
import net.skirnir.xom.annotation.Child;

public class Profile {
    private String id_;

    private List<Element> elementList_ = new ArrayList<Element>();

    public Profile() {
    }

    public Profile(String id) {
        setId(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || obj.getClass() != getClass()) {
            return false;
        }
        Profile o = (Profile) obj;
        if (!equals(o.id_, id_)) {
            return false;
        }

        return true;
    }

    private boolean equals(Object o1, Object o2) {
        if (o1 == null) {
            return o2 == null;
        } else {
            return o1.equals(o2);
        }
    }

    @Override
    public int hashCode() {
        int h = 0;
        if (id_ != null) {
            h += id_.hashCode();
        }
        return h;
    }

    public String getId() {
        return id_;
    }

    @Child(order = 1)
    public void setId(String id) {
        id_ = id;
    }

    public Element[] getElements() {
        return elementList_.toArray(new Element[0]);
    }

    @Child(order = 2, value = "*")
    public void addElement(Element element) {
        elementList_.add(element);
    }
}
