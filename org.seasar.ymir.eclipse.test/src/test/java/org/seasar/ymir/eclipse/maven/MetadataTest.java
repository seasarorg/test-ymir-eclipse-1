package org.seasar.ymir.eclipse.maven;

import java.beans.Introspector;
import java.io.InputStreamReader;

import junit.framework.TestCase;
import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XMLParserFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

public class MetadataTest extends TestCase {
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
        Metadata actual = (Metadata) mapper.toBean(XMLParserFactory.newInstance().parse(
                new InputStreamReader(getClass().getClassLoader().getResourceAsStream(
                        getClass().getName().replace('.', '/').concat("_metadata1.xml")), "UTF-8")).getRootElement(),
                Metadata.class);
        assertEquals("1.0.2", actual.getVersion());
        assertEquals(new Long("20080120101219"), actual.getVersioning().getLastUpdated());
    }
}
