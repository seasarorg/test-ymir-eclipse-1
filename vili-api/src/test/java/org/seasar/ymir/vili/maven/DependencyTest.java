package org.seasar.ymir.vili.maven;

import java.beans.Introspector;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.seasar.ymir.vili.model.maven.Dependency;

import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import junit.framework.TestCase;

public class DependencyTest extends TestCase {
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
        Dependency actual = mapper.toBean(XMLParserFactory.newInstance().parse(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                        getClass().getName().replace('.', '/').concat("_dependency1.xml")), "UTF-8")).getRootElement(),
                Dependency.class);
        assertEquals("com.h2database", actual.getGroupId());
        assertEquals("h2", actual.getArtifactId());
        assertEquals("1.0.78", actual.getVersion());
        assertEquals("runtime", actual.getScope());

        StringWriter sw = new StringWriter();
        mapper.toXML(actual, sw);
    }

    public void testEquals() throws Exception {
        Dependency dependency1 = new Dependency("g", "a", "1.0", "runtime");
        Dependency dependency2 = new Dependency("g", "a", "1.1", "compile");
        assertEquals(dependency1, dependency2);
        assertEquals(dependency1.hashCode(), dependency2.hashCode());
    }
}
