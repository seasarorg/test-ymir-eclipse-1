package org.seasar.ymir.vili.maven;

import java.beans.Introspector;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;

import junit.framework.TestCase;
import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.vili.maven.Parent;
import org.seasar.ymir.vili.maven.Project;

public class ProjectTest extends TestCase {
    private XOMapper mapper = XOMapperFactory.newInstance().setBeanAccessorFactory(new BeanAccessorFactory() {
        public BeanAccessor newInstance() {
            return new AnnotationBeanAccessor() {
                @Override
                protected String toXMLName(String javaName) {
                    return Introspector.decapitalize(javaName);
                }
            };
        }
    }).setStrict(true);

    public void testToBean1() throws Exception {
        String expected = IOUtils.readString(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                getClass().getName().replace('.', '/').concat("_project1.xml")), "UTF-8"), true);

        Project actual = mapper.toBean(XMLParserFactory.newInstance().parse(new StringReader(expected))
                .getRootElement(), Project.class);

        Parent parent = actual.getParent();
        assertEquals("parent-group", parent.getGroupId());
        assertEquals("parent-artifact", parent.getArtifactId());
        assertEquals("parent-version", parent.getVersion());
        assertEquals("group", actual.getGroupId());
        assertEquals("artifact", actual.getArtifactId());
        assertEquals("version", actual.getVersion());
    }

    public void testToXML1() throws Exception {
        String expected = IOUtils.readString(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                getClass().getName().replace('.', '/').concat("_project1.xml")), "UTF-8"), true);

        StringWriter actual = new StringWriter();
        mapper.toXML(mapper.toBean(XMLParserFactory.newInstance().parse(new StringReader(expected)).getRootElement(),
                Project.class), actual);
        assertEquals(expected, actual.toString());
    }

    public void testToBean2() throws Exception {
        mapper.setStrict(false);
        String expected = IOUtils.readString(new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                getClass().getName().replace('.', '/').concat("_project2.xml")), "UTF-8"), true);

        Project actual = mapper.toBean(XMLParserFactory.newInstance().parse(new StringReader(expected))
                .getRootElement(), Project.class);

        Parent parent = actual.getParent();
        assertEquals("parent-group", parent.getGroupId());
        assertEquals("parent-artifact", parent.getArtifactId());
        assertEquals("parent-version", parent.getVersion());
        assertEquals("group", actual.getGroupId());
        assertEquals("artifact", actual.getArtifactId());
        assertEquals("version", actual.getVersion());
    }
}
