package org.seasar.ymir.eclipse.impl;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import net.skirnir.freyja.impl.TemplateContextImpl;
import net.skirnir.xom.ValidationException;

import org.seasar.ymir.vili.model.dicon.Component;
import org.seasar.ymir.vili.model.dicon.Components;
import org.seasar.ymir.vili.model.dicon.Include;
import org.seasar.ymir.vili.model.dicon.Meta;
import org.seasar.ymir.vili.util.XOMUtils;

class DiconTemplateContext extends TemplateContextImpl {
    private Set<Include> includeSet = new LinkedHashSet<Include>();

    private Set<Component> componentSet = new LinkedHashSet<Component>();

    private Set<Meta> metaSet = new LinkedHashSet<Meta>();

    private boolean includeOutputted;

    public void setMetadataToMerge(Components dicon) {
        includeSet.addAll(Arrays.asList(dicon.getIncludes()));
        componentSet.addAll(Arrays.asList(dicon.getComponents()));
        metaSet.addAll(Arrays.asList(dicon.getMetas()));
    }

    public void removeInclude(String path) {
        if (path != null) {
            includeSet.remove(new Include(path));
        }
    }

    public void removeComponent(String className, String name) {
        if (className != null || name != null) {
            componentSet.remove(new Component(className, name));
        }
    }

    public void removeMeta(String name) {
        if (name != null) {
            metaSet.remove(new Meta(name));
        }
    }

    public String outputIncludeString(boolean addToEnd) {
        StringWriter sw = new StringWriter();
        for (Include include : includeSet) {
            if (addToEnd) {
                sw.write("  "); //$NON-NLS-1$
            }
            try {
                XOMUtils.getXOMapper().toXML(include, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
            if (!addToEnd) {
                sw.write("  "); //$NON-NLS-1$
            }
        }
        includeOutputted = true;
        return sw.toString();
    }

    public String outputComponentString() {
        StringWriter sw = new StringWriter();
        for (Component component : componentSet) {
            sw.write("  "); //$NON-NLS-1$
            try {
                XOMUtils.getXOMapper().toXML(component, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return sw.toString();
    }

    public String outputMetaString() {
        StringWriter sw = new StringWriter();
        for (Meta meta : metaSet) {
            sw.write("  "); //$NON-NLS-1$
            try {
                XOMUtils.getXOMapper().toXML(meta, sw);
            } catch (ValidationException ex) {
                throw new RuntimeException(ex);
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }
        }
        return sw.toString();
    }

    public boolean isIncludeOutputted() {
        return includeOutputted;
    }

    public Meta popExpandMeta() {
        for (Meta meta : metaSet) {
            if (DiconTagEvaluator.METANAME_EXPAND.equals(meta.getName())) {
                metaSet.remove(meta);
                return meta;
            }
        }
        return null;
    }
}
