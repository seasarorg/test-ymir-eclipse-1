package org.seasar.ymir.eclipse.impl;

import java.util.Properties;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.Macro;
import net.skirnir.freyja.TagElement;
import net.skirnir.freyja.TagEvaluator;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.VariableResolver;

import org.seasar.ymir.vili.model.maven.Dependencies;
import org.seasar.ymir.vili.model.maven.Dependency;
import org.seasar.ymir.vili.model.maven.Exclusion;
import org.seasar.ymir.vili.model.maven.Exclusions;

class PomTagEvaluator implements TagEvaluator {
    private static final String LS = System.getProperty("line.separator"); //$NON-NLS-1$

    private static final String DEFAULT_PADDING = "  ";

    private static final int DEFAULT_INDENT = DEFAULT_PADDING.length();

    public String[] getSpecialTagPatternStrings() {
        return new String[] {
                "project", "build", "profiles", "repositories", "repository", "pluginRepositories", "pluginRepository", "url", "dependencies", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
                "dependency", "groupId", "artifactId", "version", "classifier", "type", "scope", "systemPath", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$
                "optional", "exclusions", "exclusion" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String evaluate(TemplateContext context, String name, Attribute[] attributes, Element[] body) {
        PomTemplateContext ctx = (PomTemplateContext) context;
        if (ctx.isTopLevel()) {
            if ("project".equals(name)) { //$NON-NLS-1$
                StringBuilder sb = new StringBuilder();
                sb.append(TagEvaluatorUtils.getBeginTagString(name, attributes)).append(
                        TagEvaluatorUtils.evaluateElements(ctx, body));
                if (!ctx.isDependenciesOutputted()) {
                    sb
                            .append(DEFAULT_PADDING)
                            .append("<dependencies>").append(LS).append(ctx.outputDependenciesString(DEFAULT_INDENT * 2)) //$NON-NLS-1$
                            .append(DEFAULT_PADDING).append("</dependencies>").append(LS); //$NON-NLS-1$
                }
                if (!ctx.isRepositoriesOutputted()) {
                    sb.append(DEFAULT_PADDING).append("<repositories>").append(LS).append( //$NON-NLS-1$
                            ctx.outputRepositoriesString(DEFAULT_INDENT * 2)).append(DEFAULT_PADDING).append(
                            "</repositories>").append(LS); //$NON-NLS-1$
                }
                if (!ctx.isPluginRepositoriesOutputted()) {
                    sb.append(DEFAULT_PADDING).append("<pluginRepositories>").append(LS).append( //$NON-NLS-1$
                            ctx.outputPluginRepositoriesString(DEFAULT_INDENT * 2)).append(DEFAULT_PADDING).append(
                            "</pluginRepositories>").append(LS); //$NON-NLS-1$
                }
                if (!ctx.isProfilesOutputted()) {
                    sb.append(DEFAULT_PADDING).append("<profiles>").append(LS).append( //$NON-NLS-1$
                            ctx.outputProfilesString(DEFAULT_INDENT * 2)).append(DEFAULT_PADDING)
                            .append("</profiles>").append(LS); //$NON-NLS-1$
                }
                sb.append(TagEvaluatorUtils.getEndTagString(name));
                return sb.toString();
            } else if ("build".equals(name)) { //$NON-NLS-1$
                ctx.enter();
                try {
                    return TagEvaluatorUtils.evaluate(ctx, name, attributes, body);
                } finally {
                    ctx.leave();
                }
            } else if ("dependencies".equals(name)) { //$NON-NLS-1$
                ctx.setDependencies(buildDependencies(ctx, (TagElement) ctx.getElement()));
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + ctx.outputDependenciesString(getCurrentIndent(ctx) * 2)
                        + TagEvaluatorUtils.getEndTagString(name);
            } else if ("repositories".equals(name)) { //$NON-NLS-1$
                for (Element elem : body) {
                    if (elem instanceof TagElement && "repository".equals(((TagElement) elem).getName())) { //$NON-NLS-1$
                        ctx.removeRepository((TagElement) elem);
                    }
                }
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + TagEvaluatorUtils.evaluateElements(ctx, body)
                        + ctx.outputRepositoriesString(getCurrentIndent(ctx) * 2)
                        + TagEvaluatorUtils.getEndTagString(name);
            } else if ("pluginRepositories".equals(name)) { //$NON-NLS-1$
                for (Element elem : body) {
                    if (elem instanceof TagElement && "pluginRepository".equals(((TagElement) elem).getName())) { //$NON-NLS-1$
                        ctx.removePluginRepository((TagElement) elem);
                    }
                }
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + TagEvaluatorUtils.evaluateElements(ctx, body)
                        + ctx.outputPluginRepositoriesString(getCurrentIndent(ctx) * 2)
                        + TagEvaluatorUtils.getEndTagString(name);
            } else if ("profiles".equals(name)) { //$NON-NLS-1$
                ctx.enter();
                try {
                    return TagEvaluatorUtils.getBeginTagString(name, attributes)
                            + TagEvaluatorUtils.evaluateElements(ctx, body)
                            + ctx.outputProfilesString(getCurrentIndent(ctx) * 2)
                            + TagEvaluatorUtils.getEndTagString(name);
                } finally {
                    ctx.leave();
                }
            } else {
                return TagEvaluatorUtils.evaluate(ctx, name, attributes, body);
            }
        } else {
            return TagEvaluatorUtils.evaluate(ctx, name, attributes, body);
        }
    }

    int getCurrentIndent(TemplateContext context) {
        return context.getElement().getColumnNumber() - 1;
    }

    Dependencies buildDependencies(TemplateContext context, TagElement element) {
        Dependencies dependencies = new Dependencies();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            String tagName = tag.getName();
            if ("dependency".equals(tagName)) { //$NON-NLS-1$
                dependencies.addDependency(buildDependency(context, tag));
            }
        }
        return dependencies;
    }

    Dependency buildDependency(TemplateContext context, TagElement element) {
        Dependency dependency = new Dependency();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            String tagName = tag.getName();
            if ("groupId".equals(tagName)) { //$NON-NLS-1$
                dependency.setGroupId(getBodyAsString(context, tag));
            } else if ("artifactId".equals(tagName)) { //$NON-NLS-1$
                dependency.setArtifactId(getBodyAsString(context, tag));
            } else if ("version".equals(tagName)) { //$NON-NLS-1$
                dependency.setVersion(getBodyAsString(context, tag));
            } else if ("classifier".equals(tagName)) { //$NON-NLS-1$
                dependency.setClassifier(getBodyAsString(context, tag));
            } else if ("type".equals(tagName)) { //$NON-NLS-1$
                dependency.setType(getBodyAsString(context, tag));
            } else if ("scope".equals(tagName)) { //$NON-NLS-1$
                dependency.setScope(getBodyAsString(context, tag));
            } else if ("systemPath".equals(tagName)) { //$NON-NLS-1$
                dependency.setSystemPath(getBodyAsString(context, tag));
            } else if ("optional".equals(tagName)) { //$NON-NLS-1$
                dependency.setOptional(getBodyAsString(context, tag));
            } else if ("exclusions".equals(tagName)) { //$NON-NLS-1$
                dependency.setExclusions(buildExclusions(context, tag));
            }
        }
        return dependency;
    }

