package org.seasar.ymir.eclipse.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import net.skirnir.freyja.Element;
import net.skirnir.freyja.TagElement;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.impl.TemplateContextImpl;
import net.skirnir.xom.ValidationException;

import org.eclipse.core.resources.IProject;
import org.seasar.ymir.vili.ViliBehavior;
import org.seasar.ymir.vili.ViliProjectPreferences;
import org.seasar.ymir.vili.maven.util.ArtifactUtils;
import org.seasar.ymir.vili.model.maven.Dependencies;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.PluginRepository;
import org.seasar.ymir.vili.model.maven.Profile;
import org.seasar.ymir.vili.model.maven.Project;
import org.seasar.ymir.vili.model.maven.Repository;
import org.seasar.ymir.vili.util.ViliUtils;
import org.seasar.ymir.vili.util.XOMUtils;

class PomTemplateContext extends TemplateContextImpl {
    private Map<Dependency, Dependency> dependencyMap = new LinkedHashMap<Dependency, Dependency>();

    private Map<Dependency, Dependency> fragmentDependencyMap = new LinkedHashMap<Dependency, Dependency>();

    private Set<Repository> fragmentRepositorySet = new LinkedHashSet<Repository>();

    private Set<PluginRepository> fragmentPluginRepositorySet = new LinkedHashSet<PluginRepository>();

    private List<Profile> fragmentProfileList = new ArrayList<Profile>();

    private int depth;

    private boolean dependenciesOutputted;

    private boolean repositoriesOutputted;

    private boolean pluginRepositoriesOutputted;

    private boolean profilesOutputted;

    private IProject project;

    private ViliBehavior behavior;

    private ViliProjectPreferences preferences;

    private Map<String, Object> parameters;

    public void setMetadataToMerge(Project pom, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        this.project = project;
        this.behavior = behavior;
        this.preferences = preferences;
        this.parameters = parameters;
        if (pom.getDependencies() != null) {
            for (Dependency dependency : pom.getDependencies().getDependencies()) {
                fragmentDependencyMap.put(dependency, dependency);
            }
        }
        if (pom.getRepositories() != null) {
            fragmentRepositorySet.addAll(Arrays.asList(pom.getRepositories().getRepositories()));
        }
        if (pom.getPluginRepositories() != null) {
            fragmentPluginRepositorySet.addAll(Arrays.asList(pom.getPluginRepositories().getPluginRepositories()));
        }
        if (pom.getProfiles() != null) {
            fragmentProfileList.addAll(Arrays.asList(pom.getProfiles().getProfiles()));
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
        fragmentRepositorySet.remove(repository);
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
        fragmentPluginRepositorySet.remove(pluginRepository);
    }

    public String outputDependenciesString(int indent) {
        Dependency[] dependencies = behavior.getConfigurator().mergePomDependencies(dependencyMap,
                fragmentDependencyMap, project, behavior, preferences, parameters);
        if (dependencies == null) {
            dependencies = mergePomDependencies(dependencyMap, fragmentDependencyMap, project, behavior, preferences,
                    parameters);
        }
        StringWriter sw = new StringWriter();
        for (Dependency dependency : dependencies) {
            try {
                XOMUtils.getXOMapper().toXML(dependency, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        dependenciesOutputted = true;
        return ViliUtils.addIndent(sw.toString(), ViliUtils.padding(indent));
    }

    Dependency[] mergePomDependencies(Map<Dependency, Dependency> dependencyMap,
            Map<Dependency, Dependency> fragmentDependencyMap, IProject project, ViliBehavior behavior,
            ViliProjectPreferences preferences, Map<String, Object> parameters) {
        List<Dependency> list = new ArrayList<Dependency>();
        for (Dependency dependency : dependencyMap.values()) {
            Dependency fragmentDependency = fragmentDependencyMap.remove(dependency);
            if (fragmentDependency != null
                    && ArtifactUtils.compareVersions(fragmentDependency.getVersion(), dependency.getVersion()) > 0) {
                list.add(fragmentDependency);
            } else {
                list.add(dependency);
            }
        }
        for (Dependency dependency : fragmentDependencyMap.values()) {
            list.add(dependency);
        }

        return list.toArray(new Dependency[0]);
    }

    public String outputRepositoriesString(int indent) {
        StringWriter sw = new StringWriter();
        for (Repository repository : fragmentRepositorySet) {
            try {
                XOMUtils.getXOMapper().toXML(repository, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        repositoriesOutputted = true;
        return ViliUtils.addIndent(sw.toString(), ViliUtils.padding(indent));
    }

    public String outputPluginRepositoriesString(int indent) {
        StringWriter sw = new StringWriter();
        for (PluginRepository pluginRepository : fragmentPluginRepositorySet) {
            try {
                XOMUtils.getXOMapper().toXML(pluginRepository, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        pluginRepositoriesOutputted = true;
        return ViliUtils.addIndent(sw.toString(), ViliUtils.padding(indent));
    }

    public String outputProfilesString(int indent) {
        StringWriter sw = new StringWriter();
        for (Profile profile : fragmentProfileList) {
            try {
                XOMUtils.getXOMapper().toXML(profile, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        profilesOutputted = true;
        return ViliUtils.addIndent(sw.toString(), ViliUtils.padding(indent));
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

    public void setDependencies(Dependencies dependencies) {
        dependencyMap.clear();
        for (Dependency dependency : dependencies.getDependencies()) {
            dependencyMap.put(dependency, dependency);
        }
    }
}
