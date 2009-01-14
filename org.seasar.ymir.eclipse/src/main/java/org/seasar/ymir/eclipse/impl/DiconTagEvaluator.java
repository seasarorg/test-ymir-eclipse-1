package org.seasar.ymir.eclipse.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import net.skirnir.freyja.Attribute;
import net.skirnir.freyja.ConstantElement;
import net.skirnir.freyja.Element;
import net.skirnir.freyja.Macro;
import net.skirnir.freyja.TagEvaluator;
import net.skirnir.freyja.TagEvaluatorUtils;
import net.skirnir.freyja.TemplateContext;
import net.skirnir.freyja.TemplateEvaluator;
import net.skirnir.freyja.VariableResolver;
import net.skirnir.xom.XOMapper;

import org.seasar.kvasir.util.PropertyUtils;
import org.seasar.kvasir.util.StringUtils;
import org.seasar.ymir.vili.model.dicon.Meta;
import org.seasar.ymir.vili.util.XOMUtils;

class DiconTagEvaluator implements TagEvaluator {
    static final String METANAME_EXPAND = "expand"; //$NON-NLS-1$

    private static final String SP = System.getProperty("line.separator"); //$NON-NLS-1$

    public String[] getSpecialTagPatternStrings() {
        return new String[] { "components", "include", "component", "meta", }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
    }

    public String evaluate(TemplateContext context, String name, Attribute[] attributes, Element[] body) {
        DiconTemplateContext ctx = (DiconTemplateContext) context;
        @SuppressWarnings("unchecked") //$NON-NLS-1$
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
            if ("include".equals(name)) { //$NON-NLS-1$
                ctx.removeInclude(getValue(attrMap.get("path"))); //$NON-NLS-1$
            } else if ("component".equals(name)) { //$NON-NLS-1$
                ctx.removeComponent(getValue(attrMap.get("class")), getValue(attrMap.get("name"))); //$NON-NLS-1$ //$NON-NLS-2$
                if (!ctx.isIncludeOutputted()) {
                    sb.append(ctx.outputIncludeString(false));
                }
            } else if ("meta".equals(name)) { //$NON-NLS-1$
                String metaName = getValue(attrMap.get("name")); //$NON-NLS-1$
                if (!METANAME_EXPAND.equals(metaName)) {
                    ctx.removeMeta(metaName);
                } else {
                    body = mergeExpandMetaBody(context, body, ctx.popExpandMeta());
                }
                if (!ctx.isIncludeOutputted()) {
                    sb.append(ctx.outputIncludeString(false));
                }
            }

            sb.append(TagEvaluatorUtils.evaluate(context, name, attributes, body));
        }
        return sb.toString();
    }

    private Element[] mergeExpandMetaBody(TemplateContext context, Element[] body, Meta merging) {
        if (merging == null) {
            return body;
        } else if (merging.getContent() == null) {
            return bodyToElement(merging);
        }

        String source = TagEvaluatorUtils.evaluateElements(context, body);
        String[] paths = parseExpandMetaBody(source);
        if (paths == null) {
            return bodyToElement(merging);
        }
        String[] mergedPaths = parseExpandMetaBody(merging.getContent());
        if (mergedPaths == null) {
            return bodyToElement(merging);
        }
        Set<String> pathSet = new LinkedHashSet<String>(Arrays.asList(paths));
        pathSet.addAll(Arrays.asList(mergedPaths));

        return new Element[] { new ConstantElement(source.substring(0, source.indexOf('"'))
                + StringUtils.quoteString(PropertyUtils.join(pathSet.toArray(new String[0])), '"')
                + source.substring(source.lastIndexOf('"') + 1)) };
    }

    private String[] parseExpandMetaBody(String body) {
        int startQuote = body.indexOf('"');
        if (startQuote < 0 || body.substring(0, startQuote).trim().length() > 0) {
            return null;
        }
        int endQuote = body.lastIndexOf('"');
        if (endQuote == startQuote || body.substring(endQuote + 1).trim().length() > 0) {
            return null;
        }
        String unquoted = unquote(body.substring(startQuote + 1, endQuote));
        return PropertyUtils.toArray(unquoted, ",", true); //$NON-NLS-1$
    }

    private String unquote(String quoted) {
        StringBuilder sb = new StringBuilder(quoted.length());
        for (int i = 0; i < quoted.length(); i++) {
            char ch = quoted.charAt(i);
            if (ch == '\\') {
                continue;
            }
            sb.append(ch);
        }
        return sb.toString();
    }

    private Element[] bodyToElement(Meta meta) {
        String content;
        if (meta.getContent() != null) {
            content = meta.getContent();
        } else {
            XOMapper mapper = XOMUtils.getXOMapper();
            StringWriter sw = new StringWriter();
            for (net.skirnir.xom.Element element : meta.getElements()) {
                try {
                    mapper.toXML(element, sw);
                } catch (IOException ex) {
                    throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
                }
            }
            content = SP + sw.toString();
        }
        return new Element[] { new ConstantElement(content) };
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
