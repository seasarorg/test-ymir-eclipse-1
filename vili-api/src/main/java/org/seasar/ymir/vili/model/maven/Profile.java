package org.seasar.ymir.vili.model.maven;

import java.util.ArrayList;
import java.util.List;

import net.skirnir.xom.Element;
import net.skirnir.xom.annotation.Child;

public class Profile {
    private List<Element> elementList_ = new ArrayList<Element>();

    public Element[] getElements() {
        return elementList_.toArray(new Element[0]);
    }

    @Child("*")
    public void addElement(Element element) {
        elementList_.add(element);
    }
}
