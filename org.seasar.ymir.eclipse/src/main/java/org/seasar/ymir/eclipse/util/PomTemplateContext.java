package org.seasar.ymir.eclipse.util;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.skirnir.freyja.Element;
import net.skirnir.freyja.TagElement;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.impl.TemplateContextImpl;
import net.skirnir.xom.ValidationException;
import net.skirnir.xom.XOMapper;

import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.eclipse.maven.Dependency;
import org.seasar.ymir.eclipse.maven.Project;
import org.seasar.ymir.eclipse.maven.Repository;

class PomTemplateContext extends TemplateContextImpl {
    private Set<Repository> repositorySet = new HashSet<Repository>();

    private Set<Dependency> dependencySet = new HashSet<Dependency>();

    private int depth;

    private boolean repositoryOutputted;

    private boolean dependencyOutputted;

    public void setMetadataToAdd(Project project) {
        if (project.getRepositories() != null) {
            repositorySet.addAll(Arrays.asList(project.getRepositories().getRepositories()));
        }
        if (project.getDependencies() != null) {
            dependencySet.addAll(Arrays.asList(project.getDependencies().getDependencies()));
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
            if ("url".equals(tag.getName())) {
                repository.setUrl(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            }
        }
        repositorySet.remove(repository);
    }

    public void removeDependency(TagElement element) {
        Dependency dependency = new Dependency();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            if ("groupId".equals(tag.getName())) {
                dependency.setGroupId(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            } else if ("artifactId".equals(tag.getName())) {
                dependency.setArtifactId(TagEvaluatorUtils.evaluateElements(this, tag.getBodyElements()).trim());
            }
        }
        dependencySet.remove(dependency);
    }

    public String outputRepositoriesString() {
        StringWriter sw = new StringWriter();
        for (Repository repository : repositorySet) {
            try {
                getXOMapper().toXML(repository, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        repositoryOutputted = true;
        return sw.toString();
    }

    public String outputDependenciesString() {
        StringWriter sw = new StringWriter();
        for (Dependency dependency : dependencySet) {
            try {
                getXOMapper().toXML(dependency, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        dependencyOutputted = true;
        return sw.toString();
    }

    XOMapper getXOMapper() {
        return Activator.getDefault().getXOMapper();
    }

    public boolean isRepositoryOutputted() {
        return repositoryOutputted;
    }

    public boolean isDependenciesOutputted() {
        return dependencyOutputted;
    }
}
