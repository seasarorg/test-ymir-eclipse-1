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

class PomTagEvaluator implements TagEvaluator {
    private static final String SP = System.getProperty("line.separator"); //$NON-NLS-1$

    public String[] getSpecialTagPatternStrings() {
        return new String[] {
                "project", "build", "profiles", "repositories", "repository", "pluginRepositories", "pluginRepository", "url", "dependencies", //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$ //$NON-NLS-7$ //$NON-NLS-8$ //$NON-NLS-9$
                "dependency", "groupId", "artifactId" }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
    }

    public String evaluate(TemplateContext context, String name, Attribute[] attributes, Element[] body) {
        PomTemplateContext ctx = (PomTemplateContext) context;
        if (ctx.isTopLevel()) {
            if ("project".equals(name)) { //$NON-NLS-1$
                StringBuilder sb = new StringBuilder();
                sb.append(TagEvaluatorUtils.getBeginTagString(name, attributes)).append(
                        TagEvaluatorUtils.evaluateElements(context, body));
                if (!ctx.isDependenciesOutputted()) {
                    sb.append("<dependencies>").append(SP).append(ctx.outputDependenciesString()).append( //$NON-NLS-1$
                            "</dependencies>").append(SP); //$NON-NLS-1$
                }
                if (!ctx.isRepositoriesOutputted()) {
                    sb.append("<repositories>").append(SP).append(ctx.outputRepositoriesString()).append( //$NON-NLS-1$
                            "</repositories>").append(SP); //$NON-NLS-1$
                }
                if (!ctx.isPluginRepositoriesOutputted()) {
                    sb.append("<pluginRepositories>").append(SP).append(ctx.outputPluginRepositoriesString()).append( //$NON-NLS-1$
                            "</pluginRepositories>").append(SP); //$NON-NLS-1$
                }
                if (!ctx.isProfilesOutputted()) {
                    sb.append("<profiles>").append(SP).append(ctx.outputProfilesString()).append( //$NON-NLS-1$
                            "</profiles>").append(SP); //$NON-NLS-1$
                }
                sb.append(TagEvaluatorUtils.getEndTagString(name));
                return sb.toString();
            } else if ("build".equals(name)) { //$NON-NLS-1$
                ctx.enter();
                try {
                    return TagEvaluatorUtils.evaluate(context, name, attributes, body);
                } finally {
                    ctx.leave();
                }
            } else if ("dependencies".equals(name)) { //$NON-NLS-1$
                for (Element elem : body) {
                    if (elem instanceof TagElement && "dependency".equals(((TagElement) elem).getName())) { //$NON-NLS-1$
                        ctx.removeDependency((TagElement) elem);
                    }
                }
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + TagEvaluatorUtils.evaluateElements(context, body) + ctx.outputDependenciesString()
                        + TagEvaluatorUtils.getEndTagString(name);
            } else if ("repositories".equals(name)) { //$NON-NLS-1$
                for (Element elem : body) {
                    if (elem instanceof TagElement && "repository".equals(((TagElement) elem).getName())) { //$NON-NLS-1$
                        ctx.removeRepository((TagElement) elem);
                    }
                }
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + TagEvaluatorUtils.evaluateElements(context, body) + ctx.outputRepositoriesString()
                        + TagEvaluatorUtils.getEndTagString(name);
            } else if ("pluginRepositories".equals(name)) { //$NON-NLS-1$
                for (Element elem : body) {
                    if (elem instanceof TagElement && "pluginRepository".equals(((TagElement) elem).getName())) { //$NON-NLS-1$
                        ctx.removePluginRepository((TagElement) elem);
                    }
                }
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + TagEvaluatorUtils.evaluateElements(context, body) + ctx.outputPluginRepositoriesString()
                        + TagEvaluatorUtils.getEndTagString(name);
            } else if ("profiles".equals(name)) { //$NON-NLS-1$
                ctx.enter();
                try {
                    return TagEvaluatorUtils.getBeginTagString(name, attributes)
                            + TagEvaluatorUtils.evaluateElements(context, body) + ctx.outputProfilesString()
                            + TagEvaluatorUtils.getEndTagString(name);
                } finally {
                    ctx.leave();
                }
            } else {
                return TagEvaluatorUtils.evaluate(context, name, attributes, body);
            }
        } else {
            return TagEvaluatorUtils.evaluate(context, name, attributes, body);
        }
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