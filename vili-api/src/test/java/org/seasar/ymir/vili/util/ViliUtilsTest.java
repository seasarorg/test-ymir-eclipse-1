package org.seasar.ymir.vili.util;

import junit.framework.TestCase;

public class ViliUtilsTest extends TestCase {
    public void testIsCompatible() throws Exception {
        assertFalse("アーティファクトがバージョン情報を持っていない場合はfalse", ViliUtils.isCompatible(
                "2.0.0", null));

        assertFalse("major.minorが違う場合はfalse", ViliUtils.isCompatible("2.1.0",
                "2.0.0"));
        assertFalse("major.minorが違う場合はfalse", ViliUtils.isCompatible("1.0.0",
                "2.0.0"));
        assertFalse("major.minorが違う場合はfalse", ViliUtils.isCompatible("2.0.0",
                "1.0.0"));

        assertFalse("major.minorが同じでincrementalが大きい場合はfalse", ViliUtils
                .isCompatible("2.0.0", "2.0.1"));

        assertTrue("major.minorが同じでincrementalが同じ場合はtrue", ViliUtils
                .isCompatible("2.0.0", "2.0.0"));
        assertTrue("major.minorが同じでincrementalが同じ場合はtrue", ViliUtils
                .isCompatible("2.0.0-1", "2.0.0"));

        assertTrue("major.minorが同じでincrementalが小さい場合はtrue", ViliUtils
                .isCompatible("2.0.1", "2.0.0"));
        assertTrue("major.minorが同じでincrementalが小さい場合はtrue", ViliUtils
                .isCompatible("2.0.1-0", "2.0.0"));
    }
}
