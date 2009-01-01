package org.seasar.ymir.eclipse.preferences;

import java.io.IOException;
import java.io.StringWriter;

import net.skirnir.xom.ValidationException;

import org.eclipse.core.runtime.preferences.AbstractPreferenceInitializer;
import org.eclipse.jface.preference.IPreferenceStore;
import org.seasar.ymir.eclipse.Activator;
import org.seasar.ymir.vili.model.Fragment;
import org.seasar.ymir.vili.model.Fragments;
import org.seasar.ymir.vili.model.Skeleton;
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
        defaultTemplate
                .setSkeletons(new Skeletons(
                        new Skeleton("ymir-skeleton-generic", Messages.getString("PreferenceInitializer.65"), //$NON-NLS-1$ //$NON-NLS-2$
                                Messages.getString("PreferenceInitializer.10")), //$NON-NLS-1$
                        new Skeleton(
                                "ymir-skeleton-generic", Messages.getString("PreferenceInitializer.66"), //$NON-NLS-1$ //$NON-NLS-2$
                                Messages.getString("PreferenceInitializer.13"), new Fragment("dbflute-fragment-generic", "", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        new Skeleton("ymir-skeleton-generic", Messages.getString("PreferenceInitializer.79"), //$NON-NLS-1$ //$NON-NLS-2$
                                Messages.getString("PreferenceInitializer.80"), new Fragment("dbflute-fragment-generic", //$NON-NLS-1$ //$NON-NLS-2$
                                        "", ""), new Fragment("ymir-fragment-utility", "", ""), new Fragment( //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
                                        "org.seasar.ymir.skeleton.ysp", "ysp-base", "", "")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
                        new Skeleton(
                                "ymir-skeleton-skeleton-generic", Messages.getString("PreferenceInitializer.68"), Messages.getString("PreferenceInitializer.69")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        new Skeleton("ymir-skeleton-skeleton-web", Messages.getString("PreferenceInitializer.74"), //$NON-NLS-1$ //$NON-NLS-2$
                                Messages.getString("PreferenceInitializer.75")), //$NON-NLS-1$
                        new Skeleton(
                                "ymir-skeleton-fragment-generic", Messages.getString("PreferenceInitializer.71"), Messages.getString("PreferenceInitializer.72")) //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                ));
        defaultTemplate
                .setFragments(new Fragments(
                        new Fragment(
                                "ymir-fragment-utility", Messages.getString("PreferenceInitializer.63"), Messages.getString("PreferenceInitializer.64")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        new Fragment(
                                "dbflute-fragment-generic", Messages.getString("PreferenceInitializer.4"), Messages.getString("PreferenceInitializer.3")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        new Fragment(
                                "ymir-fragment-json", Messages.getString("PreferenceInitializer.1"), Messages.getString("PreferenceInitializer.0")), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                        new Fragment("ymir-fragment-amf", Messages.getString("PreferenceInitializer.60"), //$NON-NLS-1$ //$NON-NLS-2$
                                Messages.getString("PreferenceInitializer.61")), //$NON-NLS-1$
                        new Fragment("yonex-fragment-generic", Messages.getString("PreferenceInitializer.76"), //$NON-NLS-1$ //$NON-NLS-2$
                                Messages.getString("PreferenceInitializer.77")), //$NON-NLS-1$
                        new Fragment("org.seasar.ymir.skeleton.ysp", "ysp-base", Messages.getString("PreferenceInitializer.93"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                Messages.getString("PreferenceInitializer.94")), new Fragment( //$NON-NLS-1$
                                "org.seasar.ymir.skeleton.ysp", "ysp-login", Messages.getString("PreferenceInitializer.103"), //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
                                Messages.getString("PreferenceInitializer.98")), new Fragment("org.seasar.ymir.skeleton.ysp", //$NON-NLS-1$ //$NON-NLS-2$
                                "ysp-crud", Messages.getString("PreferenceInitializer.101"), Messages.getString("PreferenceInitializer.102")))); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
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
