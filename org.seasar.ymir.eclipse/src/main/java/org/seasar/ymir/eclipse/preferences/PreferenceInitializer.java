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
                        new Skeleton("ymir-skeleton-generic", "Ymir標準プロジェクト（YSP）",
                                "The Ymir Projectで規定している標準構成のプロジェクトを作成します。", new Fragment("dbflute-fragment-generic",
                                        "", ""), new Fragment("ymir-fragment-utility", "", ""), new Fragment(
                                        "org.seasar.ymir.skeleton.ysp", "ysp-base", "", "")),
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
                        new Fragment("org.seasar.ymir.skeleton.ysp", "ysp-base", "[YSP] 基本リソース",
                                "[Ymir標準プロジェクト] Ymir標準プロジェクトを構築するための基本となるリソースを追加します。"), new Fragment(
                                "org.seasar.ymir.skeleton.ysp", "ysp-login", "[YSP] ログイン機能",
                                "[Ymir標準プロジェクト] ログイン機能を追加します。"), new Fragment("org.seasar.ymir.skeleton.ysp",
                                "ysp-crud", "[YSP] CRUD用のビューとロジック", "[Ymir標準プロジェクト] CRUD用のビューとロジックを追加します。")));
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
