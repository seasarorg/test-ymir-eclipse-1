package org.seasar.ymir.eclipse.maven.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.skirnir.freyja.Element;
import net.skirnir.freyja.TagElement;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.impl.TemplateContextImpl;
import net.skirnir.xom.ValidationException;

import org.seasar.ymir.eclipse.util.XOMUtils;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.PluginRepository;
import org.seasar.ymir.vili.model.maven.Profile;
import org.seasar.ymir.vili.model.maven.Project;
import org.seasar.ymir.vili.model.maven.Repository;

class PomTemplateContext extends TemplateContextImpl {
    private Set<Dependency> dependencySet = new HashSet<Dependency>();

    private Set<Repository> repositorySet = new HashSet<Repository>();

    private Set<PluginRepository> pluginRepositorySet = new HashSet<PluginRepository>();

    private List<Profile> profileList = new ArrayList<Profile>();

    private int depth;

    private boolean dependenciesOutputted;

    private boolean repositoriesOutputted;

    private boolean pluginRepositoriesOutputted;

    private boolean profilesOutputted;

    public void setMetadataToAdd(Project project) {
        if (project.getDependencies() != null) {
            dependencySet.addAll(Arrays.asList(project.getDependencies().getDependencies()));
        }
        if (project.getRepositories() != null) {
            repositorySet.addAll(Arrays.asList(project.getRepositories().getRepositories()));
        }
        if (project.getPluginRepositories() != null) {
            pluginRepositorySet.addAll(Arrays.asList(project.getPluginRepositories().getPluginRepositories()));
        }
        if (project.getProfiles() != null) {
            profileList.addAll(Arrays.asList(project.getProfiles().getProfiles()));
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

    public void removeDependency(TagElement element) {
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
            }
        }
        dependencySet.remove(dependency);
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
        for (Dependency dependency : dependencySet) {
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
}