    String getBodyAsString(TemplateContext context, TagElement element) {
        return TagEvaluatorUtils.evaluateElements(context, element.getBodyElements()).trim();
    }

    Exclusions buildExclusions(TemplateContext context, TagElement element) {
        Exclusions exclusions = new Exclusions();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            if ("exclusion".equals(tag.getName())) {
                exclusions.addExclusion(buildExclusion(context, tag));
            }
        }
        return exclusions;
    }

    Exclusion buildExclusion(TemplateContext context, TagElement element) {
        Exclusion exclusion = new Exclusion();
        for (Element elem : element.getBodyElements()) {
            if (!(elem instanceof TagElement)) {
                continue;
            }
            TagElement tag = (TagElement) elem;
            String tagName = tag.getName();
            if ("groupId".equals(tagName)) { //$NON-NLS-1$
                exclusion.setGroupId(getBodyAsString(context, tag));
            } else if ("artifactId".equals(tagName)) { //$NON-NLS-1$
                exclusion.setArtifactId(getBodyAsString(context, tag));
            }
        }
        return exclusion;
    }

    public String[] getSpecialAttributePatternStrings() {
        return null;
    }

    public Element expandMacroVariables(TemplateContext context, VariableResolver macroVarResolver, String name,
            Attribute[] attributes, Element[] body) {
        return null;
    }

    public void gatherMacroVariables(TemplateContext context, VariableResolver macroVarResolver, String name,
            Attribute[] attributes, Element[] body) {
    }

    public Macro getMacro(TemplateEvaluator evaluator, String name, Attribute[] attributes, Element[] body,
            String macroName, Element previousElement) {
        return null;
    }

    public String getProperty(String key) {
        return null;
    }

    public TemplateContext newContext() {
        return new PomTemplateContext();
    }

    public void setProperties(Properties properties) {
    }
}
