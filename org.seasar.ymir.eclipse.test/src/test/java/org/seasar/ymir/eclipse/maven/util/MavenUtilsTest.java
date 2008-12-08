package org.seasar.ymir.eclipse.maven.util;

import java.beans.Introspector;
import java.io.InputStreamReader;

import junit.framework.TestCase;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.impl.TemplateEvaluatorImpl;
import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.eclipse.maven.Dependencies;
import org.seasar.ymir.eclipse.maven.Dependency;
import org.seasar.ymir.eclipse.maven.Project;
import org.seasar.ymir.eclipse.maven.Repositories;
import org.seasar.ymir.eclipse.maven.Repository;
import org.seasar.ymir.eclipse.maven.util.MavenUtils;
import org.seasar.ymir.eclipse.maven.util.PomExpressionEvaluator;
import org.seasar.ymir.eclipse.maven.util.PomTagEvaluator;
import org.seasar.ymir.eclipse.maven.util.PomTemplateContext;

public class MavenUtilsTest extends TestCase {
    @Override
    protected void setUp() throws Exception {
        super.setUp();
        MavenUtils.setTemplateEvaluator(new TemplateEvaluatorImpl(new PomTagEvaluator() {
            private XOMapper mapper = XOMapperFactory.newInstance().setBeanAccessorFactory(new BeanAccessorFactory() {
                public BeanAccessor newInstance() {
                    return new AnnotationBeanAccessor() {
                        @Override
                        protected String toXMLName(String javaName) {
                            return Introspector.decapitalize(javaName);
                        }
                    };
                }
            }).setStrict(false);

            public TemplateContext newContext() {
                return new PomTemplateContext() {
                    @Override
                    XOMapper getXOMapper() {
                        return mapper;
                    }
                };
            }
        }, new PomExpressionEvaluator()));
    }

    public void testAddToPom() throws Exception {
        Project project = new Project();
        project.setRepositories(new Repositories(new Repository("www.seasar.org",
                "The Seasar Foundation Maven2 Repository", "http://maven.seasar.org/maven2"), new Repository(
                "snapshot.maven.seasar.org", "The Seasar Foundation Maven2 Snapshot Repository",
                "http://maven.seasar.org/maven2-snapshot", true)));
        project.setDependencies(new Dependencies(new Dependency("group1", "artifact1", "version1"), new Dependency(
                "org.seasar.container", "s2-extension", "1.0.0")));
        assertEquals(IOUtils.readString(getClass().getResourceAsStream("pom_expected.xml"), "UTF-8", false), MavenUtils
                .addToPom(new InputStreamReader(getClass().getResourceAsStream("pom.xml"), "UTF-8"), project));
    }
}
