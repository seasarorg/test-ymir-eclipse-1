package org.seasar.ymir.eclipse.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.skirnir.freyja.ConstantElement;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.TagElement;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.impl.TemplateContextImpl;
import net.skirnir.xom.ValidationException;

import org.eclipse.core.resources.IProject;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.ArtifactVersion;
import org.seasar.ymir.vili.model.maven.Dependencies;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.PluginRepository;
import org.seasar.ymir.vili.model.maven.Profile;
import org.seasar.ymir.vili.model.maven.Project;
import org.seasar.ymir.vili.model.maven.Repository;
import org.seasar.ymir.vili.util.XOMUtils;

class PomTemplateContext extends TemplateContextImpl {
    private static final int DEFAULT_DEPENDENCY_INDENT = 4;

    private Map<Dependency, Dependency> dependencyMap = new HashMap<Dependency, Dependency>();

    private Set<Repository> repositorySet = new HashSet<Repository>();

    private Set<PluginRepository> pluginRepositorySet = new HashSet<PluginRepository>();

    private List<Profile> profileList = new ArrayList<Profile>();

    private int depth;

    private boolean dependenciesOutputted;

    private boolean repositoriesOutputted;

    private boolean pluginRepositoriesOutputted;

    private boolean profilesOutputted;

    private IProject project;

    private ViliBehavior behavior;

    private ViliProjectPreferences preferences;

    private Map<String, Object> parameters;

    private int dependencyIndent = DEFAULT_DEPENDENCY_INDENT;

    public void setMetadataToMerge(Project pom, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        this.project = project;
        this.behavior = behavior;
        this.preferences = preferences;
        this.parameters = parameters;
        if (pom.getDependencies() != null) {
            for (Dependency dependency : pom.getDependencies().getDependencies()) {
                dependencyMap.put(dependency, dependency);
            }
        }
        if (pom.getRepositories() != null) {
            repositorySet.addAll(Arrays.asList(pom.getRepositories().getRepositories()));
        }
        if (pom.getPluginRepositories() != null) {
            pluginRepositorySet.addAll(Arrays.asList(pom.getPluginRepositories().getPluginRepositories()));
        }
        if (pom.getProfiles() != null) {
            profileList.addAll(Arrays.asList(pom.getProfiles().getProfiles()));
        }
    }

    public void enter() {
        depth++;
    }

    public void leave() {
        depth--;
    }

    public boolean isTopLevel() {
        return depth == 0;
    }

    public TagElement mergeDependency(TagElement element) {
        Dependency dependency = new Dependency();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            if ("groupId".equals(tag.getName())) { //$NON-NLS-1$
                dependency.setGroupId(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            } else if ("artifactId".equals(tag.getName())) { //$NON-NLS-1$
                dependency.setArtifactId(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            } else if ("version".equals(tag.getName())) { //$NON-NLS-1$
                dependency.setVersion(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            }
        }

        Dependency dep = dependencyMap.remove(dependency);
        if (dep == null
                || new ArtifactVersion(dep.getVersion()).compareTo(new ArtifactVersion(dependency.getVersion())) <= 0) {
            return element;
        } else {
            // フラグメントのpom.xmlで指定されているdependencyと同一groupId、artifactIdのdependencyが既に存在している場合は、
            // フラグメント側のバージョンの方が新しい時にだけ、バージョン情報だけをフラグメントのものに変更する。
            // バージョン以外の情報については、プロジェクト生成後にカスタマイズが入っている可能性があるのでそのまま保存しておく。
            // XXX そうするとバージョン以外の情報を更新できないことになる。それでもいいのか？
            // Configuratorで、特定のgroupId、artifactIdのdependencyを好きなようにマージできるようにできるようにしてもいいかも。
            List<Element> bodyList = new ArrayList<Element>();
            for (Element elem : element.getBodyElements()) {
                if (elem instanceof TagElement) {
                    TagElement tag = (TagElement) elem;
                    if ("version".equals(tag.getName())) { //$NON-NLS-1$
                        elem = new TagElement(tag.getName(), tag.getAttributes(), new Element[] { new ConstantElement(
                                dep.getVersion()) });
                    }
                }
                bodyList.add(elem);
            }

            return new TagElement(element.getName(), element.getAttributes(), bodyList.toArray(new Element[0]));
        }
    }

    public void removeRepository(TagElement element) {
        Repository repository = new Repository();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            if ("url".equals(tag.getName())) { //$NON-NLS-1$
                repository.setUrl(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            }
        }
        repositorySet.remove(repository);
    }

    public void removePluginRepository(TagElement element) {
        PluginRepository pluginRepository = new PluginRepository();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            if ("url".equals(tag.getName())) { //$NON-NLS-1$
                pluginRepository.setUrl(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            }
        }
        pluginRepositorySet.remove(pluginRepository);
    }

    public String outputDependenciesString() {
        StringWriter sw = new StringWriter();
        for (Dependency dependency : dependencyMap.values()) {
            try {
                XOMUtils.getXOMapper().toXML(dependency, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        dependenciesOutputted = true;
        return sw.toString();
    }

    public String outputRepositoriesString() {
        StringWriter sw = new StringWriter();
        for (Repository repository : repositorySet) {
            try {
                XOMUtils.getXOMapper().toXML(repository, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        repositoriesOutputted = true;
        return sw.toString();
    }

    public String outputPluginRepositoriesString() {
        StringWriter sw = new StringWriter();
        for (PluginRepository pluginRepository : pluginRepositorySet) {
            try {
                XOMUtils.getXOMapper().toXML(pluginRepository, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        pluginRepositoriesOutputted = true;
        return sw.toString();
    }

    public String outputProfilesString() {
        StringWriter sw = new StringWriter();
        for (Profile profile : profileList) {
            try {
                XOMUtils.getXOMapper().toXML(profile, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        profilesOutputted = true;
        return sw.toString();
    }

    public boolean isDependenciesOutputted() {
        return dependenciesOutputted;
    }

    public boolean isRepositoriesOutputted() {
        return repositoriesOutputted;
    }

    public boolean isPluginRepositoriesOutputted() {
        return pluginRepositoriesOutputted;
    }

    public boolean isProfilesOutputted() {
        return profilesOutputted;
    }

    public void setDependencyIndent(int dependencyIndent) {
        this.dependencyIndent = dependencyIndent;
    }

    public void setDependencies(Dependencies dependencies) {
        // TODO Auto-generated method stub

    }
}
