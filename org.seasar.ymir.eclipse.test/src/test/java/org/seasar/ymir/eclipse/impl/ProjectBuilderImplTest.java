package org.seasar.ymir.eclipse.impl;

import java.io.InputStreamReader;

import junit.framework.TestCase;
import net.skirnir.xom.Attribute;
import net.skirnir.xom.Element;
import net.skirnir.xom.Node;
import net.skirnir.xom.Text;

import org.seasar.kvasir.util.io.IOUtils;
import org.seasar.ymir.vili.model.dicon.Component;
import org.seasar.ymir.vili.model.dicon.Components;
import org.seasar.ymir.vili.model.dicon.Include;
import org.seasar.ymir.vili.model.dicon.Meta;
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
    }

    public void testMergePom() throws Exception {
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
                .mergePom(new InputStreamReader(getClass().getResourceAsStream("pom.xml"), "UTF-8"), project));
    }

    public void testMergeDicon1_混合パターン() throws Exception {
        Components dicon = new Components();
        dicon.setComponents(new Component("com.example.Hoe"), new Component("com.example.Hoe2"), new Component(
                "com.example.Class1", "class1"), new Component("com.example.Class2", "class2"));
        dicon.setIncludes(new Include("path1"), new Include("path2"));
        dicon.setMetas(new Meta(), new Meta("meta1"), new Meta("meta2"));
        assertEquals(IOUtils.readString(getClass().getResourceAsStream("dicon1_expected.dicon"), "UTF-8", false),
                target
                        .mergeDicon(new InputStreamReader(getClass().getResourceAsStream("dicon1.dicon"), "UTF-8"),
                                dicon));
    }

    public void testMergeDicon2_componentやmetaの前にincludeが挿入されること_最初がcomponent() throws Exception {
        Components dicon = new Components();
        dicon.setIncludes(new Include("path"));
        assertEquals(IOUtils.readString(getClass().getResourceAsStream("dicon2_expected.dicon"), "UTF-8", false),
                target
                        .mergeDicon(new InputStreamReader(getClass().getResourceAsStream("dicon2.dicon"), "UTF-8"),
                                dicon));
    }

    public void testMergeDicon3_componentやmetaの前にincludeが挿入されること_最初がmeta() throws Exception {
        Components dicon = new Components();
        dicon.setIncludes(new Include("path"));
        assertEquals(IOUtils.readString(getClass().getResourceAsStream("dicon3_expected.dicon"), "UTF-8", false),
                target
                        .mergeDicon(new InputStreamReader(getClass().getResourceAsStream("dicon3.dicon"), "UTF-8"),
                                dicon));
    }

    public void testMergeDicon4_componentやmetaの前にincludeが挿入されること_componentもmetaもない() throws Exception {
        Components dicon = new Components();
        dicon.setIncludes(new Include("path"));
        assertEquals(IOUtils.readString(getClass().getResourceAsStream("dicon4_expected.dicon"), "UTF-8", false),
                target
                        .mergeDicon(new InputStreamReader(getClass().getResourceAsStream("dicon4.dicon"), "UTF-8"),
                                dicon));
    }
}
