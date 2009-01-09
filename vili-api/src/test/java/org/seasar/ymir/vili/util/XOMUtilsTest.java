package org.seasar.ymir.vili.util;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.seasar.ymir.vili.model.dicon.Component;
import org.seasar.ymir.vili.model.dicon.Components;

import net.skirnir.xom.Attribute;
import net.skirnir.xom.Element;
import net.skirnir.xom.Node;

public class XOMUtilsTest extends TestCase {
    public void testToXML() throws Exception {
        // TODO diconから読み込んだcomponent定義がきれいに整形されることを確認する。
        StringWriter sw = new StringWriter();
        Components dicon = new Components();
        Component component1 = new Component("name1");
        component1.addElement(new Element("child1", new Attribute[0],
                new Node[0]));
        Component component2 = new Component("name2");
        dicon.setComponents(component1, component2);
        XOMUtils.getXOMapper().toXML(dicon, sw);
        System.out.println(sw.toString());
    }
}
