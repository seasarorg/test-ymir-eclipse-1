package org.seasar.ymir.eclipse.preferences;

import java.io.IOException;
import java.io.StringWriter;

import net.skirnir.xom.ValidationException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.vili.model.Fragments;
import org.seasar.ymir.vili.model.Skeletons;
import org.seasar.ymir.vili.model.Template;
import org.seasar.ymir.vili.util.XOMUtils;

/**
 * Class used to initialize default preference values.
 */
public class PreferenceInitializer extends AbstractPreferenceInitializer {
    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer#initializeDefaultPreferences()
     */
    public void initializeDefaultPreferences() {
        IPreferenceStore store = Activator.getDefault().getPreferenceStore();
        store.setDefault(PreferenceConstants.P_USE_SNAPSHOT_SKELETON, false);
        store.setDefault(PreferenceConstants.P_USE_SNAPSHOT_FRAGMENT, false);
        store.setDefault(PreferenceConstants.P_OFFLINE, false);

        Template defaultTemplate = new Template();
        defaultTemplate.setSkeletons(new Skeletons());
        defaultTemplate.setFragments(new Fragments());
        StringWriter sw = new StringWriter();
        try {
            XOMUtils.getXOMapper().toXML(defaultTemplate, sw);
        } catch (ValidationException ex) {
            throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
        } catch (IOException ex) {
            throw new RuntimeException("Can't happen!", ex); //$NON-NLS-1$
        }
        String defaultTemplateXML = sw.toString();
        store.setDefault(PreferenceConstants.P_TEMPLATE, defaultTemplateXML);
        store.setDefault(PreferenceConstants.P_TEMPLATE_PROJECTSPECIFICSETTINGSENABLED, false);
    }
}
