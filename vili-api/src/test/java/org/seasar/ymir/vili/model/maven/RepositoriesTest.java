package org.seasar.ymir.vili.model.maven;

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
import net.skirnir.xom.annotation.Content;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.vili.model.maven.Repositories;

public class RepositoriesTest extends TestCase {
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

    public void testToXML1() throws Exception {
        String expected = IOUtils.readString(new InputStreamReader(getClass()
                .getClassLoader().getResourceAsStream(
                        getClass().getName().replace('.', '/').concat(
                                "_repositories1.xml")), "UTF-8"), true);

        StringWriter actual = new StringWriter();
        mapper.toXML(mapper.toBean(XMLParserFactory.newInstance().parse(
                new StringReader(expected)).getRootElement(),
                Repositories.class), actual);
        assertEquals(expected, actual.toString());
    }

    public static class Hoe {
        private String content;

        public String getContent() {
            return content;
        }

        @Content
        public void setContent(String content) {
            this.content = content;
        }
    }
}
