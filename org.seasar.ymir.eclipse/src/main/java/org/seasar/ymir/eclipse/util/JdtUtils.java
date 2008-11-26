package org.seasar.ymir.eclipse.util;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.core.runtime.preferences.InstanceScope;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.preferences.ScopedPreferenceStore;

public class JdtUtils {
    private static final String QUALIFIER = "org.eclipse.jdt.core";

    private static final String P_FIELDPREFIXES = "org.eclipse.jdt.core.codeComplete.fieldPrefixes";

    private static final String P_FIELDSUFFIXES = "org.eclipse.jdt.core.codeComplete.fieldSuffixes";

    private static final String FIELDSPECIALPREFIX = "this.";

    private JdtUtils() {
    }

    public static String getFieldPrefix() {
        return getFieldPrefix(getPreferenceStore());
    }

    public static String getFieldSuffix() {
        return getFieldSuffix(getPreferenceStore());
    }

    public static String getFieldSpecialPrefix() {
        return getFieldSpecialPrefix(getPreferenceStore());
    }

    public static String getFieldPrefix(IProject project) {
        return getFieldPrefix(getPreferenceStore(project));
    }

    public static String getFieldSuffix(IProject project) {
        return getFieldSuffix(getPreferenceStore(project));
    }

    public static String getFieldSpecialPrefix(IProject project) {
        return getFieldSpecialPrefix(getPreferenceStore(project));
    }

    private static String getFieldPrefix(IPreferenceStore store) {
        return getFirstElement(store.getString(P_FIELDPREFIXES));
    }

    private static String getFieldSuffix(IPreferenceStore store) {
        return getFirstElement(store.getString(P_FIELDSUFFIXES));
    }

    private static String getFieldSpecialPrefix(IPreferenceStore store) {
        if (getFieldPrefix(store).equals("") && getFieldSuffix(store).equals("")) {
            return FIELDSPECIALPREFIX;
        } else {
            return "";
        }
    }

    private static IPreferenceStore getPreferenceStore() {
        return new ScopedPreferenceStore(new InstanceScope(), QUALIFIER);
    }

    private static IPreferenceStore getPreferenceStore(IProject project) {
        ScopedPreferenceStore store = new ScopedPreferenceStore(new ProjectScope(project), QUALIFIER);
        store.setSearchContexts(new IScopeContext[] { new ProjectScope(project), new InstanceScope() });
        return store;
    }

    private static String getFirstElement(String string) {
        if (string == null) {
            return "";
        }
        int comma = string.indexOf(',');
        if (comma < 0) {
            return string.trim();
        } else {
            return string.substring(0, comma).trim();
        }
    }
}
