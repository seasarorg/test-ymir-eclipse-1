package org.seasar.ymir.vili.model.maven;

import java.beans.Introspector;
import java.io.InputStreamReader;
import java.io.StringWriter;

import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.vili.model.maven.Dependency;

import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import junit.framework.TestCase;

public class DependencyTest extends TestCase {
    private XOMapper mapper = XOMapperFactory.newInstance()
            .setBeanAccessorFactory(new BeanAccessorFactory() {
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
                new InputStreamReader(getClass().getClassLoader()
                        .getResourceAsStream(
                                getClass().getName().replace('.', '/').concat(
                                        "_dependency1.xml")), "UTF-8"))
                .getRootElement(), Dependency.class);
        assertEquals("com.h2database", actual.getGroupId());
        assertEquals("h2", actual.getArtifactId());
        assertEquals("1.0.78", actual.getVersion());
        assertEquals("runtime", actual.getScope());
        Exclusions exclusions = actual.getExclusions();
        assertNotNull(exclusions);
        Exclusion[] es = exclusions.getExclusions();
        assertNotNull(es);
        assertEquals(1, es.length);
        int idx = 0;
        assertEquals("com.hoe", es[idx].getGroupId());
        assertEquals("hoe", es[idx].getArtifactId());
    }

    public void testToXML() throws Exception {
        Dependency dependency = new Dependency();
        dependency.setGroupId("com.h2database");
        dependency.setArtifactId("h2");
        dependency.setVersion("1.0.78");
        dependency.setScope("runtime");
        dependency.setType("jar");
        dependency.setOptional("true");
        dependency
                .setExclusions(new Exclusions(new Exclusion("com.hoe", "hoe")));

        StringWriter sw = new StringWriter();
        mapper.toXML(dependency, sw);

        assertEquals(IOUtils.readString(getClass().getClassLoader()
                .getResourceAsStream(
                        getClass().getName().replace('.', '/').concat(
                                "_dependency1.xml")), "UTF-8", true), sw
                .toString());
    }

    public void testEquals() throws Exception {
        Dependency dependency1 = new Dependency("g", "a", "1.0", "runtime");
        Dependency dependency2 = new Dependency("g", "a", "1.1", "compile");
        assertEquals(dependency1, dependency2);
        assertEquals(dependency1.hashCode(), dependency2.hashCode());
    }
}
