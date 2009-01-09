package org.seasar.ymir.vili.model.dicon;

import org.seasar.ymir.vili.util.XOMUtils;

import junit.framework.TestCase;

public class MetaTest extends TestCase {
    public void testGetAsBean_子タグを正しく取れること() throws Exception {
        Meta actual = XOMUtils.getAsBean("<meta><meta /></meta>", Meta.class);

        assertNull(actual.getContent());
        assertEquals(1, actual.getElements().length);
        int idx = 0;
        assertEquals("meta", actual.getElements()[idx++].getName());
    }

    public void testGetAsBean_子としてテキストを正しく取れること() throws Exception {
        Meta actual = XOMUtils.getAsBean("<meta>value</meta>", Meta.class);

        assertEquals(0, actual.getElements().length);
        assertEquals("value", actual.getContent());
    }
}
