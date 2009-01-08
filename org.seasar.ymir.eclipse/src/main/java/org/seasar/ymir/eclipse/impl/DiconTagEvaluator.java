package org.seasar.ymir.eclipse.impl;

import java.util.Map;
import java.util.Properties;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.Macro;
import net.skirnir.freyja.TagEvaluator;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.VariableResolver;

class DiconTagEvaluator implements TagEvaluator {
    private static final String SP = System.getProperty("line.separator"); //$NON-NLS-1$

    public String[] getSpecialTagPatternStrings() {
        return new String[] { "components", "include", "component", "meta", }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public String evaluate(TemplateContext context, String name, Attribute[] attributes, Element[] body) {
        DiconTemplateContext ctx = (DiconTemplateContext) context;
        @SuppressWarnings("unchecked")
        Map<String, Attribute> attrMap = TagEvaluatorUtils.toMap(attributes);

        StringBuilder sb = new StringBuilder();
        if ("components".equals(name)) { //$NON-NLS-1$
            sb.append(TagEvaluatorUtils.getBeginTagString(name, attributes)).append(
                    TagEvaluatorUtils.evaluateElements(context, body));
            if (!ctx.isIncludeOutputted()) {
                sb.append(ctx.outputIncludeString(true));
            }
            sb.append(ctx.outputComponentString());
            sb.append(ctx.outputMetaString());
            sb.append(TagEvaluatorUtils.getEndTagString(name));
        } else {
            if ("include".equals(name)) {
                ctx.removeInclude(getValue(attrMap.get("path")));
            } else if ("component".equals(name)) {
                ctx.removeComponent(getValue(attrMap.get("class")), getValue(attrMap.get("name")));
                if (!ctx.isIncludeOutputted()) {
                    sb.append(ctx.outputIncludeString(false));
                }
            } else if ("meta".equals(name)) {
                ctx.removeMeta(getValue(attrMap.get("name")));
                if (!ctx.isIncludeOutputted()) {
                    sb.append(ctx.outputIncludeString(false));
                }
            }

            sb.append(TagEvaluatorUtils.evaluate(context, name, attributes, body));
        }
        return sb.toString();
    }

    private String getValue(Attribute attribute) {
        if (attribute == null) {
            return null;
        } else {
            return TagEvaluatorUtils.defilter(attribute.getValue());
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
        return new DiconTemplateContext();
    }

    public void setProperties(Properties properties) {
    }
}
