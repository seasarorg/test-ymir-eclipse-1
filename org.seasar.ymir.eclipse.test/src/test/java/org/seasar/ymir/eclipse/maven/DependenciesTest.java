package org.seasar.ymir.eclipse.maven;

import java.beans.Introspector;
import java.io.InputStreamReader;
import java.io.StringWriter;

import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import junit.framework.TestCase;

public class DependenciesTest extends TestCase {
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

    public void testToBean() throws Exception {
        Dependencies actual = mapper.toBean(XMLParserFactory.newInstance().parse(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                        getClass().getName().replace('.', '/').concat("_dependencies1.xml")), "UTF-8"))
                .getRootElement(), Dependencies.class);
        assertEquals(2, actual.getDependencies().length);
        int idx = 0;
        Dependency dependency = actual.getDependencies()[idx++];
        assertEquals("com.h2database", dependency.getGroupId());
        assertEquals("h2", dependency.getArtifactId());
        assertEquals("1.0.78", dependency.getVersion());
        assertEquals("runtime", dependency.getScope());
        dependency = actual.getDependencies()[idx++];
        assertEquals("log4j", dependency.getGroupId());
        assertEquals("log4j", dependency.getArtifactId());
        assertEquals("1.2.11", dependency.getVersion());
        assertNull(dependency.getScope());

        StringWriter sw = new StringWriter();
        mapper.toXML(actual, sw);
    }
}
