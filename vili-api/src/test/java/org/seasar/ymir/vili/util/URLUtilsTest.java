package org.seasar.ymir.vili.util;

import java.io.File;

import junit.framework.TestCase;

public class URLUtilsTest extends TestCase {
    public void testToFile() throws Exception {
        File file = new File(
                "C:\\Documents and Settings\\yokota\\.m2\\repository\\org\\seasar\\ymir\\skeleton\\ymir-fragment-dbflute\\1.0.0-SNAPSHOT\\ymir-fragment-dbflute-1.0.0-SNAPSHOT.pom");
        assertEquals(file, URLUtils.toFile(file.toURI().toURL()));
    }
}
