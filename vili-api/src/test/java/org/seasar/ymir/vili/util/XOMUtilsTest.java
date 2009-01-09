package org.seasar.ymir.vili.util;

import java.io.StringWriter;

import junit.framework.TestCase;

import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.vili.model.dicon.Components;

public class XOMUtilsTest extends TestCase {
    public void testToXML() throws Exception {
        String content = IOUtils.readString(getClass().getClassLoader()
                .getResourceAsStream(
                        getClass().getName().replace('.', '/').concat(
                                "_dicon.dicon")), "UTF-8", false);
        Components dicon = XOMUtils.getAsBean(content, Components.class);
        StringWriter actual = new StringWriter();
        XOMUtils.getXOMapper().toXML(dicon, actual);
        String expected = IOUtils.readString(getClass().getClassLoader()
                .getResourceAsStream(
                        getClass().getName().replace('.', '/').concat(
                                "_dicon_expected.dicon")), "UTF-8", false);

        assertEquals(expected, actual.toString());
    }
}
