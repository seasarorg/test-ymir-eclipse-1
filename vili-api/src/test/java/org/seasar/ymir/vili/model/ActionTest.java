package org.seasar.ymir.vili.model;

import java.io.InputStreamReader;

import junit.framework.TestCase;

import org.seasar.ymir.vili.util.XOMUtils;

import net.skirnir.xom.XMLParserFactory;

public class ActionTest extends TestCase {

    public void testToBean() throws Exception {
        Action actual = XOMUtils.getXOMapper().toBean(
                XMLParserFactory.newInstance().parse(
                        new InputStreamReader(getClass().getClassLoader()
                                .getResourceAsStream(
                                        getClass().getName().replace('.', '/')
                                                .concat("_action1.xml")),
                                "UTF-8")).getRootElement(), Action.class);

        assertEquals("upgrade", actual.getCategoryId());
        assertEquals("groupId", actual.getGroupId());
        assertEquals("artifactId", actual.getArtifactId());
        assertEquals("version", actual.getVersion());
        assertEquals("upgrade", actual.getActionId());
        assertEquals("DBFluteをアップグレードする", actual.getName());
        assertEquals("UpgradeDbfluteAction", actual.getActionClass());
    }
}
