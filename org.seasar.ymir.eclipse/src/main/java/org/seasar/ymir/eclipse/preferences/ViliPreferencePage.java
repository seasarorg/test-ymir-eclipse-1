package org.seasar.ymir.eclipse.preferences;

import org.eclipse.jface.preference.*;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.IWorkbench;
import org.seasar.ymir.eclipse.Activator;

/**
 * This class represents a preference page that
 * is contributed to the Preferences dialog. By 
 * subclassing <samp>FieldEditorPreferencePage</samp>, we
 * can use the field support built into JFace that allows
 * us to create a page that is small and knows how to 
 * save, restore and apply itself.
 * <p>
 * This page is used to modify preferences only. They
 * are stored in the preference store that belongs to
 * the main plug-in class. That way, preferences can
 * be accessed directly via the preference store.
 */

public class ViliPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public ViliPreferencePage() {
        super(GRID);
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription(Messages.getString("ViliPreferencePage.0")); //$NON-NLS-1$
    }

    /**
     * Creates the field editors. Field editors are abstractions of
     * the common GUI blocks needed to manipulate various types
     * of preferences. Each field editor knows how to save and
     * restore itself.
     */
    public void createFieldEditors() {
        addField(new BooleanFieldEditor(PreferenceConstants.P_USE_SNAPSHOT_SKELETON, Messages.getString("ViliPreferencePage.1"), //$NON-NLS-1$
                getFieldEditorParent()));
        addField(new BooleanFieldEditor(PreferenceConstants.P_USE_SNAPSHOT_FRAGMENT, Messages.getString("ViliPreferencePage.2"), //$NON-NLS-1$
                getFieldEditorParent()));
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.eclipse.ui.IWorkbenchPreferencePage#init(org.eclipse.ui.IWorkbench)
     */
    public void init(IWorkbench workbench) {
    }

}