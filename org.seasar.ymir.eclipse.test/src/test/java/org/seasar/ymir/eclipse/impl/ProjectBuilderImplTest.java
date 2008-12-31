package org.seasar.ymir.eclipse.impl;

import java.beans.Introspector;
import java.io.InputStreamReader;

import junit.framework.TestCase;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.impl.TemplateEvaluatorImpl;
import net.skirnir.xom.Attribute;
import net.skirnir.xom.BeanAccessor;
import net.skirnir.xom.BeanAccessorFactory;
import net.skirnir.xom.Element;
import net.skirnir.xom.Node;
import net.skirnir.xom.Text;
import net.skirnir.xom.XOMapper;
import net.skirnir.xom.XOMapperFactory;
import net.skirnir.xom.annotation.impl.AnnotationBeanAccessor;

import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.vili.model.maven.Dependencies;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.PluginRepositories;
import org.seasar.ymir.vili.model.maven.PluginRepository;
import org.seasar.ymir.vili.model.maven.Profile;
import org.seasar.ymir.vili.model.maven.Profiles;
import org.seasar.ymir.vili.model.maven.Project;
import org.seasar.ymir.vili.model.maven.Repositories;
import org.seasar.ymir.vili.model.maven.Repository;

public class ProjectBuilderImplTest extends TestCase {
    private ProjectBuilderImpl target;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        target = new ProjectBuilderImpl(null);
        target.setTemplateEvaluator(new TemplateEvaluatorImpl(new PomTagEvaluator() {
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
        project.setPluginRepositories(new PluginRepositories(new PluginRepository("codehaus snapshot repository", null,
                "http://snapshots.repository.codehaus.org/", true)));
        project.setDependencies(new Dependencies(new Dependency("group1", "artifact1", "version1"), new Dependency(
                "org.seasar.container", "s2-extension", "1.0.0")));
        Profile profile = new Profile();
        profile.addElement(new Element("id", new Attribute[0], new Node[] { new Text("executable") }));
        project.setProfiles(new Profiles(profile));
        assertEquals(IOUtils.readString(getClass().getResourceAsStream("pom_expected.xml"), "UTF-8", false), target
                .addToPom(new InputStreamReader(getClass().getResourceAsStream("pom.xml"), "UTF-8"), project));
    }
}
