package org.seasar.ymir.eclipse.util;

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
    private static final String SP = System.getProperty("line.separator");

    public String[] getSpecialTagPatternStrings() {
        return new String[] { "project", "build", "profiles", "repositories", "repository", "url", "dependencies",
                "dependency", "groupId", "artifactId" };
    }

    public String evaluate(TemplateContext context, String name, Attribute[] attributes, Element[] body) {
        PomTemplateContext ctx = (PomTemplateContext) context;
        if (ctx.isTopLevel()) {
            if ("project".equals(name)) {
                StringBuilder sb = new StringBuilder();
                sb.append(TagEvaluatorUtils.getBeginTagString(name, attributes)).append(
                        TagEvaluatorUtils.evaluateElements(context, body));
                if (!ctx.isRepositoryOutputted()) {
                    sb.append("<repositories>").append(SP).append(ctx.outputRepositoriesString()).append(
                            "</repositories>").append(SP);
                }
                if (!ctx.isDependenciesOutputted()) {
                    sb.append("<dependencies>").append(SP).append(ctx.outputDependenciesString()).append(
                            "</dependencies>").append(SP);
                }
                sb.append(TagEvaluatorUtils.getEndTagString(name));
                return sb.toString();
            } else if ("build".equals(name) || "profiles".equals(name)) {
                ctx.enter();
                try {
                    return TagEvaluatorUtils.evaluate(context, name, attributes, body);
                } finally {
                    ctx.leave();
                }
            } else if ("repositories".equals(name)) {
                for (Element elem : body) {
                    if (elem instanceof TagElement && "repository".equals(((TagElement) elem).getName())) {
                        ctx.removeRepository((TagElement) elem);
                    }
                }
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + TagEvaluatorUtils.evaluateElements(context, body) + ctx.outputRepositoriesString()
                        + TagEvaluatorUtils.getEndTagString(name);
            } else if ("dependencies".equals(name)) {
                for (Element elem : body) {
                    if (elem instanceof TagElement && "dependency".equals(((TagElement) elem).getName())) {
                        ctx.removeDependency((TagElement) elem);
                    }
                }
                return TagEvaluatorUtils.getBeginTagString(name, attributes)
                        + TagEvaluatorUtils.evaluateElements(context, body) + ctx.outputDependenciesString()
                        + TagEvaluatorUtils.getEndTagString(name);
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
